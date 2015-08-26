package transapps.ballistic.lib.retard;

public class G2 extends BaseRetardFunction {

	@Override
	public String name() {
		return "G2";
	}

	@Override
	protected double getCD(double mach)
	{
		if (mach > 2.5)
			return 0.4465610 + mach*(-0.0958548 + mach*0.00799645);
		else if (mach > 1.2)
			return 0.7016110 + mach*(-0.3075100 + mach*0.05192560);
		else if (mach > 1.0)
			return -1.105010 + mach*(2.77195000 - mach*1.26667000);
		else if (mach > 0.9)
			return -2.240370 + mach*2.63867000;
		else if (mach >= 0.7)
			return 0.9099690 + mach*(-1.9017100 + mach*1.21524000);
		else
			return 0.2302760 + mach*(0.000210564 - mach*0.1275050);
	}
}
