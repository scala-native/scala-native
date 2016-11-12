package som

class Random {
  private var seed = 74755

  def next(): Int = {
    seed = ((seed * 1309) + 13849) & 65535;
    seed
  }
}

object Random {
  def testRNG(): Unit = {
    val rnd = new Random()

    try {
      if (rnd.next() != 22896) { throw new RuntimeException() }
      if (rnd.next() != 34761) { throw new RuntimeException() }
      if (rnd.next() != 34014) { throw new RuntimeException() }
      if (rnd.next() != 39231) { throw new RuntimeException() }
      if (rnd.next() != 52540) { throw new RuntimeException() }
      if (rnd.next() != 41445) { throw new RuntimeException() }
      if (rnd.next() != 1546) { throw new RuntimeException() }
      if (rnd.next() != 5947) { throw new RuntimeException() }
      if (rnd.next() != 65224) { throw new RuntimeException() }
    } catch {
      case e: RuntimeException =>
        System.err.println("FAILED")
    }
  }
}
