/**
 * a separate class; the Individual class might grow, no need to allocate all of them
 */
class Location {
  Individual here;

  Location() {
    here = null;
  }

  Individual whoshere() {
    return here;
  }

  void placeIndividual(Individual i) {
    here = i;
  }

}

