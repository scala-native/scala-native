import scala.scalanative.libc.stdatomic._
// import scala.scalanative.libc.stdatomic.conversions._
import scala.scalanative.unsafe._
import scala.language.implicitConversions
object Test {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    val x = stackalloc[Boolean]().atomic
    println(x)
    x.store(true)
    println(x.load())
  }
}

