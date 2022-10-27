//> using scala "2"
//> using lib "dev.zio::izumi-reflect::2.2.0"
//> using option "-Xprint:typer"

import izumi.reflect._

object Main {
  def foo[T: Tag] = ???
  def main(args: Array[String]): Unit = {
    println(foo)
  }

}