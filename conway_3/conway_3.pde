
/**
 * <p>
 * INFECTIOUS 2009: Conway's Game of Life meets RFID, Bluetooth via Arduino.
 * </p><p>
 * An art project by: Anna D., Tom K., Lorenzo G., Simon P. and Blay W.
 * </p>
 * hardware: <em>Tom K.</em>
 * source code: <em>Lorenzo G.</em>
 * philosophy: <em>Anna D.</em> and <em>Blay W.</em>
 * bacteria: <em>Simon P.</em>
 * </p><p>
 * credits: <ul>
 * <li>Conway's <a href="http://processing.org/learning/topics/conway.html">Game Of Life</a> by Mike Davis</li>
 * <li>Sun Microsystem lightweight <a href="http://java.sun.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html#package_description">http server</a> </li>
 * <li>and various code examples from Processing's website</ul>
 * </p>
 */



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

Set genePool = Collections.synchronizedSet(new LinkedHashSet());


// 1 = Red, 2 = Blue, 3 = White
Random rand = new Random();

final static int MAX_INDIV = 4; // red, blue, white

final static float DENSITY = 0.4f;

/* inoculation area radius */
final static int INOC_MIN = 20;
final static int INOC_MAX = 40;

int TEXT_SIZE = 16;

// count every X steps to increase/decrease death/birth ratio
int STEP_INTERVAL = 100;

// //////// END USER PARAMETERS /////////

final color COLOR_BLACK = color(0, 0, 0);
final color COLOR_RED = color(255, 0, 0);
final color COLOR_GREEN = color(0, 255, 0);
final color COLOR_BLUE = color(128, 0, 255);
final color COLOR_WHITE = color(255, 255, 255);

final int BLACK = 0, RED = 1, BLUE = 2, WHITE = 3;

int TEXT_STARTX, TEXT_STARTY;

// goes to zero every tick of the simulation
int fadingAlpha = 255;

int stepCounter = 0;

// becomes true every new update
boolean newUpdate = false;

int[] population;
int numIndiv, newBorns, deaths;

String textToWrite = "";

PFont font;

// the off-screen buffer
PGraphics buffer;
PImage img;
final int textAreaYSize = 50;

int sx, sy;
int[][][] world;

void setup() {
  //size(screen.width, screen.height, P2D);
  size(800, 600, P2D);

  // doesn't go more than 16FP on my mac..
  frameRate(1);

  sx = width;
  sy = height - textAreaYSize;

  // the text buffer
  buffer = createGraphics(sx, sy, P2D);

  font = createFont("Courier", 18);
  // where to display the text
  TEXT_STARTX = 10;
  TEXT_STARTY = sy + 20;

  world = new int[sx][sy][2]; // third field: [0] for type, [1] for future

  population = new int[MAX_INDIV];

  //  for (int i = 0; i < MAX_INDIV; i++)
  //    population[i] = 0;
  population[BLACK] = COLOR_BLACK;
  population[RED] = COLOR_RED;
  population[BLUE] = COLOR_BLUE;
  population[WHITE] = COLOR_WHITE;

  // inoculate first area with first type of individual
  // inoculate(numIndiv);

  // do one step of the game of life
  oneStep(buffer);

  // get the result
  img = buffer.get(0, 0, buffer.width, buffer.height);


  // run web server
  MyWebServer s = new MyWebServer(genePool);
  new Thread(s).start();


  Timer t = new Timer();
  t.schedule(new TimerTask() {
    private int oldSize = 0, newSize = 0;
    private String lastRecordedIndividual = null;
    Iterator i;
    List newIndividuals = new ArrayList();

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
            && (newIndividual = (String)i.next()) != lastRecordedIndividual) {
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
          lastRecordedIndividual = (String)i.next();
          inoculate(lastRecordedIndividual);
        }
      }
    }
  }
  , 1000, 500);

}

public void mousePressed() {
  generateRandomGenotype();
}

