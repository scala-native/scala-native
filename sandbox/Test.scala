object Test {

  def newBuf: java.lang.StringBuffer =
    new java.lang.StringBuffer

  def initBuf(str: String): java.lang.StringBuffer =
    new java.lang.StringBuffer(str)

  def main(args: Array[String]): Unit = {
    val rhs = initBuf("abef").insert(2, initBuf("abcde"), 2, 4).toString
    assert("abcdef" == rhs, rhs)
  }
}
