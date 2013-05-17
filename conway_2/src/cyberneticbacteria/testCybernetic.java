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

public class testCybernetic {

	static int sx, sy;

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
	 * @param args
	 */
	public static void main(String[] args) {

		sx = 640;
		sy = 480;
		testCybernetic t = new testCybernetic();

	}

	testCybernetic() {

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
		}, 1000, 3000);

		while (true) {

			Iterator<Coordinates> i;
			Coordinates coord;

			synchronized (locations) {
				// for (Coordinates c : locations) {
				// set(c.x, c.y, c.color);
				// }
				i = locations.iterator();
				while (i.hasNext()) {
					coord = i.next();
					// set(c.x, c.y, c.color);
				}

				locations.clear();
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
