object Main {
  def main(args: Array[String]): Unit = {
    assert(
      getClass().getResourceAsStream("e-res") != null,
      "e-res should be embedded"
    )

    val is = getClass().getResourceAsStream("e-res")
    val data = Iterator.continually(is.read()).takeWhile(_ != -1).toList
    assert(
      data == List(0, 127, 255, 0, 128, 255),
      "the binary contents of e-res should be correct"
    )
  }
}
