class Individual {
  int x, y;
  int type;
  Individual[] neigh;

  Individual(int x, int y) {
    this.x = x;
    this.y = y;
  }

  Individual(int x, int y, int type) {
    this(x, y);
    this.type = type;
  }

  Individual(int x, int y, int type, Individual[] neigh) {
    this(x, y, type);
    this.neigh = neigh;
  }

  void setX(int x) {
    this.x = x;
  }

  void setY(int y) {
    this.y = y;
  }

  void setType(int type) { 
    this.type = type;
  }

  void setNeigh(Individual[] neigh) {
    this.neigh = neigh;
  }

  int getX() {
    return x;
  }

  int getY() {
    return y;
  }

  int getType() {
    return type;
  }

  Individual[] getNeigh() {
    return neigh;
  }

  boolean equals(Individual i) {
    return ( i.x == this.x && i.y == this.y );
  }
}

