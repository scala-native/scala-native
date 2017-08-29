package java.nio.channels

import java.nio.channels.spi._

import java.util.{HashSet, Collection, Set => JSet}
import java.io.{IOException, FileDescriptor}

import scala.collection.mutable.{Set, Map}
import scala.collection.JavaConverters._

import scalanative.native._
import scalanative.posix.poll._
import scalanative.posix.pollOps._
import scalanative.posix.errno._

private class UnaddableSet[E](collection: Collection[_ <: E])
    extends HashSet[E](collection) {

  // needed because HashSet constructor uses addAll
  val initialized = true

  override def add(e: E): Boolean = {
    if (initialized)
      throw new UnsupportedOperationException
    else
      super.add(e)
  }

  override def addAll(c: Collection[_ <: E]): Boolean = {
    if (initialized)
      throw new UnsupportedOperationException
    else
      super.addAll(c)
  }
}

final class SelectorImpl(provider: SelectorProvider)
    extends AbstractSelector(provider) {

  private val keyMap         = Map.empty[SelectionKey, Int]
  private val selectedKeySet = Set.empty[SelectionKey]

  private[channels] val keysLock: Object = new Object

  private var pollSize = 8
  private var pollArr: Ptr[pollfd] =
    stdlib.malloc(pollSize * sizeof[pollfd]).cast[Ptr[pollfd]]
  private var pollCount = 0

  override def implCloseSelector: Unit = {
    synchronized {
      keyMap.synchronized {
        selectedKeySet.synchronized {
          doCancel()
          keyMap.foreach {
            case (key, i) =>
              deregister(key.asInstanceOf[AbstractSelectionKey])
          }
          stdlib.free(pollArr.cast[Ptr[Byte]])
        }
      }
    }
  }

  override def keys: JSet[SelectionKey] = keyMap.keySet.asJava

  override def select: Int = selectInternal(-1)

  override def select(timeout: Long): Int = {
    if (timeout < 0) throw new IllegalArgumentException("Timeout is negative")
    else if (timeout == 0) selectInternal(-1)
    else selectInternal(timeout)
  }

  override def selectNow: Int = selectInternal(0)

  private def selectInternal(timeout: Long): Int = {
    closeCheck
    synchronized {
      keyMap.synchronized {
        selectedKeySet.synchronized {
          doCancel()

          val result = poll(pollArr, pollCount.toUInt, timeout.toInt)
          if (result < 0) {
            throw new IOException("Select failed, errno: " + errno.errno)
          }
          if (result == 0) {
            // timeout
          } else {
            for ((key, i) <- keyMap) {
              val revents = (pollArr + i).revents
              val ops     = key.interestOps
              if ((revents & POLLOUT) == POLLOUT) {
                if ((ops & SelectionKey.OP_CONNECT) != 0) {
                  if ((ops & SelectionKey.OP_WRITE) == 0) {
                    (pollArr + i).events =
                      ((pollArr + i).events & ~POLLOUT).toShort
                  }
                  applyOperation(key, SelectionKey.OP_CONNECT)
                }
                if ((ops & SelectionKey.OP_WRITE) != 0) {
                  applyOperation(key, SelectionKey.OP_WRITE)
                }
              }
              if ((revents & POLLIN) == POLLIN) {
                if ((ops & SelectionKey.OP_READ) != 0) {
                  applyOperation(key, SelectionKey.OP_READ)
                }
                if ((ops & SelectionKey.OP_ACCEPT) != 0) {
                  applyOperation(key, SelectionKey.OP_ACCEPT)
                }
              }
              if (revents == 0) {
                key.asInstanceOf[SelectionKeyImpl].readyOperations = 0
              }
              (pollArr + i).revents = 0.toShort
            }
          }
          doCancel()
          result
        }
      }
    }
  }

  private def applyOperation(selKey: SelectionKey, op: Int): Unit = {
    val key = selKey.asInstanceOf[SelectionKeyImpl]
    if (!selectedKeySet.contains(key)) {
      selectedKeySet.add(key)
      key.readyOperations = op
    } else {
      key.readyOperations = key.readyOperations | op
    }
  }

  private def doCancel(): Unit = {
    val cancelled = cancelledKeys.asScala
    cancelled.synchronized {
      cancelled.foreach(key => {
        (pollArr + keyMap(key)).fd = -1
        keyMap.remove(key)
        deregister(key.asInstanceOf[AbstractSelectionKey])
        selectedKeySet.remove(key)
      })
      cancelled.clear
    }
  }

  private def closeCheck = if (!isOpen) throw new ClosedSelectorException

  override def selectedKeys: JSet[SelectionKey] = {
    closeCheck
    new UnaddableSet(selectedKeySet.asJava)
  }

  override def wakeup: Selector = ???

  override def register(ch: AbstractSelectableChannel,
                        ops: Int,
                        att: Object): SelectionKey = {
    if (provider != ch.provider) {
      throw new IllegalSelectorException
    }
    synchronized {
      keyMap.synchronized {
        val key = new SelectionKeyImpl(ch, ops, att, this)
        if ((pollCount + 1) == pollSize) {
          doublePollArr()
        }
        val pollPtr = (pollArr + pollCount)
        pollPtr.fd = key.channel.asInstanceOf[FileDescriptorHandler].fd.fd
        pollPtr.events = opsToEvents(key.interestOps)
        pollPtr.revents = 0
        keyMap.put(key, pollCount)
        pollCount += 1
        key
      }
    }
  }

  private def opsToEvents(ops: Int): Short = {
    var events: Short = 0
    if ((ops & SelectionKey.OP_WRITE) != 0 || (ops & SelectionKey.OP_CONNECT) != 0) {
      events = (events | POLLOUT).toShort
    }
    if ((ops & SelectionKey.OP_READ) != 0 || (ops & SelectionKey.OP_ACCEPT) != 0) {
      events = (events | POLLIN).toShort
    }
    events
  }

  private def doublePollArr(): Unit = {
    pollSize *= 2
    pollArr = stdlib
      .realloc(pollArr.cast[Ptr[Byte]], pollSize * sizeof[pollfd])
      .cast[Ptr[pollfd]]
  }

  private[channels] def modKey(key: SelectionKeyImpl): Unit = synchronized {
    keyMap.synchronized {
      selectedKeySet.synchronized {
        val pollPtr = pollArr + keyMap(key)
        pollPtr.events = opsToEvents(key.interestOps)
      }
    }
  }

}
