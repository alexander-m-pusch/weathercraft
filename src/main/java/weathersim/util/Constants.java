package weathersim.util;

import weathersim.math.Vector;

public class Constants {
	/** In meters */
	public static final int GRIDSIZE = 50;
	/** In meters */
	public static final int GRIDSIZE_VERTICAL = 1000;
	/** Dimensionless */
	public static final int GRIDSIZE_LAYERS = 10;
	/** In seconds */
	public static final int TIME_DIFF = 5;
	/** in Kelvin */
	public static final float SURFACE_TEMPERATURE_DEFAULT = 15f + 273f;
	/** in Kelvin */
	public static final float SURFACE_DEWPOINT_DEFAULT = 5f + 273f;
	/** in Kelvin / Meter */
	public static final float GRADIENT_TEMPERATURE_DEFAULT = 0.005f;
	/** in Kelvin / Meter */
	public static final float GRADIENT_DEWPOINT_DEFAULT = 0.0075f;
	/** in Kelvin / Meter */
	public static final float GRADIENT_MOIST_ADIABATIC = 0.0065f;
	/** in Kelvin / Meter */
	public static final float GRADIENT_DRY_ADIABATIC = 0.0098f;
	/** in Meters per Second squared */
	public static final Vector g = new Vector(0.0f, 0.0f, 9.81f);
	/** in Joule per Kilogram times Kelvin */
	public static final float GAS_CONSTANT = 287.058f;
}
