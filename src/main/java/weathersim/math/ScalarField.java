package weathersim.math;

public class ScalarField extends VariableField<Float, Vector>{
	public ScalarField(int d, int dvert) {
		super(d, dvert);
	}
	
	public ScalarField() {
		super();
	}
	
	public Vector getGradient(int x, int y, int z) {
		float dx = (this.get(x + 1, y, z) - this.get(x - 1, y, z)) / (2 * d);
		float dy = (this.get(x, y + 1, z) - this.get(x, y - 1, z)) / (2 * d);
		float dz = (this.get(x, y, z + 1) - this.get(x, y, z - 1)) / (2 * dvert);
		
		return new Vector(dx, dy, dz);
	}

	@Override
	public Float get(int x, int y, int z) {
		Float fraw = this.getRaw(x, y, z);
		
		if(fraw == null) fraw = 0f;
		
		return null;
	}
}
