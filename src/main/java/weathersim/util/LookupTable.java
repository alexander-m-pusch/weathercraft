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
		return 0.01f;
	}
	
	//adjustment coefficient for adjusting the temperature of the lowest level of the atmosphere to the terrain heat
	//formula: temperature[sealevel] = temperature[sealevel] + coefficient * (Orography.getTemperatureAtPosition()- temperature[sealevel])
	public static float getTemperatureAdjustmentCoefficient() {
		return 0.01f;
	}
	
	public static float getDryAdiabaticLapseRate() {
		return 9.8f; //	9.8K/km
	}
	
	public static float getMoistAdiabaticLapseRate() {
		return 6.5f; //	I am aware that the MALR changes with height, but for the point of this sim it
					 // should be enough since I want to keep the calculation efforts minimal.
	}
	
	public static float[] getDefaultTemperature() {
		
		return new float[] {15f, 8.5f, 2f, -4.5f, -11f, -17.5f, -24f, -30f, -37f, -43.5f, -50f};
		
	}
	
	public static float[] getDefaultDewpoint() {
		
		return new float[] {-100f, -100f, -100f, -100f, -100f, -100f, -100f, -100f, -100f, -100f, -100f};
		
	}
}
