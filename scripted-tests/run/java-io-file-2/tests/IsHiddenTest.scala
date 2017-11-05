object IsHiddenTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(currentDirectory.isHidden())
    assert(existingHiddenFile.exists())
    assert(existingHiddenFile.isHidden())
    assert(existingHiddenDirectory.exists())
    assert(existingHiddenDirectory.isHidden())
    assert(!nonexistentHiddenFile.exists())
    assert(nonexistentHiddenFile.isHidden())
  }
}
