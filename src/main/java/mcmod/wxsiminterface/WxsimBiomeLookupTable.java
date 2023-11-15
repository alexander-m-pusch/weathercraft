package mcmod.wxsiminterface;

import net.minecraft.world.level.biome.Biome;

public class WxsimBiomeLookupTable {
	
	private static final float STRIDE_FROM_ZERO = 50.0f; //all temps will be between -50 and +50°C
	
	public static float getTemperatureForBiome(Biome biome, float bias) {
		
		float adjustedTemperature = (biome.getModifiedClimateSettings().temperature()); 
		
		return adjustedTemperature * (STRIDE_FROM_ZERO / 2f) + bias;
	}
	
	//this works garbage in - garbage out, so if you pass in a bogus temp it will get out a bogus num
	public static float getDewpointForBiome(Biome biome, float bias) {
		//this has a habit of spitting out NaN and idk why
		
		float maxDew = getTemperatureForBiome(biome, bias);
		
		float reallyStupidUnreliableHumidityByBiome = biome.getModifiedClimateSettings().downfall(); //seriously, why are mushroom islands more moist than oceans???????
		
		return maxDew - ((STRIDE_FROM_ZERO) * (1 - reallyStupidUnreliableHumidityByBiome));
	}
}
