package weathersim.math;

public class VectorInt {
	private final int x;
	private final int y;
	private final int z;
	
	public VectorInt(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	@Override
	public String toString() {
		return "(" + this.getX() + ", " + this.getY() + ", " + this.getZ() + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		
		if(obj instanceof VectorInt) {
			VectorInt vec = (VectorInt) obj;
			isEqual = (vec.getX() == this.getX()) && (vec.getY() == this.getY()) && (vec.getZ() == this.getZ());
		}
		
		return isEqual;
	}
}