public void generateRandomGenotype() {
  int randomType = rand.nextInt(MAX_INDIV);
  String randomGenotype = null;
  switch ( randomType ) {
    case ( 1 ):
    randomGenotype = "R";
    break;
  case 2:
    randomGenotype = "B";
    break;
  case 3:
    randomGenotype = "W";
    break;
  default:
    randomGenotype = "B";
    break;
  }

  randomGenotype = randomGenotype.concat(" ");

  randomGenotype = randomGenotype.concat(Integer.toHexString(Math.abs(rand.nextInt()))).toUpperCase();

  inoculate(randomGenotype);
}


public void draw() {
  background(0);

  oneStep(buffer);

  image(img, 0, 0);

  synchronized(textToWrite) {
    updateText(textToWrite);
  }

}

public void updateText(String textToWrite) {
  textFont(font);
  textSize(TEXT_SIZE);
  if ( newUpdate ) {
    fill(COLOR_GREEN);
    newUpdate = false;
    fadingAlpha = 255;
  } 
  else {
    fadingAlpha -= 10;
    fadingAlpha = fadingAlpha < 0 ? 0 : fadingAlpha;
    fill(COLOR_GREEN, fadingAlpha);
  }
  stroke(255);
  strokeWeight(10);
  text(textToWrite, TEXT_STARTX, TEXT_STARTY);

  // back to visible
  // TODO growth rate? calculate
  fill(COLOR_GREEN);
  text("New cybernetic bacteria: " + newBorns + " dead: " + deaths + " total: " + numIndiv,  TEXT_STARTX, TEXT_STARTY + TEXT_SIZE);
}

/**
 * one step of the game of life
 */
public void oneStep(PGraphics buffer) {

  // the total counter
  numIndiv = 0;
  newBorns = 0;
  deaths = 0;

  buffer.beginDraw();
  buffer.background(0);
  buffer.noFill();

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
        buffer.set(i, j, toset);
        numIndiv++;


      }

      /* if death will occur */
      if (world[i][j][1] == -1) {
        world[i][j][0] = 0;
      }

      /* draw black the dead ones */
      if (world[i][j][0] == 0 && world[i][j][1] == 0)
        buffer.set(i, j, COLOR_BLACK);

      /* reset future state */
      world[i][j][1] = 0;
    }
  }

  // no more to be written on the screen, end buffer drawing and export it
  buffer.endDraw();
  img = buffer.get(0, 0, buffer.width, buffer.height);

  int majorityOfType;
  /* death and rebirth */
  for (int i = 0; i < sx; i++) {
    for (int j = 0; j < sy; j++) {
      int neighbors = countNeighbors(i, j);

      /* rebirth: if we're dead but we have 3 neighbors... */
      if (neighbors == 3 && world[i][j][0] == 0) {
        /* decide what will our type be */
        majorityOfType = countMajorityType(i, j);
        world[i][j][1] = majorityOfType;
        newBorns++;
        continue;
      }

      /* communication: if we're white, a blue makes us blue */
      if ( world[i][j][0] == WHITE && hasNeighOfType(i, j, BLUE) ) {
        world[i][j][0] = BLUE;
        continue;
      }      

      /* death for overcrowding or loneliness */
      if ((neighbors < 2 || neighbors > 3) && world[i][j][0] > 0) {
        world[i][j][1] = -1;
        deaths++;
        continue;
      }

      //      if ( ( neighbors < 2 || neighbors > 4 ) && world[i][j][0] > 0 ) 
      //        world[i][j][1] = -1;

    }
  }
}

boolean hasNeighOfType(int x, int y, int type) { 
  // look around
  int[] neighbors = getNeighs(x, y);

  for ( int i = 0 ; i < neighbors.length ; i++ )
    if ( neighbors[i] == type ) return true;
  return false;
}




/* create phenotype from parameters */
public int parseGenotype(String genotype) {
  int col;

  if (genotype.length() == 0) {
    System.err.println("no genotype?");
    col = 0;
    return col;
  }

  char c = genotype.charAt(0);

  switch (c) {
  case 'R':
    return RED;
  case 'B':
    return BLUE;
  case 'W':
    return WHITE;
  }
  return 0;
}


