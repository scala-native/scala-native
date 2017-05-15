object CompareToTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(fileA.compareTo(fileB) < 0)
    assert(fileA.compareTo(fileA) == 0)
    assert(fileB.compareTo(fileA) > 0)
    assert(fileB.compareTo(fileB) == 0)
  }
}
