object LinksTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(directoryLinkedTo.exists)
    assert(linkToDirectory.exists)
    assert(
      linkToDirectory.getCanonicalPath == directoryLinkedTo.getCanonicalPath)
    assert(linkToDirectory.getName != directoryLinkedTo.getName)
  }
}
