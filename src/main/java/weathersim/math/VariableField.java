package weathersim.math;

import java.util.HashMap;

import weathersim.util.Constants;

public abstract class VariableField<Type, GradientType> {
	protected final int d;
	protected final int dvert;
	
	private final HashMap<Integer, HashMap<Integer, HashMap<Integer, Type>>> values = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Type>>>();
	
	public VariableField(int distanceBetweenPoints, int distanceBetweenPointsVertical) {
		this.d = distanceBetweenPoints;
		this.dvert = distanceBetweenPointsVertical;
	}
	
	public VariableField() {
		this(Constants.GRIDSIZE, Constants.GRIDSIZE_VERTICAL);
	}

	
	public void put(int x, int y, int z, Type f) {
		if(!values.containsKey(x)) {
			values.put(x, new HashMap<Integer, HashMap<Integer, Type>>());
		}
		if(!values.get(x).containsKey(y)) {
			values.get(x).put(y, new HashMap<Integer, Type>());
		}
		
		values.get(x).get(y).put(z, f);
	}
	
	public Type getRaw(int x, int y, int z) {
		if(!values.containsKey(x)) {
			values.put(x, new HashMap<Integer, HashMap<Integer, Type>>());
		}
		if(!values.get(x).containsKey(y)) {
			values.get(x).put(y, new HashMap<Integer, Type>());
		}
		
		return values.get(x).get(y).get(z);
	}
	
	public abstract Type get(int x, int y, int z);
	
	public abstract GradientType getGradient(int x, int y, int z);
}
