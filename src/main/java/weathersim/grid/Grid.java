package weathersim.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import weathersim.math.BooleanField;
import weathersim.math.ScalarField;
import weathersim.math.VectorField;
import weathersim.math.VectorInt;
import weathersim.orography.Orography;
import weathersim.orography.api.Coordinate;
import weathersim.util.LookupTable;

public class Grid {

	// Prognostic fields used
	private final ScalarField T = new ScalarField();
	private final ScalarField rho = new ScalarField();
	// private final ScalarField q = new ScalarField();
	private final ScalarField Tdew = new ScalarField();

	private final VectorField v = new VectorField();

	private final BooleanField lazy = new BooleanField();
	
	private final List<VectorInt> gridpoints = new ArrayList<VectorInt>();
	private final List<VectorInt> queue = new ArrayList<VectorInt>();
	
	private volatile boolean locked;
	
	private final Orography OROGRAPHY;
	public Grid(Orography oro) {
		this.OROGRAPHY = oro;
	}
	
	public void tick() {
		if(!locked) {
			System.out.println("queue " + locked);
			for(VectorInt pos : this.queue) {
				this.gridpoints.add(pos);
				for(int i = 0; i <= 10; i++) {
					this.T.put(pos.getX(), pos.getY(), i, LookupTable.getDefaultTemperature()[i]);
					this.Tdew.put(pos.getX(), pos.getY(), i, LookupTable.getDefaultDewpoint()[i]);
					this.lazy.put(pos.getX(), pos.getY(), 0, true);
				}
			}
		} else {
			return;
		}
		this.queue.clear();
		
		locked = true;
		new Thread(new GridTask()).start();
	}

	public Orography getOrography() {
		return this.OROGRAPHY;
	}

	private class GridTask implements Runnable {
		
		@Override
		public void run() {
			List<GridExecutor> executors = new ArrayList<GridExecutor>();
			
			System.out.println("task " + locked);
			// because looping over an entire array isn't resource-intensive, like, at all
			for (VectorInt pos : gridpoints) {
				float[] temps = new float[11];
				float[] dews = new float[11];
				float[][] stemps = new float[4][11];
				float[][] sdews = new float[4][11];
				float terrainHeight = getOrography().getHeightAtPosition(new Coordinate(pos.getX(), pos.getY()));

				// ah yes, efficiency

				for(int i = 0; i <= 10; i++) {
					temps[i] = T.get(pos.getX(), pos.getY(), i);
					dews[i] = Tdew.get(pos.getX(), pos.getY(), i);
				}
				
				for(int i = 0; i <= 10; i++) {
					float[] surrTemps = T.getSurrounding(pos.getX(), pos.getY(), i);
					float[] surrDews = Tdew.getSurrounding(pos.getX(), pos.getY(), i);
					
					stemps[0][i] = surrTemps[0];
					sdews[0][i] = surrDews[0];
					stemps[1][i] = surrTemps[1];
					sdews[1][i] = surrDews[1];
					stemps[2][i] = surrTemps[2];
					sdews[2][i] = surrDews[2];
					stemps[3][i] = surrTemps[3];
					sdews[3][i] = surrDews[3];
				}
				
				float terrainTemp = OROGRAPHY.getHeatAtPosition(new Coordinate(pos.getX(), pos.getY()));
				float terrainDew = OROGRAPHY.getDewpointAtPosition(new Coordinate(pos.getX(), pos.getY()));

				GridExecutor executor = new GridExecutor(temps, dews, pos.getX(), pos.getY(), 0);
				
				executor.setSurrTemp(stemps);
				executor.setSurrDew(sdews);

				executor.setTerrainTemp(terrainTemp);
				executor.setTerrainDew(terrainDew);

				executor.setTerrainHeight(terrainHeight);

				executor.setLazy(lazy.get(pos.getX(), pos.getY(), 0));
				
				executors.add(executor);
			}

			// hideous parallelization
			executors.stream().forEach(executor -> {
				executor.run();
			});

			for (GridExecutor executor : executors) {
				for(int i = 0; i <= 10; i++) {
					T.put(executor.getX(), executor.getY(), i, executor.getTemp()[i]);
					Tdew.put(executor.getX(), executor.getY(), i, executor.getDew()[i]);
					
					if(executor.getX() == 0 && executor.getY() == 3) {
						System.out.println(T.get(executor.getX(), executor.getY(), i));
					}
				}
				
				lazy.put(executor.getX(), executor.getY(), 0, executor.isLazy());
			}
			locked = false;
		}
	}
	
