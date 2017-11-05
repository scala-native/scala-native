object CanWriteTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(!emptyNameFile.canWrite())

    assert(writableFile.canWrite())
    assert(!unwritableFile.canWrite())
    assert(!nonexistentFile.canWrite())

    assert(writableFile.canWrite())
    assert(!unwritableFile.canWrite())
    assert(!nonexistentDirectory.canWrite())
  }

}
