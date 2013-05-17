final static int MAX_INDIV = 255;   // at most MAXINT (used in world[][][xxx])

final static float DENSITY = 0.8;

/* inoculation area radius */
final static int INOC_MIN = 20;
final static int INOC_MAX = 40;


////////// END USER PARAMETERS /////////

color[] individuals;
int num_individuals;

PFont font;

int sx, sy;
int[][][] world;

void setup() 
{
  size(640,480);
  frameRate(10);
  sx = width;
  sy = height;

  font = loadFont("Univers45.vlw");
  textFont(font);
  
  world = new int[sx][sy][2];  // third field: [0] for type, [1] for future

  individuals = new color[MAX_INDIV];

  for ( int i = 0 ; i < MAX_INDIV ; i++ )
    individuals[i] = 0 ;

  num_individuals = 1;
  // inoculate first area with first type of individual
  //inoculate(num_individuals);

  background(0);
}

void inoculate (int x, int y, int type) {
  int INOC_AREA = int(random(INOC_MIN, INOC_MAX));

  /* wrap around */
  if ( type >= MAX_INDIV )
  {
    num_individuals = 1;
    type = num_individuals;
  }
  
  individuals[type] = color(int(random(255)), int(random(255)), int(random(255)));
  
  int bact_x, bact_y;
  for ( int i = 0 ; i < INOC_AREA ; i++ )
  {
    for ( int j = 0 ; j < INOC_AREA ; j++ )
    {
      // inoculate
      if ( random(1) > DENSITY )
      {
        bact_x = x-INOC_AREA/2+i;
        bact_y = y-INOC_AREA/2+j;
        // set that spot to the individual type if within paintable area
        world[bact_x > 0 && bact_x < sx ? bact_x : 0 ]
          [bact_y > 0 && bact_y < sy ? bact_y : 0]
          [1] = type; 
          
      }
    }
  }

  num_individuals++;
}

/* old version, random location */
void inoculate_random (int type) {

  /* wrap around */
  if ( type >= MAX_INDIV )
  {
    num_individuals = 1;
    type = num_individuals;
  }

  // the size of the inoculation area
  int INOC_AREA = int(random(INOC_MIN, INOC_MAX));
  // decide placement
  int x = int(random(sx-2*INOC_AREA)) + INOC_AREA/2;
  int y = int(random(sy-2*INOC_AREA)) + INOC_AREA/2;

  // the color
  individuals[type] = color(int(random(255)), int(random(255)), int(random(255)));

  for ( int i = 0 ; i < INOC_AREA ; i++ )
  {
    for ( int j = 0 ; j < INOC_AREA ; j++ )
    {
      // inoculate
      if ( random(1) > DENSITY )
      {
        world[x-INOC_AREA/2+i][y-INOC_AREA/2+j][0] = type; 
      }
    }
  }

  num_individuals++;
}



