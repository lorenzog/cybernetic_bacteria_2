package cyberneticbacteria;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


import processing.core.*;

//import processing.xml.*; 

@SuppressWarnings("serial")
public class MyConway extends PApplet { // implements Runnable {

	final static int MAX_INDIV = 255; // at most MAXINT (used in world[][][xxx])

	final static float DENSITY = 0.8f;

	/* inoculation area radius */
	final static int INOC_MIN = 20;
	final static int INOC_MAX = 40;

	// //////// END USER PARAMETERS /////////

	int[] population;
	int num_individuals;

	PFont font;

	int sx, sy;
	int[][][] world;
	
	Random rand = new Random();
	

	/**
	 * a dynamic list of 'real' individuals: RFID and bluetooth codes, etc; will
	 * be monitored for size; if growing -> the new individual will be added to
	 * population through the inoculate() method
	 */
	static Set<String> genePool = Collections
			.synchronizedSet(new LinkedHashSet<String>());


	public void inoculate(int x, int y, int type) {
		int INOC_AREA = PApplet.parseInt(random(INOC_MIN, INOC_MAX));

		/* wrap around */
		if (type >= MAX_INDIV) {
			num_individuals = 1;
			type = num_individuals;
		}

		/* initialise individual with random color */
		population[type] = color(PApplet.parseInt(random(255)), PApplet
				.parseInt(random(255)), PApplet.parseInt(random(255)));

		/* coordinates of current bacterium */
		int bact_x, bact_y;
		for (int i = 0; i < INOC_AREA; i++) {
			for (int j = 0; j < INOC_AREA; j++) {
				// inoculate
				if (random(1) > DENSITY) {
					bact_x = x - INOC_AREA / 2 + i;
					bact_y = y - INOC_AREA / 2 + j;
					// set that spot to the individual type if within paintable
					// area
					world[bact_x > 0 && bact_x < sx ? bact_x : 0][bact_y > 0
							&& bact_y < sy ? bact_y : 0][1] = type;

				}
			}
		}

		num_individuals++;
	}

	/* old version, random location */
	public void inoculate_random(int type) {

		/* wrap around */
		if (type >= MAX_INDIV) {
			num_individuals = 1;
			type = num_individuals;
		}

		// the size of the inoculation area
		int INOC_AREA = PApplet.parseInt(random(INOC_MIN, INOC_MAX));
		// decide placement
		int x = PApplet.parseInt(random(sx - 2 * INOC_AREA)) + INOC_AREA / 2;
		int y = PApplet.parseInt(random(sy - 2 * INOC_AREA)) + INOC_AREA / 2;

		// the color
		population[type] = color(PApplet.parseInt(random(255)), PApplet
				.parseInt(random(255)), PApplet.parseInt(random(255)));

		for (int i = 0; i < INOC_AREA; i++) {
			for (int j = 0; j < INOC_AREA; j++) {
				// inoculate
				if (random(1) > DENSITY) {
					world[x - INOC_AREA / 2 + i][y - INOC_AREA / 2 + j][0] = type;
				}
			}
		}

		num_individuals++;
	}
	

	public int getMajorityTypeRandom(int x, int y) {
		int selectedType = 0;
		int neighbors[] = new int[8];

		/* right */neighbors[0] = world[(x + 1) % sx][y][0];
		/* top */neighbors[1] = world[x][(y + 1) % sy][0];
		/* left */neighbors[2] = world[(x + sx - 1) % sx][y][0];
		/* bottom */neighbors[3] = world[x][(y + sy - 1) % sy][0];
		/* top right */neighbors[4] = world[(x + 1) % sx][(y + 1) % sy][0];
		/* top left */neighbors[5] = world[(x + sx - 1) % sx][(y + 1) % sy][0];
		/* bottom left */neighbors[6] = world[(x + sx - 1) % sx][(y + sy - 1)
				% sy][0];
		/* bottom right */neighbors[7] = world[(x + 1) % sx][(y + sy - 1) % sy][0];

		// we already know that there are exactly 3 non-null neighbors
		do {
			selectedType = neighbors[PApplet.parseInt(random(8))];
		} while (selectedType == 0);

		return selectedType;
	}

	public int countMajorityType(int x, int y) {
		int neighbors[] = new int[8];

		/* right */neighbors[0] = world[(x + 1) % sx][y][0];
		/* top */neighbors[1] = world[x][(y + 1) % sy][0];
		/* left */neighbors[2] = world[(x + sx - 1) % sx][y][0];
		/* bottom */neighbors[3] = world[x][(y + sy - 1) % sy][0];
		/* topright */neighbors[4] = world[(x + 1) % sx][(y + 1) % sy][0];
		/* topleft */neighbors[5] = world[(x + sx - 1) % sx][(y + 1) % sy][0];
		/* bottomleft */neighbors[6] = world[(x + sx - 1) % sx][(y + sy - 1)
				% sy][0];
		/* bottomright */neighbors[7] = world[(x + 1) % sx][(y + sy - 1) % sy][0];

		// we already know we have 3 'on' neighbors;
		// so it can be either AAA, AAB, ABC
		// therefore the first one that has 2 matches is the winner
		// otherwise random choice amongst the 3
		int[] candidates = new int[3];
		for (int i = 0; i < 3; i++)
			candidates[i] = 0;

		for (int i = 0; i < 8; i++) {
			// don't care about empty neighbors
			if (neighbors[i] == 0)
				continue;

			// if uninitialised
			if (candidates[0] == 0) {
				candidates[0] = neighbors[i]; // assign
				continue;
			}
			// second match
			if (candidates[0] == neighbors[i])
				return candidates[0];

			if (candidates[1] == 0) {
				candidates[1] = neighbors[i];
				continue;
			}
			if (candidates[1] == neighbors[i])
				return candidates[1];

			if (candidates[2] == 0) {
				candidates[2] = neighbors[i];
				continue;
			}
			if (candidates[2] == neighbors[i])
				return candidates[2];
		}

		// if it reaches here then exactly three matching cases
		return candidates[PApplet.parseInt(random(3))];

	}

