object SetLastModifiedTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(!nonexistentFile.exists())
    assert(!nonexistentFile.setLastModified(10000))

    assert(willBeSetLastModified.exists())
    assert(willBeSetLastModified.setLastModified(expectedLastModified))
    assert(willBeSetLastModified.lastModified == expectedLastModified)
  }
}
