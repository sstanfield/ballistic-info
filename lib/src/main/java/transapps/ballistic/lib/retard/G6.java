package transapps.ballistic.lib.retard;

public class G6 extends BaseRetardFunction {

	@Override
	public String name() {
		return "G6";
	}

	@Override
	protected double getCD(double mach)
	{
		if (mach > 2.0)
			return 0.746228 + mach*(-0.255926 + mach*0.0291726);
		else if (mach > 1.1)
			return 0.513638 + mach*(-0.015269 - mach*0.0331221);
		else if (mach > 0.9)
			return -0.908802 + mach*1.25814;
		else if (mach >= 0.6)
			return 0.366723 + mach*(-0.458435 + mach*0.337906);
		else
			return 0.264481 + mach*(-0.157237 + mach*0.117441);
	}
}
