package cyberneticbacteria;

public class Coordinates {
	int x, y, color;

	Coordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}

	Coordinates(int x, int y, int color) {
		this(x, y);
		this.color = color;
	}

	public boolean equals(Object other) {
		return (((Coordinates) other).x == this.x && ((Coordinates) other).y == this.y);
	}

	public String toString() {
		return this.x + "," + this.y;
	}

	/**
	 * thanks to: matteo caprari at gmail com
	 */
	@Override
	public int hashCode() {
		return x * 1000 + y;
	}
}