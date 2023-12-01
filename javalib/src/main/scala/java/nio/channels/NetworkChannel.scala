package java.nio.channels

import java.net.{SocketAddress, SocketOption}

trait NetworkChannel extends Channel {
  def bind(local: SocketAddress): NetworkChannel

  def getLocalAddress(): SocketAddress

  def setOption[T](name: SocketOption[T], value: T): NetworkChannel

  def getOption[T](name: SocketOption[T]): T

  def supportedOptions(): java.util.Set[SocketOption[_]]
}
