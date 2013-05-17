final color COLOR_GREEN = color(0, 255, 0);

PFont font;
int count = 0;

String shortText = "Prokariotic scale-free network initializing";
int TEXTSIZE = 16;

String[] longText = {
  "The world’s first prokaryotic scale free network has been initiated.",
  "",
  "The scientist, unconcerned with the",
  "ethical implications of his experiment",
  "and also unaware of the artist’s intentions,",
  "never anticipated that the fusion of ",
  "the Earth’s global bacterial communications network,",
  "with that of human origin would lead to ",
  "the evolution of a novel and chimeric life form.",
  "",
  "A new kind of pathogen mutated by the ",
  "Bluetooth, RFID and Packet Data surveilled in the gallery.",
  "Dublin became the centre of the epidemic, ",
  "and the origin of a new life form ",
  "able to subvert both biology and technology.",
  "What followed was inevitable.",
  "",
  "What else would a creature with access to:",
  "humanity’s entire digital knowledge;",
  "the genetic toolbox that drives evolution; ",
  "the sophistication of the pathogen;",
  "and intimate awareness of our vulnerabilities do?",
};

void setup() {
  size(screen.width, screen.height, P2D);
//    size(800, 600, P2D);

  frameRate(1);
  noCursor();

  font = createFont("Courier", TEXTSIZE);
}

void draw() {
  background(0);
  textFont (font);
  textSize(TEXTSIZE);
  fill(COLOR_GREEN);
  textAlign(LEFT);

//  for ( int i = 0 ; i < count ; i++ )
int i;
  for ( i = 0 ; i < longText.length ; i++ )
//    text(longText[i], width/2, height/5 + i*(TEXTSIZE+2));
    text(longText[i], 20, height/5 + i*(TEXTSIZE+2));

//
//  if ( count == longText.length )
//    count = 0;
//  else
//    count++;
  if ( count++ % 2 == 0 ) {
    rect(520, (i+5)*(TEXTSIZE+2)-5, 10, TEXTSIZE+2);
  }
//noLoop();

}







