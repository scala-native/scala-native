object LastModifiedTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(!nonexistentFile.exists())
    assert(nonexistentFile.lastModified() == 0L)

    assert(fileWithLastModifiedSet.exists())
    assert(fileWithLastModifiedSet.lastModified() == expectedLastModified)
  }
}
