package java.nio.channels

import java.nio.channels.spi._
import java.util.Set

abstract class Selector protected[channels] () {

  def close: Unit
  def isOpen: Boolean
  def keys: Set[SelectionKey]
  def provider: SelectorProvider
  def select: Int
  def select(timeout: Int): Int
  def selectedKeys: Set[SelectionKey]
  def selectNow: Int
  def wakeup: Selector

}
