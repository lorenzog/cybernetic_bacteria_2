package cyberneticbacteria;

/**
 * This object represents a single individual.
 * 
 * Its genotype is similar to
 * 
 * 00DD8E96E014E010C8 
 * 0098996E014E0857B 
 * 001B4EC1D014E0D57B
 * 
 * where every locus is characterized by 
 * 
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author lorenzo
 * 
 *         the individual is defined by its genotype, a string of length 9
 * 
 *         T XX XX XX XX XX XX XX XX
 * 
 *         where T is the type: (B)luetooth, card(R)fid and (W)irelessRFID -
 *         type also defines the color; red, blue and white.
 * 
 *         remaining part of genotype depends on type.
 * 
 *         R and W: aa XX XX XX 00 00 00 00 B: aa XX XX XX XX XX XX XX
 * 
 *         where 'aa' age
 * 
 */
public class Individual {

	/* weird, processing things */
	private static final int COLOR_RED = 0xff000000 | (255 << 16);
	private static final int COLOR_BLUE = 0xff000000 | 255;
	private static final int COLOR_WHITE = 0xff000000 | (255 << 16)
			| (255 << 8) | 255;
	private static final int COLOR_BLACK = 0xff000000;

	// private int x = -1, y = -1; /* the location */
	private Coordinates c;

	private String genotype;

	private int color; 

	Random rand = new Random();

	Individual(Coordinates c) {
		this.c = c;
	}

	Individual(String genotype) {
		// this();
		setGenotype(genotype);
	}

	Individual(String genotype, Coordinates c) {
		this(genotype);
		this.c = c;
	}

	public boolean equals(Object other) {
		return (((Individual) other).getX() == this.c.x && ((Individual) other)
				.getY() == this.c.y);
	}

	public void setGenotype(String genotype) {
		this.genotype = genotype;
		parseGenotype();
	}

	public void setLocation(int x, int y) {
		this.c = new Coordinates(x, y);
	}

	/* create phenotype from parameters */
	private void parseGenotype() {

		if (genotype.length() == 0) {
			System.err.println("no genotype?");
			color = 0;
			return;
		}

		char c = genotype.charAt(0);

		switch (c) {
		case 'B':
			color = COLOR_BLUE;
			// type = BacteriaType.COMUNICATOR;
			break;
		case 'R':
			color = COLOR_RED;
			// type = BacteriaType.MUTE;
			break;
		case 'W':
			color = COLOR_WHITE;
			// type = BacteriaType.LISTENER;
			break;
		}
	}

	public int getX() {
		return c.x;
	}

	public int getY() {
		return c.y;
	}

	public int getColor() {
		return color;
	}

	public String getGenotype() {
		return genotype;
	}

	public String toString() {
		return genotype + " " + getX() + "," + getY();
	}
	//
	// public boolean isFood() {
	// return (currentState == PossibleStates.FOOD);
	// }
	//
	// public void isFood(boolean isFood) {
	// // or futurestate?
	// currentState = PossibleStates.FOOD;
	// }
	//
	// public boolean isDead() {
	// return currentState == PossibleStates.DEAD;
	// }
	//
	// public boolean isAlive() {
	// return (currentState == PossibleStates.ALIVE || futureState ==
	// PossibleStates.ALIVE);
	// }
	//
	// public void isAlive(boolean isAlive) {
	// if (isAlive)
	// futureState = PossibleStates.ALIVE;
	// else
	// futureState = PossibleStates.DEAD;
	// }
	//
	// public void age() {
	// age--;
	// if (age > 0)
	// futureState = PossibleStates.ALIVE;
	// else
	// futureState = PossibleStates.DEAD;
	// currentState = futureState;
	// }
	//
	// public void kill() {
	// currentState = PossibleStates.FOOD;
	// }

}
