object CanExecuteTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(!emptyNameFile.canExecute())

    assert(executableFile.canExecute())
    assert(!unexecutableFile.canExecute())
    assert(!nonexistentFile.canExecute())

    assert(executableDirectory.canExecute())
    assert(!unexecutableDirectory.canExecute())
    assert(!nonexistentDirectory.canExecute())
  }

}