public void inoculate(String genotype) {
  int type = parseGenotype(genotype);


  int x, y;

  //  // get inoculation position from list
  //  switch ( type ) {
  //  case RED: // red
  //    break;
  //  case BLUE: // blue
  //    break;
  //  case WHITE: // white
  //    break;
  //  }

  // XXX remove
  x = rand.nextInt(sx);
  y = rand.nextInt(sy);

  // TODO make it circular
  int INOC_AREA = PApplet.parseInt(random(INOC_MIN, INOC_MAX));
  //  int INOC_AREA = 30;


  /* coordinates of current bacterium */
  int bact_x, bact_y;
  for (int i = 0; i < INOC_AREA; i++) {
    for (int j = 0; j < INOC_AREA; j++) {
      // inoculate
      if (random(1) > DENSITY) {
        bact_x = x - INOC_AREA / 2 + i;
        bact_y = y - INOC_AREA / 2 + j;
        if ( pow(INOC_AREA / 2 - i, 2) + pow(INOC_AREA / 2 - j, 2) > pow(INOC_AREA/2, 2) )
          continue;
        // set that spot to the individual type if within paintable
        // area
        world[bact_x > 0 && bact_x < sx ? bact_x : 0][bact_y > 0
          && bact_y < sy ? bact_y : 0][1] = type;
      }
    }
  }

  synchronized(textToWrite) {
    textToWrite = "Genotype " + genotype + ": inoculation at " + x + "," + y;
  }
  newUpdate = true;

}

