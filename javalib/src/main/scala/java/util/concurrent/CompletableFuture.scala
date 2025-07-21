// scalafmt: { maxColumn = 120}
// Revision 1.225, Committed: Jan 19 2021

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

// import java.lang.invoke.MethodHandles
// import java.lang.invoke.VarHandle
import scala.scalanative.libc.stdatomic.AtomicRef
import scala.scalanative.libc.stdatomic.memory_order.memory_order_release
import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.annotation.alwaysinline

import java.util.concurrent.locks.LockSupport
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.Objects

object CompletableFuture {
  /* ------------- Encoding and decoding outcomes -------------- */
  private[concurrent] class AltResult(val ex: Throwable) // null only for NIL
  private[concurrent] val NIL: AltResult = new AltResult(null)

  private[concurrent] def encodeThrowable(x: Throwable): AltResult = new AltResult(
    x match {
      case _: CompletionException => x
      case _                      => new CompletionException(x)
    }
  )

  private[concurrent] def encodeThrowable(x: Throwable, r: AnyRef): AnyRef = {
    new AltResult(x match {
      case _: CompletionException =>
        r match {
          case r: AltResult if x eq r.ex => return r
        }
      case _ => new CompletionException(x)
    })
  }

  private[concurrent] def encodeRelay(r: AnyRef): AnyRef = {
    r match {
      case r: AltResult if r.ex != null && !r.ex.isInstanceOf[CompletionException] =>
        new AltResult(new CompletionException(r.ex))
      case _ => r
    }
  }

  @throws[InterruptedException]
  @throws[ExecutionException]
  private def reportGet(r: AnyRef): AnyRef = r match {
    case null         => throw new InterruptedException // by convention below, null means interrupted
    case r: AltResult =>
      r.ex match {
        case null                      => null
        case ex: CancellationException => throw ex
        case ex: CompletionException   => throw new ExecutionException(ex.getCause())
        case ex                        => throw new ExecutionException(ex)
      }
    case _ => r
  }

  private def reportJoin(r: AnyRef): AnyRef = r match {
    case r: AltResult =>
      r.ex match {
        case null                      => null
        case ex: CancellationException => throw ex
        case ex: CompletionException   => throw ex
        case ex                        => throw new CompletionException(ex)
      }
    case _ => r
  }

  /* ------------- Async task preliminaries -------------- */
  trait AsynchronousCompletionTask

  private val USE_COMMON_POOL: Boolean = ForkJoinPool.getCommonPoolParallelism() > 1
  private val ASYNC_POOL: Executor =
    if (USE_COMMON_POOL) ForkJoinPool.commonPool()
    else new ThreadPerTaskExecutor

  final private[concurrent] class ThreadPerTaskExecutor extends Executor {
    override def execute(r: Runnable): Unit = {
      Objects.requireNonNull(r)
      new Thread(r).start()
    }
  }

  private[concurrent] def screenExecutor(e: Executor): Executor = {
    if (!USE_COMMON_POOL && (e eq ForkJoinPool.commonPool())) ASYNC_POOL
    else if (e == null) throw new NullPointerException
    else e
  }

  // Modes for Completion.tryFire. Signedness matters.
  private[concurrent] final val SYNC: Int = 0
  private[concurrent] final val ASYNC: Int = 1
  private[concurrent] final val NESTED: Int = -1

  /* ------------- Base Completion classes and operations -------------- */
  abstract private[concurrent] class Completion
      extends ForkJoinTask[Void]
      with Runnable
      with AsynchronousCompletionTask {
    @volatile private[concurrent] var next: Completion = _ // Treiber stack link
    @alwaysinline
    private[concurrent] def nextAtomic = new AtomicRef[Completion](
      fromRawPtr(classFieldRawPtr(this, "next"))
    )

    private[concurrent] def tryFire(mode: Int): CompletableFuture[_ <: AnyRef]
    private[concurrent] def isLive(): Boolean
    override final def run(): Unit = tryFire(ASYNC)
    override final def exec(): Boolean = {
      tryFire(ASYNC)
      false
    }

    override final def getRawResult(): Void = null
    override final def setRawResult(v: Void): Unit = ()
  }

  /* ------------- One-input Completions -------------- */
  abstract private[concurrent] class UniCompletion[T <: AnyRef, V <: AnyRef](
      var executor: Executor, // executor to use (null if none)
      var dep: CompletableFuture[V], // the dependent to complete
      var src: CompletableFuture[T] // source for action
  ) extends Completion {
    final private[concurrent] def claim(): Boolean = {
      val e: Executor = executor
      if (compareAndSetForkJoinTaskTag(0.toShort, 1.toShort)) {
        if (e == null) return true
        executor = null // disable
        e.execute(this)
      }
      false
    }

    override final private[concurrent] def isLive(): Boolean = dep != null
  }

  final private[concurrent] class UniApply[T <: AnyRef, V <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[V],
      _src: CompletableFuture[T],
      var fn: Function[_ >: T, _ <: V]
  ) extends UniCompletion[T, V](_executor, _dep, _src) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[V] = {
      var a: CompletableFuture[T] = null
      var d: CompletableFuture[V] = null
      var r: AnyRef = null
      var f: Function[_ >: T, _ <: V] = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { d = dep; d == null } || { f = fn; f == null })
        return null

