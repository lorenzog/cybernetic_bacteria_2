package cyberneticbacteria;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

public class MyWebServer implements Runnable {

	HttpServer server = null;
	Set<String> genePool;

	MyHandler handler = new MyHandler();

	MyWebServer(Set<String> genePool) {
		this.genePool = genePool;

		try {
			server = HttpServer.create(new InetSocketAddress(8000), 0);
		} catch (BindException e) {
			System.out.println("Kill other servers please");
		} catch (IOException e) {
			e.printStackTrace();
			// TODO throw our exception...
		}

		if (server == null) {
			System.out.println("Server not initialised");
			System.exit(-1);
		}
		server.createContext("/", handler);
		server.setExecutor(null); // creates a default executor
	}

	public void run() {
		server.start();
		System.out.println("HTTP server started.");
	}

	class MyHandler implements HttpHandler {

		String genotype;
		Pattern removeColonsPattern;
		Matcher m;

		String idRegexp = "(\\p{XDigit}{2}+:\\p{XDigit}{2}+:\\p{XDigit}{2}+:\\p{XDigit}{2}+(:\\p{XDigit}{2}+:\\p{XDigit}{2}+)*).*";

		MyHandler() {
			super();
			// prepare the pattern matching once..
			removeColonsPattern = Pattern.compile(idRegexp);
		}

		private String removeColons(String s, Matcher m, String type) {
			String genotype = null;

			// removes the : from the wireless rfid ID
			m = removeColonsPattern.matcher(s);
			if (m.matches()) {
				m.usePattern(Pattern.compile(":"));
				// type: wireless rfid
				genotype = type;
				genotype += m.replaceAll("");
			}
			// clean up after usage
			m = m.reset();
			return genotype;
		}

		private void parseIncomingData(InputStream rawInput) {

			String rawData = "";
			String[] rawIndividuals;
			String cRfid; /* the card rfid id */
			String wRfid; /* the wireless rfid id */
			String bluetoothData; /* bluetooth data */

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					rawInput));
			try {
				rawData = reader.readLine();
				reader.close();
			} catch (IOException e) {
				// connection reset by peer, more likely
				//e.printStackTrace();
				return;
			}

			// System.out.println("got: >" + rawData + "<");

			// split around empty space
			rawIndividuals = rawData.split(" ");

			// bluetooth is last
			int bluetoothDataPos = rawIndividuals.length - 1;
			// before it there is wireless rfid
			int wRfidPos = bluetoothDataPos - 1;
			// and before that the contact rfid
			int cRfidPos = bluetoothDataPos - 2;

			// check that there is some data
			if (cRfidPos < 0)
				// ok, got bogus data. back to where we came
				return;

			// we have real data from now on
			bluetoothData = rawIndividuals[bluetoothDataPos];
			wRfid = rawIndividuals[wRfidPos];
			cRfid = rawIndividuals[cRfidPos];

			boolean res;

			if ((genotype = removeColons(wRfid, m, "W")) != null)
				synchronized (genePool) {
					res = genePool.add(genotype);
					if (!res)
						System.out.println("duplicate string: " + genotype);
				}

			if ((genotype = removeColons(cRfid, m, "R")) != null)
				synchronized (genePool) {
					res = genePool.add(genotype);
					if (!res)
						System.out.println("duplicate string: " + genotype);
				}

			// split around "_ME_"
			String[] btInds = bluetoothData.split("_ME_");

			// find the single individuals
			for (int i = 0; i < btInds.length; i++) {
				m = (Pattern.compile(idRegexp)).matcher(btInds[i]);
				if (m.matches()) {
					if ((genotype = removeColons(m.group(1), m, "B")) != null)
						synchronized (genePool) {
							res = genePool.add(genotype);
							if (!res)
								System.out.println("duplicate string: "
										+ genotype);
						}
				}
			}
		}

		public void handle(HttpExchange exchange) throws IOException {

			//			
			// InputStream is = t.getRequestBody();
			// is.read(); // .. read the request body
			// String response = "This is the response; current time "
			// + System.currentTimeMillis();
			// t.sendResponseHeaders(200, response.length());
			// OutputStream os = t.getResponseBody();
			// os.write(response.getBytes());
			// os.close();

			// to determine the command
			String requestMethod = exchange.getRequestMethod();

			if (requestMethod.equalsIgnoreCase("GET")) {
				// getRequestHeaders()

				// SET the response headers, no cotent-length
				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-Type", "text/plain");

				String response = "genepool size at "
						+ System.currentTimeMillis() + ": " + genePool.size();

				Iterator<String> i = genePool.iterator();
				int count = 0;
				while (i.hasNext())
					response = response.concat("\nindividual[" + ++count
							+ "]: " + i.next());

				response = response.concat("\nEOT");

				// must happen before getResponseBody
				exchange.sendResponseHeaders(200, response.length());
				// send the response body
				OutputStream responseBody = exchange.getResponseBody();
				responseBody.write(response.getBytes());

				// Headers requestHeaders = exchange.getRequestHeaders();
				// Set<String> keySet = requestHeaders.keySet();
				// Iterator<String> iter = keySet.iterator();
				// while (iter.hasNext()) {
				// String key = iter.next();
				// List values = requestHeaders.get(key);
				// String s = key + " = " + values.toString() + "\n";
				// responseBody.write(s.getBytes());
				// }

				// close exchange and consume data from input
				responseBody.close();
			}

			if (requestMethod.equalsIgnoreCase("PUT")) {
				// get the body
				InputStream in = exchange.getRequestBody();
				parseIncomingData(in);
				// set response headers (do we?)
				// Headers responseHeaders = exchange.getResponseHeaders();
				// set 200 - OK; set to 0 for chunked sending
				exchange.sendResponseHeaders(200, -1);
				// write out
				// OutputStream out = exchange.getResponseBody();
				// nothing to do
				// out.close();
				in.close();
			}
		}
	}
}
