package transapps.ballistic.lib.data;


/**
 * Encapsulates a weapon system including bullet, zero data, velocity, etc.
 */
public class Weapon {
	public final Integer id;
	public final String  name;
	public final String  description;
	/** initial velocity in ft/s */
	public final double  velocity;
	/** The height in inches of the sighting system above the bore centerline. 
	 *  Most scopes are in the 1.5"-2.0" range. */
	public final double  sightHeight;
	/** True if the barrel is right hand twist, false for left hand twist. */
	public final boolean rightTwist;
	/** Barrel twist inches per turn (a 1:10 twist barrel woudl have a value of 10). */
	public final double  barrelTwist;
	/** Yards the weapon is zeroed at. */
	public final double  zeroRange;
	/** Info for the bullet used for this weapon system. */
	public final Bullet  bullet;
	/** Atmospheric conditions in effect WHEN ZEROED. */
	public final Atmosphere atmosphere;

	public Weapon(Weapon weapon) {
		this(weapon.id, weapon.name, weapon.description, weapon.velocity,
				weapon.sightHeight, weapon.rightTwist, weapon.barrelTwist,
				weapon.zeroRange, weapon.atmosphere, weapon.bullet);
	}

	public Weapon(String name, String description, double velocity, 
			double sightHeight, boolean rightTwist, double barrelTwist,
			double zeroRange, Atmosphere atmosphere, Bullet bullet) {
		this(null, name, description, velocity,
				sightHeight, rightTwist, barrelTwist, zeroRange, atmosphere, bullet);
	}

	public Weapon(Integer id, String name, String description, double velocity,
			double sightHeight, boolean rightTwist, double barrelTwist,
			double zeroRange, Atmosphere atmosphere, Bullet bullet) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.velocity = velocity;
		this.sightHeight = sightHeight;
		this.rightTwist = rightTwist;
		this.barrelTwist = barrelTwist;
		this.zeroRange = zeroRange;
		this.atmosphere = atmosphere;
		this.bullet = bullet;
	}

	public Weapon newZero(double zeroRange, Atmosphere atmo) {
		return new Weapon(id, name, description, velocity,
				sightHeight, rightTwist, barrelTwist, zeroRange, atmo, bullet);
	}

	public Weapon newBullet(Bullet b) {
		return new Weapon(id, name, description, velocity,
				sightHeight, rightTwist, barrelTwist, zeroRange, atmosphere, b);
	}

	public Weapon newVelocity(double v) {
		return new Weapon(id, name, description, v,
				sightHeight, rightTwist, barrelTwist, zeroRange, atmosphere, bullet);
	}

	@Override
	public String toString() {
		return name;
	}
}
