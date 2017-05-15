object SetExecutableTest {
  import Files._
  import scala.scalanative.runtime.Platform
  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(willBeSetExecutableFile.exists())
    assert(!willBeSetExecutableFile.canExecute())
    assert(!willBeSetExecutableFile.canRead())
    assert(!willBeSetExecutableFile.canWrite())

    assert(willBeSetExecutableFile.setExecutable(true))
    assert(willBeSetExecutableFile.canExecute())
    assert(!willBeSetExecutableFile.canRead())
    assert(!willBeSetExecutableFile.canWrite())

    assert(willBeSetExecutableFile.setExecutable(false))
    assert(!willBeSetExecutableFile.canExecute())
    assert(!willBeSetExecutableFile.canRead())
    assert(!willBeSetExecutableFile.canWrite())

    assert(willBeSetExecutableDirectory.exists())
    assert(!willBeSetExecutableDirectory.canExecute())
    assert(!willBeSetExecutableDirectory.canRead())
    assert(!willBeSetExecutableDirectory.canWrite())

    assert(willBeSetExecutableDirectory.setExecutable(true))
    assert(willBeSetExecutableDirectory.canExecute())
    assert(!willBeSetExecutableDirectory.canRead())
    assert(!willBeSetExecutableDirectory.canWrite())

    assert(willBeSetExecutableDirectory.setExecutable(false))
    assert(!willBeSetExecutableDirectory.canExecute())
    assert(!willBeSetExecutableDirectory.canRead())
    assert(!willBeSetExecutableDirectory.canWrite())

    assert(!nonexistentFile.exists)
    assert(!nonexistentFile.setExecutable(true))

  }
}
