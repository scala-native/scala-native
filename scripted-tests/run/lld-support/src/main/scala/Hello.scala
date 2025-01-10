object Hello {
  def main(args: Array[String]): Unit = {
    try { throw new Exception("BOOM") }
    catch { case _: Exception => }
  }
}
