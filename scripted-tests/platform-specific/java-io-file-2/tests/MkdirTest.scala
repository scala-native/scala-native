object MkdirTest {
  import Files._

  def run(): Unit = {
    assert(!willBeCreatedDirectory.exists())
    assert(willBeCreatedDirectory.mkdir())
    assert(!willBeCreatedDirectory.mkdir())
    assert(willBeCreatedDirectory.exists())

    assert(!nestedWillBeCreatedDirectory.exists())
    assert(nestedWillBeCreatedDirectory.mkdirs())
    assert(!nestedWillBeCreatedDirectory.mkdir())
    assert(nestedWillBeCreatedDirectory.exists())
  }
}
