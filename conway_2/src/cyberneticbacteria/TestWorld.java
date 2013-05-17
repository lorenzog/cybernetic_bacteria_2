package cyberneticbacteria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

public class TestWorld {

	/**
	 * the world, as a collection of locations
	 */
	private Map<Coordinates, Individual> world;
	int sx = 640;
	int sy = 480;
	Random rand = new Random();

	TestWorld() {
		world = new HashMap<Coordinates, Individual>();

		Coordinates c1 = new Coordinates(rand.nextInt(sx), rand.nextInt(sy));
		Individual i1 = new Individual("Baaaaaaa", c1);

		world.put(c1, i1);

		System.out.println("i1 is at " + c1.x + "," + c1.y);

		Coordinates test = new Coordinates(c1.x, c1.y);

		System.out.println(world.containsKey(test));
		System.out.println(world.get(test));
		System.out.println(world.size());

		for (Coordinates c : world.keySet())
			System.out.println(c);

		for (Map.Entry<Coordinates, Individual> e : world.entrySet())
			System.out.println(e.getKey() + ": " + e.getValue());

	}

	private List<Coordinates> findNeighbors(Individual ind) {
		int x = ind.getX();
		int y = ind.getY();

		List<Coordinates> possibleLocations = new ArrayList<Coordinates>();

		int tmp_x, tmp_y;

		/* right */
		tmp_x = (x + 1) % sx;
		tmp_y = y;
		if (world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* top */
		tmp_x = x;
		tmp_y = (y + 1) % sy;
		if (world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* left */
		tmp_x = (x + sx - 1) % sx;
		tmp_y = y;
		if (world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* bottom */
		tmp_x = x;
		tmp_y = (y + sy - 1) % sy;
		if (world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* topright */
		tmp_x = (x + 1) % sx;
		tmp_y = (y + 1) % sy;
		if (world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* topleft */
		tmp_x = (x + sx - 1) % sx;
		tmp_y = (y + 1) % sy;
		if (world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* bottomleft */
		tmp_x = (x + sx - 1) % sx;
		tmp_y = (y + sy - 1) % sy;
		if (world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		/* bottomright */
		tmp_x = (x + 1) % sx;
		tmp_y = (y + sy - 1) % sy;
		if (world.containsKey(new Coordinates(tmp_x, tmp_y)))
			possibleLocations.add(new Coordinates(tmp_x, tmp_y));

		return possibleLocations;
	}

	public static void main(String[] args) {
		new TestWorld();
	}
}
