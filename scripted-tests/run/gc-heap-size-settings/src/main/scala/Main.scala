import scala.scalanative.runtime.GC

object Main {
  def main(args: Array[String]): Unit = {
    // Allocate so the GC is initialised and has used its settings.
    val data = Array.fill(1000)(new Array[Byte](64))
    assert(data.length == 1000)
    println(s"init=${GC.getInitHeapSize()} max=${GC.getMaxHeapSize()}")
  }
}
