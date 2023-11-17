package weathersim.math;

import java.util.ArrayList;
import java.util.List;

public class ScalarField extends VariableField<Float, Vector>{

	public float[] getSurrounding(int x, int y, int z) {
		float plusx = this.get(x + 1, y, z);
		float minusx = this.get(x - 1, y, z);
		float plusz = this.get(x, y, z + 1);
		float minusz = this.get(x, y, z - 1);
		
		if(plusx < -500f) plusx = this.get(x, y, z);
		if(minusx < -500f) minusx = this.get(x, y, z);
		if(plusz < -500f) plusz = this.get(x, y, z);
		if(minusz < -500f) minusz = this.get(x, y, z);
		
		float[] vals = new float[4];
		vals[0] = plusx;
		vals[1] = minusx;
		vals[2] = plusz;
		vals[3] = minusz;
		
		return vals;
	}
	
	public Vector getGradient(int x, int y, int z) {
		float plusx = this.get(x + 1, y, z);
		float minusx = this.get(x - 1, y, z);
		float plusy = this.get(x, y + 1, z);
		float minusy = this.get(x, y - 1, z);
		float plusz = this.get(x, y, z + 1);
		float minusz = this.get(x, y, z - 1);
		
		if(plusx < -500f) plusx = this.get(x, y, z);
		if(minusx < -500f) minusx = this.get(x, y, z);
		if(plusy < -500f) plusy = this.get(x, y, z);
		if(minusy < -500f) minusy = this.get(x, y, z);
		if(plusz < -500f) plusz = this.get(x, y, z);
		if(minusz < -500f) minusz = this.get(x, y, z);
		
		float dx = (plusx - minusx) / (2 * d);
		float dy = (plusy - minusy) / (2 * d);
		float dz = (plusz - minusz) / (2 * dvert);
		
		return new Vector(dx, dy, dz);
	}

	@Override
	public Float get(int x, int y, int z) {
		Float fraw = this.getRaw(x, y, z);
		
		if(fraw == null) fraw = -1000f;
		
		return fraw;
	}
}