	private class GridExecutor {
		private final int x;
		private final int y;

		// not used till later
		private final int z;

		/*
		 * private float temp; //temperature private float rho; //density private float
		 * dewpoint; //dewpoint (will fix later) private Vector v; //Windspeed
		 * 
		 * private Vector tempgrad; //temperature gradient private Vector rhograd;
		 * //density gradient private Vector dewpointgrad; //dewpoint gradient private
		 * Tensor vgrad; //Windspeed gradient
		 */

		/*
		 * Legacy physics engine
		 */
		private float temp[]; // temperature
		private float dew[]; // dewpoint (will fix later)
		private float[][] surrTemp;
		private float[][] surrDew;
		private float terrainTemp;
		private float terrainDew;
		private float height;
		private float terrainHeight;
		private boolean isLazy;

		public GridExecutor(float[] temp, /* float rho, */ float[] dewpoint,
				/*
				 * Vector v, Vector tempgrad, Vector rhograd, Vector dewpointgrad, Tensor vgrad,
				 */ int x, int y, int z) {
			this.temp = temp;
			this.dew = dewpoint;
			/*
			 * this.rho = rho; this.dewpoint = dewpoint; this.v = v; this.tempgrad =
			 * tempgrad; this.rhograd = rhograd; this.dewpointgrad = dewpointgrad;
			 * this.vgrad = vgrad;
			 */

			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void run() {
			if (!this.isLazy) {
				return;
			}

			// First: pressure flow, accounting for terrain etc
			// Second: modify air with terrain
			// Third: perform convection

			float[] newTemps = new float[11];
			float[] newDews = new float[11];

			int index = 0;

			// now that we've done that, we need to calculate the winds (and we need to fake
			// the coriolis effect, since
			// minecraft maps are infinitely flat!)

			// yes, this is correct (east is +x, west -x, south +z, north -z in minecraft)
			final int NORTH = 3;
			final int WEST = 1;
			final int SOUTH = 2;
			final int EAST = 0;
			// we use these so that we can compare the index (recycled variable from above)
			// with the direction
			// constants as this greatly improves code readability

			for (int i = 0; i < 11; i++) {
				float dTempX;
				float dTempZ;

				dTempX = surrTemp[EAST][i] - surrTemp[WEST][i];
				dTempZ = surrTemp[SOUTH][i] - surrTemp[NORTH][i];

				int pullFrom = -1;

				if (Math.abs(dTempZ) >= Math.abs(dTempX)) {
					// we're moving air along the x axis (because that dude coriolis makes winds
					// perpendicular, y'know)

					if (dTempZ < 0) {
						pullFrom = EAST;
					} else if (dTempZ > 0) {
						pullFrom = WEST;
					} else {
						pullFrom = -1;
					}
				} else {
					// we're moving air along the z axis

					if (dTempX < 0) {
						pullFrom = NORTH;
					} else if (dTempX > 0) {
						pullFrom = SOUTH;
					} else {
						pullFrom = -1;
					}
				}

				if (pullFrom == -1) {
					newTemps[i] = this.temp[i];
					newDews[i] = this.dew[i];
				} else {
					float newTemp;
					float newDew;

					newTemp = surrTemp[pullFrom][i];
					newDew = surrDew[pullFrom][i];

					if (newTemp < this.temp[i]) {
						// do nothing, we never want to pull air from lower pressure, that's not how it
						// works

						newTemps[i] = this.temp[i];
						newDews[i] = this.dew[i];
					} else {
						// we wanna grab their temperature, their dew, and blend it with ours, with more
						// hilly
						// terrain resulting in less blending

						// newTemps[i] = this.temp[i];
						// newDews[i] = this.dew[i];

						// something is wrong with this?

						newTemps[i] = this.temp[i] + LookupTable.getTerrainPermeability(this.height) * (newTemp - this.temp[i]);
						newDews[i] = this.dew[i] + LookupTable.getTerrainPermeability(this.height) * (newDew - this.dew[i]);
					}
				}

			}

			float prev = 0f;
			index = 0;

			for (float temperature : this.temp) {
				if (index != 0) {
					boolean moistConvection = prev - 0.5f >= dew[index - 1];

					float adjustedTemperature = 10.0f; // Using 10°C juuust in case something goes wrong

					if (moistConvection) {
						adjustedTemperature = prev - LookupTable.getMoistAdiabaticLapseRate();
					} else {
						adjustedTemperature = prev - LookupTable.getDryAdiabaticLapseRate();
					}

					if (adjustedTemperature > temperature) {
						newTemps[index] = adjustedTemperature; // we performed convection
						newTemps[index - 1] = moistConvection ? temperature + LookupTable.getMoistAdiabaticLapseRate() : LookupTable.getDryAdiabaticLapseRate();
						// we need to swap the temps too, that's how displacement works
					} else {
						newTemps[index] = temperature; // else, we don't
					}
				} else {
					newTemps[0] = temperature;
				}

				prev = temperature;
				index++;
			}

			newDews[0] = newDews[0]
					+ LookupTable.getDewpointAdjustmentCoefficient() * (this.terrainDew - newDews[0]);
			newTemps[0] = newTemps[0]
					+ LookupTable.getTemperatureAdjustmentCoefficient() * (this.terrainTemp - newTemps[0]);

			for (index = 0; index < 11; index++) {
				float dew = newDews[index];
				float temp = newDews[index];

				if (Float.isNaN(dew) && this.x == 0 && this.y == 0 || newDews[index] < -100f || newDews[index] > 100f) {
					newDews[index] = -100f; // because we're having some issues
				}

				if (dew > temp) {
					dew = temp - 0.1f; // 0.1f for safety margin, floating point numbers aren't exact, sadly
				}
			}
			this.temp = newTemps;
			this.dew = newDews;
		}

		public void setSurrTemp(float[][] surrTemp) {
			this.surrTemp = surrTemp;
		}

		public void setSurrDew(float[][] surrDew) {
			this.surrDew = surrDew;
		}

		public void setTerrainTemp(float terrainTemp) {
			this.terrainTemp = terrainTemp;
		}

		public void setTerrainDew(float terrainDew) {
			this.terrainDew = terrainDew;
		}
		
		public void setTerrainHeight(float terrainHeight) {
			this.terrainHeight = terrainHeight;
		}

		public void setLazy(boolean lazy) {
			this.isLazy = lazy;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getZ() {
			return z;
		}

		public float[] getTemp() {
			return temp;
		}

		public float[] getDew() {
			return dew;
		}

		public boolean isLazy() {
			return isLazy;
		}
	}

	public float[] getTemps(int x, int y, boolean b) {
		float[] temps = new float[11];
		
		for(int i = 0; i <= 10; i++) {
			temps[i] = this.T.get(x, y, i);
		}
		
		return temps;
	}

	public float[] getDews(int x, int y, boolean b) {
		float[] dews = new float[11];
		
		for(int i = 0; i <= 10; i++) {
			dews[i] = this.Tdew.get(x, y, i);
		}
		
		return dews;
	}

	public boolean getLazy(int x, int y, boolean b) {
		return this.lazy.get(x, y, 0);
	}

	public void newGridCell(int x, int y) {
		this.queue.add(new VectorInt(x, y, 0));
	}
}