      if (d.result == null) {
        r match {
          case res: AltResult =>
            val x = res.ex
            if (x != null) {
              d.completeThrowable(x, r)
              src = null; dep = null; fn = null
              return d.postFire(a, mode)
            }
            r = null
          case _ => ()
        }

        try
          if (mode <= 0 && !claim()) return null
          else d.completeValue(f.apply(r.asInstanceOf[T]))
        catch { case ex: Throwable => d.completeThrowable(ex) }
      }
      src = null; dep = null; fn = null
      d.postFire(a, mode)
    }
  }

  final private[concurrent] class UniAccept[T <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[Void],
      _src: CompletableFuture[T],
      var fn: Consumer[_ >: T]
  ) extends UniCompletion[T, Void](_executor, _dep, _src) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[Void] = {
      var a: CompletableFuture[T] = null
      var d: CompletableFuture[Void] = null
      var r: AnyRef = null
      var f: Consumer[_ >: T] = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { d = dep; d == null } || { f = fn; f == null })
        return null

      if (d.result == null) {
        r match {
          case res: AltResult =>
            val x = res.ex
            if (x != null) {
              d.completeThrowable(x, r)
              src = null; dep = null; fn = null
              return d.postFire(a, mode)
            }
            r = null
          case _ => ()
        }

        try
          if (mode <= 0 && !claim()) return null
          else {
            f.accept(r.asInstanceOf[T])
            d.completeNull()
          }
        catch { case ex: Throwable => d.completeThrowable(ex) }
      }
      src = null; dep = null; fn = null
      d.postFire(a, mode)
    }
  }

  final private[concurrent] class UniRun[T <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[Void],
      _src: CompletableFuture[T],
      var fn: Runnable
  ) extends UniCompletion[T, Void](_executor, _dep, _src) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[Void] = {
      var a: CompletableFuture[T] = null
      var d: CompletableFuture[Void] = null
      var r: AnyRef = null
      var f: Runnable = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { d = dep; d == null } || { f = fn; f == null })
        return null

      if (d.result == null) {
        r match {
          case r: AltResult if r.ex != null =>
            d.completeThrowable(r.ex, r)

          case _ =>
            try
              if (mode <= 0 && !claim())
                return null
              else {
                f.run()
                d.completeNull()
              }
            catch { case ex: Throwable => d.completeThrowable(ex) }
        }
      }
      src = null; dep = null; fn = null
      d.postFire(a, mode)
    }
  }

  final private[concurrent] class UniWhenComplete[T <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[T],
      _src: CompletableFuture[T],
      var fn: BiConsumer[_ >: T, _ >: Throwable]
  ) extends UniCompletion[T, T](_executor, _dep, _src) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[T] = {
      var d: CompletableFuture[T] = null
      var a: CompletableFuture[T] = null
      var r: AnyRef = null
      var f: BiConsumer[_ >: T, _ >: Throwable] = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { d = dep; d == null } ||
          { f = fn; f == null } || !d.uniWhenComplete(r, f, if (mode > 0) null else this)) return null

      src = null; dep = null; fn = null
      d.postFire(a, mode)
    }
  }

  final private[concurrent] class UniHandle[T <: AnyRef, V <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[V],
      _src: CompletableFuture[T],
      var fn: BiFunction[_ >: T, Throwable, _ <: V]
  ) extends UniCompletion[T, V](_executor, _dep, _src) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[V] = {
      var d: CompletableFuture[V] = null
      var a: CompletableFuture[T] = null
      var r: AnyRef = null
      var f: BiFunction[_ >: T, Throwable, _ <: V] = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { d = dep; d == null } ||
          { f = fn; f == null } || !d.uniHandle[T](r, f, if (mode > 0) null else this)) return null

      src = null; dep = null; fn = null
      d.postFire(a, mode)
    }
  }

  final private[concurrent] class UniExceptionally[T <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[T],
      _src: CompletableFuture[T],
      var fn: Function[_ >: Throwable, _ <: T]
  ) extends UniCompletion[T, T](_executor, _dep, _src) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[T] = {
      var d: CompletableFuture[T] = null
      var a: CompletableFuture[T] = null
      var r: AnyRef = null
      var f: Function[_ >: Throwable, _ <: T] = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { d = dep; d == null } ||
          { f = fn; f == null } || !(d.uniExceptionally(r, f, if (mode > 0) null else this))) return null

      src = null; dep = null; fn = null
      d.postFire(a, mode)
    }
  }

  final private[concurrent] class UniComposeExceptionally[T <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[T],
      _src: CompletableFuture[T],
      var fn: Function[Throwable, _ <: CompletionStage[T]]
  ) extends UniCompletion[T, T](_executor, _dep, _src) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[T] = {
      var d: CompletableFuture[T] = null
      var a: CompletableFuture[T] = null
      var f: Function[Throwable, _ <: CompletionStage[T]] = null
      var r: AnyRef = null
      var x: Throwable = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { d = dep; d == null } || { f = fn; f == null })
        return null
      if (d.result == null) r match {
        case res: AltResult if { x = res.ex; x != null } =>
          try {
            if (mode <= 0 && !claim())
              return null
            val g: CompletableFuture[T] = f.apply(x).toCompletableFuture()
            if ({ r = g.result; r != null })
              d.completeRelay(r)
            else {
              g.unipush(new UniRelay[T, T](d, g))
              if (d.result == null)
                return null
            }
          } catch { case ex: Throwable => d.completeThrowable(ex) }
        case _ => d.internalComplete(r)
      }

      src = null; dep = null; fn = null
      d.postFire(a, mode)
    }
  }

  final private[concurrent] class UniRelay[U <: AnyRef, T <: U](
      _dep: CompletableFuture[U],
      _src: CompletableFuture[T]
  ) extends UniCompletion[T, U](null, _dep, _src) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[U] = {
      var d: CompletableFuture[U] = null
      var a: CompletableFuture[T] = null
      var r: AnyRef = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { d = dep; d == null }) return null
      if (d.result == null) d.completeRelay(r)
      src = null
      dep = null
      return d.postFire(a, mode)
    }
  }

  private def uniCopyStage[U <: AnyRef, T <: U](src: CompletableFuture[T]): CompletableFuture[U] = {
    var r: AnyRef = null
    val d: CompletableFuture[U] = src.newIncompleteFuture[U]
    if ({ r = src.result; r != null }) d.result = encodeRelay(r)
    else src.unipush(new UniRelay[U, T](d, src))
    d
  }

  final private[concurrent] class UniCompose[T <: AnyRef, V <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[V],
      _src: CompletableFuture[T],
      var fn: Function[_ >: T, _ <: CompletionStage[V]]
  ) extends UniCompletion[T, V](_executor, _dep, _src) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[V] = {
      var d: CompletableFuture[V] = null
      var a: CompletableFuture[T] = null
      var f: Function[_ >: T, _ <: CompletionStage[V]] = null
      var r: AnyRef = null
      var x: Throwable = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { d = dep; d == null } || { f = fn; f == null })
        return null

      if (d.result == null) {
        if (r.isInstanceOf[AltResult]) {
          if ({ x = (r.asInstanceOf[AltResult]).ex; x != null }) {
            d.completeThrowable(x, r)
            src = null; dep = null; fn = null
            return d.postFire(a, mode)
          }
          r = null
        }
        try {
          if (mode <= 0 && !claim()) return null
          val t: T = r.asInstanceOf[T]
          val g: CompletableFuture[V] = f.apply(t).toCompletableFuture()
          if ({ r = g.result; r != null }) d.completeRelay(r)
          else {
            g.unipush(new UniRelay[V, V](d, g))
            if (d.result == null) return null
          }
        } catch { case ex: Throwable => d.completeThrowable(ex) }
      }

      src = null; dep = null; fn = null
      d.postFire(a, mode)
    }
  }

  /* ------------- Two-input Completions -------------- */
  abstract private[concurrent] class BiCompletion[T <: AnyRef, U <: AnyRef, V <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[V],
      _src: CompletableFuture[T],
      var snd: CompletableFuture[U] // second source for action
  ) extends UniCompletion[T, V](_executor, _dep, _src) {}

  final private[concurrent] class CoCompletion(var base: BiCompletion[_, _, _]) extends Completion {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[_ <: AnyRef] = {
      val c: BiCompletion[_, _, _] = base
      var d: CompletableFuture[_ <: AnyRef] = null
      if (c == null || { d = c.tryFire(mode); d == null })
        return null
      base = null // detach
      d
    }

    override final private[concurrent] def isLive(): Boolean = {
      var c: BiCompletion[_, _, _] = null
      return { c = base; c != null } && c.dep != null
    }
  }

  final private[concurrent] class BiApply[T <: AnyRef, U <: AnyRef, V <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[V],
      _src: CompletableFuture[T],
      _snd: CompletableFuture[U],
      var fn: BiFunction[_ >: T, _ >: U, _ <: V]
  ) extends BiCompletion[T, U, V](_executor, _dep, _src, _snd) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[V] = {
      var d: CompletableFuture[V] = null
      var a: CompletableFuture[T] = null
      var b: CompletableFuture[U] = null
      var r: AnyRef = null
      var s: AnyRef = null
      var f: BiFunction[_ >: T, _ >: U, _ <: V] = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { b = snd; b == null } ||
          { s = b.result; s == null } || { d = dep; d == null } || { f = fn; f == null } ||
          !d.biApply[T, U](r, s, f, if (mode > 0) null else this)) return null
      src = null; snd = null; dep = null; fn = null

      return d.postFire(a, b, mode)
    }
  }

  final private[concurrent] class BiAccept[T <: AnyRef, U <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[Void],
      _src: CompletableFuture[T],
      _snd: CompletableFuture[U],
      var fn: BiConsumer[_ >: T, _ >: U]
  ) extends BiCompletion[T, U, Void](_executor, _dep, _src, _snd) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[Void] = {
      var d: CompletableFuture[Void] = null
      var a: CompletableFuture[T] = null
      var b: CompletableFuture[U] = null
      var r: AnyRef = null
      var s: AnyRef = null
      var f: BiConsumer[_ >: T, _ >: U] = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { b = snd; b == null } ||
          { s = b.result; s == null } || { d = dep; d == null } || { f = fn; f == null } ||
          !(d.biAccept[T, U](r, s, f, if (mode > 0) null else this)))
        return null

      src = null; snd = null; dep = null; fn = null
      return d.postFire(a, b, mode)
    }
  }

  final private[concurrent] class BiRun[T <: AnyRef, U <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[Void],
      _src: CompletableFuture[T],
      _snd: CompletableFuture[U],
      var fn: Runnable
  ) extends BiCompletion[T, U, Void](_executor, _dep, _src, _snd) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[Void] = {
      var d: CompletableFuture[Void] = null
      var a: CompletableFuture[T] = null
      var b: CompletableFuture[U] = null
      var r: AnyRef = null
      var s: AnyRef = null
      var f: Runnable = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { b = snd; b == null } ||
          { s = b.result; s == null } || { d = dep; d == null } || { f = fn; f == null } ||
          !(d.biRun(r, s, f, if (mode > 0) null else this)))
        return null

      src = null; snd = null; dep = null; fn = null
      return d.postFire(a, b, mode)
    }
  }

  final private[concurrent] class BiRelay[T <: AnyRef, U <: AnyRef](
      _dep: CompletableFuture[Void],
      _src: CompletableFuture[T],
      _snd: CompletableFuture[U]
  ) // for And
      extends BiCompletion[T, U, Void](null, _dep, _src, _snd) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[Void] = {
      var d: CompletableFuture[Void] = null
      var a: CompletableFuture[T] = null
      var b: CompletableFuture[U] = null
      var r: AnyRef = null
      var s: AnyRef = null
      var z: AnyRef = null
      var x: Throwable = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { b = snd; b == null } ||
          { s = b.result; s == null } || { d = dep; d == null }) return null
      if (d.result == null)
        if (r.isInstanceOf[AltResult] && { z = r; x = z.asInstanceOf[AltResult].ex; x != null } ||
            (s.isInstanceOf[AltResult] && { z = s; x = z.asInstanceOf[AltResult].ex; x != null }))
          d.completeThrowable(x, z)
        else d.completeNull()

      src = null; snd = null; dep = null
      return d.postFire(a, b, mode)
    }
  }

  private[concurrent] def andTree(
      cfs: Array[CompletableFuture[_ <: AnyRef]],
      lo: Int,
      hi: Int
  ): CompletableFuture[Void] = {
    val d: CompletableFuture[Void] = new CompletableFuture[Void]
    if (lo > hi) // empty
      d.result = NIL
    else {
      var a: CompletableFuture[_ <: AnyRef] = null
      var b: CompletableFuture[_ <: AnyRef] = null
      var r: AnyRef = null
      var s: AnyRef = null
      var z: AnyRef = null
      var x: Throwable = null
      val mid: Int = (lo + hi) >>> 1
      if ({
            a =
              if (lo == mid) cfs(lo)
              else andTree(cfs, lo, mid);
            a == null
          } || {
            b =
              if (lo == hi) a
              else if ((hi == mid + 1)) cfs(hi)
              else andTree(cfs, mid + 1, hi);
            b == null
          }) throw new NullPointerException
      if ({ r = a.result; r == null } || { s = b.result; s == null })
        a.bipush(b, new BiRelay(d, a, b))
      else if ((r.isInstanceOf[AltResult] && { z = r; x = z.asInstanceOf[AltResult].ex; x != null }) ||
          (s.isInstanceOf[AltResult] && { z = s; x = z.asInstanceOf[AltResult].ex; x != null }))
        d.result = encodeThrowable(x, z)
      else d.result = NIL
    }
    d
  }

  final private[concurrent] class OrApply[T <: AnyRef, U <: T, V <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[V],
      _src: CompletableFuture[T],
      _snd: CompletableFuture[U],
      var fn: Function[_ >: T, _ <: V]
  ) extends BiCompletion[T, U, V](_executor, _dep, _src, _snd) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[V] = {
      var d: CompletableFuture[V] = null
      var a: CompletableFuture[_ <: T] = null
      var b: CompletableFuture[_ <: T] = null
      var r: AnyRef = null
      var x: Throwable = null
      var f: Function[_ >: T, _ <: V] = null
      if ({ a = src; a == null } || { b = snd; b == null } || ({ r = a.result; r == null } &&
          { r = b.result; r == null }) || { d = dep; d == null } || { f = fn; f == null })
        return null

      if (d.result == null) try {
        if (mode <= 0 && !claim())
          return null
        if (r.isInstanceOf[AltResult]) {
          if ({ x = (r.asInstanceOf[AltResult]).ex; x != null }) {
            d.completeThrowable(x, r)
            src = null; snd = null; dep = null; fn = null
            return d.postFire(a, b, mode)
          }
          r = null
        }
        val t: T = r.asInstanceOf[T]
        d.completeValue(f.apply(t))
      } catch { case ex: Throwable => d.completeThrowable(ex) }
      src = null; snd = null; dep = null; fn = null
      d.postFire(a, b, mode)
    }
  }

  final private[concurrent] class OrAccept[T <: AnyRef, U <: T](
      _executor: Executor,
      _dep: CompletableFuture[Void],
      _src: CompletableFuture[T],
      _snd: CompletableFuture[U],
      var fn: Consumer[_ >: T]
  ) extends BiCompletion[T, U, Void](_executor, _dep, _src, _snd) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[Void] = {
      var d: CompletableFuture[Void] = null
      var a: CompletableFuture[_ <: T] = null
      var b: CompletableFuture[_ <: T] = null
      var r: AnyRef = null
      var x: Throwable = null
      var f: Consumer[_ >: T] = null
      if ({ a = src; a == null } || { b = snd; b == null } || ({ r = a.result; r == null } &&
          { r = b.result; r == null }) || { d = dep; d == null } || { f = fn; f == null }) return null

      if (d.result == null) try {
        if (mode <= 0 && !(claim())) return null
        if (r.isInstanceOf[AltResult]) {
          if ({ x = (r.asInstanceOf[AltResult]).ex; x != null }) {
            d.completeThrowable(x, r)
            src = null; snd = null; dep = null; fn = null
            return d.postFire(a, b, mode)
          }
          r = null
        }
        val t: T = r.asInstanceOf[T]
        f.accept(t)
        d.completeNull()
      } catch { case ex: Throwable => d.completeThrowable(ex) }

      src = null; snd = null; dep = null; fn = null
      return d.postFire(a, b, mode)
    }
  }

  final private[concurrent] class OrRun[T <: AnyRef, U <: AnyRef](
      _executor: Executor,
      _dep: CompletableFuture[Void],
      _src: CompletableFuture[T],
      _snd: CompletableFuture[U],
      var fn: Runnable
  ) extends BiCompletion[T, U, Void](_executor, _dep, _src, _snd) {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[Void] = {
      var d: CompletableFuture[Void] = null
      var a: CompletableFuture[_ <: AnyRef] = null
      var b: CompletableFuture[_ <: AnyRef] = null
      var r: AnyRef = null
      var x: Throwable = null
      var f: Runnable = null
      if ({ a = src; a == null } || { b = snd; b == null } || ({ r = a.result; r == null } &&
          { r = b.result; r == null }) || { d = dep; d == null } || { f = fn; f == null }) return null

      if (d.result == null)
        try
          if (mode <= 0 && !(claim())) return null
          else if (r.isInstanceOf[AltResult] && { x = r.asInstanceOf[AltResult].ex; x != null })
            d.completeThrowable(x, r)
          else {
            f.run()
            d.completeNull()
          }
        catch {
          case ex: Throwable => d.completeThrowable(ex)
        }

      src = null; snd = null; dep = null; fn = null
      return d.postFire(a, b, mode)
    }
  }

  private[concurrent] class AnyOf(
      var dep: CompletableFuture[AnyRef],
      var src: CompletableFuture[_ <: AnyRef],
      var srcs: Array[CompletableFuture[_ <: AnyRef]]
  ) extends Completion {
    override final private[concurrent] def tryFire(mode: Int): CompletableFuture[AnyRef] = {
      // assert mode != ASYNC;
      var d: CompletableFuture[AnyRef] = null
      var a: CompletableFuture[_ <: AnyRef] = null
      var as: Array[CompletableFuture[_ <: AnyRef]] = null
      var r: AnyRef = null
      if ({ a = src; a == null } || { r = a.result; r == null } || { d = dep; d == null } || { as = srcs; as == null })
        return null

      src = null; dep = null; srcs = null
      if (d.completeRelay(r)) {
        for (b <- as) {
          if (b ne a) b.cleanStack()
        }
        if (mode < 0) return d
        else d.postComplete()
      }
      null
    }

    override final private[concurrent] def isLive(): Boolean = {
      var d: CompletableFuture[AnyRef] = null
      return { d = dep; d != null } && d.result == null
    }
  }

  /* ------------- Zero-input Async forms -------------- */
  final private[concurrent] class AsyncSupply[T <: AnyRef](
      var dep: CompletableFuture[T],
      var fn: Supplier[_ <: T]
  ) extends ForkJoinTask[Void]
      with Runnable
      with AsynchronousCompletionTask {
    override final def getRawResult(): Void = null

    override final def setRawResult(v: Void): Unit = {}

    override final def exec(): Boolean = {
      run()
      false
    }

    override def run(): Unit = {
      var d: CompletableFuture[T] = null
      var f: Supplier[_ <: T] = null
      if ({ d = dep; d != null } && { f = fn; f != null }) {
        dep = null
        fn = null
        if (d.result == null)
          try d.completeValue(f.get())
          catch {
            case ex: Throwable =>
              d.completeThrowable(ex)
          }
        d.postComplete()
      }
    }
  }

  private[concurrent] def asyncSupplyStage[U <: AnyRef](e: Executor, f: Supplier[U]): CompletableFuture[U] = {
    if (f == null) throw new NullPointerException
    val d: CompletableFuture[U] = new CompletableFuture[U]
    e.execute(new AsyncSupply[U](d, f))
    d
  }

  final private[concurrent] class AsyncRun(
      var dep: CompletableFuture[Void],
      var fn: Runnable
  ) extends ForkJoinTask[Void]
      with Runnable
      with AsynchronousCompletionTask {
    override final def getRawResult(): Void = return null

    override final def setRawResult(v: Void): Unit = {}

    override final def exec(): Boolean = {
      run()
      false
    }

    override def run(): Unit = {
      var d: CompletableFuture[Void] = null
      var f: Runnable = null
      if ({ d = dep; d != null } && { f = fn; f != null }) {
        dep = null
        fn = null
        if (d.result == null) try {
          f.run()
          d.completeNull()
        } catch {
          case ex: Throwable => d.completeThrowable(ex)
        }
        d.postComplete()
      }
    }
  }

  private[concurrent] def asyncRunStage(e: Executor, f: Runnable): CompletableFuture[Void] = {
    if (f == null) throw new NullPointerException
    val d: CompletableFuture[Void] = new CompletableFuture[Void]
    e.execute(new AsyncRun(d, f))
    d
  }

  /* ------------- Signallers -------------- */
  final private[concurrent] class Signaller(
      val interruptible: Boolean,
      var nanos: Long, // remaining wait time if timed
      val deadline: Long // non-zero if timed
  ) extends Completion
      with ForkJoinPool.ManagedBlocker {
    var interrupted: Boolean = false
    @volatile var thread: Thread = Thread.currentThread()

    override final private[concurrent] def tryFire(ignore: Int): CompletableFuture[_ <: AnyRef] = {
      var w: Thread = null // no need to atomically claim()

      if ({ w = thread; w != null }) {
        thread = null
        LockSupport.unpark(w)
      }
      null
    }

    override def isReleasable(): Boolean = {
      if (Thread.interrupted()) interrupted = true
      (interrupted && interruptible) ||
        (deadline != 0L && nanos <= 0L || { nanos = deadline - System.nanoTime(); nanos <= 0L }) ||
        thread == null
    }

    override def block(): Boolean = {
      while (!isReleasable())
        if (deadline == 0L) LockSupport.park(this)
        else LockSupport.parkNanos(this, nanos)
      true
    }

    override final private[concurrent] def isLive(): Boolean = thread != null
  }

  def supplyAsync[U <: AnyRef](supplier: Supplier[U]): CompletableFuture[U] = asyncSupplyStage(ASYNC_POOL, supplier)
  def supplyAsync[U <: AnyRef](supplier: Supplier[U], executor: Executor): CompletableFuture[U] =
    asyncSupplyStage(screenExecutor(executor), supplier)
  def runAsync(runnable: Runnable): CompletableFuture[Void] = asyncRunStage(ASYNC_POOL, runnable)
  def runAsync(runnable: Runnable, executor: Executor): CompletableFuture[Void] =
    asyncRunStage(screenExecutor(executor), runnable)

  def completedFuture[U <: AnyRef](value: U): CompletableFuture[U] = new CompletableFuture[U](
    if (value == null) NIL
    else value
  )

  /* ------------- Arbitrary-arity constructions -------------- */
  // TODO: check
  // def allOf(cfs: CompletableFuture[_ <: AnyRef]*): CompletableFuture[Void] = andTree(cfs, 0, cfs.length - 1)
  // def anyOf(cfs: CompletableFuture[_ <: AnyRef]*): CompletableFuture[AnyRef] = {
  def allOf(cfs: Array[CompletableFuture[_ <: AnyRef]]): CompletableFuture[Void] = andTree(cfs, 0, cfs.length - 1)
  def anyOf(_cfs: Array[CompletableFuture[_ <: AnyRef]]): CompletableFuture[AnyRef] = {
    val n: Int = _cfs.length
    var r: AnyRef = null
    if (n <= 1)
      return if (n == 0) new CompletableFuture[AnyRef]
      else uniCopyStage(_cfs(0))

    var i = 0
    while (i < _cfs.length) {
      val cf = _cfs(i)
      if ({ r = cf.result; r != null }) return new CompletableFuture[AnyRef](encodeRelay(r))
      i += 1
    }

    val cfs = _cfs.clone
    val d: CompletableFuture[AnyRef] = new CompletableFuture[AnyRef]
    for (cf <- cfs) {
      cf.unipush(new AnyOf(d, cf, cfs))
    }
    // If d was completed while we were adding completions, we should
    // clean the stack of any sources that may have had completions
    // pushed on their stack after d was completed.
    if (d.result != null) {
      var i: Int = 0
      val len: Int = cfs.length
      while (i < len) {
        if (cfs(i).result != null) {
          i += 1
          while (i < len) {
            if (cfs(i).result == null) cfs(i).cleanStack()
            i += 1
          }
        }
        i += 1
      }
    }
    d
  }

  def delayedExecutor(delay: Long, unit: TimeUnit, executor: Executor): Executor = {
    if (unit == null || executor == null) throw new NullPointerException
    new DelayedExecutor(delay, unit, executor)
  }

  def delayedExecutor(delay: Long, unit: TimeUnit): Executor = {
    if (unit == null) throw new NullPointerException
    new DelayedExecutor(delay, unit, ASYNC_POOL)
  }

  def completedStage[U <: AnyRef](value: U): CompletionStage[U] = new MinimalStage[U](
    if ((value == null)) NIL
    else value
  )

  def failedFuture[U <: AnyRef](ex: Throwable): CompletableFuture[U] = {
    if (ex == null) throw new NullPointerException
    return new CompletableFuture[U](new AltResult(ex))
  }

  def failedStage[U <: AnyRef](ex: Throwable): CompletionStage[U] = {
    if (ex == null) throw new NullPointerException
    return new MinimalStage[U](new AltResult(ex))
  }

  private[concurrent] object Delayer {
    private[concurrent] def delay(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_ <: AnyRef] =
      return delayer.schedule(command, delay, unit)

    final private[concurrent] class DaemonThreadFactory extends ThreadFactory {
      override def newThread(r: Runnable): Thread = {
        val t: Thread = new Thread(r)
        t.setDaemon(true)
        t.setName("CompletableFutureDelayScheduler")
        t
      }
    }

    private[concurrent] val delayer: ScheduledThreadPoolExecutor =
      new ScheduledThreadPoolExecutor(1, new Delayer.DaemonThreadFactory)
    delayer.setRemoveOnCancelPolicy(true)
  }

  // Little class-ified lambdas to better support monitoring
  final private[concurrent] class DelayedExecutor(
      private[concurrent] val delay: Long,
      private[concurrent] val unit: TimeUnit,
      private[concurrent] val executor: Executor
  ) extends Executor {
    override def execute(r: Runnable): Unit = {
      Delayer.delay(new TaskSubmitter(executor, r), delay, unit)
    }
  }

  final private[concurrent] class TaskSubmitter(
      private[concurrent] val executor: Executor,
      private[concurrent] val action: Runnable
  ) extends Runnable {
    override def run(): Unit = {
      executor.execute(action)
    }
  }

  final private[concurrent] class Timeout(private[concurrent] val f: CompletableFuture[_ <: AnyRef]) extends Runnable {
    override def run(): Unit = {
      if (f != null && !f.isDone()) f.completeExceptionally(new TimeoutException)
    }
  }

  final private[concurrent] class DelayedCompleter[U <: AnyRef](
      private[concurrent] val f: CompletableFuture[U],
      private[concurrent] val u: U
  ) extends Runnable {
    override def run(): Unit = {
      if (f != null) f.complete(u)
    }
  }

  final private[concurrent] class Canceller(private[concurrent] val f: Future[_ <: AnyRef])
      extends BiConsumer[AnyRef, Throwable] {
    override def accept(ignore: AnyRef, ex: Throwable): Unit = {
      if (ex == null && f != null && !f.isDone()) f.cancel(false)
    }
  }

  /** A subclass that just throws UOE for most non-CompletionStage methods.
   */
  final private[concurrent] class MinimalStage[T <: AnyRef](r: AnyRef) extends CompletableFuture[T](r) {
    def this() = this(null)

    override def newIncompleteFuture[U <: AnyRef]: CompletableFuture[U] = return new MinimalStage[U]

    override def get(): T = throw new UnsupportedOperationException

    override def get(timeout: Long, unit: TimeUnit): T = throw new UnsupportedOperationException

    override def getNow(valueIfAbsent: T): T = throw new UnsupportedOperationException

    override def join(): T = throw new UnsupportedOperationException

    override def complete(value: T): Boolean = throw new UnsupportedOperationException

    override def completeExceptionally(ex: Throwable): Boolean = throw new UnsupportedOperationException

    override def cancel(mayInterruptIfRunning: Boolean): Boolean = throw new UnsupportedOperationException

    override def obtrudeValue(value: T): Unit = {
      throw new UnsupportedOperationException
    }

    override def obtrudeException(ex: Throwable): Unit = {
      throw new UnsupportedOperationException
    }

    override def isDone(): Boolean = throw new UnsupportedOperationException

    override def isCancelled(): Boolean = throw new UnsupportedOperationException

    override def isCompletedExceptionally(): Boolean = throw new UnsupportedOperationException

    override def getNumberOfDependents(): Int = throw new UnsupportedOperationException

    override def completeAsync(supplier: Supplier[_ <: T], executor: Executor): CompletableFuture[T] =
      throw new UnsupportedOperationException

    override def completeAsync(supplier: Supplier[_ <: T]): CompletableFuture[T] =
      throw new UnsupportedOperationException

    override def orTimeout(timeout: Long, unit: TimeUnit): CompletableFuture[T] =
      throw new UnsupportedOperationException

    override def completeOnTimeout(value: T, timeout: Long, unit: TimeUnit): CompletableFuture[T] =
      throw new UnsupportedOperationException

    override def toCompletableFuture(): CompletableFuture[T] = {
      var r: AnyRef = null
      if ({ r = result; r != null }) return new CompletableFuture[T](encodeRelay(r))
      else {
        val d: CompletableFuture[T] = new CompletableFuture[T]
        unipush(new UniRelay[T, T](d, this))
        d
      }
    }
  }

  // VarHandle mechanics
  // private val RESULT: VarHandle = MethodHandles
  //   .lookup()
  //   .findVarHandle(classOf[CompletableFuture[_ <: AnyRef]], "result", classOf[AnyRef])
  // private val STACK: VarHandle = MethodHandles
  //   .lookup()
  //   .findVarHandle(classOf[CompletableFuture[_ <: AnyRef]], "stack", classOf[Completion])
  // private val NEXT: VarHandle = MethodHandles
  //   .lookup()
  //   .findVarHandle(classOf[Completion], "next", classOf[Completion])
}

