import scala.scalanative.posix.unistd
import java.lang.ProcessBuilder
import scala.io.Source

object Test {
  def main(args: Array[String]): Unit = {
    val pid = unistd.getpid().intValue()
    val proc = new ProcessBuilder("vmmap", "-summary", s"$pid").start()
    val vmmap = Source.fromInputStream(proc.getInputStream())
    vmmap.getLines().foreach { line =>
      if (line.startsWith("Load Address")) {
        val addressStr = line.split(":").last.trim.drop(2)
        println(java.lang.Long.parseLong(addressStr, 16))
      }

    }

    f()
  }

  def f() = g()

  def g() = error()

  def error() = throw new Error("test")
}