void draw()
{
  //background(0);

  /* drawing and update cycle */
  for ( int i = 0 ; i < sx ; i++ )
  {
    for ( int j = 0 ; j < sy ; j++ )
    {
      /* if future state is alive or if no change should take place */
      if ( world[i][j][1] > 0 || ( world[i][j][1] == 0 && world[i][j][0] > 0 ) )
      {
        world[i][j][0] = (world[i][j][1] > 0 ? world[i][j][1] : world[i][j][0]);
        // colour with the color it had before
        int toset = ( world[i][j][0] > 0 ? individuals[world[i][j][0]] : individuals[world[i][j][1]] );
        set(i, j, toset);
      }

      /* if death will occur */
      if ( world[i][j][1] == -1 )
        world[i][j][0] = 0;
        
      /* draw black the dead ones */
      if ( world[i][j][0] == 0 && world[i][j][1] == 0 ) set (i, j, #000000);
      
      /* reset future state */
      world[i][j][1] = 0;
    }
  }

  int most;
  /* death and rebirth */
  for ( int i = 0 ; i < sx ; i++ )
  {
    for ( int j = 0 ; j < sy ; j++ )
    {
      int count = count_neighs(i, j);

      // stay alive
      if ( count == 3 && world[i][j][0] == 0 )
      {
        // the most abundant individuals nearby are of type...
        most = most_ab(i, j);
        // most = random_most_ab(i, j);
        world[i][j][1] = most;
      }

      /* death for overcrowding or loneliness */
      if ( ( count < 2 || count > 3 ) && world[i][j][0] > 0 )
        world[i][j][1] = -1;      
    }
  }
}

int random_most_ab(int x, int y) {
  int copy_this = 0;
  int nei[] = new int[8];

  /* right */  nei[0] = world[(x + 1) % sx][y][0];
  /* top */  nei[1] = world[x][(y + 1) % sy][0];
  /* left */  nei[2] = world[(x + sx - 1) % sx][y][0];
  /* bottom */  nei[3] = world[x][(y + sy - 1) % sy][0];
  /* topright */  nei[4] = world[(x + 1) % sx][(y + 1) % sy][0];
  /* topleft */  nei[5] = world[(x + sx - 1) % sx][(y + 1) % sy][0];
  /* bottomleft */  nei[6] = world[(x + sx - 1) % sx][(y + sy - 1) % sy][0];
  /* bottomright */  nei[7] = world[(x + 1) % sx][(y + sy - 1) % sy][0];

  // we already know that there are exactly 3 non-null neighbours
  do {
    copy_this = nei[int(random(8))];
  } 
  while ( copy_this == 0 );

  return copy_this;
}

int most_ab(int x, int y) {
  int nei[] = new int[8];

  /* right */  nei[0] = world[(x + 1) % sx]      [y]                  [0];
  /* top */  nei[1] = world[x]                 [(y + 1) % sy]       [0];
  /* left */  nei[2] = world[(x + sx - 1) % sx] [y]                  [0];
  /* bottom */  nei[3] = world[x]                 [(y + sy - 1) % sy]  [0];
  /* topright */  nei[4] = world[(x + 1) % sx]      [(y + 1) % sy]       [0];
  /* topleft */  nei[5] = world[(x + sx - 1) % sx] [(y + 1) % sy]       [0];
  /* bottomleft */  nei[6] = world[(x + sx - 1) % sx] [(y + sy - 1) % sy]  [0];
  /* bottomright */  nei[7] = world[(x + 1) % sx]      [(y + sy - 1) % sy]  [0];

  // we already know we have 3 'on' neighbours;
  // so it can be either AAA, AAB, ABC
  // therefore the first one that has 2 matches is the winner
  // otherwise random choice amongst the 3
  int[] candidates = new int[3];
  for ( int i = 0 ; i < 3 ; i++ )
    candidates[i] = 0;

  for ( int i = 0 ; i < 8 ; i++ ) 
  {
    // don't care about empty neighbours
    if ( nei[i] == 0 ) continue;

    // if uninitialised
    if ( candidates[0] == 0 )
    {
      candidates[0] = nei[i];  // assign
      continue;
    }
    // second match
    if ( candidates[0] == nei[i] ) return candidates[0];

    if ( candidates[1] == 0 )
    {
      candidates[1] = nei[i];
      continue;
    }
    if ( candidates[1] == nei[i] ) return candidates[1];

    if ( candidates[2] == 0 )
    {
      candidates[2] = nei[i];
      continue;
    }
    if ( candidates[2] == nei[i] ) return candidates[2];
  }

  // exactly three matching cases
  return candidates[int(random(3))];

}

// Count the number of adjacent cells 'on' 
int count_neighs(int x, int y) 
{ 
    int count = 0;
    int nei[] = new int[8];

  /* right */  nei[0] = world[(x + 1) % sx][y][0];
  /* top */  nei[1] = world[x][(y + 1) % sy][0];
  /* left */  nei[2] = world[(x + sx - 1) % sx][y][0];
  /* bottom */  nei[3] = world[x][(y + sy - 1) % sy][0];
  /* topright */  nei[4] = world[(x + 1) % sx][(y + 1) % sy][0];
  /* topleft */  nei[5] = world[(x + sx - 1) % sx][(y + 1) % sy][0];
  /* bottomleft */  nei[6] = world[(x + sx - 1) % sx][(y + sy - 1) % sy][0];
  /* bottomright */  nei[7] = world[(x + 1) % sx][(y + sy - 1) % sy][0];

  for ( int i = 0 ; i < 8 ; i++ ) 
    if ( nei[i] != 0 ) count++;
    
    return count;
} 

// add individual ?
void mousePressed() {
  fill(#0000FF);
  text("new colony", width/2, height/2);
  inoculate(mouseX, mouseY, num_individuals);
  // inoculate_random(num_individuals);
}










