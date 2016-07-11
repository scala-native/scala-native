import scalanative.native._, stdio._
import java.nio._
import java.nio.charset._

object Test  {
  def main(args: Array[String]): Unit = {
    var x = 2
    val v = scalanative.runtime.select(x > 0, 1, 2)
  }
}
