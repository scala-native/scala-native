object IsHiddenTest {
  import Files._

  def main(args: Array[String]): Unit = {
    assert(currentDirectory.isHidden())
    assert(existingHiddenFile.exists())
    assert(existingHiddenFile.isHidden())
    assert(existingHiddenDirectory.exists())
    assert(existingHiddenDirectory.isHidden())
    assert(!nonexistentHiddenFile.exists())
    assert(nonexistentHiddenFile.isHidden())
  }
}
