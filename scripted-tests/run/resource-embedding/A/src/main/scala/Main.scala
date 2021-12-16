object Main {
  def main(args: Array[String]): Unit = {
    assert(getClass().getResourceAsStream("a-res") == null)
    assert(getClass().getResourceAsStream("repeated.txt") == null)
  }
}