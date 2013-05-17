/**
 *
 * inspired by conway
 */

int sx, sy;
Location[][] world;

/* individual types */
//Individual[] preys;
//Individual[] predators;
//Individual[] neutrals;

ArrayList preys;
ArrayList predators;
ArrayList neutrals;


static final float PREYS_RATIO = 0.01;
static final float PREDATORS_RATIO = 0.001;
static final float NEUTRALS_RATIO = 0.05;

static final int PREY = 1;
static final int PREDATOR = 2;
static final int NEUTRAL = 3;

static final int COLOR_PREY = #FFFFFF;
static final int COLOR_PREDATOR = #FF0000;
static final int COLOR_NEUTRAL = #007F00;

void setup() 
{
  size(300,200, P2D);
  frameRate(10);
  sx = width;
  sy = height;
  int size = sx * sy;

  int i, j;

  world = new Location[sx][sy];


  for ( i = 0 ; i < sx ; i++ ) 
  {
    for ( j = 0 ; j < sy ; j++ )
    {
      world[i][j] = new Location();
    }
  }

  /* max number of individuals */
  /*
	   preys = new Individual[size];
   	   predators = new Individual[size];
   	   neutrals = new Individual[size];
   	 */
  preys = new ArrayList(size);
  predators = new ArrayList(size);
  neutrals = new ArrayList(size);

  /* set up the initial population */
  int num_preys = (int)(random(size) * PREYS_RATIO);
  int num_predators = (int)(random(size) * PREDATORS_RATIO);
  int num_neutrals = (int)(random(size) * NEUTRALS_RATIO);
  /*
  // XXX
   num_preys = 0;
   num_predators = 1;
   num_neutrals = 0;
   */
  /* place preys at initial locations */
  int xnew, ynew;

  Individual[] neigh;
  for ( i = 0 ; i < num_preys ; i++ )
  {
    do {
      xnew = int(random(sx));
      ynew = int(random(sy));
    } 
    while ( world[xnew][ynew].whoshere() != null );
    neigh = calculateNeigh(xnew, ynew);
    //preys[i] = new Individual(xnew, ynew, PREY, neigh);

    Individual indiv = new Individual(xnew, ynew, PREY, neigh);
    preys.add(indiv);
    world[xnew][ynew].placeIndividual(indiv);
  }

  /* place predators */
  for ( i = 0 ; i < num_predators ; i++ )
  {
    do {
      xnew = int(random(sx));
      ynew = int(random(sy));
    } 
    while ( world[xnew][ynew].whoshere() != null );
    neigh = calculateNeigh(xnew, ynew);
    //predators[i] = new Individual(xnew, ynew, PREDATOR, neigh);
    Individual indiv = new Individual(xnew, ynew, PREDATOR, neigh);
    predators.add(indiv);
    world[xnew][ynew].placeIndividual(indiv);
  }

  /* place neutrals */
  for ( i = 0 ; i < num_neutrals ; i++ )
  {
    do {
      xnew = int(random(sx));
      ynew = int(random(sy));
    } 
    while ( world[xnew][ynew].whoshere() != null );
    neigh = calculateNeigh(xnew, ynew);
    //neutrals[i] = new Individual(xnew, ynew, NEUTRAL, neigh);
    Individual indiv = new Individual(xnew, ynew, NEUTRAL, neigh);
    neutrals.add(indiv);
    world[xnew][ynew].placeIndividual(indiv);
  }

}

/**
 * calculate the neighbours
 */
Individual[] calculateNeigh(int x, int y) {
  Individual[] n = new Individual[9];

  n[0] = world[(x+1) % sx][(y+sy-1) % sy].whoshere();
  n[1] = world[(x+1) % sx][y].whoshere();
  n[2] = world[(x+1) % sx][(y+1) % sy].whoshere();
  n[3] = world[x][(y+sy-1) % sy].whoshere();
  //  n[4] = world[x][y].whoshere();  // me!
  n[4] = null;
  n[5] = world[x][(y+1) % sy].whoshere();
  n[6] = world[(x+sx-1) % sx][(y+sy-1) % sy].whoshere();
  n[7] = world[(x+sx-1) % sx][y].whoshere();
  n[8] = world[(x+sx-1) % sx][(y+1) % sy].whoshere();

  return n; 
}


