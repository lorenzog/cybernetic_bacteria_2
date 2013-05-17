package cyberneticbacteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import processing.core.*;

@SuppressWarnings("serial")
public class CyberneticBacteriaApplet extends PApplet {

	int sx, sy;

	/**
	 * a dynamic list of 'real' individuals: RFID and bluetooth codes, etc; will
	 * be monitored for size; if growing -> the new individual will be added to
	 * population through the inoculate() method
	 */
	static Set<String> genePool = Collections
			.synchronizedSet(new LinkedHashSet<String>());

	// the locations to be painted
	static List<Coordinates> locations = Collections
			.synchronizedList(new ArrayList<Coordinates>());

	Random rand = new Random();

	/**
	 * main drawing function; gets iterated every frame
	 */
	public void draw() {

		synchronized (locations) {
			for (Coordinates c : locations) {
				set(c.x, c.y, c.color);
			}

			locations.clear();
		}
	}

	// ///

	/**
	 * ran once at launch
	 */
	public void setup() {
		// this should be the first call..?
		size(640, 480);

		// XXX
		// leave a corridor below for writing

		noStroke();
		fill(0);

		/*
		 * would be more accurate to use real milliseconds and do time
		 * slicing?...
		 */
		frameRate(1);

		sx = width;
		sy = height;

		// run the simulator
		final CyberneticBacteria c = new CyberneticBacteria(genePool,
				locations, sx, sy);
		new Thread(c).start();

		// run web server
		MyWebServer s = new MyWebServer(genePool);
		new Thread(s).start();

		// keep checking for new individuals and adds them to gene pool +
		// inoculation
		// starts in a second, runs every 10 seconds
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
						c.inoculate(rand.nextInt(sx), rand.nextInt(sy),
								lastRecordedIndividual = i.next());
						// TODO print on screen!
					}
				}
			}
		}, 1000, 500);

		background(0);
	}

	public static void main(String args[]) {

		// run ourselves
		PApplet.main(new String[] { "--bgcolor=#FFFFFF -Xms256m -Xmx1024m",
				"CyberneticBacteriaApplet" });
	}

}
