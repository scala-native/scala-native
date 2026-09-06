object CanExecuteTest {
  import Files._
  import Utils._

  def run(): Unit = {
    assert(!emptyNameFile.canExecute())

    assert(executableFile.canExecute())
    assertOsSpecific(
      unexecutableFile.canExecute(),
      "unexecutableFile.canExecute"
    )(onUnix = false, onWindows = true)
    assert(!nonexistentFile.canExecute())

    assert(executableDirectory.canExecute())
    assertOsSpecific(
      unexecutableDirectory.canExecute(),
      "!unexecutableDirectory.canExecute"
    )(onUnix = false, onWindows = true)
    assert(!nonexistentDirectory.canExecute())
  }

}
