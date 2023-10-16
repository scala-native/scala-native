object Main {
  def main(args: Array[String]): Unit = {
    assert(
      getClass().getResourceAsStream("a.txt") != null,
      "a.txt should be embedded because of '**.txt'"
    )
    assert(
      getClass().getResourceAsStream("/b/c.txt") != null,
      "b/c.txt should be embedded because of '**.txt'"
    )
    assert(
      getClass().getResourceAsStream("dir/foo.h") != null,
      "dir/foo.h should be embedded because of 'dir/**'"
    )
    assert(
      getClass().getResourceAsStream("test.h") == null,
      "test.h shouldn't be embedded because include pattern doesn't match"
    )
  }
}
