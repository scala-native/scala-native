package java.net

class URL(from: String) {
  def getPath(): java.lang.String              = ???
  def getProtocol(): java.lang.String          = ???
  def openConnection(): java.net.URLConnection = ???
  def openStream(): java.io.InputStream        = ???
  override def hashCode: Int                   = ???
}
