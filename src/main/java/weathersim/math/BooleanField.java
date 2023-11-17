package weathersim.math;

public class BooleanField extends VariableField<Boolean, Void> {
	@Override
	public Boolean get(int x, int y, int z) {
		Boolean val = this.getRaw(x, y, 0); // we ignore z
		if(val == null) val = false;
		return val;
	}

	@Override
	public Void getGradient(int x, int y, int z) {
		return null; //Because we don't need a boolean gradient. What would that even be?
	}

}
