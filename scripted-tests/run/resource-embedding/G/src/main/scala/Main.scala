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
      "test.h shouldn't be embedded because exclude directory matches with exclude pattern"
    )
  }
}
