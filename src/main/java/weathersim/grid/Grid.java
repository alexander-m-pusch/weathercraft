package weathersim.grid;

import java.util.ArrayList;
import java.util.List;

import mcmod.wxsiminterface.WxsimAdapter;
import weathersim.math.BooleanField;
import weathersim.math.ScalarField;
import weathersim.math.Tensor;
import weathersim.math.Vector;
import weathersim.math.VectorField;
import weathersim.math.VectorInt;
import weathersim.orography.Orography;
import weathersim.orography.api.Coordinate;
import weathersim.util.Constants;
import weathersim.util.LookupTable;

public class Grid {

	// Prognostic fields used
	private final ScalarField T = new ScalarField();
	private final ScalarField rho = new ScalarField();
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

	public float getTemps(int x, int y, int z, boolean b) {
		float temp = this.T.get(x, y, z);
		if(b) {
			temp = temp - 273f;
		}
		
		return temp;
	}

	public float getDews(int x, int y, int z, boolean b) {
		float temp = this.Tdew.get(x, y, z);
		if(b) {
			temp = temp - 273f;
		}
		
		return temp;
	}

	public boolean getLazy(int x, int y, boolean b) {
		return this.lazy.get(x, y, 0);
	}

	public void newGridCell(int x, int y) {
		this.queue.add(new VectorInt(x, y, 0));
	}

	public Orography getOrography() {
		return this.OROGRAPHY;
	}
	
	public void tick() {
		if(!locked) {
			for(VectorInt pos : this.queue) {
				boolean isNew = (this.T.get(pos.getX(), pos.getY(), pos.getZ()) > -500.0f) ? false : true;
				if(isNew) {
					System.out.println("[WeatherPlus] Generating new gridpoint at " + pos.toString());
					this.gridpoints.add(pos);
					for(int i = 0; i <= Constants.GRIDSIZE_LAYERS; i++) {
						this.T.put(pos.getX(), pos.getY(), i, LookupTable.getDefaultTemperature(i));
						this.Tdew.put(pos.getX(), pos.getY(), i, LookupTable.getDefaultDewpoint(i));
					}
				}
				
				for(int i = 0; i <= Constants.GRIDSIZE_LAYERS; i++) {
					this.lazy.put(pos.getX(), pos.getY(), pos.getZ(), true);
				}
			}
			this.queue.clear();
		}
		
		locked = true;
		new Thread(new GridTask()).start();
	}

	private class GridTask implements Runnable {
		@Override
		public void run() {
			List<GridExecutor> executors = new ArrayList<GridExecutor>();
			
			// because looping over an entire array isn't resource-intensive, like, at all
			for (VectorInt pos : gridpoints) {
				// ah yes, efficiency
				GridExecutor executor = new GridExecutor(T.get(pos.getX(), pos.getY(), pos.getZ()), rho.get(pos.getX(), pos.getY(), pos.getZ()), Tdew.get(pos.getX(), pos.getY(), pos.getZ()), v.get(pos.getX(), pos.getY(), pos.getZ()), T.getGradient(pos.getX(), pos.getY(), pos.getZ()), rho.getGradient(pos.getX(), pos.getY(), pos.getZ()), Tdew.getGradient(pos.getX(), pos.getY(), pos.getZ()), v.getGradient(pos.getX(), pos.getY(), pos.getZ()), pos.getX(), pos.getY(), pos.getZ());
				
				executor.setLazy(lazy.get(pos.getX(), pos.getY(), 0));
				
				executors.add(executor);
			}

			//uncomment the .parallel() for parallelization. My processor is broken, that's why it's commented out
			executors.stream()/*.parallel()*/.forEach(executor -> {
				executor.run();
			});

			for (GridExecutor executor : executors) {
				T.put(executor.getX(), executor.getY(), executor.getZ(), executor.getTemp());
				Tdew.put(executor.getX(), executor.getY(), executor.getZ(), executor.getDew());

				if(executor.getX() == 0 && executor.getY() == 3) {
					System.out.println(T.get(executor.getX(), executor.getY(), executor.getZ()));
				}
				
				lazy.put(executor.getX(), executor.getY(), executor.getZ(), executor.isLazy());
			}
			locked = false;
		}
	}
	
	private class GridExecutor {
		private final int x;
		private final int y;

		// not used till later
		private final int z;

		
		private float temp; //temperature 
		private float rho; //density private float
		private float dew; //dewpoint (will fix later) 
		private Vector v; //Windspeed
		
		private Vector tempgrad; //temperature gradient 
		private Vector rhograd; //density gradient
		private Vector dewpointgrad; //dewpoint gradient private
		Tensor vgrad; //Windspeed gradient

		private boolean isLazy;

		public GridExecutor(float temp, float rho, float dewpoint, Vector v, Vector tempgrad, Vector rhograd, Vector dewpointgrad, Tensor vgrad, int x, int y, int z) {
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

		public float getDew() {
			return this.dew;
		}

		public float getTemp() {
			return this.temp;
		}

		public void run() {
			if (!this.isLazy) {
				return;
			}

			this.isLazy = false;
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

		public boolean isLazy() {
			return isLazy;
		}
	}
}