class CompletableFuture[T <: AnyRef] extends Future[T] with CompletionStage[T] {
  import CompletableFuture._

  @volatile private[concurrent] var result: AnyRef = null // Either the result or boxed AltResult
  @volatile private[concurrent] var stack: Completion = null // Top of Treiber stack of dependent actions

  @alwaysinline private def resultAtomic = new AtomicRef[AnyRef](
    fromRawPtr(classFieldRawPtr(this, "result"))
  )
  @alwaysinline private def stackAtomic = new AtomicRef[Completion](
    fromRawPtr(classFieldRawPtr(this, "stack"))
  )

  final private[concurrent] def internalComplete(r: AnyRef): Boolean = { // CAS from null to r
    // RESULT.compareAndSet(this, null, r)
    resultAtomic.compareExchangeStrong(null: AnyRef, r)
  }

  final private[concurrent] def tryPushStack(c: Completion): Boolean = {
    val h: Completion = stack
    // NEXT.set(c, h) // CAS piggyback
    // STACK.compareAndSet(this, h, c)
    c.nextAtomic.store(h)
    this.stackAtomic.compareExchangeStrong(h, c)
  }

  final private[concurrent] def pushStack(c: Completion): Unit = {
    while (!tryPushStack(c)) ()
  }

  final private[concurrent] def completeNull(): Boolean =
    // RESULT.compareAndSet(this, null, NIL)
    this.resultAtomic.compareExchangeStrong(null: AnyRef, NIL)

