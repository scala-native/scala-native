import scala.scalanative.unsafe.*
import scala.scalanative.libc.stdio.*
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

object SingleThreaded {
  def main(args: Array[String]): Unit = {
    assert(isMultithreadingEnabled == false)
    println("Hello world")
  }
}
