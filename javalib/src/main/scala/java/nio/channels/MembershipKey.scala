package java.nio.channels

import java.net.{InetAddress, NetworkInterface}

trait MembershipKey {
  def isValid(): Boolean

  def drop(): Unit

  def block(source: InetAddress): MembershipKey

  def unblock(source: InetAddress): MembershipKey

  def channel(): MulticastChannel

  def group(): InetAddress

  def networkInterface(): NetworkInterface

  def sourceAddress(): InetAddress
}
