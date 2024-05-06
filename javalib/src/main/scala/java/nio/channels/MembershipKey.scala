package java.nio.channels

import java.net.{InetAddress, NetworkInterface}

abstract class MembershipKey {
  def isValid(): Boolean
  def drop(): Unit
  // TODO: implement these methods
  //  def block(source: InetAddress): MembershipKey
  //  def unblock(source: InetAddress): MembershipKey
  def channel(): MulticastChannel
  def group(): InetAddress
  def networkInterface(): NetworkInterface
  def sourceAddress(): InetAddress
}
