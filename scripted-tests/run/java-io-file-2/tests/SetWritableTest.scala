object SetWritableTest {
  import Files.*
  import Utils.*
  def main(args: Array[String]): Unit = {
    assert(willBeSetWritableFile.exists())
    assertOsSpecific(
      willBeSetWritableFile.canExecute(),
      "!willBeSetWritableFile.canExecute()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetWritableFile.canRead(),
      "!willBeSetWritableFile.canRead()"
    )(onUnix = false, onWindows = true)
    assert(!willBeSetWritableFile.canWrite())

    assert(willBeSetWritableFile.setWritable(true))
    assertOsSpecific(
      willBeSetWritableFile.canExecute(),
      "!willBeSetWritableFile.canExecute()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetWritableFile.canRead(),
      "!willBeSetWritableFile.canRead()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetWritableFile.canWrite(),
      "willBeSetWritableFile.canWrite()"
    )(onUnix = true, onWindows = false)

    assert(willBeSetWritableFile.setWritable(false))
    assertOsSpecific(
      willBeSetWritableFile.canExecute(),
      "!willBeSetWritableFile.canExecute()"
    )(onUnix = false, onWindows = true)
    assert(!willBeSetWritableFile.canRead())
    assert(!willBeSetWritableFile.canWrite())

    assert(willBeSetWritableDirectory.exists())
    assertOsSpecific(
      willBeSetWritableDirectory.canExecute(),
      "!willBeSetWritableDirectory.canExecute()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetWritableDirectory.canRead(),
      "!willBeSetWritableDirectory.canRead()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetWritableDirectory.canWrite(),
      "!willBeSetWritableDirectory.canWrite()"
    )(onUnix = false, onWindows = true)

    assert(willBeSetWritableDirectory.setWritable(true))
    assertOsSpecific(
      willBeSetWritableDirectory.canExecute(),
      "!willBeSetWritableDirectory.canExecute()"
    )(onUnix = false, onWindows = true)
    assertOsSpecific(
      willBeSetWritableDirectory.canRead(),
      "!willBeSetWritableDirectory.canRead()"
    )(onUnix = false, onWindows = true)
    assert(willBeSetWritableDirectory.canWrite())

    assert(willBeSetWritableDirectory.setWritable(false))
    assertOsSpecific(
      willBeSetWritableDirectory.canExecute(),
      "!willBeSetWritableDirectory.canExecute()"
    )(onUnix = false, onWindows = true)
    assert(!willBeSetWritableDirectory.canRead())
    assert(!willBeSetWritableDirectory.canWrite())

    assert(!nonexistentFile.exists)
    assert(!nonexistentFile.setWritable(true))

  }
}
