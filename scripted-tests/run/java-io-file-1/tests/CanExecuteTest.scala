object CanExecuteTest {
  import Files.*
  import Utils.*

  def main(args: Array[String]): Unit = {
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
