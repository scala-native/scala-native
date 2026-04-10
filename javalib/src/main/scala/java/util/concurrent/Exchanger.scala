/*
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.concurrent.locks.LockSupport

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.annotation.safePublish
import scala.scalanative.libc.stdatomic._
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.runtime.{Intrinsics, ObjectArray, fromRawPtr}
import scala.scalanative.unsafe._

/** A synchronization point at which threads can pair and swap elements
 *  within pairs.  Each thread presents some object on entry to the
 *  {@code exchange} method, matches with a partner thread,
 *  and receives its partner's object on return.  An Exchanger may be
 *  viewed as a bidirectional form of a {@link SynchronousQueue}.
 *  Exchangers may be useful in applications such as genetic algorithms
 *  and pipeline designs.
 *
 *  <p><b>Sample Usage:</b>
 *  Here are the highlights of a class that uses an {@code Exchanger}
 *  to swap buffers between threads so that the thread filling the
 *  buffer gets a freshly emptied one when it needs it, handing off the
 *  filled one to the thread emptying the buffer.
 *  <pre> {@code
 *  class FillAndEmpty {
 *    Exchanger<DataBuffer> exchanger = new Exchanger<>();
 *    DataBuffer initialEmptyBuffer = ...; // a made-up type
 *    DataBuffer initialFullBuffer = ...;
 *
 *    class FillingLoop implements Runnable {
 *      public void run() {
 *        DataBuffer currentBuffer = initialEmptyBuffer;
 *        try {
 *          while (currentBuffer != null) {
 *            addToBuffer(currentBuffer);
 *            if (currentBuffer.isFull())
 *              currentBuffer = exchanger.exchange(currentBuffer);
 *          }
 *        } catch (InterruptedException ex) { ... handle ...}
 *      }
 *    }
 *
 *    class EmptyingLoop implements Runnable {
 *      public void run() {
 *        DataBuffer currentBuffer = initialFullBuffer;
 *        try {
 *          while (currentBuffer != null) {
 *            takeFromBuffer(currentBuffer);
 *            if (currentBuffer.isEmpty())
 *              currentBuffer = exchanger.exchange(currentBuffer);
 *          }
 *        } catch (InterruptedException ex) { ... handle ...}
 *      }
 *    }
 *
 *    void start() {
 *      new Thread(new FillingLoop()).start();
 *      new Thread(new EmptyingLoop()).start();
 *    }
 *  }}</pre>
 *
 *  <p>Memory consistency effects: For each pair of threads that
 *  successfully exchange objects via an {@code Exchanger}, actions
 *  prior to the {@code exchange()} in each thread
 *  <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 *  those subsequent to a return from the corresponding {@code exchange()}
 *  in the other thread.
 *
 *  @since 1.5
 *  @author Doug Lea and Bill Scherer and Michael Scott
 *  @param <V>
 *    The type of objects that may be exchanged
 */
object Exchanger {

  /** The maximum supported arena index. The maximum allocatable
   *  arena size is MMASK + 1. Must be a power of two minus one. The
   *  cap of 255 (0xff) more than suffices for the expected scaling
   *  limits of the main algorithms.
   */
  private final val MMASK = 0xff

  /** Unit for sequence/version bits of bound field. Each successful
   *  change to the bound also adds SEQ.
   */
  private final val SEQ = MMASK + 1

  /** The bound for spins while waiting for a match before either
   *  blocking or possibly shrinking arena.
   */
  private final val SPINS = 1 << 10

  /** Padded arena cells to avoid false-sharing memory contention.
   *  Note: @Contended annotation is a hint to the JVM; scala-native
   *  may not honor it, but we include it for API compatibility.
   */
  @Contended
  private[concurrent] final class Slot {
    var entry: Node = _
  }

  /** Nodes hold partially exchanged data, plus other per-thread
   *  bookkeeping.
   */
  private[concurrent] final class Node {
    var seed: Long = _ // Random seed
    var index: Int = _ // Arena index
    var item: Object = _ // This thread's current item
    @volatile var `match`: Object = _ // Item provided by releasing thread
    @volatile var parked: Thread = _ // Set to this thread when parked, else null

    `match` = null
    parked = null
    index = -1 // initialize on first use
    seed = Thread.currentThread().threadId()
  }

  /** The corresponding thread local class */
  private[concurrent] final class Participant extends ThreadLocal[Node] {
    override protected def initialValue(): Node = new Node()
  }

  /** The participant thread-locals. Because it is impossible to
   *  exchange, we also use this reference for dealing with null user
   *  arguments that are translated in and out of this value
   *  surrounding use.
   */
  private final val participant: Participant = new Participant()

  // Support methods for atomic access to volatile fields

  @alwaysinline
  private def slotEntryAtomic(slot: Slot): AtomicRef[Node] =
    new AtomicRef[Node](
      fromRawPtr(Intrinsics.classFieldRawPtr(slot, "entry"))
    )

  @alwaysinline
  private def nodeMatchAtomic(node: Node): AtomicRef[Object] =
    new AtomicRef[Object](
      fromRawPtr(Intrinsics.classFieldRawPtr(node, "match"))
    )

