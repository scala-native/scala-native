import scalanative.native._
import scalanative.libc.stdlib._
import scalanative.runtime.GC

object Test {
  def uint: UInt = ???
  def main(args: Array[String]): Unit = uint.toString // or any other method
}