  final private[concurrent] def encodeValue(t: T): AnyRef =
    if (t == null) NIL
    else t

  final private[concurrent] def completeValue(t: T): Boolean =
    // RESULT.compareAndSet(this, null, if (t == null) NIL else t)
    this.resultAtomic.compareExchangeStrong(null: AnyRef, if (t == null) NIL else t)

  final private[concurrent] def completeThrowable(x: Throwable): Boolean =
    // RESULT.compareAndSet(this, null, encodeThrowable(x))
    this.resultAtomic.compareExchangeStrong(null: AnyRef, encodeThrowable(x))

  final private[concurrent] def completeThrowable(x: Throwable, r: AnyRef): Boolean =
    // RESULT.compareAndSet(this, null, encodeThrowable(x, r))
    this.resultAtomic.compareExchangeStrong(null: AnyRef, encodeThrowable(x, r))

  private[concurrent] def encodeOutcome(t: T, x: Throwable): AnyRef =
    if (x == null)
      if (t == null) NIL
      else t
    else encodeThrowable(x)

  final private[concurrent] def completeRelay(r: AnyRef): Boolean =
    // RESULT.compareAndSet(this, null, encodeRelay(r))
    this.resultAtomic.compareExchangeStrong(null: AnyRef, encodeRelay(r))

