package java.net

import scala.util.Try

// Ported from Apache Harmony
@SerialVersionUID(1L)
class InetSocketAddress private[net] (private var addr: InetAddress,
                                      private val port: Int,
                                      private var hostName: String,
                                      needsResolving: Boolean)
    extends SocketAddress {

  if (port < 0 || port > 65535) {
    throw new IllegalArgumentException(
      "Port parameter: " + port +
        " is outside of the 0-65535 range")
  }

  if (needsResolving) {
    if (addr == null) {
      addr = InetAddress.wildcard
    }
    hostName = addr.getHostAddress()
  }

  private var gotHostName = false

  if (hostName == null) {
    throw new IllegalArgumentException("Host name in InetSocketAddress is null")
  }

  private val isResolved = (addr != null)

  def this(port: Int) =
    this(InetAddress.wildcard, port, InetAddress.wildcard.getHostName(), false)

  def this(hostname: String, port: Int) =
    this(Try(InetAddress.getByName(hostname)).getOrElse(null),
         port,
         hostname,
         false)

  def this(addr: InetAddress, port: Int) =
    this(addr, port, null, true)

  final def getPort: Int            = port
  final def getAddress: InetAddress = addr
  final def getHostString: String   = hostName
  final def getHostName: String = {
    if (!gotHostName && addr != null) {
      gotHostName = true
      hostName = addr.getHostName()
    }
    hostName
  }

  final def isUnresolved: Boolean = !isResolved

  override final def hashCode: Int = {
    if (addr == null)
      hostName.hashCode + port
    else
      addr.hashCode + port
  }

  override def equals(obj: Any): Boolean = {
    if (!obj.isInstanceOf[InetSocketAddress])
      false
    else if (this eq obj.asInstanceOf[InetSocketAddress])
      true
    else if (obj.asInstanceOf[InetSocketAddress].port != port)
      false
    else if (obj.asInstanceOf[InetSocketAddress].addr == null && addr == null)
      hostName == obj.asInstanceOf[InetSocketAddress].hostName
    else if (addr == null)
      false
    else
      addr == obj.asInstanceOf[InetSocketAddress].addr
  }

  override def toString: String = {
    val host = if (addr != null) addr.toString else hostName
    host + ":" + port
  }

}

object InetSocketAddress {

  def createUnresolved(host: String, port: Int) =
    new InetSocketAddress(null, port, host, false)
}
