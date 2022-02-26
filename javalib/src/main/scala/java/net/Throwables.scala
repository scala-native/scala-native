package java.net

import java.io.{IOException, InterruptedIOException}
import java.rmi.RemoteException

class URISyntaxException(input: String, reason: String, index: Int)
    extends Exception(s"$reason in $input at $index") {

  def this(input: String, reason: String) = this(input, reason, -1)

  if (input == null || reason == null)
    throw new NullPointerException()

  if (index < -1)
    throw new IllegalArgumentException()

  def getIndex(): Int = index
  def getInput(): String = input
  def getReason(): String = reason

}

class MalformedURLException(msg: String) extends IOException(msg) {
  def this() = this(null)
}

class UnknownHostException(s: String, e: Exception)
    extends RemoteException(s, e) {
  def this(s: String) = this(s, null)
  def this() = this(null)
}

class UnknownServiceException(msg: String) extends IOException(msg) {
  def this() = this(null)
}

class SocketException(msg: String) extends IOException(msg) {
  def this() = this(null)
}

class BindException(msg: String) extends SocketException(msg) {
  def this() = this(null)
}

class ConnectException(msg: String, ex: Exception)
    extends RemoteException(msg, ex) {
  def this(msg: String) = this(msg, null)
  def this() = this(null)
}

class NoRouteToHostException(msg: String) extends SocketException(msg) {
  def this() = this(null)
}

class PortUnreachableException(msg: String) extends SocketException(msg) {
  def this() = this(null)
}

class SocketTimeoutException(msg: String) extends InterruptedIOException(msg) {
  def this() = this(null)
}
