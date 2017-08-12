package java.nio.channels

import java.nio.channels.spi._

import java.util.{HashSet, Collection, Set => JSet}
import java.io.{IOException, FileDescriptor}

import scala.collection.mutable.Set
import scala.collection.JavaConverters._

import scalanative.native._
import scalanative.posix.poll._
import scalanative.posix.pollOps._
import scalanative.posix.errno._

private class UnaddableSet[E](collection: Collection[_ <: E]) 
extends HashSet[E](collection) {
  
  override def add(e: E): Boolean = 
    throw new UnsupportedOperationException

  override def addAll(c: Collection[_ <: E]): Boolean =
    throw new UnsupportedOperationException
}

final class SelectorImpl(provider: SelectorProvider) 
extends AbstractSelector(provider) {

  private val keySet = Set.empty[SelectionKey]
  private val selectedKeySet = Set.empty[SelectionKey]

  private class KeysLock
  private[channels] val keysLock: Object = new KeysLock

  override def implCloseSelector: Unit = {
    //wakeup TODO
    synchronized {
      keySet.synchronized {
        selectedKeySet.synchronized {
          doCancel
          //source, sink TODO
          keySet.foreach(key => deregister(key.asInstanceOf[AbstractSelectionKey]))
        }
      }
    }
  }

  override def keys: JSet[SelectionKey] = keySet.toSet.asJava

  override def select: Int = selectInternal(-1)

  override def select(timeout: Long): Int = {
    if(timeout < 0) throw new IllegalArgumentException("Timeout is negative")
    else if(timeout == 0) selectInternal(-1)
    else selectInternal(timeout)
  }

  override def selectNow: Int = selectInternal(0)

  private def selectInternal(timeout: Long): Int = {
    closeCheck
    synchronized {
      keySet.synchronized {
        selectedKeySet.synchronized {
          doCancel
          //do some stuff as Harmony does

          val fds = stackalloc[pollfd](keySet.size)

          var i = 0
          for(key <- keySet) {
            val ops = key.interestOps
            (fds + i).fd = key.asInstanceOf[FileDescriptorHandler].fd.fd
            (fds + i).events = 
              if(((SelectionKey.OP_ACCEPT | SelectionKey.OP_READ) & ops) != 0) {
                POLLIN.toShort
              } 
              else if (((SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE) & ops) != 0) {
                POLLOUT.toShort
              }
              else 0.toShort
            i += 1
          }
          println("events: " + fds.events)

          val result = poll(fds, keySet.size.toUInt, timeout.toInt)
          if(result == -1) {
            throw new IOException("Select failed, errno: " + errno.errno)
          }
          if(result == 0) {
            // TODO
            println("select TIMEOUT")
          } else {
            println("RESULT == " + result)
          }
        }
      }
    }
    0
  }

  private def doCancel = {
    val cancelled = cancelledKeys.asScala
    cancelled.synchronized {
      cancelled.foreach(key => {
        // delete from internal storage TODO
        keySet.remove(key)
        deregister(key.asInstanceOf[AbstractSelectionKey])
        selectedKeySet.remove(key)
      })
      cancelled.clear
    }
  }

  // TODO delKey and addKey for internal storage

  private def closeCheck = if(!isOpen) throw new ClosedSelectorException

  override def selectedKeys: JSet[SelectionKey] = {
    closeCheck
    new UnaddableSet(selectedKeySet.asJava)
  }

  override def wakeup: Selector = ???

  override def register(ch: AbstractSelectableChannel, ops: Int,
                        att: Object): SelectionKey = {
    if(provider != ch.provider) {
      throw new IllegalSelectorException
    }
    synchronized {
      keySet.synchronized {
        val key = new SelectionKeyImpl(ch, ops, att, this)
        keySet.add(key)
        key
      }
    }
  }

  private[channels] def modKey(key: SelectionKeyImpl): Unit = synchronized {
    keySet.synchronized {
      selectedKeySet.synchronized {
        // remove a key from internal storage and set it again TODO
      }
    }
  }

}
