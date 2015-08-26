package transapps.ballistic.lib.retard;

public class G7 extends BaseRetardFunction {

	@Override
	public String name() {
		return "G7";
	}

	@Override
	protected double getCD(double mach)
	{
		if (mach > 1.9) {
			return 0.439493 + mach*(-0.0793543 + mach*0.00448477);
		} else if (mach > 1.05) {
			return 0.642743 + mach*(-0.2725450 + mach*0.049247500);
		} else if (mach > 0.90) {
			return -1.69655 + mach*2.03557;
		} else if (mach >= 0.60) {
			return 0.353384 + mach*(-0.69240600 + mach*0.50946900);
		} else {
			return 0.119775 + mach*(-0.00231118 + mach*0.00286712);
		}
	}
}