/* old version, random location */
public void inoculate_random(int type) {

  /* wrap around */
  if (type >= MAX_INDIV) {
    type = 0;
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

}


int[] getNeighs(int x, int y) {
  int neighbors[] = new int[8];

  /* right */  neighbors[0] = world[(x + 1) % sx][y][0];
  /* top */  neighbors[1] = world[x][(y + 1) % sy][0];
  /* left */  neighbors[2] = world[(x + sx - 1) % sx][y][0];
  /* bottom */  neighbors[3] = world[x][(y + sy - 1) % sy][0];
  /* topright */  neighbors[4] = world[(x + 1) % sx][(y + 1) % sy][0];
  /* topleft */  neighbors[5] = world[(x + sx - 1) % sx][(y + 1) % sy][0];
  /* bottomleft */  neighbors[6] = world[(x + sx - 1) % sx][(y + sy - 1)
    % sy][0];
  /* bottomright */  neighbors[7] = world[(x + 1) % sx][(y + sy - 1) % sy][0];

  return neighbors;
}

public int getMajorityTypeRandom(int x, int y) {
  int selectedType = 0;
  int neighbors[] = getNeighs(x, y);

  // we already know that there are exactly 3 non-null neighbors
  do {
    selectedType = neighbors[PApplet.parseInt(random(8))];
  } 
  while (selectedType == 0);

  return selectedType;
}


public int countMajorityType(int x, int y) {
  int neighbors[] = getNeighs(x, y);

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
  int neighbors[] = getNeighs(x, y);

  for (int i = 0; i < 8; i++)
    if (neighbors[i] != 0)
      count++;

  return count;
}





//////////


















public class MyWebServer implements Runnable {

  HttpServer server = null;
  Set genePool;

  MyHandler handler = new MyHandler();

  MyWebServer(Set genePool) {
    this.genePool = genePool;

    try {
      server = HttpServer.create(new InetSocketAddress(8000), 0);
    } 
    catch (BindException e) {
      System.out.println("Kill other servers please");
    } 
    catch (IOException e) {
      // e.printStackTrace();
      // TODO throw our exception...?
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

      BufferedReader reader = new BufferedReader(new InputStreamReader(
      rawInput));
      try {
        rawData = reader.readLine();
        //System.out.println("got: >" + rawData + "<");
        reader.close();
      } 
      catch (IOException e) {
        // connection reset by peer, more likely
        //e.printStackTrace();
        return;
      }
    }

    private void parseIncomingData(String rawData) {
      String[] rawIndividuals;

      String cRfid; /* the card rfid id */
      String wRfid; /* the wireless rfid id */
      String bluetoothData; /* bluetooth data */


      System.out.println("got: >" + rawData + "<");

      // split around empty space
      rawIndividuals = rawData.split(" ");

      // bluetooth is last
      int bluetoothDataPos = rawIndividuals.length - 1;
      // before it there is wireless rfid
      int wRfidPos = bluetoothDataPos - 1;
      // and before that the contact rfid
      int cRfidPos = bluetoothDataPos - 2;

      //      for ( int i = 0 ; i < rawIndividuals.length ; i++ ) 
      //        System.out.println("rawInd[" + i + "]: " + rawIndividuals[i]);

      // check that there is some data
      if (cRfidPos < 0)
        // ok, got bogus data. back to where we came
        return;

      // we have real data from now on
      bluetoothData = rawIndividuals[bluetoothDataPos];
      wRfid = rawIndividuals[wRfidPos];
      cRfid = rawIndividuals[cRfidPos];

      boolean res;

      if ((genotype = removeColons(wRfid, m, "W")) != null) {
        //        System.out.println("w: genotype " + genotype);
        synchronized (genePool) {
          res = genePool.add(genotype);
          if (!res)
            System.out.println("duplicate string: " + genotype);
        }
      }

      if ((genotype = removeColons(cRfid, m, "R")) != null) {
        //        System.out.println("r: genotype " + genotype);
        synchronized (genePool) {
          res = genePool.add(genotype);
          if (!res)
            System.out.println("duplicate string: " + genotype);
        }
      }

      // split around "_ME_"
      String[] btInds = bluetoothData.split("_ME_");

      // find the single individuals
      for (int i = 0; i < btInds.length; i++) {
        m = (Pattern.compile(idRegexp)).matcher(btInds[i]);
        if (m.matches()) {
          if ((genotype = removeColons(m.group(1), m, "B")) != null) {
            //            System.out.println("b: genotype " + genotype);
            synchronized (genePool) {
              res = genePool.add(genotype);
              if (!res)
                System.out.println("duplicate string: "
                  + genotype);
            }
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
        System.out.println("got get");


        // getRequestHeaders()

        // SET the response headers, no cotent-length
        Headers responseHeaders = exchange.getResponseHeaders();

        InputStream in = exchange.getRequestBody();
        parseIncomingData(in);
        exchange.sendResponseHeaders(200, -1);
        // write out
        // OutputStream out = exchange.getResponseBody();
        // nothing to do
        // out.close();
        in.close();


        //        responseHeaders.set("Content-Type", "text/plain");
        //
        //        String response = "genepool size at "
        //          + System.currentTimeMillis() + ": " + genePool.size();
        //
        //        Iterator<String> i = genePool.iterator();
        //        int count = 0;
        //        while (i.hasNext())
        //          response = response.concat("\nindividual[" + ++count
        //            + "]: " + i.next());
        //
        //        response = response.concat("\nEOT");
        //
        //        // must happen before getResponseBody
        //        exchange.sendResponseHeaders(200, response.length());
        //        // send the response body
        //        OutputStream responseBody = exchange.getResponseBody();
        //        responseBody.write(response.getBytes());




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
        //        responseBody.close();
      }

      if (requestMethod.equalsIgnoreCase("PUT")) {
        // get the body
        // InputStream in = exchange.getRequestBody();
        URI uri = exchange.getRequestURI();
        // parseIncomingData(in);
        parseIncomingData(uri.getSchemeSpecificPart());
        // set response headers (do we?)
        // Headers responseHeaders = exchange.getResponseHeaders();
        // set 200 - OK; set to 0 for chunked sending
        exchange.sendResponseHeaders(200, -1);
        // write out
        // OutputStream out = exchange.getResponseBody();
        // nothing to do
        // out.close();
        //        in.close();
      }
    }
  }


}










































