package weathersim.base;

import weathersim.util.LookupTable;

//one column
public class GridEntry {
	
	private final int x;
	private final int y;
	
	private final Grid grid;
	
	private float[] temp;
	private float[] dew;
	
	private boolean isLazy;
	
	private float[][] surrTemp;
	private float[][] surrDew;

	private float terrainTemp;
	private float terrainDew;
	
	private float height;
	
	private int tickID;
	
	//all arrays MUST!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! have length 11 (one for each every 1000m up to 10km as well as sea level). It's never checked, so if you crash, 
	//it's your fault. Bad coder. (and shame on me for coding such a hideous requirement tbh)
	public GridEntry(int x, int y, Grid grid, float[] temp, float[] dew) {
		this.x = x;
		this.y = y;
		
		this.grid = grid;
		
		//TODO use this for multithreading
		this.temp = temp;
		this.dew = dew;
	}
	
	public void setTickID(int id) {
		this.tickID = id;
	}
	
	public void setSurroundingTemps(float[][] surrTemp) {
		this.surrTemp = surrTemp;
	}
	
	public void setSurroundingDews(float[][] surrDew) {
		this.surrDew = surrDew;
	}
	
	public void setTerrainTemp(float terrainTemp) {
		this.terrainTemp = terrainTemp;
	}
	
	public void setTerrainDew(float terrainDew) {
		this.terrainDew = terrainDew;
	}
	
	public void setTerrainHeight(float terrainHeight) {
		this.height = terrainHeight;
	}
	
	public GridEntry tick() {
		
		if(this.isLazy) {
			return this;
		}
		
		//First: pressure flow, accounting for terrain etc
		//Second: modify air with terrain
		//Third: perform convection
		
		float[] newTemps = new float[11];
		float[] newDews = new float[11];
		
		int index = 0;
		
		//now that we've done that, we need to calculate the winds (and we need to fake the coriolis effect, since
		//minecraft maps are infinitely flat!)
		
		//yes, this is correct (east is +x, west -x, south +z, north -z in minecraft)
		final int NORTH = 3;
		final int WEST = 1;
		final int SOUTH = 2;
		final int EAST = 0;
		//we use these so that we can compare the index (recycled variable from above) with the direction
		//constants as this greatly improves code readability
		
		for(float[] entry : surrTemp) {
			if(entry == null) {
				this.setLazy(true);
				return this;
			}
		}
		
		for (int i = 0; i < 11; i++) {
			float dTempX;
			float dTempZ;
			
			synchronized(GridEntry.class) {
				dTempX = surrTemp[EAST][i] - surrTemp[WEST][i];
				dTempZ = surrTemp[SOUTH][i] - surrTemp[NORTH][i];
			}
			
			int pullFrom = -1;
			
			if(Math.abs(dTempZ) >= Math.abs(dTempX)) {
				//we're moving air along the x axis (because that dude coriolis makes winds perpendicular, y'know)
				
				if(dTempZ < 0) {
					pullFrom = EAST;
				} else if(dTempZ > 0) {
					pullFrom = WEST;
				} else {
					pullFrom = -1;
				}
			} else {
				//we're moving air along the z axis
				
				if(dTempX < 0) {
					pullFrom = NORTH;
				} else if(dTempX > 0) {
					pullFrom = SOUTH;
				} else {
					pullFrom = -1;
				}
			}
			
			if(pullFrom == -1) {
				newTemps[i] = this.temp[i];
				newDews[i] = this.dew[i];
			} else {
				float newTemp;
				float newDew;
				
				newTemp = surrTemp[pullFrom][i];
				newDew = surrDew[pullFrom][i]; 
				
				
				if(newTemp < this.temp[i]) {
					//do nothing, we never want to pull air from lower pressure, that's not how it works
					
					newTemps[i] = this.temp[i];
					newDews[i] = this.dew[i];
				} else {
					//we wanna grab their temperature, their dew, and blend it with ours, with more hilly
					//terrain resulting in less blending
					
					//newTemps[i] = this.temp[i];
					//newDews[i] = this.dew[i];
					
					//something is wrong with this?
					
					newTemps[i] = this.temp[i] + LookupTable.getTerrainPermeability(this.height) * (newTemp - this.temp[i]);
					newDews[i] = this.dew[i] + LookupTable.getTerrainPermeability(this.height) * (newDew - this.dew[i]);
				}
			}
			
		}
		
		float prev = 0f;
		index = 0;
		
		for(float temperature : this.temp) {
			if(index != 0) {
				boolean moistConvection = prev - 0.5f >= dew[index - 1];
				
				float adjustedTemperature = 10.0f; //Using 10°C juuust in case something goes wrong
				
				if(moistConvection) {
					adjustedTemperature = prev - LookupTable.getMoistAdiabaticLapseRate();
				} else {
					adjustedTemperature = prev - LookupTable.getDryAdiabaticLapseRate();
				}
				
				if(adjustedTemperature > temperature) {
					newTemps[index] = adjustedTemperature; //we performed convection
					newTemps[index - 1] = moistConvection ? temperature + LookupTable.getMoistAdiabaticLapseRate() : LookupTable.getDryAdiabaticLapseRate();
					//we need to swap the temps too, that's how displacement works
				} else {
					newTemps[index] = temperature; //else, we don't
				}
			} else {
				newTemps[0] = temperature;
			}
			
			prev = temperature;
			index++;
		}
		
		synchronized(GridEntry.class) {
			newDews[0] = newDews[0] + LookupTable.getDewpointAdjustmentCoefficient() * (this.terrainDew - newDews[0]);
			newTemps[0] = newTemps[0] + LookupTable.getTemperatureAdjustmentCoefficient() * (this.terrainTemp - newTemps[0]);
		}
		
		for(index = 0; index < 11; index++) {
			float dew = newDews[index];
			float temp = newDews[index];
			
			if(Float.isNaN(dew) && this.getX() == 0 && this.getY() == 0 || newDews[index] < -100f || newDews[index] > 100f) {
				newDews[index] = -100f; //because we're having some issues
			}
			
			if(dew > temp) {
				dew = temp - 0.1f; // 0.1f for safety margin, floating point numbers aren't exact, sadly
				
				//TODO perform cloud and precipitation initiation
			}
		}
		
		GridEntry entry = new GridEntry(this.x, this.y, this.grid, newTemps, newDews);
		
		entry.setLazy(true);
		
		return entry;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public synchronized float getHeight() {
		return this.height;
	}
	
	public void setLazy(boolean lazy) {
		this.isLazy = lazy;
	}
	
	public boolean isLazy() {
		return this.isLazy;
	}
	
	public synchronized float getTemp(int level) {
		return this.temp[level];
	}
	
	public synchronized float getDew(int level) {
		return this.dew[level];
	}

	public synchronized float[] getTemps() {
		return this.temp;
	}
	
	public synchronized float[] getDews() {
		return this.dew;
	}
	
	public int getTickID() {
		return this.tickID;
	}
}
