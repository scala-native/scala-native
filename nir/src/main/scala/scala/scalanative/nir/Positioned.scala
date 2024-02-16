package scala.scalanative.nir

trait Positioned {

  /** Returns the site in the program sources corresponding to the definition.
   */
  def pos: SourcePosition

  if (Positioned.debugEmptyPositions && pos.isEmpty) {
    System.err.println(s"\nFound empty position in $this, backtrace:")
    new RuntimeException()
      .getStackTrace()
      .take(10)
      .foreach(println)
  }
}

object Positioned {
  private final val debugEmptyPositions =
    sys.props.contains("scalanative.debug.nir.positions")
}
