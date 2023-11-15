package weathersim.math;

public class VectorField extends VariableField<Vector, Tensor> {
	
	public VectorField(int d, int dvert) {
		super(d, dvert);
	}
	
	public VectorField() {
		super();
	}
	
	@Override
	public Vector get(int x, int y, int z) {
		Vector vraw = this.getRaw(x, y, z);
		
		if(vraw == null) vraw = new Vector(0f, 0f, 0f);
		
		return null;
	}
	
	public Vector gradX(Vector plus, Vector minus) {
		float dx = (plus.getX() - minus.getX()) / this.d;
		float dy = (plus.getY() - minus.getY()) / this.d;
		float dz = (plus.getZ() - minus.getZ()) / this.d;
		
		return new Vector(dx, dy, dz);
	}

	public Vector gradY(Vector plus, Vector minus) {
		float dx = (plus.getX() - minus.getX()) / this.d;
		float dy = (plus.getY() - minus.getY()) / this.d;
		float dz = (plus.getZ() - minus.getZ()) / this.d;
		
		return new Vector(dx, dy, dz);
	}

	public Vector gradZ(Vector plus, Vector minus) {
		float dx = (plus.getX() - minus.getX()) / this.dvert;
		float dy = (plus.getY() - minus.getY()) / this.dvert;
		float dz = (plus.getZ() - minus.getZ()) / this.dvert;
		
		return new Vector(dx, dy, dz);
	}
	
	@Override
	public Tensor getGradient(int x, int y, int z) {
		Vector plusX = this.get(x + 1, y, z);
		Vector plusY = this.get(x, y + 1, z);
		Vector plusZ = this.get(x, y, z + 1);
		Vector minusX = this.get(x - 1, y, z);
		Vector minusY = this.get(x, y - 1, z);
		Vector minusZ = this.get(x, y, z - 1);
		
		return new Tensor(this.gradX(plusX, minusX), this.gradY(plusY, minusY), this.gradZ(plusZ, minusZ));
	}

	public float getDivergence(int x, int y, int z) {
		Vector plusX = this.get(x + 1, y, z);
		Vector plusY = this.get(x, y + 1, z);
		Vector plusZ = this.get(x, y, z + 1);
		Vector minusX = this.get(x - 1, y, z);
		Vector minusY = this.get(x, y - 1, z);
		Vector minusZ = this.get(x, y, z - 1);
		
		float div = this.gradX(plusX, minusX).getX() + this.gradY(plusY, minusY).getY() + this.gradZ(plusZ, minusZ).getZ();
		
		return div;
	}
	
	public Vector getRotation(int x, int y, int z) {
		Vector plusX = this.get(x + 1, y, z);
		Vector plusY = this.get(x, y + 1, z);
		Vector plusZ = this.get(x, y, z + 1);
		Vector minusX = this.get(x - 1, y, z);
		Vector minusY = this.get(x, y - 1, z);
		Vector minusZ = this.get(x, y, z - 1);
		
		Vector rot = new Vector(this.gradZ(plusZ, minusZ).getY() - this.gradY(plusY, minusY).getZ(), this.gradX(plusX, minusX).getZ() - this.gradZ(plusZ, minusZ).getX(), this.gradY(plusY, minusY).getX() - this.gradY(plusY, minusY).getX());
		
		return rot;
	}
}
