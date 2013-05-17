package cyberneticbacteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CyberneticBacteria implements Runnable {

	Set<String> genePool;

	final static float DENSITY = 0.3f;

	private static final double MUTATE_PROB = 0.000001;

	/* inoculation area radius */
	static int INOC_MIN = 50;
	static int INOC_MAX = 90;

	/* ----- END USER MODIFIABLE DATA ----- */

	/**
	 * the collection of individuals that take part into the simulation; new
	 * individuals are added from the inoculate() method. their genotype is
	 * taken from the genePool collection (see below)
	 */
	private List<Individual> population = new ArrayList<Individual>(307200);
	// Collections
	// .synchronizedList(new ArrayList<Individual>());

	Set<Individual> newborns = new HashSet<Individual>();

	List<Coordinates> sharedLocations;

	/**
	 * the world, as a collection of locations
	 */
	private Map<Coordinates, Individual> world;

	// the random numbah generator
	private static Random rand = new Random();

	private static final int MAX_NEIGHBORS = 7;
	private static final int MIN_NEIGHBORS = 0;

	/**
	 * paintable area
	 */
	int sx, sy;

	CyberneticBacteria(Set<String> genePool, List<Coordinates> sharedLocations,
			int sx, int sy) {
		this.genePool = genePool;
		this.sharedLocations = sharedLocations;
		this.sx = sx;
		this.sy = sy;
		// world size
		world = new HashMap<Coordinates, Individual>(sx * sy);
	}

	private List<Coordinates> emptySpaces(Individual ind) {
		int x = ind.getX();
		int y = ind.getY();

		List<Coordinates> possibleLocations = new ArrayList<Coordinates>(8);

		int tmp_x, tmp_y;

		/* right */
		tmp_x = (x + 1) % sx;
		tmp_y = y;
		if (!world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* top */
		tmp_x = x;
		tmp_y = (y + 1) % sy;
		if (!world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* left */
		tmp_x = (x + sx - 1) % sx;
		tmp_y = y;
		if (!world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* bottom */
		tmp_x = x;
		tmp_y = (y + sy - 1) % sy;
		if (world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* topright */
		tmp_x = (x + 1) % sx;
		tmp_y = (y + 1) % sy;
		if (!world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* topleft */
		tmp_x = (x + sx - 1) % sx;
		tmp_y = (y + 1) % sy;
		if (!world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* bottomleft */
		tmp_x = (x + sx - 1) % sx;
		tmp_y = (y + sy - 1) % sy;
		if (!world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* bottomright */
		tmp_x = (x + 1) % sx;
		tmp_y = (y + sy - 1) % sy;
		if (!world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		return possibleLocations;
	}

	private char[] reproduceAndMutate(char[] genotype) {
		for (int i = 0; i < genotype.length; i++) {
			if (rand.nextFloat() < MUTATE_PROB) {
				// get char at that location
				char gene = genotype[i];
				if (i == 0) {
					// if it's the type locus
					switch (gene) {
					case 'B':
						genotype[i] = rand.nextBoolean() ? 'R' : 'W';
						break;
					case 'R':
						genotype[i] = rand.nextBoolean() ? 'B' : 'W';
						break;
					case 'W':
						genotype[i] = rand.nextBoolean() ? 'B' : 'R';
						break;
					}
				} else {
					// if it's another gene
					genotype[i] = Character.forDigit(((gene + 1) % 16), 16);
				}
			}
		}
		return genotype;
	} // end reproduceAndMutate(char[])

	/*
	 * extended reproduce and mutate routine; look into world for neighbors, if
	 * any empty space then reproduce there otherwise stay put
	 */
	private Individual reproduceAndMutate(Individual parent,
			List<Coordinates> locations) {

		// get a random location
		Coordinates coord = locations.get(rand.nextInt(locations.size()));

		// reproduce and/or mutate there

		char[] newGenotype = reproduceAndMutate(parent.getGenotype()
				.toCharArray());

		Individual sibling = new Individual(new String(newGenotype), coord);
		return sibling;
	}

	/**
	 * place individuals where specified
	 */
	public void inoculate(int x, int y, String genotype) {

		// how big the inoculation area
		// returns value in [0, n)
		int INOC_AREA = rand.nextInt(1 + INOC_MAX - INOC_MIN);

		/* coordinates of current bacterium */
		int bact_x, bact_y;
		for (int i = 0; i < INOC_AREA; i++) {
			for (int j = 0; j < INOC_AREA; j++) {
				// inoculate
				if (rand.nextFloat() > DENSITY) {
					bact_x = x - INOC_AREA / 2 + i;
					bact_y = y - INOC_AREA / 2 + j;
					// set that spot to become the nth individual in the next
					// cycle
					bact_x = bact_x > 0 && bact_x < sx ? bact_x : 0;
					bact_y = bact_y > 0 && bact_y < sy ? bact_y : 0;
					Individual indiv = new Individual(genotype,
							new Coordinates(bact_x, bact_y));
					// add to population
					synchronized (newborns) {
						newborns.add(indiv);
					}
				}
			}
		}
	}

	/**
	 * Generates a random genotype composed of two integers (converted to hex)
	 * concatenated;
	 * 
	 * @return a hex string of the genotype; guaranteed to be a non-negative
	 *         number
	 */
	private String generateRandomGenotype() {
		/* generate a new random individual */
		String genotype = null;
		switch (rand.nextInt(3)) {
		case 0:
			genotype = 'B' + Integer.toHexString(Math.abs(rand.nextInt()))
					.toUpperCase().concat(
							Integer.toHexString(Math.abs(rand.nextInt()))
									.toUpperCase());
			break;
		case 1:
			genotype = 'R' + Integer.toHexString(Math.abs(rand.nextInt()))
					.toUpperCase();
			break;
		case 2:
			genotype = 'W' + Integer.toHexString(Math.abs(rand.nextInt()))
					.toUpperCase();
			break;
		}

		// make it a new individual
		synchronized (genePool) {
			genePool.add(genotype);
		}

		return genotype;
	}

	public void run() {

		/*
		 * a private copy of locations to be passed to the graphics engine
		 */
		List<Coordinates> locations = new ArrayList<Coordinates>();
		Set<Individual> deathList = new HashSet<Individual>();

		while (true) {

			// add the newborns to be rendered the next loop
			synchronized (newborns) {
				for (Individual who : newborns) {
					population.add(who);
					// map into world
					world.put(new Coordinates(who.getX(), who.getY()), who);
				}
				newborns.clear();
			}

			for (Individual who : population) {
				int x = who.getX();
				int y = who.getY();

				List<Coordinates> emptySpaces = emptySpaces(who);
				int numEmptySpaces = emptySpaces.size();

				if (numEmptySpaces == 0)
					continue;

				if (locations.size() > 50000) {
					deathList.add(who);
					break;
				}

				// if (numEmptySpaces < 2 || numEmptySpaces > 3) {
				if (numEmptySpaces > 4) {
					// paint it black (remove from screen)
					locations.add(new Coordinates(x, y, 0xff000000));
					deathList.add(who);

					continue;
				}

				// if it survived,

				// paint it with its own color
				locations.add(new Coordinates(x, y, who.getColor()));
				// survive and reproduce
				Individual sibling = reproduceAndMutate(who, emptySpaces);
				synchronized (newborns) {
					if (sibling != null)
						newborns.add(sibling);
				}

			} // end for
			// System.out.println();

			// for each individual in the death list,
			for (Individual who : deathList) {
				// take it off the population
				population.remove(population.indexOf(who));
				// also from the world
				world.remove(new Coordinates(who.getX(), who.getY()));
			}
			deathList.clear();

			// pass the locations to be painted back to the graphics engine
			synchronized (sharedLocations) {
				sharedLocations.addAll(locations);
			}
			System.out.println("locations: " + locations.size());
			locations.clear();
//
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		} // end while
	}

} // end class
