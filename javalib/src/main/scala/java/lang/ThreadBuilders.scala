package java.lang

import java.util.Objects
import java.util.concurrent.ThreadFactory
import java.lang.Thread.{Builder, Characteristics}
import scala.scalanative.libc.atomic.CAtomicLongLong
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}

// ScalaNative specific
object ThreadBuilders {

  sealed abstract class BaseThreadBuilder[Self <: Builder] extends Builder {
    var name: String = _
    var counter: scala.Long = -1
    var characteristics: Int = Characteristics.Default
    var ueh: Thread.UncaughtExceptionHandler = _

    private def self: Self = this.asInstanceOf[Self]

    protected def nextThreadName(): String = if (name != null && counter >= 0) {
      val res = name + counter.toString
      counter += 1
      res
    } else name

    override def name(name: String): Self = {
      this.name = Objects.requireNonNull(name)
      this.counter = -1
      self
    }

    override def name(prefix: String, start: scala.Long): Self = {
      if (start < 0) throw new IllegalArgumentException("'start' is negative")
      this.name = Objects.requireNonNull(prefix)
      this.counter = start
      self
    }

    override def allowSetThreadLocals(allow: scala.Boolean): Self = {
      val flag = Characteristics.NoThreadLocal
      if (allow) characteristics &= ~flag
      else characteristics |= flag
      self
    }

    override def inheritInheritableThreadLocals(
        inherit: scala.Boolean
    ): Self = {
      val flag = Characteristics.NoInheritThreadLocal
      if (inherit) this.characteristics &= ~flag
      else characteristics |= flag
      self
    }

    override def uncaughtExceptionHandler(
        ueh: Thread.UncaughtExceptionHandler
    ): Self = {
      this.ueh = Objects.requireNonNull(ueh)
      self
    }

  }

  final class PlatformThreadBuilder
      extends BaseThreadBuilder[Builder.OfPlatform]
      with Builder.OfPlatform {
    private var group: ThreadGroup = _
    private var daemonOpt: Option[Boolean] = None
    private var priority: Int = 0
    private var stackSize: scala.Long = 0L

    override protected def nextThreadName(): String =
      super.nextThreadName() match {
        case null => Thread.nextThreadName()
        case name => name
      }

    override def group(group: ThreadGroup): Builder.OfPlatform = {
      this.group = Objects.requireNonNull(group)
      this
    }

    override def daemon(on: scala.Boolean): Builder.OfPlatform = {
      daemonOpt = Some(on)
      this
    }

    override def priority(priority: Int): Builder.OfPlatform = {
      if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY)
        throw new IllegalArgumentException("Thread priority out of range")
      this.priority = priority
      this
    }

    override def stackSize(stackSize: scala.Long): Builder.OfPlatform = {
      if (stackSize < 0L)
        throw new IllegalArgumentException("Negative thread stack size")
      this.stackSize = stackSize
      this
    }

    override def unstarted(task: Runnable): Thread = {
      Objects.requireNonNull(task)
      val thread =
        new Thread(group, nextThreadName(), characteristics, task, stackSize)
      daemonOpt.foreach(thread.setDaemon(_))
      if (priority != 0) thread.setPriority(priority)
      if (ueh != null) thread.setUncaughtExceptionHandler(ueh)
      thread
    }

    override def start(task: Runnable): Thread = {
      val thread = unstarted(task)
      thread.start()
      thread
    }

    override def factory(): ThreadFactory = new PlatformThreadFactory(
      group = group,
      name = name,
      start = counter,
      characteristics = characteristics,
      daemon = daemonOpt,
      priority = priority,
      stackSize = stackSize,
      ueh = ueh
    )
  }

  final class VirtualThreadBuilder
      extends BaseThreadBuilder[Builder.OfVirtual]
      with Builder.OfVirtual {

    override def unstarted(task: Runnable): Thread = {
      Objects.requireNonNull(task)
      val thread = new VirtualThread(nextThreadName(), characteristics, task)
      if (ueh != null) thread.setUncaughtExceptionHandler(ueh)
      thread
    }

    override def start(task: Runnable): Thread = {
      val thread = unstarted(task)
      thread.start()
      thread
    }

    override def factory(): ThreadFactory =
      new VirtualThreadFactory(name, counter, characteristics, ueh)
  }

  private abstract class BaseThreadFactory(
      name: String,
      start: scala.Long
  ) extends ThreadFactory {
    @volatile var counter: scala.Long = start

    private val counterRef = new CAtomicLongLong(
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "counter"))
    )
    private val hasCounter = name != null && start >= 0

    def nextThreadName(): String = {
      if (hasCounter) name + counterRef.fetchAdd(1L)
      else name
    }
  }

  private class PlatformThreadFactory(
      group: ThreadGroup,
      name: String,
      start: scala.Long,
      characteristics: Int,
      daemon: Option[Boolean],
      priority: Int,
      stackSize: scala.Long,
      ueh: Thread.UncaughtExceptionHandler
  ) extends BaseThreadFactory(name, start) {
    override def nextThreadName(): String = super.nextThreadName() match {
      case null => Thread.nextThreadName()
      case name => name
    }

    override def newThread(task: Runnable): Thread = {
      Objects.requireNonNull(task)
      val thread =
        new Thread(group, nextThreadName(), characteristics, task, stackSize)
      daemon.foreach(thread.setDaemon(_))
      if (priority != 0) thread.setPriority(priority)
      if (ueh != null) thread.setUncaughtExceptionHandler(ueh)
      thread
    }
  }

  private class VirtualThreadFactory(
      name: String,
      start: scala.Long,
      characteristics: Int,
      ueh: Thread.UncaughtExceptionHandler
  ) extends BaseThreadFactory(name, start) {
    override def newThread(task: Runnable): Thread = {
      Objects.requireNonNull(task)
      val thread = new VirtualThread(nextThreadName(), characteristics, task)
      if (ueh != null) thread.setUncaughtExceptionHandler(ueh)
      thread
    }
  }

}
