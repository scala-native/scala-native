import scala.scalanative.unsafe._
import scala.scalanative.libc.stdio._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

object SingleThreaded {
  def main(args: Array[String]): Unit = {
    assert(isMultithreadingEnabled == false)
    println("Hello world")
  }
}
