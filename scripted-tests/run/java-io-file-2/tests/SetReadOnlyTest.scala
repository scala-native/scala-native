object SetReadOnlyTest {
  import Files._
  import scala.scalanative.runtime.Platform
  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(willBeSetReadOnlyFile.exists())
    assert(willBeSetReadOnlyFile.canRead())
    assert(willBeSetReadOnlyFile.canWrite())
    assert(willBeSetReadOnlyFile.canExecute())

    assert(willBeSetReadOnlyFile.setReadOnly())
    assert(willBeSetReadOnlyFile.canRead())
    assert(!willBeSetReadOnlyFile.canWrite())
    assert(willBeSetReadOnlyFile.canExecute())

    assert(willBeSetReadOnlyDirectory.exists())
    assert(willBeSetReadOnlyDirectory.canRead())
    assert(willBeSetReadOnlyDirectory.canWrite())
    assert(willBeSetReadOnlyDirectory.canExecute())

    assert(willBeSetReadOnlyDirectory.setReadOnly())
    assert(willBeSetReadOnlyDirectory.canRead())
    assert(!willBeSetReadOnlyDirectory.canWrite())
    assert(willBeSetReadOnlyDirectory.canExecute())

    assert(!nonexistentFile.exists)
    assert(!nonexistentFile.setReadOnly())
  }
}