  final private[concurrent] def postComplete(): Unit = {
    var f: CompletableFuture[_ <: AnyRef] = this
    var h: Completion = null
    while ({ h = f.stack; h != null } || { (f ne this) && { f = this; h = f.stack; h != null } }) {
      var d: CompletableFuture[_ <: AnyRef] = null
      var t: Completion = null
      var restart = false
      // if (STACK.compareAndSet(f, h, t = h.next)) {
      if (f.stackAtomic.compareExchangeStrong(h, { t = h.next; t })) {
        if (t != null) {
          if (f ne this) {
            pushStack(h)
            restart = true
          } else {
            // NEXT.compareAndSet(h, t, null) // try to detach
            h.nextAtomic.compareExchangeStrong(t, null)
          }
        }
        if (!restart) {
          f =
            if ({ d = h.tryFire(NESTED); d == null }) this
            else d
        }
      }
    }
  }

  final private[concurrent] def cleanStack(): Unit = {
    var p: Completion = stack
    // ensure head of stack live
    var unlinked: Boolean = false
    var break = false
    while (!break) {
      if (p == null) return
      else if (p.isLive())
        if (unlinked) return
        else break = true
      // else if (STACK.weakCompareAndSet(this, p, {p = p.next; p}))
      else if (this.stackAtomic.compareExchangeWeak(p, { p = p.next; p }))
        unlinked = true
      else p = stack
    }
    // try to unlink first non-live
    var q: Completion = p.next
    while (q != null) {
      val s: Completion = q.next
      if (q.isLive()) {
        p = q
        q = s
        // } else if (NEXT.weakCompareAndSet(p, q, s)) break
      } else if (p.nextAtomic.compareExchangeWeak(q, s)) return
      else q = p.next
    }
  }

  final private[concurrent] def unipush(c: Completion): Unit = {
    if (c != null) {
      var break = false
      while (!break && !tryPushStack(c)) if (result != null) {
        // NEXT.set(c, null)
        c.nextAtomic.store(null)
        break = true
      }
      if (result != null) c.tryFire(SYNC)
    }
  }

  final private[concurrent] def postFire(a: CompletableFuture[_ <: AnyRef], mode: Int): CompletableFuture[T] = {
    if (a != null && a.stack != null) {
      var r: AnyRef = null
      if ({ r = a.result; r == null }) a.cleanStack()
      if (mode >= 0 && (r != null || a.result != null)) a.postComplete()
    }
    if (result != null && stack != null)
      if (mode < 0) return this
      else postComplete()
    null
  }

  private def uniApplyStage[V <: AnyRef](e: Executor, f: Function[_ >: T, _ <: V]): CompletableFuture[V] = {
    if (f == null) throw new NullPointerException
    var r: AnyRef = null
    if ({ r = result; r != null }) return uniApplyNow(r, e, f)
    val d: CompletableFuture[V] = newIncompleteFuture
    unipush(new UniApply[T, V](e, d, this, f))
    d
  }

  private def uniApplyNow[V <: AnyRef](_r: AnyRef, e: Executor, f: Function[_ >: T, _ <: V]): CompletableFuture[V] = {
    var r = _r
    var x: Throwable = null
    val d: CompletableFuture[V] = newIncompleteFuture
    if (r.isInstanceOf[AltResult]) {
      if ({ x = (r.asInstanceOf[AltResult]).ex; x != null }) {
        d.result = encodeThrowable(x, r)
        return d
      }
      r = null
    }
    try
      if (e != null) e.execute(new UniApply[T, V](null, d, this, f))
      else {
        val t: T = r.asInstanceOf[T]
        d.result = d.encodeValue(f.apply(t))
      }
    catch {
      case ex: Throwable => d.result = encodeThrowable(ex)
    }
    d
  }

  private def uniAcceptStage(e: Executor, f: Consumer[_ >: T]): CompletableFuture[Void] = {
    if (f == null) throw new NullPointerException
    var r: AnyRef = null
    if ({ r = result; r != null }) return uniAcceptNow(r, e, f)
    val d: CompletableFuture[Void] = newIncompleteFuture
    unipush(new UniAccept[T](e, d, this, f))
    d
  }

  private def uniAcceptNow(_r: AnyRef, e: Executor, f: Consumer[_ >: T]): CompletableFuture[Void] = {
    var r = _r
    var x: Throwable = null
    val d: CompletableFuture[Void] = newIncompleteFuture
    if (r.isInstanceOf[AltResult]) {
      if ({ x = (r.asInstanceOf[AltResult]).ex; x != null }) {
        d.result = encodeThrowable(x, r)
        return d
      }
      r = null
    }
    try
      if (e != null) e.execute(new UniAccept[T](null, d, this, f))
      else {
        val t: T = r.asInstanceOf[T]
        f.accept(t)
        d.result = NIL
      }
    catch {
      case ex: Throwable =>
        d.result = encodeThrowable(ex)
    }
    d
  }

  private def uniRunStage(e: Executor, f: Runnable): CompletableFuture[Void] = {
    if (f == null) throw new NullPointerException
    var r: AnyRef = null
    if ({ r = result; r != null }) return uniRunNow(r, e, f)
    val d: CompletableFuture[Void] = newIncompleteFuture
    unipush(new UniRun[T](e, d, this, f))
    d
  }

  private def uniRunNow(r: AnyRef, e: Executor, f: Runnable): CompletableFuture[Void] = {
    var x: Throwable = null
    val d: CompletableFuture[Void] = newIncompleteFuture
    if (r.isInstanceOf[AltResult] && { x = (r.asInstanceOf[AltResult]).ex; x != null })
      d.result = encodeThrowable(x, r)
    else
      try
        if (e != null) e.execute(new UniRun[T](null, d, this, f))
        else {
          f.run()
          d.result = NIL
        }
      catch {
        case ex: Throwable =>
          d.result = encodeThrowable(ex)
      }
    d
  }

  final private[concurrent] def uniWhenComplete(
      r: AnyRef,
      f: BiConsumer[_ >: T, _ >: Throwable],
      c: UniWhenComplete[T]
  ): Boolean = {
    var t: T = null.asInstanceOf[T]
    var x: Throwable = null
    if (result == null) {
      try {
        if (c != null && !(c.claim())) return false
        if (r.isInstanceOf[AltResult]) {
          x = (r.asInstanceOf[AltResult]).ex
          t = null.asInstanceOf[T]
        } else {
          val tr: T = r.asInstanceOf[T]
          t = tr
        }
        f.accept(t, x)
        if (x == null) {
          internalComplete(r)
          return true
        }
      } catch {
        case ex: Throwable =>
          if (x == null) x = ex
          else if (x ne ex) x.addSuppressed(ex)
      }
      completeThrowable(x, r)
    }
    true
  }

  private def uniWhenCompleteStage(e: Executor, f: BiConsumer[_ >: T, _ >: Throwable]): CompletableFuture[T] = {
    if (f == null) throw new NullPointerException
    val d: CompletableFuture[T] = newIncompleteFuture
    var r: AnyRef = null
    if ({ r = result; r == null }) unipush(new UniWhenComplete[T](e, d, this, f))
    else if (e == null) d.uniWhenComplete(r, f, null)
    else
      try e.execute(new UniWhenComplete[T](null, d, this, f))
      catch {
        case ex: Throwable =>
          d.result = encodeThrowable(ex)
      }
    d
  }

  final private[concurrent] def uniHandle[S <: AnyRef](
      r: AnyRef,
      f: BiFunction[_ >: S, Throwable, _ <: T],
      c: UniHandle[S, T]
  ): Boolean = {
    var s: S = null.asInstanceOf[S]
    var x: Throwable = null
    if (result == null) try {
      if (c != null && !(c.claim())) return false
      if (r.isInstanceOf[AltResult]) {
        x = (r.asInstanceOf[AltResult]).ex
        s = null.asInstanceOf[S]
      } else {
        x = null
        val ss: S = r.asInstanceOf[S]
        s = ss
      }
      completeValue(f.apply(s, x))
    } catch {
      case ex: Throwable =>
        completeThrowable(ex)
    }
    true
  }

  private def uniHandleStage[V <: AnyRef](
      e: Executor,
      f: BiFunction[_ >: T, Throwable, _ <: V]
  ): CompletableFuture[V] = {
    if (f == null) throw new NullPointerException
    val d: CompletableFuture[V] = newIncompleteFuture
    var r: AnyRef = null
    if ({ r = result; r == null }) unipush(new UniHandle[T, V](e, d, this, f))
    else if (e == null) d.uniHandle[T](r, f, null)
    else
      try e.execute(new UniHandle[T, V](null, d, this, f))
      catch {
        case ex: Throwable =>
          d.result = encodeThrowable(ex)
      }
    d
  }

