object LinksTest {
  import Files._

  def main(args: Array[String]): Unit = {
    assert(directoryLinkedTo.exists)
    assert(linkToDirectory.exists)
    assert(
      linkToDirectory.getCanonicalPath == directoryLinkedTo.getCanonicalPath)
    assert(linkToDirectory.getName != directoryLinkedTo.getName)
  }
}
