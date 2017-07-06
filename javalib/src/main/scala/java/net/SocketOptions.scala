package java.net

trait SocketOptions {

  def getOption(optID: Int): Object
  def setOption(optID: Int, value: Object): Object

}


