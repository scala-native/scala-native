package java.net

object IsReachableTest {

  def main(args: Array[String]): Unit = {
    assert(SocketHelpers.isReachableByEcho("127.0.0.1", 1000, 5832) == true)
  }

}
