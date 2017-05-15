object ListTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    val listedFiles = nonEmptyDirectory.list().sorted
    assert(listedFiles.length == 3)
    assert(listedFiles(0) == firstChildFile.getName)
    assert(listedFiles(1) == secondChildFile.getName)
    assert(listedFiles(2) == thirdChildDirectory.getName)
  }
}
