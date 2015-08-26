package transapps.ballistic.lib.util;

public class Vector {
	public static final Vector NULL_VECTOR = new Vector(0, 0, 0);
	public final double x;
	public final double y;
	public final double z;

	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double modulus() { return dot(this); }
	public double length() { return Math.sqrt(modulus()); }
	public Vector mul(double a) { return new Vector(a*x, a*y, a*z); }
	public Vector add(Vector v) { return new Vector(x+v.x, y+v.y, z+v.z); }
	public Vector sub(Vector v) { return new Vector(x-v.x, y-v.y, z-v.z); }
//	public Vector reverse() { return new Vector(-x, -y, -z); }
	public double distance(Vector v) { return sub(v).length(); }
	public double dotX(Vector v) { return x*v.x; }
	public double dotY(Vector v) { return y*v.y; }
	public double dotZ(Vector v) { return z*v.z; }
	public double dot(Vector v) { return dotX(v) + dotY(v) + dotZ(v); }
//	public double crossX(Vector v) { return (y*v.z) - (z*v.y); }
//	public double crossY(Vector v) { return (z*v.x) - (x*v.z); }
//	public double crossZ(Vector v) { return (x*v.y) - (y*v.x); }
//	public Vector cross(Vector v) { return new Vector(crossX(v), crossY(v), crossZ(v)); }
//	public double striple(Vector v, Vector w){ return dot(v.cross(w)); }
//	public Vector vtriple(Vector v, Vector w) { return cross(v.cross(w)); }
}
