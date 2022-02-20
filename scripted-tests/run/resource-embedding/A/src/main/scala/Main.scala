object Main {
  def main(args: Array[String]): Unit = {
    assert(
      getClass().getResourceAsStream("a-res") == null,
      "a-res should not be embedded"
    )
    assert(
      getClass().getResourceAsStream("repeated.txt") == null,
      "repeated.txt should not be embedded"
    )
  }
}
