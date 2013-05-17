import java.util.ArrayList;
import java.util.Random;

public class MyTest {

	MyTest() {

		Individual[] neighbors = findNeighbors(parent);
		parent.setNeighbors(neighbors);

		int n;

		// copy around
		for (int i = 0; i < neighbors.length; i++)
			neighbors[i] = new Individual(g);

		// kill one at random
		n = rand.nextInt(neighbors.length);
		neighbors[n].isFood(true);
		System.out.println("Killed neighbor " + n);

		if (isFoodAround(parent)) {
			System.out.println("Found Food");

			// select a food spot where to replicate
			do {
				n = rand.nextInt(neighbors.length);
			} while (!neighbors[n].isFood());

			Individual sibling = neighbors[n];

			char[] genotype = sibling.getGenotype().toCharArray();

			for (int i = 0; i < genotype.length; i++) {

				if (rand.nextGaussian() + 0.5 < MUTATE_PROB) {
					// mutate that gene

					// get char at that location
					char gene = genotype[i];
					System.out.print("Mutating gene at location " + i + ":"
							+ gene);

					if (i == 0) {
						System.out.print(" (type locus) ");
						
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
						System.out.print(" (non-type locus) ");
						
						// if it's another gene
						genotype[i] = Character.forDigit(((gene + 1) % 16), 16);
					}

					System.out.println("into " + genotype[i]);

				}
			}
			
			sibling.setGenotype(new String(genotype));

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MyTest t = new MyTest();
	}
}
