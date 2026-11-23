object LinksTest {
  import Files._
  import Utils._

  def run(): Unit = {
    if (!PlatformInfo.isWindows) {
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
