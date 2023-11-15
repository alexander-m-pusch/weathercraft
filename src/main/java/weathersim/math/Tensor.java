package weathersim.math;

public class Tensor {
	private Vector[] columns = new Vector[3];
	
	public Tensor(float x1, float x2, float x3, float y1, float y2, float y3, float z1, float z2, float z3) {
		this.columns[0] = new Vector(x1, y1, z1);
		this.columns[1] = new Vector(x1, y1, z1);
		this.columns[2] = new Vector(x1, y1, z1);
	}
	
	public Tensor(Vector column0, Vector column1, Vector column2) {
		this.columns[0] = column0;
		this.columns[1] = column1;
		this.columns[2] = column2;
	}

	public Vector getColumn0() {
		return this.columns[0];
	}
	
	public Vector getColumn1() {
		return this.columns[1];
	}
	
	public Vector getColumn2() {
		return this.columns[2];
	}
}
