object DeleteTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(willBeDeletedFile.exists())
    assert(willBeDeletedFile.delete())
    assert(!willBeDeletedFile.exists())

    assert(willBeDeletedDirectory.exists())
    assert(willBeDeletedDirectory.delete())
    assert(!willBeDeletedDirectory.exists())
  }
}
