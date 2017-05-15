object RenamedToTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(willBeRenamedFrom.exists)
    assert(!willBeRenamedTo.exists)
    assert(willBeRenamedFrom.renameTo(willBeRenamedTo))
    assert(!willBeRenamedFrom.exists)
    assert(willBeRenamedTo.exists)
  }
}
