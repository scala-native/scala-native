import scala.scalanative.runtime.GC

object Main {
  def main(args: Array[String]): Unit = {
    val init = GC.getInitHeapSize().toLong
    val max = GC.getMaxHeapSize().toLong
    println(s"initialHeapSize=$init maxHeapSize=$max")
  }
}
