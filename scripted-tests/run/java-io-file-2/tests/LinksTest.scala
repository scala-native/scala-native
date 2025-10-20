object LinksTest {
  import Files.*
  import Utils.*

  def main(args: Array[String]): Unit = {
    if (!Platform.isWindows) {
      // Not testing symbolic links on Windows, needs admin privileges
      assert(directoryLinkedTo.exists)
      assert(linkToDirectory.exists)
      assert(
        linkToDirectory.getCanonicalPath == directoryLinkedTo.getCanonicalPath
      )
      assert(linkToDirectory.getName != directoryLinkedTo.getName)
    }
  }
}
