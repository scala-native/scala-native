object Main {
  def main(args: Array[String]): Unit = {
    assert(
      getClass().getResourceAsStream("dir/d-res") != null,
      "d-res should be embedded"
    )

    assert(
      getClass().getResourceAsStream("dir\\d-res") != null,
      "d-res should be embedded"
    )
  }
}
