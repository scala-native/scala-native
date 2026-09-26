object CompareToTest {
  import Files._

  def run(): Unit = {
    assert(fileA.compareTo(fileB) < 0)
    assert(fileA.compareTo(fileA) == 0)
    assert(fileB.compareTo(fileA) > 0)
    assert(fileB.compareTo(fileB) == 0)
  }
}