  final private[concurrent] def uniExceptionally(
      r: AnyRef,
      f: Function[_ >: Throwable, _ <: T],
      c: UniExceptionally[T]
  ): Boolean = {
    var x: Throwable = null
    if (result == null) try {
      if (c != null && !(c.claim())) return false
      if (r.isInstanceOf[AltResult] && { x = (r.asInstanceOf[AltResult]).ex; x != null }) completeValue(f.apply(x))
      else internalComplete(r)
    } catch {
      case ex: Throwable =>
        completeThrowable(ex)
    }
    true
  }

  private def uniExceptionallyStage(e: Executor, f: Function[Throwable, _ <: T]): CompletableFuture[T] = {
    if (f == null) throw new NullPointerException
    val d: CompletableFuture[T] = newIncompleteFuture
    var r: AnyRef = null
    if ({ r = result; r == null }) unipush(new UniExceptionally[T](e, d, this, f))
    else if (e == null) d.uniExceptionally(r, f, null)
    else
      try e.execute(new UniExceptionally[T](null, d, this, f))
      catch {
        case ex: Throwable =>
          d.result = encodeThrowable(ex)
      }
    d
  }

  private def uniComposeExceptionallyStage(
      e: Executor,
      f: Function[Throwable, _ <: CompletionStage[T]]
  ): CompletableFuture[T] = {
    if (f == null) throw new NullPointerException
    val d: CompletableFuture[T] = newIncompleteFuture
    var r: AnyRef = null
    var s: AnyRef = null
    var x: Throwable = null
    if ({ r = result; r == null }) unipush(new UniComposeExceptionally[T](e, d, this, f))
    else if (!((r.isInstanceOf[AltResult])) || { x = (r.asInstanceOf[AltResult]).ex; x == null }) d.internalComplete(r)
    else
      try
        if (e != null) e.execute(new UniComposeExceptionally[T](null, d, this, f))
        else {
          val g: CompletableFuture[T] = f.apply(x).toCompletableFuture()
          if ({ s = g.result; s != null }) d.result = encodeRelay(s)
          else g.unipush(new UniRelay[T, T](d, g))
        }
      catch {
        case ex: Throwable =>
          d.result = encodeThrowable(ex)
      }
    d
  }

  private def uniAsMinimalStage(): MinimalStage[T] = {
    var r: AnyRef = null
    if ({ r = result; r != null }) return new MinimalStage[T](encodeRelay(r))
    val d: MinimalStage[T] = new MinimalStage[T]
    unipush(new UniRelay[T, T](d, this))
    d
  }

  private def uniComposeStage[V <: AnyRef](
      e: Executor,
      f: Function[_ >: T, _ <: CompletionStage[V]]
  ): CompletableFuture[V] = {
    if (f == null) throw new NullPointerException
    val d: CompletableFuture[V] = newIncompleteFuture
    var r: AnyRef = null
    var s: AnyRef = null
    var x: Throwable = null
    if ({ r = result; r == null }) unipush(new UniCompose[T, V](e, d, this, f))
    else {
      if (r.isInstanceOf[AltResult]) {
        if ({ x = (r.asInstanceOf[AltResult]).ex; x != null }) {
          d.result = encodeThrowable(x, r)
          return d
        }
        r = null
      }
      try
        if (e != null) e.execute(new UniCompose[T, V](null, d, this, f))
        else {
          val t: T = r.asInstanceOf[T]
          val g: CompletableFuture[V] = f.apply(t).toCompletableFuture()
          if ({ s = g.result; s != null }) d.result = encodeRelay(s)
          else g.unipush(new UniRelay[V, V](d, g))
        }
      catch {
        case ex: Throwable =>
          d.result = encodeThrowable(ex)
      }
    }
    d
  }

  final private[concurrent] def bipush(b: CompletableFuture[_ <: AnyRef], c: BiCompletion[_, _, _]): Unit = {
    if (c != null) {
      while (result == null) if (tryPushStack(c)) {
        if (b.result == null) b.unipush(new CoCompletion(c))
        else if (result != null) c.tryFire(SYNC)
        return
      }
      b.unipush(c)
    }
  }

  final private[concurrent] def postFire(
      a: CompletableFuture[_ <: AnyRef],
      b: CompletableFuture[_ <: AnyRef],
      mode: Int
  ): CompletableFuture[T] = {
    if (b != null && b.stack != null) { // clean second source
      var r: AnyRef = null
      if ({ r = b.result; r == null }) b.cleanStack()
      if (mode >= 0 && (r != null || b.result != null)) b.postComplete()
    }
    return postFire(a, mode)
  }

  final private[concurrent] def biApply[R <: AnyRef, S <: AnyRef](
      _r: AnyRef,
      _s: AnyRef,
      f: BiFunction[_ >: R, _ >: S, _ <: T],
      c: BiApply[R, S, T]
  ): Boolean = {
    var r = _r
    var s = _s
    var x: Throwable = null
    if (result == null) {
      if (r.isInstanceOf[AltResult]) {
        if ({ x = (r.asInstanceOf[AltResult]).ex; x != null }) {
          completeThrowable(x, r)
          return true
        }
        r = null
      }
      if (s.isInstanceOf[AltResult]) {
        if ({ x = (s.asInstanceOf[AltResult]).ex; x != null }) {
          completeThrowable(x, s)
          return true
        }
        s = null.asInstanceOf[S]
      }
      try {
        if (c != null && !(c.claim())) return false
        val rr: R = r.asInstanceOf[R]
        val ss: S = s.asInstanceOf[S]
        completeValue(f.apply(rr, ss))
      } catch { case ex: Throwable => completeThrowable(ex) }
    }
    true
  }

  private def biApplyStage[U <: AnyRef, V <: AnyRef](
      e: Executor,
      o: CompletionStage[U],
      f: BiFunction[_ >: T, _ >: U, _ <: V]
  ): CompletableFuture[V] = {
    var b: CompletableFuture[U] = null
    var r: AnyRef = null
    var s: AnyRef = null
    if (f == null || { b = o.toCompletableFuture(); b == null }) throw new NullPointerException
    val d: CompletableFuture[V] = newIncompleteFuture
    if ({ r = result; r == null } || { s = b.result; s == null }) bipush(b, new BiApply[T, U, V](e, d, this, b, f))
    else if (e == null) d.biApply[T, U](r, s, f, null)
    else
      try e.execute(new BiApply[T, U, V](null, d, this, b, f))
      catch {
        case ex: Throwable =>
          d.result = encodeThrowable(ex)
      }
    d
  }

  final private[concurrent] def biAccept[R <: AnyRef, S <: AnyRef](
      _r: AnyRef,
      _s: AnyRef,
      f: BiConsumer[_ >: R, _ >: S],
      c: BiAccept[R, S]
  ): Boolean = {
    var r = _r
    var s = _s
    var x: Throwable = null
    if (result == null) {
      if (r.isInstanceOf[AltResult]) {
        if ({ x = (r.asInstanceOf[AltResult]).ex; x != null }) {
          completeThrowable(x, r)
          return true
        }
        r = null
      }
      if (s.isInstanceOf[AltResult]) {
        if ({ x = (s.asInstanceOf[AltResult]).ex; x != null }) {
          completeThrowable(x, s)
          return true
        }
        s = null
      }
      try {
        if (c != null && !(c.claim())) return false
        val rr: R = r.asInstanceOf[R]
        val ss: S = s.asInstanceOf[S]
        f.accept(rr, ss)
        completeNull()
      } catch { case ex: Throwable => completeThrowable(ex) }
    }
    true
  }

  private def biAcceptStage[U <: AnyRef](
      e: Executor,
      o: CompletionStage[U],
      f: BiConsumer[_ >: T, _ >: U]
  ): CompletableFuture[Void] = {
    var b: CompletableFuture[U] = null
    var r: AnyRef = null
    var s: AnyRef = null
    if (f == null || { b = o.toCompletableFuture(); b == null }) throw new NullPointerException
    val d: CompletableFuture[Void] = newIncompleteFuture
    if ({ r = result; r == null } || { s = b.result; s == null }) bipush(b, new BiAccept[T, U](e, d, this, b, f))
    else if (e == null) d.biAccept[T, U](r, s, f, null)
    else
      try e.execute(new BiAccept[T, U](null, d, this, b, f))
      catch {
        case ex: Throwable =>
          d.result = encodeThrowable(ex)
      }
    d
  }

  final private[concurrent] def biRun(r: AnyRef, s: AnyRef, f: Runnable, c: BiRun[_, _]): Boolean = {
    var x: Throwable = null
    var z: AnyRef = null
    if (result == null)
      if ((r.isInstanceOf[AltResult] && { z = r; x = z.asInstanceOf[AltResult].ex; x != null }) ||
          (s.isInstanceOf[AltResult] && { z = s; x = z.asInstanceOf[AltResult].ex; x != null })) completeThrowable(x, z)
      else
        try {
          if (c != null && !(c.claim())) return false
          f.run()
          completeNull()
        } catch { case ex: Throwable => completeThrowable(ex) }
    true
  }

