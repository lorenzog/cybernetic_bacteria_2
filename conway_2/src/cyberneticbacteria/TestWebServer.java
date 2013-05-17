package cyberneticbacteria;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class TestWebServer {

	static Set<String> genePool = new HashSet<String>();
	Random rand = new Random();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// run web server
		MyWebServer s = new MyWebServer(genePool);
		new Thread(s).start();
		//
		// Timer t = new Timer();
		// t.scheduleAtFixedRate(new TimerTask() {
		// private int oldSize = 0, newSize = 0;
		//
		// public void run() {
		// synchronized (genePool) {
		// newSize = genePool.size();
		// }
		// if (oldSize != newSize) {
		// System.out.println("old size: " + oldSize + " new size: " + newSize);
		// oldSize = newSize;
		// }
		// }
		// }, 1000, 2000);

		// keep checking for new individuals
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
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

						System.out.println("old size: " + oldSize
								+ " new size: " + newSize);

						newIndividuals.clear();
						oldSize = newSize;
						i = genePool.iterator();
						// for each new guy
						while (i.hasNext()
								&& (newIndividual = i.next()) != lastRecordedIndividual) {
							// add to temporary list
							newIndividuals.add(newIndividual);
						}
						// everybody has been added to list, let's go process it
						canProcessTemporaryList = true;
					}
				} // end critical section

				if (canProcessTemporaryList) {
					i = newIndividuals.iterator();
					while (i.hasNext())
						// TODO inoculate where?!?
						// inoculate(rand.nextInt(sx), rand.nextInt(sy),
						// lastRecordedIndividual = i.next());
						System.out.println("new inoculation with "
								+ (lastRecordedIndividual = i.next()));
				}
			}
		}, 1000, 2000);

	}

}
