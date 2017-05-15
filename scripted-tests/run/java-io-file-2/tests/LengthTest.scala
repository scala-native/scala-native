object LengthTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(fileWith3Bytes.exists())
    assert(fileWith3Bytes.length() == 3L)

    assert(!nonexistentFile.exists())
    assert(nonexistentFile.length() == 0L)
  }
}
