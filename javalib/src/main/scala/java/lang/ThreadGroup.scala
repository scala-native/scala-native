package java.lang

import java.util.{Arrays, Map, HashMap, List, ArrayList}
import java.io.PrintStream
import java.lang.Thread.UncaughtExceptionHandler
import java.lang.ref.WeakReference

import scala.annotation.tailrec
import scala.scalanative.runtime.{NativeThread, Proxy}
class ThreadGroup(
    final val parent: ThreadGroup,
    final val name: String,
    @volatile private var daemon: Boolean,
    @volatile private var maxPriority: Int
) extends UncaughtExceptionHandler {

  // Array of weak references to subgroups of this ThreadGroup
  private[this] var weekSubgroups: Array[WeakReference[ThreadGroup]] =
    new Array(4)
  // Current number of populated subgroups
  private[this] var subgroups = 0

  def this(parent: ThreadGroup, name: String) = {
    this(
      parent = {
        if (parent != null) parent
        else
          throw new NullPointerException(
            "The parent thread group specified is null!"
          )
      },
      name = name,
      daemon = parent.daemon,
      maxPriority = parent.maxPriority
    )
    parent.add(this)
  }

  def this(name: String) = this(
    parent = Thread.currentThread().getThreadGroup(),
    name = name
  )

  final def getMaxPriority(): Int = maxPriority
  final def setMaxPriority(priority: Int): Unit = {
    if (priority >= Thread.MIN_PRIORITY && priority <= Thread.MAX_PRIORITY)
      synchronized {
        maxPriority = parent match {
          case null   => priority
          case parent => Math.min(priority, parent.maxPriority)
        }
        snapshot().forEach(_.setMaxPriority(priority))
      }
  }

  final def getName(): String = name
  final def getParent(): ThreadGroup = parent

  @deprecated(
    "The API and mechanism for destroying a ThreadGroup is inherently flawed.",
    since = "Java 16"
  )
  final def isDaemon(): scala.Boolean = daemon

  @deprecated(
    "The API and mechanism for destroying a ThreadGroup is inherently flawed.",
    since = "java 16"
  )
  final def setDaemon(daemon: scala.Boolean): Unit = this.daemon = daemon

  @deprecated(
    "The API and mechanism for destroying a ThreadGroup is inherently flawed.",
    since = "java 16"
  )
  def isDestroyed(): scala.Boolean = false

  def activeCount(): Int = {
    NativeThread.Registry.aliveThreads
      .count { nativeThread =>
        val group = nativeThread.thread.getThreadGroup()
        this.parentOf(group)
      }
  }

  def activeGroupCount(): Int = {
    var n = 0
    snapshot().forEach { group => n += group.activeGroupCount() + 1 }
    n
  }

  @deprecated(
    "The definition of this call depends on suspend(), which is deprecated.",
    since = "Java 1.2"
  )
  def allowThreadSuspension(b: scala.Boolean): scala.Boolean = true

  @deprecated(
    "The API and mechanism for destroying a ThreadGroup is inherently flawed.",
    since = "Java 16"
  )
  def destroy(): Unit = ()

  def enumerate(out: Array[Thread]): Int =
    enumerate(out, recurse = true)

  def enumerate(out: Array[Thread], recurse: scala.Boolean): Int = {
    if (out == null) throw new NullPointerException()
    if (out.length == 0) 0
    else {
      val aliveThreads = NativeThread.Registry.aliveThreads
      @tailrec def loop(idx: Int, included: Int): Int =
        if (idx == aliveThreads.length || included == out.length) included
        else {
          val thread = aliveThreads(idx).thread
          val group = thread.getThreadGroup()
          val nextIdx = idx + 1
          if ((group eq this) || (recurse && this.parentOf(group))) {
            out(included) = thread
            loop(nextIdx, included + 1)
          } else loop(nextIdx, included)
        }
      loop(0, 0)
    }
  }

  def enumerate(groups: Array[ThreadGroup]): Int =
    enumerate(groups, recurse = true)

  def enumerate(out: Array[ThreadGroup], recurse: scala.Boolean): Int = {
    if (out == null) throw new NullPointerException()
    if (out.isEmpty) 0
    else enumerate(out, 0, recurse)
  }

  private def enumerate(
      out: Array[ThreadGroup],
      idx: Int,
      recurse: Boolean
  ): Int = {
    var i = idx
    snapshot().forEach { group =>
      if (i < out.length) {
        out(i) = group
        i += 1
        if (recurse) {
          i = group.enumerate(out, i, recurse)
        }
      }
    }
    i
  }

  final def interrupt(): Unit = {
    for (nativeThread <- NativeThread.Registry.aliveThreads) {
      val thread = nativeThread.thread
      val group = thread.getThreadGroup()
      if (this.parentOf(group)) thread.interrupt()
    }
  }

  def list(): Unit = {
    val groupThreads = new HashMap[ThreadGroup, List[Thread]]
    for (nativeThread <- NativeThread.Registry.aliveThreads) {
      val thread = nativeThread.thread
      val group = thread.getThreadGroup()
      if (this.parentOf(group)) {
        groupThreads
          .computeIfAbsent(group, _ => new ArrayList())
          .add(thread)
      }
    }
    list(groupThreads, System.out)
  }

  private def list(
      map: Map[ThreadGroup, List[Thread]],
      out: PrintStream,
      indent: String = ""
  ): Unit = {
    out.print(indent)
    out.println(this)
    val newIndent =
      if (indent.isEmpty()) " " * 4
      else indent * 2
    map.get(this) match {
      case null => ()
      case threads =>
        threads.forEach { thread =>
          out.print(newIndent)
          out.println(thread)
        }
    }
    snapshot().forEach(_.list(map, out, newIndent))
  }

  def parentOf(group: ThreadGroup): scala.Boolean = {
    if (group == null) false
    else if (this == group) true
    else parentOf(group.getParent())
  }

  @deprecated(
    "This method is used solely in conjunction with Thread.suspend and ThreadGroup.suspend, both of which have been deprecated, as they are inherently deadlock-prone.",
    since = "Java 1.2"
  )
  def resume(): Unit = throw new UnsupportedOperationException()

  @deprecated("This method is inherently unsafe.", since = "Java 1.2")
  def stop(): Unit = throw new UnsupportedOperationException()

  @deprecated("This method is inherently deadlock-prone.", since = "Java 1.2")
  def suspend(): Unit = throw new UnsupportedOperationException()

  override def toString: String =
    s"${getClass().getName()}[name=$name,maxpri=$maxPriority]"

  def uncaughtException(thread: Thread, throwable: Throwable): Unit =
    parent match {
      case null =>
        Thread.getDefaultUncaughtExceptionHandler() match {
          case null =>
            val threadName = "\"" + thread.getName() + "\""
            System.err.print(s"Exception in thread $threadName")
            throwable.printStackTrace(System.err)
          case handler =>
            Proxy.executeUncaughtExceptionHandler(handler, thread, throwable)
        }
      case parent =>
        Proxy.executeUncaughtExceptionHandler(parent, thread, throwable)
    }

  private def add(group: ThreadGroup): Unit = synchronized {
    @tailrec def tryClean(idx: Int): Unit = {
      if (idx < subgroups) weekSubgroups(idx).get() match {
        case null =>
          removeGroupAtIndex(idx)
          tryClean(idx)
        case _ => tryClean(idx + 1)
      }
    }
    tryClean(0)
    if (weekSubgroups.length == subgroups)
      weekSubgroups = Arrays.copyOf(weekSubgroups, subgroups * 2)

    weekSubgroups(subgroups) = new WeakReference(group)
    subgroups += 1
  }

  private def removeGroupAtIndex(idx: Int): Unit = {
    // Remove element on index and compact array
    val lastIdx = subgroups - 1
    if (idx < subgroups) weekSubgroups(idx) = weekSubgroups(lastIdx)
    weekSubgroups(lastIdx) = null
    subgroups -= 1
  }

  private def snapshot() = synchronized {
    val snapshot = new ArrayList[ThreadGroup]()
    var i = 0
    while (i < subgroups) {
      weekSubgroups(i).get() match {
        case null => removeGroupAtIndex(i)
        case group =>
          snapshot.add(group)
          i += 1
      }
    }
    snapshot
  }

  @deprecated(
    "This method is only useful in conjunction with the Security Manager, which is deprecated and subject to removal in a future release.",
    since = "Java 17"
  )
  def checkAccess(): Unit = ()

}

object ThreadGroup {
  private[lang] val System = new ThreadGroup(
    parent = null,
    name = "system",
    daemon = false,
    maxPriority = Thread.MAX_PRIORITY
  )
}