  private def biRunStage(e: Executor, o: CompletionStage[_ <: AnyRef], f: Runnable): CompletableFuture[Void] = {
    var b: CompletableFuture[_ <: AnyRef] = null
    var r: AnyRef = null
    var s: AnyRef = null
    if (f == null || { b = o.toCompletableFuture(); b == null }) throw new NullPointerException
    val d: CompletableFuture[Void] = newIncompleteFuture
    if ({ r = result; r == null } || { s = b.result; s == null }) bipush(b, new BiRun(e, d, this, b, f))
    else if (e == null) d.biRun(r, s, f, null)
    else
      try e.execute(new BiRun(null, d, this, b, f))
      catch {
        case ex: Throwable =>
          d.result = encodeThrowable(ex)
      }
    d
  }

  /* ------------- Projected (Ored) BiCompletions -------------- */
  final private[concurrent] def orpush(b: CompletableFuture[_ <: AnyRef], c: BiCompletion[_, _, _]): Unit = {
    if (c != null) {
      var break = false
      while (!break && !tryPushStack(c)) if (result != null) {
        // NEXT.set(c, null)
        c.nextAtomic.store(null)
        break = true
      }
      if (result != null) c.tryFire(SYNC)
      else b.unipush(new CoCompletion(c))
    }
  }

  private def orApplyStage[U <: T, V <: AnyRef](
      e: Executor,
      o: CompletionStage[U],
      f: Function[_ >: T, _ <: V]
  ): CompletableFuture[V] = {
    var b: CompletableFuture[U] = null
    if (f == null || { b = o.toCompletableFuture(); b == null }) throw new NullPointerException
    var r: AnyRef = null
    var z: CompletableFuture[_ <: T] = null
    if ({ z = this; r = z.result; r != null } || { z = b; r = z.result; r != null }) return z.uniApplyNow[V](r, e, f)
    val d: CompletableFuture[V] = newIncompleteFuture[V]
    orpush(b, new OrApply[T, U, V](e, d, this, b, f))
    d
  }

  private def orAcceptStage[U <: T](
      e: Executor,
      o: CompletionStage[U],
      f: Consumer[_ >: T]
  ): CompletableFuture[Void] = {
    var b: CompletableFuture[U] = null
    if (f == null || { b = o.toCompletableFuture(); b == null }) throw new NullPointerException
    var r: AnyRef = null
    var z: CompletableFuture[_ <: T] = null
    if ({ z = this; r = z.result; r != null } || { z = b; r = z.result; r != null }) return z.uniAcceptNow(r, e, f)
    val d: CompletableFuture[Void] = newIncompleteFuture
    orpush(b, new OrAccept[T, U](e, d, this, b, f))
    d
  }

  private def orRunStage(e: Executor, o: CompletionStage[_ <: AnyRef], f: Runnable): CompletableFuture[Void] = {
    var b: CompletableFuture[_ <: AnyRef] = null
    if (f == null || { b = o.toCompletableFuture(); b == null }) throw new NullPointerException
    var r: AnyRef = null
    var z: CompletableFuture[_ <: AnyRef] = null
    if ({ z = this; r = z.result; r != null } || { z = b; r = z.result; r != null }) return z.uniRunNow(r, e, f)
    val d: CompletableFuture[Void] = newIncompleteFuture
    orpush(b, new OrRun(e, d, this, b, f))
    d
  }

  private def waitingGet(interruptible: Boolean): AnyRef = {
    if (interruptible && Thread.interrupted()) return null
    var q: Signaller = null
    var queued: Boolean = false
    var r: AnyRef = null
    while ({ r = result; r == null })
      if (q == null) {
        q = new Signaller(interruptible, 0L, 0L)
        if (Thread.currentThread().isInstanceOf[ForkJoinWorkerThread])
          ForkJoinPool.helpAsyncBlocker(defaultExecutor(), q)
      } else if (!(queued)) queued = tryPushStack(q)
      else if (interruptible && q.interrupted) {
        q.thread = null
        cleanStack()
        return null
      } else
        try ForkJoinPool.managedBlock(q)
        catch {
          case ie: InterruptedException =>
            // currently cannot happen
            q.interrupted = true
        }
    if (q != null) {
      q.thread = null
      if (q.interrupted) Thread.currentThread().interrupt()
    }
    postComplete()
    r
  }

  @throws[TimeoutException]
  private def timedGet(_nanos: Long): AnyRef = {
    var nanos = _nanos
    val d: Long = System.nanoTime() + nanos
    val deadline: Long =
      if ((d == 0L)) 1L
      else d // avoid 0

    var interrupted: Boolean = false
    var queued: Boolean = false
    var q: Signaller = null
    var r: AnyRef = null

    var break = false
    while (!break) { // order of checking interrupt, result, timeout matters
      if (interrupted || { interrupted = Thread.interrupted(); interrupted })
        break = true
      else if ({ r = result; r != null })
        break = true
      else if (nanos <= 0L)
        break = true
      else if (q == null) {
        q = new Signaller(true, nanos, deadline)
        if (Thread.currentThread().isInstanceOf[ForkJoinWorkerThread])
          ForkJoinPool.helpAsyncBlocker(defaultExecutor(), q)
      } else if (!queued)
        queued = tryPushStack(q)
      else
        try {
          ForkJoinPool.managedBlock(q)
          interrupted = q.interrupted
          nanos = q.nanos
        } catch {
          case ie: InterruptedException => interrupted = true
        }
    }
    if (q != null) {
      q.thread = null
      if (r == null) cleanStack()
    }
    if (r != null) {
      if (interrupted) Thread.currentThread().interrupt()
      postComplete()
      return r
    } else if (interrupted) return null
    else throw new TimeoutException
  }

  def this(r: AnyRef) = {
    this()
    // RESULT.setRelease(this, r)
    this.resultAtomic.store(r, memory_order_release)
  }

  override def isDone(): Boolean = return result != null

  @throws[InterruptedException]
  @throws[ExecutionException]
  override def get(): T = {
    var r: AnyRef = null
    if ({ r = result; r == null }) r = waitingGet(true)
    return reportGet(r).asInstanceOf[T]
  }

  @throws[InterruptedException]
  @throws[ExecutionException]
  @throws[TimeoutException]
  override def get(timeout: Long, unit: TimeUnit): T = {
    val nanos: Long = unit.toNanos(timeout)
    var r: AnyRef = null
    if ({ r = result; r == null }) r = timedGet(nanos)
    return reportGet(r).asInstanceOf[T]
  }

  def join(): T = {
    var r: AnyRef = null
    if ({ r = result; r == null }) r = waitingGet(false)
    return reportJoin(r).asInstanceOf[T]
  }

  def getNow(valueIfAbsent: T): T = {
    var r: AnyRef = null
    return if ({ r = result; r == null }) valueIfAbsent
    else reportJoin(r).asInstanceOf[T]
  }

  def complete(value: T): Boolean = {
    val triggered: Boolean = completeValue(value)
    postComplete()
    triggered
  }

  def completeExceptionally(ex: Throwable): Boolean = {
    if (ex == null) throw new NullPointerException
    val triggered: Boolean = internalComplete(new AltResult(ex))
    postComplete()
    triggered
  }

  override def thenApply[U <: AnyRef](fn: Function[_ >: T, _ <: U]): CompletableFuture[U] = {
    return uniApplyStage(null, fn)
  }

  override def thenApplyAsync[U <: AnyRef](fn: Function[_ >: T, _ <: U]): CompletableFuture[U] = {
    return uniApplyStage(defaultExecutor(), fn)
  }

  override def thenApplyAsync[U <: AnyRef](fn: Function[_ >: T, _ <: U], executor: Executor): CompletableFuture[U] = {
    return uniApplyStage(screenExecutor(executor), fn)
  }

  override def thenAccept(action: Consumer[_ >: T]): CompletableFuture[Void] = {
    return uniAcceptStage(null, action)
  }

  override def thenAcceptAsync(action: Consumer[_ >: T]): CompletableFuture[Void] = {
    return uniAcceptStage(defaultExecutor(), action)
  }

  override def thenAcceptAsync(action: Consumer[_ >: T], executor: Executor): CompletableFuture[Void] = {
    return uniAcceptStage(screenExecutor(executor), action)
  }

  override def thenRun(action: Runnable): CompletableFuture[Void] = {
    return uniRunStage(null, action)
  }

  override def thenRunAsync(action: Runnable): CompletableFuture[Void] = {
    return uniRunStage(defaultExecutor(), action)
  }

  override def thenRunAsync(action: Runnable, executor: Executor): CompletableFuture[Void] = {
    return uniRunStage(screenExecutor(executor), action)
  }

  override def thenCombine[U <: AnyRef, V <: AnyRef](
      other: CompletionStage[_ <: U],
      fn: BiFunction[_ >: T, _ >: U, _ <: V]
  ): CompletableFuture[V] = {
    return biApplyStage[U, V](null, other.asInstanceOf[CompletionStage[U]], fn)
  }

  override def thenCombineAsync[U <: AnyRef, V <: AnyRef](
      other: CompletionStage[_ <: U],
      fn: BiFunction[_ >: T, _ >: U, _ <: V]
  ): CompletableFuture[V] = {
    return biApplyStage[U, V](defaultExecutor(), other.asInstanceOf[CompletionStage[U]], fn)
  }

  override def thenCombineAsync[U <: AnyRef, V <: AnyRef](
      other: CompletionStage[_ <: U],
      fn: BiFunction[_ >: T, _ >: U, _ <: V],
      executor: Executor
  ): CompletableFuture[V] = {
    return biApplyStage[U, V](screenExecutor(executor), other.asInstanceOf[CompletionStage[U]], fn)
  }

  override def thenAcceptBoth[U <: AnyRef](
      other: CompletionStage[_ <: U],
      action: BiConsumer[_ >: T, _ >: U]
  ): CompletableFuture[Void] = {
    return biAcceptStage[U](null, other.asInstanceOf[CompletionStage[U]], action)
  }

