package transapps.ballistic.lib.data;

/**
 * Encapsulates data for a bullet.
 */
public class Bullet {
	public final Integer id;
	public final String name;
	public final String description;
	/** Drag Model function (G1 - G8) to use- see Ballistics. */
	public final int    function;
	/** Ballistic coefficient for the selected model, in Standard Metro format. */
	public final double coefficient;
	/** Bulet caliber in inches. */
	public final double calibre;
	/** bullet Weight in grains. */
	public final double weight;
	/** bullet length in inches. */
	public final double length;

	public Bullet(Bullet bullet) {
		this(bullet.id, bullet.name, bullet.description, bullet.function,
				bullet.coefficient, bullet.calibre, bullet.weight, bullet.length);
	}

	public Bullet(String name, String description, int function,
			double coefficient, double calibre, double weight, double length) {
		this(null, name, description, function, coefficient, calibre,
				weight, length);
	}

	public Bullet(Integer id, String name, String description, int function,
			double coefficient, double calibre, double weight, double length) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.function = function;
		this.coefficient = coefficient;
		this.calibre = calibre;
		this.weight = weight;
		this.length = length;
	}

	public Bullet newCoefficient(double coef) {
		return new Bullet(id, name, description, function, coef, calibre, weight, length);
	}
}
