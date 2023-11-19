package weathersim.util;

public class LookupTable {
	
	//1: complete permeability (think open ocean)
	//0: complete obstruction (think extreme hills)
	public static float getTerrainPermeability(float height) {
		return 1 - height; //this may seem lazy (and it is), but it makes the code more readable imo
	}
	
	//adjustment coefficient for evaporating water off of the terrain to the lowest level of the atmosphere
	//formula: dewpoint[sealevel] = dewpoint[sealevel] + coefficient * (Orography.getDewpointAtPosition() - dewpoint[sealevel])
	public static float getDewpointAdjustmentCoefficient() {
		return 0.1f;
	}
	
	//adjustment coefficient for adjusting the temperature of the lowest level of the atmosphere to the terrain heat
	//formula: temperature[sealevel] = temperature[sealevel] + coefficient * (Orography.getTemperatureAtPosition()- temperature[sealevel])
	public static float getTemperatureAdjustmentCoefficient() {
		return 0.1f;
	}
	
	/**
	 * Gets the default temperature which the simulator places on new gridcells
	 * 
	 * @param i
	 * @return
	 */
	public static float getDefaultTemperature(int i) {
		return Constants.SURFACE_TEMPERATURE_DEFAULT - (Constants.GRADIENT_TEMPERATURE_DEFAULT * i * Constants.GRIDSIZE_VERTICAL);
	}
	
	public static float getDefaultDewpoint(int i) {
		return Constants.SURFACE_DEWPOINT_DEFAULT - (Constants.GRADIENT_DEWPOINT_DEFAULT * i * Constants.GRIDSIZE_VERTICAL);		
	}
}
