package weathersim.math;

public class Vector {
	private float u, v, w;
	
	public Vector(float u, float v, float w) {
		this.u = u;
		this.v = v;
		this.w = w;
	}

	public float getX() {
		return u;
	}

	public float getY() {
		return v;
	}

	public float getZ() {
		return w;
	}

	public float getLength() {
		return (float) Math.sqrt(((double) u*u + v*v + w*w));
	}
	
	public float getScalarProduct(Vector vec) {
		return this.u * vec.getX() + this.v * vec.getY() + this.w * vec.getZ();
	}
	
	public Vector getTensorProduct(Tensor ten) {
		this.u = this.getX() * ten.getColumn0().getX() + this.getY() * ten.getColumn1().getX() + this.getZ() * ten.getColumn2().getX();
		this.v = this.getX() * ten.getColumn0().getY() + this.getY() * ten.getColumn1().getY() + this.getZ() * ten.getColumn2().getY();
		this.w = this.getX() * ten.getColumn0().getZ() + this.getY() * ten.getColumn1().getZ() + this.getZ() * ten.getColumn2().getZ();
		return this; 
	}
	
	public Vector getCrossProduct(Vector vec) {
		this.u = this.v * vec.getZ() - this.w * vec.getY();
		this.v = this.w * vec.getX() - this.u * vec.getZ();
		this.w = this.u * vec.getY() - this.v * vec.getX();
		return this;
	}
	
	public Vector mul(float num) {
		this.u = this.u * num;
		this.v = this.v * num;
		this.w = this.w * num;
		return this;
	}
	
	public Vector add(Vector vec) {
		this.u = this.u + vec.getX();
		this.v = this.v + vec.getY();
		this.w = this.w + vec.getZ();
		return this;
	}
	
	public Vector sub(Vector vec) {
		this.add(vec.mul(-1f));
		vec.mul(-1f);
		return this;
	}
	
	public Vector dup() {
		return new Vector(this.u, this.v, this.w);
	}
}
