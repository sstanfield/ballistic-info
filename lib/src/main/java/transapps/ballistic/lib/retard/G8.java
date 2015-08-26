package transapps.ballistic.lib.retard;

public class G8 extends BaseRetardFunction {

	@Override
	public String name() {
		return "G8";
	}

	@Override
	protected double getCD(double mach)
	{
		if (mach > 1.1)
			return 0.639096 + mach*(-0.197471 + mach*0.0216221);
		else if (mach >= 0.925)
			return -12.9053 + mach*(24.9181 - mach*11.6191);
		else
			return 0.210589 + mach*(-0.00184895 + mach*0.00211107);
	}
}
