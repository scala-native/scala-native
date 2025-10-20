object IsHiddenTest {
  import Files.*
  import Utils.*

  def main(args: Array[String]): Unit = {
    assert(currentDirectory.isHidden())
    assert(existingHiddenFile.exists())
    assert(existingHiddenFile.isHidden())
    assert(existingHiddenDirectory.exists())
    assert(existingHiddenDirectory.isHidden())
    assert(!nonexistentHiddenFile.exists())
    // On Windows (JVM) isHidden for non existing file returns false
    assertOsSpecific(
      nonexistentHiddenFile.isHidden(),
      "nonexistentHiddenFile.isHidden()"
    )(onUnix = true, onWindows = false)
  }
}
