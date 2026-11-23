import scala.scalanative.libc.stdio._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.unsafe._

object SingleThreaded {
  def main(args: Array[String]): Unit = {
    assert(isMultithreadingEnabled == false)
    println("Hello world")
  }
}
