object Main {
  def main(args: Array[String]): Unit = {
    val repeatedFile = getClass().getResourceAsStream("repeated.txt")
    assert(repeatedFile != null, "repeated.txt is not embedded")
    assert(
      repeatedFile.read().toChar == 'B',
      "correct repeated.txt was embedded"
    )

    assert(
      getClass().getResourceAsStream("a-res") == null,
      "a-res should not be embedded"
    )
    assert(
      getClass().getResourceAsStream("b-res") != null,
      "b-res should be embedded"
    )
    assert(
      getClass().getResourceAsStream("c-res") == null,
      "c-res should not be embedded"
    )
  }
}