  @alwaysinline
  private def arenaSlotAtomicAccess[T <: AnyRef](
      a: Array[T],
      i: Int
  ): AtomicRef[T] = {
    val nativeArray = a.asInstanceOf[ObjectArray]
    val elemRef = nativeArray.at(i).asInstanceOf[Ptr[T]]
    new AtomicRef[T](elemRef)
  }

  /** Attempt to CAS the entry field of a slot.
   *  @return true if successful
   */
  @alwaysinline
  private def casSlotEntry(slot: Slot, cmp: Node, `val`: Node): Boolean =
    slotEntryAtomic(slot).compareExchangeStrong(cmp, `val`)

  /** Attempt to CAS an arena array element.
   *  @return true if successful
   */
  @alwaysinline
  private def casArenaSlot(
      a: Array[Slot],
      i: Int,
      cmp: Slot,
      `val`: Slot
  ): Boolean =
    arenaSlotAtomicAccess(a, i).compareExchangeStrong(cmp, `val`)

  /** Set the match field of a node using release semantics.
   */
  @alwaysinline
  private def setMatchRelease(node: Node, v: Object): Unit =
    nodeMatchAtomic(node).store(v, memory_order_release)
}

class Exchanger[V] @safePublish (
    @safePublish private final val arena: Array[Exchanger.Slot],
    @safePublish private final val ncpu: Int
) {

  import Exchanger._

  /** The index of the largest valid arena position.
   */
  @volatile private var bound: Int = _

  // Support for atomic operations on bound

  @alwaysinline private def boundAtomic: AtomicInt =
    new AtomicInt(fromRawPtr(Intrinsics.classFieldRawPtr(this, "bound")))

  @alwaysinline
  private def casBound(cmp: Int, `val`: Int): Boolean =
    boundAtomic.compareExchangeStrong(cmp, `val`)

  /** Exchange function. See above for explanation.
   *
   *  @param x
   *    the item to exchange
   *  @param deadline
   *    if zero, untimed, else timeout deadline
   *  @return
   *    the other thread's item
   *  @throws InterruptedException
   *    if interrupted while waiting
   *  @throws TimeoutException
   *    if deadline nonzero and timed out
   */
  @throws[InterruptedException]
  @throws[TimeoutException]
  private final def xchg(x: V, deadline: Long): V = {
    val a = arena
    val alen = a.length
    val ps = participant
    val item: Object = if (x == null) ps else x // translate nulls
    val p = ps.get()
    var i = p.index // if < 0, move
    var misses = 0 // ++ on collide, -- on spinout
    var offered: Object = null // for cleanup
    var v: Object = null

    var done = false
    while (!done) {
      var b = 0
      var m = 0
      var s: Slot = null
      var q: Node = null

      b = bound // volatile read
      m = b & MMASK
      if (m == 0)
        i = 0

      if (i < 0 || i > m || i >= alen || { s = a(i); s == null }) {
        // randomly move
        var r = p.seed
        r ^= r << 13
        r ^= r >>> 7
        r ^= r << 17 // xorShift
        p.seed = r
        i = p.index = ((r % (m + 1)).toInt)
      } else if ({ q = s.entry; q } != null) { // try release
        if (casSlotEntry(s, q, null)) {
          v = q.item
          q.`match` = item
          if (i == 0) {
            val parked = q.parked
            if (parked != null)
              LockSupport.unpark(parked)
          }
          done = true
        } else { // collision
          i = -1 // move index
          if (b != bound) // stale
            misses = 0
          else if (misses <= 2) // continue sampling
            misses += 1
          else {
            val nb = (b + 1) & MMASK
            misses = 0 // try to grow
            if (nb < alen) {
              if (casBound(b, b + 1 + SEQ)) {
                i = nb
                p.index = nb
                if (a(i) == null)
                  casArenaSlot(a, nb, null, new Slot())
              }
            }
          }
        }
      } else { // try offer
        if (offered == null)
          offered = p.item = item
        if (casSlotEntry(s, null, p)) {
          var tryCancel = false // true if interrupted
          val t = Thread.currentThread()
          if (!t.isInterrupted() && ncpu > 1 &&
              (i != 0 || !hasBusyVirtualThreads())) {
            var j = SPINS
            while (j > 0 && !done) {
              v = p.`match`
              if (v != null) {
                setMatchRelease(p, null)
                done = true
              } else {
                Thread.onSpinWait()
                j -= 1
              }
            }
          }

          if (!done) {
            var ns = 1L
            var blocked = true
            while (blocked) {
              v = p.`match`
              if (v != null) {
                setMatchRelease(p, null)
                done = true
                blocked = false
              } else if (i == 0 && !tryCancel &&
                  (deadline == 0L || { ns = deadline - System.nanoTime(); ns > 0L })) {
                p.parked = t // enable unpark and recheck
                if (p.`match` == null) {
                  if (deadline == 0L)
                    LockSupport.park(this)
                  else
                    LockSupport.parkNanos(this, ns)
                  tryCancel = t.isInterrupted()
                }
                p.parked = null
              } else if (casSlotEntry(s, p, null)) { // cancel
                offered = p.item = null
                if (Thread.interrupted())
                  throw new InterruptedException()
                if (deadline != 0L && ns <= 0L)
                  throw new TimeoutException()
                i = -1 // move and restart
                if (bound != b) // stale
                  misses = 0
                else if (misses >= 0)
                  misses -= 1 // continue sampling
                else if ((b & MMASK) != 0) {
                  misses = 0 // try to shrink
                  casBound(b, b - 1 + SEQ)
                }
                blocked = false
                // continue outer loop
              }
            }
          }
        }
      }
    }

    if (offered != null) // cleanup
      p.item = null

    val ret: V = if (v.asInstanceOf[AnyRef] eq participant) null.asInstanceOf[V]
    else v.asInstanceOf[V]
    ret
  }

  /** Heuristic check for busy virtual threads. Returns true if there
   *  appear to be virtual threads with queued work that might make
   *  spinning wasteful. Since scala-native doesn't have virtual
   *  threads, we conservatively return true to fall back to blocking.
   */
  private def hasBusyVirtualThreads(): Boolean = true

  /** Creates a new Exchanger.
   */
  def this() = {
    this({
      val h = (Runtime.getRuntime().availableProcessors()) >>> 1
      val size =
        if (h == 0) 1
        else if (h > MMASK) MMASK + 1
        else h
      val arr = new Array[Slot](size)
      arr(0) = new Slot()
      arr
    }, Runtime.getRuntime().availableProcessors())
  }

  /** Waits for another thread to arrive at this exchange point (unless
   *  the current thread is {@linkplain Thread#interrupt interrupted}),
   *  and then transfers the given object to it, receiving its object
   *  in return.
   *
   *  <p>If another thread is already waiting at the exchange point then
   *  it is resumed for thread scheduling purposes and receives the object
   *  passed in by the current thread.  The current thread returns immediately,
   *  receiving the object passed to the exchange by that other thread.
   *
   *  <p>If no other thread is already waiting at the exchange then the
   *  current thread is disabled for thread scheduling purposes and lies
   *  dormant until one of two things happens:
   *  <ul>
   *  <li>Some other thread enters the exchange; or
   *  <li>Some other thread {@linkplain Thread#interrupt interrupts}
   *  the current thread.
   *  </ul>
   *  <p>If the current thread:
   *  <ul>
   *  <li>has its interrupted status set on entry to this method; or
   *  <li>is {@linkplain Thread#interrupt interrupted} while waiting
   *  for the exchange,
   *  </ul>
   *  then {@link InterruptedException} is thrown and the current thread's
   *  interrupted status is cleared.
   *
   *  @param x
   *    the object to exchange
   *  @return
   *    the object provided by the other thread
   *  @throws InterruptedException
   *    if the current thread was
   *    interrupted while waiting
   */
  @throws[InterruptedException]
  def exchange(x: V): V = {
    try {
      xchg(x, 0L)
    } catch {
      case _: TimeoutException =>
        null.asInstanceOf[V] // not reached
    }
  }

  /** Waits for another thread to arrive at this exchange point (unless
   *  the current thread is {@linkplain Thread#interrupt interrupted} or
   *  the specified waiting time elapses), and then transfers the given
   *  object to it, receiving its object in return.
   *
   *  <p>If another thread is already waiting at the exchange point then
   *  it is resumed for thread scheduling purposes and receives the object
   *  passed in by the current thread.  The current thread returns immediately,
   *  receiving the object passed to the exchange by that other thread.
   *
   *  <p>If no other thread is already waiting at the exchange then the
   *  current thread is disabled for thread scheduling purposes and lies
   *  dormant until one of three things happens:
   *  <ul>
   *  <li>Some other thread enters the exchange; or
   *  <li>Some other thread {@linkplain Thread#interrupt interrupts}
   *  the current thread; or
   *  <li>The specified waiting time elapses.
   *  </ul>
   *  <p>If the current thread:
   *  <ul>
   *  <li>has its interrupted status set on entry to this method; or
   *  <li>is {@linkplain Thread#interrupt interrupted} while waiting
   *  for the exchange,
   *  </ul>
   *  then {@link InterruptedException} is thrown and the current thread's
   *  interrupted status is cleared.
   *
   *  <p>If the specified waiting time elapses then {@link
   *  TimeoutException} is thrown.  If the time is less than or equal
   *  to zero, the method will not wait at all.
   *
   *  @param x
   *    the object to exchange
   *  @param timeout
   *    the maximum time to wait
   *  @param unit
   *    the time unit of the {@code timeout} argument
   *  @return
   *    the object provided by the other thread
   *  @throws InterruptedException
   *    if the current thread was
   *    interrupted while waiting
   *  @throws TimeoutException
   *    if the specified waiting time elapses
   *    before another thread enters the exchange
   */
  @throws[InterruptedException]
  @throws[TimeoutException]
  def exchange(x: V, timeout: Long, unit: TimeUnit): V = {
    val d = unit.toNanos(timeout) + System.nanoTime()
    xchg(x, if (d == 0L) 1L else d) // avoid zero deadline
  }
}
