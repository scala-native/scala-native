object CompareToTest {
  import Files.*

  def main(args: Array[String]): Unit = {
    assert(fileA.compareTo(fileB) < 0)
    assert(fileA.compareTo(fileA) == 0)
    assert(fileB.compareTo(fileA) > 0)
    assert(fileB.compareTo(fileB) == 0)
  }
}
