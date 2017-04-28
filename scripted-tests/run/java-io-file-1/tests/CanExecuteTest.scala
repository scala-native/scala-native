object CanExecuteTest {
  import Files._

  def main(args: Array[String]): Unit = {
    assert(!emptyNameFile.canExecute())

    assert(executableFile.canExecute())
    assert(!unexecutableFile.canExecute())
    assert(!nonexistentFile.canExecute())

    assert(executableDirectory.canExecute())
    assert(!unexecutableDirectory.canExecute())
    assert(!nonexistentDirectory.canExecute())
  }

}
