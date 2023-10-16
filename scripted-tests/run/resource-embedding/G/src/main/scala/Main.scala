object Main {
  def main(args: Array[String]): Unit = {
    assert(
      getClass().getResourceAsStream("a.txt") != null,
      "a.txt should be embedded because of '**'"
    )
    assert(
      getClass().getResourceAsStream("include/c.txt") != null,
      "b/c.txt should be embedded because of '**'"
    )
    assert(
      getClass().getResourceAsStream("exclude/b.txt") == null,
      "exclude/b.txt shouldn't be embedded even though it matches with the include pattern '**' because it also matches with exclude pattern"
    )
  }
}
