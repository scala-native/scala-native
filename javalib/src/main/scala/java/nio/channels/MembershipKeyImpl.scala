package java.nio.channels

import java.net.{InetAddress, NetworkInterface}

private[channels] class MembershipKeyImpl(
    ch: DatagramChannelImpl,
    grp: InetAddress,
    interf: NetworkInterface,
    source: InetAddress
) extends MembershipKey {

  @volatile private var valid = true

  private[channels] def invalidate(): Unit = valid = false

  override def isValid(): Boolean = valid
  override def drop(): Unit = ch.drop(this)

  override def channel(): MulticastChannel = ch
  override def group(): InetAddress = grp
  override def networkInterface(): NetworkInterface = interf
  override def sourceAddress(): InetAddress = source
}
