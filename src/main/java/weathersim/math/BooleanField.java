package weathersim.math;

public class BooleanField extends VariableField<Boolean, Boolean> {
	@Override
	public Boolean get(int x, int y, int z) {
		Boolean val = this.getRaw(x, y, 0); // we ignore z
		if(val == null) val = false;
		return val;
	}

	@Override
	public Boolean getGradient(int x, int y, int z) {
		return this.get(x, y, z); //just reroute to get, boolean gradients are nonsense
	}
}
