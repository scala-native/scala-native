object Hello {
  class Boom extends Exception("boom")

  @noinline def throwIt(): Unit = throw new Boom

  def main(args: Array[String]): Unit = {
    var caught = false
    try throwIt()
    catch { case _: Boom => caught = true }
    assert(caught, "the catch handler never ran")
  }
}
