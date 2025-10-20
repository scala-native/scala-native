object SetExecutableTest {
  import Files.*
  import Utils.*

  def main(args: Array[String]): Unit = {
    assert(willBeSetExecutableFile.exists())
    // Windows (JVM) does not allow for setExecutable(false),
    // at this point it would always return true
    assertOsSpecific(
      willBeSetExecutableFile.canExecute(),
      "!willBeSetExecutableFile.canExecute() 1"
    )(onUnix = false, onWindows = true)
    // Windows (JVM) setRedable(false, false) always returns false
    // at this point it would be still possible to read file
    assertOsSpecific(
      willBeSetExecutableFile.canRead(),
      "!willBeSetExecutableFile.canRead() 1"
    )(onUnix = false, onWindows = true)
    assert(!willBeSetExecutableFile.canWrite())

    assert(willBeSetExecutableFile.setExecutable(true))
    assert(willBeSetExecutableFile.canExecute())
    assertOsSpecific(
      willBeSetExecutableFile.canRead(),
      "!willBeSetExecutableFile.canRead() 2"
    )(onUnix = false, onWindows = true)
    assert(!willBeSetExecutableFile.canWrite())

    // Windows (JVM) does not allow for setExecutable(false)
    assertOsSpecific(
      willBeSetExecutableFile.setExecutable(false),
      "willBeSetExecutableFile.setExecutable(false)"
    )(onUnix = true, onWindows = false)
    assertOsSpecific(
      willBeSetExecutableFile.canExecute(),
      "!willBeSetExecutableFile.canExecute() 3"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetExecutableFile.canRead(),
      "!willBeSetExecutableFile.canRead() 3"
    )(onUnix = false, onWindows = true)
    assert(!willBeSetExecutableFile.canWrite())

    assert(willBeSetExecutableDirectory.exists())
    // Winodws (JVM) complience
    assertOsSpecific(
      willBeSetExecutableDirectory.canExecute(),
      "!willBeSetExecutableDirectory.canExecute()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetExecutableDirectory.canRead(),
      "!willBeSetExecutableDirectory.canRead()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetExecutableDirectory.canWrite(),
      "!willBeSetExecutableDirectory.canWrite()"
    )(onUnix = false, onWindows = true)
    assert(willBeSetExecutableDirectory.setExecutable(true))
    assert(willBeSetExecutableDirectory.canExecute())
    // Windows (JVM) complience
    assertOsSpecific(
      willBeSetExecutableDirectory.canRead(),
      "!willBeSetExecutableDirectory.canRead()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetExecutableDirectory.canWrite(),
      "!willBeSetExecutableDirectory.canWrite()"
    )(onUnix = false, onWindows = true)

    // Windows (JVM) complience, no support for setExecutable(false)
    assertOsSpecific(
      willBeSetExecutableDirectory.setExecutable(false),
      "willBeSetExecutableDirectory.setExecutable(false)"
    )(onUnix = true, onWindows = false)
    assertOsSpecific(
      willBeSetExecutableDirectory.canExecute(),
      "!willBeSetExecutableDirectory.canExecute()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetExecutableDirectory.canRead(),
      "!willBeSetExecutableDirectory.canRead()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetExecutableDirectory.canWrite(),
      "!willBeSetExecutableDirectory.canWrite()"
    )(onUnix = false, onWindows = true)

    assert(!nonexistentFile.exists)
    assert(!nonexistentFile.setExecutable(true))

  }
}
