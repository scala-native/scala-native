package java.net

import java.net.SocketHelpers

object IsReachableTest {

  def main(args: Array[String]): Unit = {
    println(SocketHelpers.isReachableByEcho("127.0.0,1", 1000, 5832))
    assert(SocketHelpers.isReachableByEcho("127.0.0.1", 1000, 5832) == true)
  }

}
