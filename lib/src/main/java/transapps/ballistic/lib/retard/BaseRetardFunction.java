package transapps.ballistic.lib.retard;


public abstract class BaseRetardFunction implements RetardFunction {
	protected abstract double getCD(double mach);

	@Override
	public double drag(double coefficient, double mach) {
		final double BC_PIR = 2.08551e-04;
		double cd = getCD(mach);
		return BC_PIR * cd / coefficient;
	}

}
