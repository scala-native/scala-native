import scala.scalanative.unsafe._
import scala.scalanative.libc.stdio._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

object MultiThreaded {
  def main(args: Array[String]): Unit = {
    assert(isMultithreadingEnabled == true)
    new Thread(() => println("hello world")).start()
  }
}
