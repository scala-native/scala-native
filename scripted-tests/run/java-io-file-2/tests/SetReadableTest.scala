object SetReadableTest {
  import Files.*
  import Utils.*

  def main(args: Array[String]): Unit = {
    assert(willBeSetReadableFile.exists())
    // Winodws (JVM) complience
    assertOsSpecific(
      willBeSetReadableFile.canExecute(),
      "!willBeSetReadableFile.canExecute()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetReadableFile.canRead(),
      "!willBeSetReadableFile.canRead()"
    )(onUnix = false, onWindows = true)
    assert(!willBeSetReadableFile.canWrite())

    assert(willBeSetReadableFile.setReadable(true))
    // Winodws (JVM) complience
    assertOsSpecific(
      willBeSetReadableFile.canExecute(),
      "!willBeSetReadableFile.canExecute()"
    )(onUnix = false, onWindows = true)
    assert(willBeSetReadableFile.canRead())
    assert(!willBeSetReadableFile.canWrite())

    assert(willBeSetReadableFile.setReadable(false))
    assertOsSpecific(
      willBeSetReadableFile.canExecute(),
      "!willBeSetReadableFile.canExecute()"
    )(onUnix = false, onWindows = true)
    assert(!willBeSetReadableFile.canRead())
    assert(!willBeSetReadableFile.canWrite())

    assert(willBeSetReadableDirectory.exists())
    // Winodws (JVM) complience
    assertOsSpecific(
      willBeSetReadableDirectory.canExecute(),
      "!willBeSetReadableDirectory.canExecute()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetReadableDirectory.canRead(),
      "!willBeSetReadableDirectory.canRead()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetReadableDirectory.canWrite(),
      "!willBeSetReadableDirectory.canWrite()"
    )(onUnix = false, onWindows = true)

    assert(willBeSetReadableDirectory.setReadable(true))
    assertOsSpecific(
      willBeSetReadableDirectory.canExecute(),
      "!willBeSetReadableDirectory.canExecute()"
    )(onUnix = false, onWindows = true)
    assert(willBeSetReadableDirectory.canRead())
    assertOsSpecific(
      willBeSetReadableDirectory.canWrite(),
      "!willBeSetReadableDirectory.canWrite()"
    )(onUnix = false, onWindows = true)

    assert(willBeSetReadableDirectory.setReadable(false))
    assertOsSpecific(
      willBeSetReadableDirectory.canExecute(),
      "!willBeSetReadableDirectory.canExecute()"
    )(onUnix = false, onWindows = true)
    assert(!willBeSetReadableDirectory.canRead())
    assert(!willBeSetReadableDirectory.canWrite())

    assert(!nonexistentFile.exists)
    assert(!nonexistentFile.setReadable(true))

  }
}
