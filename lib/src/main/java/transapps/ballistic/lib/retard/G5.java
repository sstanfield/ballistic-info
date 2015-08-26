package transapps.ballistic.lib.retard;

public class G5 extends BaseRetardFunction {

	@Override
	public String name() {
		return "G5";
	}

	@Override
	protected double getCD(double mach)
	{
		if (mach > 2.0)
			return 0.671388 + mach*(-0.185208 + mach*0.0204508);
		else if (mach > 1.1)
			return 0.134374 + mach*(0.4378330 - mach*0.1570190);
		else if (mach > 0.9)
			return -0.924258 + mach*1.24904;
		else if (mach >= 0.6)
			return 0.654405 + mach*(-1.4275000 + mach*0.998463);
		else
			return 0.186386 + mach*(-0.0342136 - mach*0.035691);
	}
}
