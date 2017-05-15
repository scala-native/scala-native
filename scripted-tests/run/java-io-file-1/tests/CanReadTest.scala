object CanReadTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(!emptyNameFile.canRead())

    assert(readableFile.canRead())
    assert(!unreadableFile.canRead())
    assert(!nonexistentFile.canRead())

    assert(readableDirectory.canRead())
    assert(!unreadableDirectory.canRead())
    assert(!nonexistentDirectory.canRead())
  }

}
