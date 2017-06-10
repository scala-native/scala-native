package java.net

import java.io.IOException

class URISyntaxException(private val input: String,
                         private val reason: String,
                         private val index: Int)
    extends Exception(s"$reason in $input at $index") {

  def this(input: String, reason: String) = this(input, reason, -1)

  if (input == null || reason == null)
    throw new NullPointerException()

  if (index < -1)
    throw new IllegalArgumentException()

  def getIndex(): Int     = index
  def getInput(): String  = input
  def getReason(): String = reason

}

class MalformedURLException(private val message: String)
    extends IOException(message) {
  def this() = this(null)
}

class UnknownHostException(private val host: String)
    extends IOException(host) {
  def this() = this(null)
}

class UnknownServiceException(private val msg: String)
    extends IOException(msg) {
  def this() = this(null)
}
