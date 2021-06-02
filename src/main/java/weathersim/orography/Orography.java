package weathersim.orography;

import java.util.function.Function;

import weathersim.orography.api.Coordinate;

public class Orography {
	//the first two are misnomers but the correct term would be too clunky. You get the theorhetical evaporable
	//water, as well as the max air temp.
	
	private final Function<Coordinate, Float> heatCallback;
	private final Function<Coordinate, Float> dewpointCallback;
	private final Function<Coordinate, Float> heightCallback;
	
	public Orography(Function<Coordinate, Float> heatCallback, Function<Coordinate, Float> dewpointCallback, Function<Coordinate, Float> heightCallback) {
		this.heatCallback = heatCallback;
		this.dewpointCallback = dewpointCallback;
		this.heightCallback = heightCallback;
	}
	
	public float getHeatAtPosition(Coordinate coord) {
		return heatCallback.apply(coord);
	}
	
	public float getDewpointAtPosition(Coordinate coord) {
		return dewpointCallback.apply(coord);
	}
	
	public float getHeightAtPosition(Coordinate coord) {
		return heightCallback.apply(coord);
	}
}
