package transapps.ballistic.lib.retard;

public interface RetardFunction {
	public String name();
	public double drag(double coefficient, double mach);
}
