package transapps.ballistic.lib.retard;


public class G1 extends BaseRetardFunction {
	@Override
	public String name() {
		return "G1";
	}

	@Override
	protected double getCD(double mach)
	{
		if (mach > 2.0)
			return 0.9482590 + mach*(-0.248367 + mach*0.0344343);
		else if (mach > 1.40)
			return 0.6796810 + mach*(0.0705311 - mach*0.0570628);
		else if (mach > 1.10)
			return -1.471970 + mach*(3.1652900 - mach*1.1728200);
		else if (mach > 0.85)
			return -0.647392 + mach*(0.9421060 + mach*0.1806040);
		else if (mach >= 0.55)
			return 0.6224890 + mach*(-1.426820 + mach*1.2094500);
		else
			return 0.2637320 + mach*(-0.165665 + mach*0.0852214);
	}
}