  override def thenAcceptBothAsync[U <: AnyRef](
      other: CompletionStage[_ <: U],
      action: BiConsumer[_ >: T, _ >: U]
  ): CompletableFuture[Void] = {
    return biAcceptStage[U](defaultExecutor(), other.asInstanceOf[CompletionStage[U]], action)
  }

  override def thenAcceptBothAsync[U <: AnyRef](
      other: CompletionStage[_ <: U],
      action: BiConsumer[_ >: T, _ >: U],
      executor: Executor
  ): CompletableFuture[Void] = {
    return biAcceptStage[U](screenExecutor(executor), other.asInstanceOf[CompletionStage[U]], action)
  }

  override def runAfterBoth(other: CompletionStage[_ <: AnyRef], action: Runnable): CompletableFuture[Void] = {
    return biRunStage(null, other, action)
  }

  override def runAfterBothAsync(other: CompletionStage[_ <: AnyRef], action: Runnable): CompletableFuture[Void] = {
    return biRunStage(defaultExecutor(), other, action)
  }

  override def runAfterBothAsync(
      other: CompletionStage[_ <: AnyRef],
      action: Runnable,
      executor: Executor
  ): CompletableFuture[Void] = {
    return biRunStage(screenExecutor(executor), other, action)
  }

  override def applyToEither[U <: AnyRef](
      other: CompletionStage[_ <: T],
      fn: Function[_ >: T, U]
  ): CompletableFuture[U] = {
    return orApplyStage(null, other, fn)
  }

  override def applyToEitherAsync[U <: AnyRef](
      other: CompletionStage[_ <: T],
      fn: Function[_ >: T, U]
  ): CompletableFuture[U] = {
    return orApplyStage(defaultExecutor(), other, fn)
  }

  override def applyToEitherAsync[U <: AnyRef](
      other: CompletionStage[_ <: T],
      fn: Function[_ >: T, U],
      executor: Executor
  ): CompletableFuture[U] = {
    return orApplyStage(screenExecutor(executor), other, fn)
  }

  override def acceptEither(other: CompletionStage[_ <: T], action: Consumer[_ >: T]): CompletableFuture[Void] = {
    return orAcceptStage(null, other, action)
  }

  override def acceptEitherAsync(other: CompletionStage[_ <: T], action: Consumer[_ >: T]): CompletableFuture[Void] = {
    return orAcceptStage(defaultExecutor(), other, action)
  }

  override def acceptEitherAsync(
      other: CompletionStage[_ <: T],
      action: Consumer[_ >: T],
      executor: Executor
  ): CompletableFuture[Void] =
    orAcceptStage(screenExecutor(executor), other, action)

  override def runAfterEither(other: CompletionStage[_ <: AnyRef], action: Runnable): CompletableFuture[Void] =
    orRunStage(null, other, action)

  override def runAfterEitherAsync(other: CompletionStage[_ <: AnyRef], action: Runnable): CompletableFuture[Void] =
    orRunStage(defaultExecutor(), other, action)

  override def runAfterEitherAsync(
      other: CompletionStage[_ <: AnyRef],
      action: Runnable,
      executor: Executor
  ): CompletableFuture[Void] = orRunStage(screenExecutor(executor), other, action)

  override def thenCompose[U <: AnyRef](fn: Function[_ >: T, _ <: CompletionStage[U]]): CompletableFuture[U] =
    uniComposeStage(null, fn)

  override def thenComposeAsync[U <: AnyRef](fn: Function[_ >: T, _ <: CompletionStage[U]]): CompletableFuture[U] =
    uniComposeStage(defaultExecutor(), fn)

  override def thenComposeAsync[U <: AnyRef](
      fn: Function[_ >: T, _ <: CompletionStage[U]],
      executor: Executor
  ): CompletableFuture[U] = {
    return uniComposeStage(screenExecutor(executor), fn)
  }

  override def whenComplete(action: BiConsumer[_ >: T, _ >: Throwable]): CompletableFuture[T] = {
    return uniWhenCompleteStage(null, action)
  }

  override def whenCompleteAsync(action: BiConsumer[_ >: T, _ >: Throwable]): CompletableFuture[T] = {
    return uniWhenCompleteStage(defaultExecutor(), action)
  }

  override def whenCompleteAsync(
      action: BiConsumer[_ >: T, _ >: Throwable],
      executor: Executor
  ): CompletableFuture[T] = {
    return uniWhenCompleteStage(screenExecutor(executor), action)
  }

  override def handle[U <: AnyRef](fn: BiFunction[_ >: T, Throwable, _ <: U]): CompletableFuture[U] = {
    return uniHandleStage(null, fn)
  }

  override def handleAsync[U <: AnyRef](fn: BiFunction[_ >: T, Throwable, _ <: U]): CompletableFuture[U] = {
    return uniHandleStage(defaultExecutor(), fn)
  }

  override def handleAsync[U <: AnyRef](
      fn: BiFunction[_ >: T, Throwable, _ <: U],
      executor: Executor
  ): CompletableFuture[U] = {
    return uniHandleStage(screenExecutor(executor), fn)
  }

  override def toCompletableFuture(): CompletableFuture[T] = this

  override def exceptionally(fn: Function[Throwable, _ <: T]): CompletableFuture[T] = {
    return uniExceptionallyStage(null, fn)
  }

  override def exceptionallyAsync(fn: Function[Throwable, _ <: T]): CompletableFuture[T] = {
    return uniExceptionallyStage(defaultExecutor(), fn)
  }

  override def exceptionallyAsync(fn: Function[Throwable, _ <: T], executor: Executor): CompletableFuture[T] = {
    return uniExceptionallyStage(screenExecutor(executor), fn)
  }

  override def exceptionallyCompose(fn: Function[Throwable, _ <: CompletionStage[T]]): CompletableFuture[T] = {
    return uniComposeExceptionallyStage(null, fn)
  }

  override def exceptionallyComposeAsync(fn: Function[Throwable, _ <: CompletionStage[T]]): CompletableFuture[T] = {
    return uniComposeExceptionallyStage(defaultExecutor(), fn)
  }

  override def exceptionallyComposeAsync(
      fn: Function[Throwable, _ <: CompletionStage[T]],
      executor: Executor
  ): CompletableFuture[T] = {
    return uniComposeExceptionallyStage(screenExecutor(executor), fn)
  }

  /* ------------- Control and status methods -------------- */
  override def cancel(mayInterruptIfRunning: Boolean): Boolean = {
    val cancelled: Boolean = (result == null) && internalComplete(new AltResult(new CancellationException))
    postComplete()
    cancelled || isCancelled()
  }

  override def isCancelled(): Boolean = result match {
    case res: AltResult => res.ex.isInstanceOf[CancellationException]
    case _              => false
  }

  def isCompletedExceptionally(): Boolean = result match {
    case res: AltResult => res ne NIL
    case _              => false
  }

  def obtrudeValue(value: T): Unit = {
    result = if (value == null) NIL else value
    postComplete()
  }

  def obtrudeException(ex: Throwable): Unit = {
    if (ex == null) {
      throw new NullPointerException
    }
    result = new AltResult(ex)
    postComplete()
  }

  def getNumberOfDependents(): Int = {
    var count: Int = 0
    var p: Completion = stack
    while (p != null) {
      count += 1
      p = p.next
    }
    count
  }

  override def toString(): String = {
    val r: AnyRef = result
    var count: Int = 0 // avoid call to getNumberOfDependents in case disabled

    var p: Completion = stack
    while (p != null) {
      count += 1
      p = p.next
    }
    return super.toString + (if ((r == null)) {
                               (if ((count == 0)) {
                                  "[Not completed]"
                                } else {
                                  "[Not completed, " + count + " dependents]"
                                })
                             } else {
                               (if (((r.isInstanceOf[AltResult]) && (r.asInstanceOf[AltResult]).ex != null)) {
                                  "[Completed exceptionally: " + (r.asInstanceOf[AltResult]).ex + "]"
                                } else {
                                  "[Completed normally]"
                                })
                             })
  }

  // jdk9 additions
  def newIncompleteFuture[U <: AnyRef]: CompletableFuture[U] = new CompletableFuture[U]

  def defaultExecutor(): Executor = ASYNC_POOL

  def copy(): CompletableFuture[T] = uniCopyStage(this)

  def minimalCompletionStage(): CompletionStage[T] = uniAsMinimalStage()

  def completeAsync(supplier: Supplier[_ <: T], executor: Executor): CompletableFuture[T] = {
    if (supplier == null || executor == null) throw new NullPointerException()
    executor.execute(new AsyncSupply[T](this, supplier))
    this
  }

  def completeAsync(supplier: Supplier[_ <: T]): CompletableFuture[T] = completeAsync(supplier, defaultExecutor())

  def orTimeout(timeout: Long, unit: TimeUnit): CompletableFuture[T] = {
    if (unit == null) {
      throw new NullPointerException
    }
    if (result == null) {
      whenComplete(new Canceller(Delayer.delay(new Timeout(this), timeout, unit)))
    }
    this
  }

  def completeOnTimeout(value: T, timeout: Long, unit: TimeUnit): CompletableFuture[T] = {
    if (unit == null) {
      throw new NullPointerException
    }
    if (result == null) {
      whenComplete(new Canceller(Delayer.delay(new DelayedCompleter[T](this, value), timeout, unit)))
    }
    this
  }
}
