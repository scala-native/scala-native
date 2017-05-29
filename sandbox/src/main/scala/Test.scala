package sandbox

import java.io._

object Test {
  def main(args: Array[String]): Unit = {
    val f = new File("build.sbt")
    println(f.exists)
    val fis = new FileInputStream(f)
    println(fis.getFD.valid)
    fis.close()
    println(fis.getFD.valid)
    println("Hello, world!")
  }

  def foo(): Int = 42
}
