package java.nio.channels

import java.io.IOException
import java.net.Net
import java.nio.channels.spi.{
  AbstractSelectableChannel,
  AbstractSelectionKey,
  AbstractSelector,
  SelectorProvider
}
import java.util
import scala.collection.mutable
import scala.scalanative.libc.stdlib.{malloc, realloc}
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.unsafe.{CSize, Ptr, sizeof}
import scala.scalanative.unsigned._
import scalanative.posix.poll._
import scalanative.posix.pollOps._

private[channels] final class SelectorImpl(provider: SelectorProvider)
    extends AbstractSelector(provider) {

  private val keyMap = mutable.Map.empty[SelectionKey, Int]
  private val selectedKeySet = mutable.Set.empty[SelectionKey]

  private[channels] val keysLock: Object = new Object

  private var pollSize: CSize = 8.toUInt
  private var pollArr: Ptr[struct_pollfd] = malloc(
    pollSize * sizeof[struct_pollfd]
  ).asInstanceOf[Ptr[struct_pollfd]]
  private var pollCount = 0

  @inline private def throwIfClosed(): Unit =
    if (!isOpen) throw new ClosedSelectorException

  override protected def implCloseSelector(): Unit = ???

  override private[nio] def register(
      ch: AbstractSelectableChannel,
      ops: Int,
      att: Object
  ): SelectionKey = ???

  override def keys: util.Set[SelectionKey] = ???

  override def select(): Int = ???

  override def select(timeout: Long): Int = ???

  override def selectedKeys: util.Set[SelectionKey] = ???

  override def selectNow(): Int = selectInternal(0)

  private def applyOperation(selKey: SelectionKey, op: Int): Unit = {
    val key = selKey.asInstanceOf[SelectionKeyImpl]
    if (!selectedKeySet.contains(key)) {
      selectedKeySet += key
      key.readyOperations = op
    } else {
      key.readyOperations = key.readyOperations | op
    }
  }

  private def doCancel(): Unit = {
    val cancelled = cancelledKeys
    cancelled.synchronized {
      cancelled.forEach { key =>
        (pollArr + keyMap(key)).fd = -1
        keyMap.remove(key)
        deregister(key.asInstanceOf[AbstractSelectionKey])
        selectedKeySet.remove(key)
      }
      cancelled.clear()
    }
  }

  private def selectInternal(timeout: Long): Int = {
    throwIfClosed()
    synchronized {
      keyMap.synchronized {
        selectedKeySet.synchronized {
          doCancel()

          val result = poll(pollArr, pollCount.toUInt, timeout.toInt)
          if (result < 0) {
            throw new IOException(
              "Select failed, errno: "
            ) // TODO + errno.errno)
          }
          if (result == 0) {
            // timeout
          } else {
            for ((key, i) <- keyMap) {
              val revents = (pollArr + i).revents
              val ops = key.interestOps()
              if ((revents & Net.POLLOUT) == Net.POLLOUT) {
                if ((ops & SelectionKey.OP_CONNECT) != 0) {
                  if ((ops & SelectionKey.OP_WRITE) == 0) {
                    (pollArr + i).events =
                      ((pollArr + i).events & ~Net.POLLOUT).toShort
                  }
                  applyOperation(key, SelectionKey.OP_CONNECT)
                }
                if ((ops & SelectionKey.OP_WRITE) != 0) {
                  applyOperation(key, SelectionKey.OP_WRITE)
                }
              }
              if ((revents & Net.POLLIN) == Net.POLLIN) {
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

  override def wakeup(): Selector = ???

  private def opsToEvents(ops: Int): Short = {
    var events: Short = 0
    if ((ops & SelectionKey.OP_WRITE) != 0 || (ops & SelectionKey.OP_CONNECT) != 0) {
      (events | Net.POLLOUT).toShort
    }
    if ((ops & SelectionKey.OP_READ) != 0 || (ops & SelectionKey.OP_ACCEPT) != 0) {
      events = (events | Net.POLLIN).toShort
    }
    events
  }

  private def doublePollArr(): Unit = {
    pollSize *= 2.toUInt
    pollArr =
      realloc(pollArr.asInstanceOf[Ptr[Byte]], pollSize * sizeof[struct_pollfd])
        .asInstanceOf[Ptr[struct_pollfd]]
  }

  private[channels] def modKey(key: SelectionKeyImpl): Unit = synchronized {
    keyMap.synchronized {
      selectedKeySet.synchronized {
        val pollPtr = pollArr + keyMap(key)
        pollPtr.events = opsToEvents(key.interestOps())
      }
    }
  }
}
