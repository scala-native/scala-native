import scala.scalanative.libc.stdio._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.unsafe._

object MultiThreaded {
  def main(args: Array[String]): Unit = {
    assert(isMultithreadingEnabled == true)
    new Thread(() => println("hello world")).start()
  }
}
