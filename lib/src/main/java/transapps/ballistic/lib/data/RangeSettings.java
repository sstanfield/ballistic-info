package transapps.ballistic.lib.data;

/**
 * Used internally to allow a Ballistic object to send flags/data to the
 * RangeData objects for each range step.
 */
public interface RangeSettings {
	public boolean spinDriftOn();
	public boolean zeroWithSpinDrift();
	public boolean coriolisOn();
	public boolean isImperial();
	public RangeData spinDriftAtZero();
	public Coriolis coriolisAtZero();
}
