package java.net

import scalanative.native.stub

class URL(from: String) {
  @stub
  def getPath(): java.lang.String = ???
  @stub
  def getProtocol(): java.lang.String = ???
  @stub
  def openConnection(): java.net.URLConnection = ???
  @stub
  def openStream(): java.io.InputStream = ???
  @stub
  override def hashCode: Int = ???
  @stub
  def toURI(): java.net.URI = ???
  @stub
  def toExternalForm(): java.lang.String = ???
}
