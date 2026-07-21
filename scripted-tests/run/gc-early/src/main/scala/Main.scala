class Base(arg: Unit) {
  def main(args: Array[String]): Unit = {
    println(args)
    args.foreach(println) // <-- CRASHED HERE!
  }
}

object Main extends Base({ System.gc() })
