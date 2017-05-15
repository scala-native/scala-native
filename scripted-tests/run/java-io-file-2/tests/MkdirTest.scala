object MkdirTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(!willBeCreatedDirectory.exists())
    assert(willBeCreatedDirectory.mkdir())
    assert(!willBeCreatedDirectory.mkdir())
    assert(willBeCreatedDirectory.exists())

    assert(!nestedWillBeCreatedDirectory.exists())
    assert(nestedWillBeCreatedDirectory.mkdirs())
    assert(!nestedWillBeCreatedDirectory.mkdir())
    assert(nestedWillBeCreatedDirectory.exists())
  }
}
