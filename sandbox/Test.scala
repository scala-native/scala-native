import scalanative.native._

object Test  {
  var foo = 1
  var funptr: FunctionPtr0[Unit] = { () =>
    stdio.printf(c"value: %d", foo)
    ()
  }
  def main(args: Array[String]): Unit = {
    funptr()
  }
}
