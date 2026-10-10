object Main {
  def main(args: Array[String]): Unit = {
    val actual = shared.Greeting.text
    assert(actual == args(0), s"Expected '${args(0)}', got '$actual'")
    println(actual)
  }
}