void draw() 
{
  background(0);

  /* draw the individuals */

  /*
	   int i = 0;
   	   while ( preys[i] != null )
   	   {
   	   set(preys[i].getX(), preys[i].getY(), COLOR_PREY);
   	   i++;
   	   }
   	   int num_preys = i-1;
   
   	   i = 0;
   	   while ( predators[i] != null )
   	   {
   	   set(predators[i].getX(), predators[i].getY(), COLOR_PREDATOR);
   	   i++;
   	   }
   	   int num_predators = i-1;
   
   	   i = 0;
   	   while ( neutrals[i] != null )
   	   {
   	   set(neutrals[i].getX(), neutrals[i].getY(), COLOR_NEUTRAL);
   	   i++;
   	   }
   	   int num_neutrals = i-1;
   	 */
  int i;
  int x, y;
  for ( i = 0 ; i < preys.size() ; i++ )
  {
    x = ((Individual)preys.get(i)).getX();
    y = ((Individual)preys.get(i)).getY();
    set (x, y, COLOR_PREY);
  }
  for ( i = 0 ; i < predators.size() ; i++ )
  {
    x = ((Individual)predators.get(i)).getX();
    y = ((Individual)predators.get(i)).getY();
    set (x, y, COLOR_PREDATOR);
  }
  for ( i = 0 ; i < neutrals.size() ; i++ )
  {
    x = ((Individual)neutrals.get(i)).getX();
    y = ((Individual)neutrals.get(i)).getY();
    set (x, y, COLOR_NEUTRAL);
  }

  System.out.println("Preys: " + preys.size() + " predators: " + predators.size() + " neutrals: " + neutrals.size());

  // if new individuals show up -> add to world
  // (perhaps - keep adding whatever comes in?)

  survival(preys);
  survival(predators);
  survival(neutrals);

  // apply genetical rules (phenotype)
  // CHECK whether the individual is still alive!!

  // if predator...
  // if prey...
  // if neutral...


}

/**
 * the survival rules
 */
void survival(ArrayList individuals) {

  int j;
  // for each individual
  for ( j = 0 ; j < individuals.size() ; j++ )
  { 
    Individual indiv = (Individual)individuals.get(j);
    int myType = indiv.getType();

    // get the neighbours
    Individual[] n = indiv.getNeigh();

    int i;
    // for each neighbour
    for ( i = 0 ; i < n.length ; i++ )
    {
      if ( n[i] == null || indiv == null )
      {
        continue;  // avoid ourselves and empty places
      }
      if ( indiv.equals(n[i]) )
        continue;

      int neighType = n[i].getType();

      switch ( myType )
      {

      case PREY:
        /* we are a prey, they are... */

        switch ( neighType )
        {
        case PREY:
          /* prey-prey: nothing happens */
          break;
        case PREDATOR:
          /* prey-predator: prey dies */
          // remove from prey list
          if ( j < preys.size() )
            preys.remove(j);
          // mark null
          indiv = null;
          break;
        case NEUTRAL:
          /* prey-neutral: got food! replicate */
          Individual tmp = n[i];
          tmp.setType(PREY);
          if ( j < neutrals.size() )
            neutrals.remove(j);  // remove element at pos ..

          preys.add(tmp);
          break;
        default:
          System.out.println("wtf?");
        }
        break;

      case PREDATOR:
        switch ( neighType )
        {
        case PREY:
          /* predator-prey: prey dies */
          int loserIndex = preys.indexOf(n[i]);
          if ( loserIndex != -1 )
          {
            preys.remove(loserIndex);
            n[i] = null;
          }
          break;
        case PREDATOR:
          System.out.println("I am at " + indiv.getX() + "," + indiv.getY() + " other is at " + n[i].getX() + "," + n[i].getY());
          Individual loser;
          if ( int(random(2)) % 2 == 0 ) 
          {
            loser = n[i];
            loserIndex = predators.indexOf(loser);
          }
          else
          {
            loser = indiv;
            loserIndex = j;
          }

          if ( loserIndex < predators.size() )
          {
            loser.setType(NEUTRAL);
            predators.remove(loserIndex);
            neutrals.add(loser);
          }

          break;
        case NEUTRAL:
          /* do nothing */
          break;
        default:
          System.out.println("wtf?");
        }      
        break;

      case NEUTRAL:
        break;

      default:
        System.out.println("wtf?!?");
      }


    }


    // if we're dead
    if ( indiv == null || myType == NEUTRAL )
    {
      //System.out.println("I'm dead or neutral.");
    }
    else
    {
      // move
      move(indiv);
    }

  }

}

void move (Individual i) {
  int direction_x, direction_y, stride;
  int xnew, ynew;
  Individual[] neigh;

  int step_x, step_y;

  do {

    // read the genotype
    // decide stride length
    // decide direction x
    // decide direction y

    // for now is random
    direction_x = int(random(2)) % 2 == 0 ? 1 : -1;
    //direction_y = int(random(2)) % 2 == 0 ? 1 : -1;
    direction_y = 0;
    stride = int(random(2));

    //   n[0] = world[(x+1) % sx][(y+sy-1) % sy].whoshere();
    step_x = ( direction_x > 0 ? stride : direction_x * stride + sx );
    step_y = ( direction_y > 0 ? stride : direction_y  * stride + sy );    
    xnew = ( i.getX() + step_x ) % sx;
    ynew = ( i.getY() + step_y ) % sy;
  }
  while ( world[xnew][ynew].whoshere() != null );

  i.setX(xnew);
  i.setY(ynew);
  neigh = calculateNeigh(xnew, ynew);
  i.setNeigh(neigh);

  // for each neigh, recalculate
  for ( int j = 0 ; j < neigh.length ; j++ ) 
  {
    if ( neigh[j] != null )
    {
      Individual[] tmpNeighs = calculateNeigh(neigh[j].getX(), neigh[j].getY());
      neigh[j].setNeigh(tmpNeighs);
    }
  }

}