	// Count the number of adjacent cells 'on'
	public int countNeighbors(int x, int y) {
		int count = 0;
		int neighbors[] = new int[8];

		/* right */neighbors[0] = world[(x + 1) % sx][y][0];
		/* top */neighbors[1] = world[x][(y + 1) % sy][0];
		/* left */neighbors[2] = world[(x + sx - 1) % sx][y][0];
		/* bottom */neighbors[3] = world[x][(y + sy - 1) % sy][0];
		/* topright */neighbors[4] = world[(x + 1) % sx][(y + 1) % sy][0];
		/* topleft */neighbors[5] = world[(x + sx - 1) % sx][(y + 1) % sy][0];
		/* bottomleft */neighbors[6] = world[(x + sx - 1) % sx][(y + sy - 1)
				% sy][0];
		/* bottomright */neighbors[7] = world[(x + 1) % sx][(y + sy - 1) % sy][0];

		for (int i = 0; i < 8; i++)
			if (neighbors[i] != 0)
				count++;

		return count;
	}

	// add individual ?
	public void mousePressed() {
		fill(0xff0000FF);
		text("new colony", width / 2, height / 2);
		inoculate(mouseX, mouseY, num_individuals);
		// inoculate_random(num_individuals);
	}

	public void draw() {
		// background(0);
	
		/* drawing and update cycle */
		for (int i = 0; i < sx; i++) {
			for (int j = 0; j < sy; j++) {
				/* if future state is alive or if no change should take place */
				if (world[i][j][1] > 0
						|| (world[i][j][1] == 0 && world[i][j][0] > 0)) {
					world[i][j][0] = (world[i][j][1] > 0 ? world[i][j][1]
							: world[i][j][0]);
					// colour with the color it had before
					int toset = (world[i][j][0] > 0 ? population[world[i][j][0]]
							: population[world[i][j][1]]);
					set(i, j, toset);
				}
	
				/* if death will occur */
				if (world[i][j][1] == -1)
					world[i][j][0] = 0;
	
				/* draw black the dead ones */
				if (world[i][j][0] == 0 && world[i][j][1] == 0)
					set(i, j, 0xff000000);
	
				/* reset future state */
				world[i][j][1] = 0;
			}
		}
	
		int majorityOfType;
		/* death and rebirth */
		for (int i = 0; i < sx; i++) {
			for (int j = 0; j < sy; j++) {
				int neighbors = countNeighbors(i, j);
	
				// stay alive
				if (neighbors == 3 && world[i][j][0] == 0) {
					// the most abundant individuals nearby are of type...
					majorityOfType = countMajorityType(i, j);
					// most = random_most_ab(i, j);
					world[i][j][1] = majorityOfType;
				}
	
				/* death for overcrowding or loneliness */
				if ((neighbors < 2 || neighbors > 3) && world[i][j][0] > 0)
					world[i][j][1] = -1;
			}
		}
	}

	public void setup() {
		size(640, 480);
		frameRate(10);
		sx = width;
		sy = height;
	
		font = loadFont("Univers45.vlw");
		textFont(font);
	
		world = new int[sx][sy][2]; // third field: [0] for type, [1] for future
	
		population = new int[MAX_INDIV];
	
		for (int i = 0; i < MAX_INDIV; i++)
			population[i] = 0;
	
		num_individuals = 1;
		// inoculate first area with first type of individual
		// inoculate(num_individuals);
		
		
	
		// set background here so it won't be redrawn when writing text
		background(0);
		
		

		// run web server
		MyWebServer s = new MyWebServer(genePool);
		new Thread(s).start();

		
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			private int oldSize = 0, newSize = 0;
			private String lastRecordedIndividual = null;
			Iterator<String> i;
			List<String> newIndividuals = new ArrayList<String>();

			public void run() {

				String newIndividual = null;
				boolean canProcessTemporaryList = false;

				synchronized (genePool) {
					newSize = genePool.size();

					// if there's something new
					if (oldSize != newSize) {

						newIndividuals.clear();
						oldSize = newSize;
						i = genePool.iterator();
						// for each new guy
						while (i.hasNext()
								&& (newIndividual = i.next()) != lastRecordedIndividual) {
							// add to temporary list
							newIndividuals.add(newIndividual);
							lastRecordedIndividual = newIndividual;
							// everybody has been added to list, let's go
							// process it
							canProcessTemporaryList = true;
						}
					}
				} // end critical section

				if (canProcessTemporaryList) {
					i = newIndividuals.iterator();
					while (i.hasNext()) {
						// TODO inoculate where?!?
						lastRecordedIndividual = i.next();
						//inoculate(rand.nextInt(sx), rand.nextInt(sy), lastRecordedIndividual);
						mousePressed();
						// TODO print on screen!
					}
				}
			}
		}, 1000, 3000);

	}

	public static void main(String args[]) {


		// run ourselves
		PApplet.main(new String[] { "--bgcolor=#FFFFFF", "MyConway" });
	}

}
