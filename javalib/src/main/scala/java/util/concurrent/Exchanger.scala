/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 *  file: src/main/java/util/concurrent/Exchanger.java
 *  revision 1.85, dated: 2020-11-27
 */

/*
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.concurrent.locks.LockSupport

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.memory_order.{
  memory_order_acquire, memory_order_release
}
import scala.scalanative.libc.stdatomic.{AtomicInt, AtomicRef}
import scala.scalanative.runtime.{Intrinsics, ObjectArray, fromRawPtr}
import scala.scalanative.unsafe.Ptr
/*
 * A synchronization point at which threads can pair and swap elements
 * within pairs.  Each thread presents some object on entry to the
 * {@link #exchange exchange} method, matches with a partner thread,
 * and receives its partner's object on return.  An Exchanger may be
 * viewed as a bidirectional form of a {@link SynchronousQueue}.
 * Exchangers may be useful in applications such as genetic algorithms
 * and pipeline designs.
 *
 * <p><b>Sample Usage:</b>
 * Here are the highlights of a class that uses an {@code Exchanger}
 * to swap buffers between threads so that the thread filling the
 * buffer gets a freshly emptied one when it needs it, handing off the
 * filled one to the thread emptying the buffer.
 * <pre> {@code
 * class FillAndEmpty {
 *   Exchanger<DataBuffer> exchanger = new Exchanger<>();
 *   DataBuffer initialEmptyBuffer = ...; // a made-up type
 *   DataBuffer initialFullBuffer = ...;
 *
 *   class FillingLoop implements Runnable {
 *     public void run() {
 *       DataBuffer currentBuffer = initialEmptyBuffer;
 *       try {
 *         while (currentBuffer != null) {
 *           addToBuffer(currentBuffer);
 *           if (currentBuffer.isFull())
 *             currentBuffer = exchanger.exchange(currentBuffer);
 *         }
 *       } catch (InterruptedException ex) { ... handle ...}
 *     }
 *   }
 *
 *   class EmptyingLoop implements Runnable {
 *     public void run() {
 *       DataBuffer currentBuffer = initialFullBuffer;
 *       try {
 *         while (currentBuffer != null) {
 *           takeFromBuffer(currentBuffer);
 *           if (currentBuffer.isEmpty())
 *             currentBuffer = exchanger.exchange(currentBuffer);
 *         }
 *       } catch (InterruptedException ex) { ... handle ...}
 *     }
 *   }
 *
 *   void start() {
 *     new Thread(new FillingLoop()).start();
 *     new Thread(new EmptyingLoop()).start();
 *   }
 * }}</pre>
 *
 * <p>Memory consistency effects: For each pair of threads that
 * successfully exchange objects via an {@code Exchanger}, actions
 * prior to the {@code exchange()} in each thread
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * those subsequent to a return from the corresponding {@code exchange()}
 * in the other thread.
 *
 * @since 1.5
 * @author Doug Lea and Bill Scherer and Michael Scott
 * @param <V> The type of objects that may be exchanged
 */

object Exchanger {

  // SN: see near essential original "Overview" note preserved at top of
  //     companion class below.

  /*
   * The index distance (as a shift value) between any two used slots
   * in the arena, spacing them out to avoid false sharing.
   */
  private final val ASHIFT = 5

  /*
   * The maximum supported arena index. The maximum allocatable
   * arena size is MMASK + 1. Must be a power of two minus one, less
   * than (1<<(31-ASHIFT)). The cap of 255 (0xff) more than suffices
   * for the expected scaling limits of the main algorithms.
   */
  private final val MMASK = 0xff

  /*
   * Unit for sequence/version bits of bound field. Each successful
   * change to the bound also adds SEQ.
   */
  private final val SEQ = MMASK + 1

  /* The number of CPUs, for sizing and spin control */
  private final val NCPU = Runtime.getRuntime().availableProcessors()

  /*
   * The maximum slot index of the arena: The number of slots that
   * can in principle hold all threads without contention, or at
   * most the maximum indexable value.
   */
  final val FULL =
    if (NCPU >= (MMASK << 1)) MMASK
    else NCPU >>> 1

  /*
   * The bound for spins while waiting for a match. The actual
   * number of iterations will on average be about twice this value
   * due to randomization. Note: Spinning is disabled when NCPU==1.
   */
  private final val SPINS = 1 << 10

  /*
   * Value representing null arguments/returns from public
   * methods. Needed because the API originally didn't disallow null
   * arguments, which it should have.
   */
  private final val NULL_ITEM = new Object()

  /*
   * Sentinel value returned by internal exchange methods upon
   * timeout, to avoid need for separate timed versions of these
   * methods.
   */
  private final val TIMED_OUT = new Object();

  // SN addition
  private type Contended = scala.scalanative.annotation.align

  /*
   * Nodes hold partially exchanged data, plus other per-thread
   * bookkeeping. Padded via @Contended to reduce memory contention.
   */
  @Contended()
  final class Node {
    var index: Int = _ // Arena index
    var bound: Int = _ // Last recorded value of Exchanger.bound
    var collides: Int = _ // Number of CAS failures at current bound
    var hash: Int = _ // Pseudo-random for spins
    var item: Object = _ // This thread's current item

    // Item provided by releasing thread
    @volatile var `match`: Object = _

    @alwaysinline private[Exchanger] def atomicMatch = new AtomicRef[Object](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "match"))
    )

    def setMatchRelease(v: Object): Unit = {
      atomicMatch.store(v, memory_order_release)
    }

    // Set to this thread when parked, else null
    @volatile var parked: Thread = _
  }

  /* The corresponding thread local class */
  final class Participant extends ThreadLocal[Node] {
    override def initialValue(): Node =
      new Node()
  }
}

class Exchanger[V <: AnyRef] {
  import Exchanger._

  /*
   * Overview: The core algorithm is, for an exchange "slot",
   * and a participant (caller) with an item:
   *
   * for (;;) {
   *   if (slot is empty) {                       // offer
   *     place item in a Node;
   *     if (can CAS slot from empty to node) {
   *       wait for release;
   *       return matching item in node;
   *     }
   *   }
   *   else if (can CAS slot from node to empty) { // release
   *     get the item in node;
   *     set matching item in node;
   *     release waiting thread;
   *   }
   *   // else retry on CAS failure
   * }
   *
   * This is among the simplest forms of a "dual data structure" --
   * see Scott and Scherer's DISC 04 paper and
   * http://www.cs.rochester.edu/research/synchronization/pseudocode/duals.html
   *
   * This works great in principle. But in practice, like many
   * algorithms centered on atomic updates to a single location, it
   * scales horribly when there are more than a few participants
   * using the same Exchanger. So the implementation instead uses a
   * form of elimination arena, that spreads out this contention by
   * arranging that some threads typically use different slots,
   * while still ensuring that eventually, any two parties will be
   * able to exchange items. That is, we cannot completely partition
   * across threads, but instead give threads arena indices that
   * will on average grow under contention and shrink under lack of
   * contention. We approach this by defining the Nodes that we need
   * anyway as ThreadLocals, and include in them per-thread index
   * and related bookkeeping state. (We can safely reuse per-thread
   * nodes rather than creating them fresh each time because slots
   * alternate between pointing to a node vs null, so cannot
   * encounter ABA problems. However, we do need some care in
   * resetting them between uses.)
   *
   * Implementing an effective arena requires allocating a bunch of
   * space, so we only do so upon detecting contention (except on
   * uniprocessors, where they wouldn't help, so aren't used).
   * Otherwise, exchanges use the single-slot slotExchange method.
   * On contention, not only must the slots be in different
   * locations, but the locations must not encounter memory
   * contention due to being on the same cache line (or more
   * generally, the same coherence unit).  Because, as of this
   * writing, there is no way to determine cacheline size, we define
   * a value that is enough for common platforms.  Additionally,
   * extra care elsewhere is taken to avoid other false/unintended
   * sharing and to enhance locality, including adding padding (via
   * @Contended) to Nodes, embedding "bound" as an Exchanger field.
   *
   * The arena starts out with only one used slot. We expand the
   * effective arena size by tracking collisions; i.e., failed CASes
   * while trying to exchange. By nature of the above algorithm, the
   * only kinds of collision that reliably indicate contention are
   * when two attempted releases collide -- one of two attempted
   * offers can legitimately fail to CAS without indicating
   * contention by more than one other thread. (Note: it is possible
   * but not worthwhile to more precisely detect contention by
   * reading slot values after CAS failures.)  When a thread has
   * collided at each slot within the current arena bound, it tries
   * to expand the arena size by one. We track collisions within
   * bounds by using a version (sequence) number on the "bound"
   * field, and conservatively reset collision counts when a
   * participant notices that bound has been updated (in either
   * direction).
   *
   * The effective arena size is reduced (when there is more than
   * one slot) by giving up on waiting after a while and trying to
   * decrement the arena size on expiration. The value of "a while"
   * is an empirical matter.  We implement by piggybacking on the
   * use of spin->yield->block that is essential for reasonable
   * waiting performance anyway -- in a busy exchanger, offers are
   * usually almost immediately released, in which case context
   * switching on multiprocessors is extremely slow/wasteful.  Arena
   * waits just omit the blocking part, and instead cancel. The spin
   * count is empirically chosen to be a value that avoids blocking
   * 99% of the time under maximum sustained exchange rates on a
   * range of test machines. Spins and yields entail some limited
   * randomness (using a cheap xorshift) to avoid regular patterns
   * that can induce unproductive grow/shrink cycles. (Using a
   * pseudorandom also helps regularize spin cycle duration by
   * making branches unpredictable.)  Also, during an offer, a
   * waiter can "know" that it will be released when its slot has
   * changed, but cannot yet proceed until match is set.  In the
   * mean time it cannot cancel the offer, so instead spins/yields.
   * Note: It is possible to avoid this secondary check by changing
   * the linearization point to be a CAS of the match field (as done
   * in one case in the Scott & Scherer DISC paper), which also
   * increases asynchrony a bit, at the expense of poorer collision
   * detection and inability to always reuse per-thread nodes. So
   * the current scheme is typically a better tradeoff.
   *
   * On collisions, indices traverse the arena cyclically in reverse
   * order, restarting at the maximum index (which will tend to be
   * sparsest) when bounds change. (On expirations, indices instead
   * are halved until reaching 0.) It is possible (and has been
   * tried) to use randomized, prime-value-stepped, or double-hash
   * style traversal instead of simple cyclic traversal to reduce
   * bunching.  But empirically, whatever benefits these may have
   * don't overcome their added overhead: We are managing operations
   * that occur very quickly unless there is sustained contention,
   * so simpler/faster control policies work better than more
   * accurate but slower ones.
   *
   * Because we use expiration for arena size control, we cannot
   * throw TimeoutExceptions in the timed version of the public
   * exchange method until the arena size has shrunken to zero (or
   * the arena isn't enabled). This may delay response to timeout
   * but is still within spec.
   *
   * Essentially all of the implementation is in methods
   * slotExchange and arenaExchange. These have similar overall
   * structure, but differ in too many details to combine. The
   * slotExchange method uses the single Exchanger field "slot"
   * rather than arena array elements. However, it still needs
   * minimal collision detection to trigger arena construction.
   * (The messiest part is making sure interrupt status and
   * InterruptedExceptions come out right during transitions when
   * both methods may be called. This is done by using null return
   * as a sentinel to recheck interrupt status.)
   *
   * As is too common in this sort of code, methods are monolithic
   * because most of the logic relies on reads of fields that are
   * maintained as local variables so can't be nicely factored --
   * mainly, here, bulky spin->yield->block/cancel code.  Note that
   * field Node.item is not declared as volatile even though it is
   * read by releasing threads, because they only do so after CAS
   * operations that must precede access, and all uses by the owning
   * thread are otherwise acceptably ordered by other operations.
   * (Because the actual points of atomicity are slot CASes, it
   * would also be legal for the write to Node.match in a release to
   * be weaker than a full volatile write. However, this is not done
   * because it could allow further postponement of the write,
   * delaying progress.)
   */

  /** Per-thread state.
   */
  private final val participant: Participant = new Participant()

  /*
   * Elimination array; null until enabled (within slotExchange).
   * Element accesses use emulation of volatile gets and CAS.
   */
  @volatile private[Exchanger] var arena: Array[Node] = _

  @alwaysinline
  private def arrayGetAtomicRef[E <: AnyRef](
      a: Array[E],
      i: Int
  ): AtomicRef[E] = {
    val elemRef = a.asInstanceOf[ObjectArray].at(i).asInstanceOf[Ptr[E]]
    new AtomicRef[E](elemRef)
  }

  @alwaysinline
  private def arrayCompareAndSet[E <: AnyRef](
      a: Array[E],
      i: Int,
      e: E,
      v: E
  ): Boolean =
    arrayGetAtomicRef(a, i).compareExchangeStrong(e, v)

  @alwaysinline
  private def arrayGetAcquire[E <: AnyRef](a: Array[E], i: Int): E =
    arrayGetAtomicRef(a, i).load(memory_order_acquire)

  /*
   * Slot used until contention detected.
   */
  @volatile private[Exchanger] var slot: Node = _

  @alwaysinline private def atomicSlot = new AtomicRef[Node](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "slot"))
  )

  /*
   * The index of the largest valid arena position, OR'ed with SEQ
   * number in high bits, incremented on each update.  The initial
   * update from 0 to SEQ is used to ensure that the arena array is
   * constructed only once.
   */
  @volatile private[Exchanger] var bound: Int = _

  @alwaysinline private def atomicBound = new AtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "bound"))
  )

  /*
   * Exchange function when arenas enabled. See above for explanation.
   *
   * @param item the (non-null) item to exchange
   * @param timed true if the wait is timed
   * @param ns if timed, the maximum wait time, else 0L
   * @return the other thread's item; or null if interrupted; or
   * TIMED_OUT if timed and timed out
   */
  private final def arenaExchange(
      item: Object,
      timed: Boolean,
      ns: Long
  ): Object = {

    var nanos = ns
    val a = arena
    val alen = a.length
    val p = participant.get() // Probably will need to be var.

    var i = p.index

    while (true) { // access slot at i
      var b = 0
      var m = 0
      var c = 0

      var j = (i << ASHIFT) + ((1 << ASHIFT) - 1)
      if (j < 0 || j >= alen)
        j = alen - 1;
      val q = arrayGetAcquire(a, j)
      if (q != null && arrayCompareAndSet(a, j, q, null)) {
        val v = q.item // release
        q.`match` = item
        val w = q.parked
        if (w != null)
          LockSupport.unpark(w)
        return v
      } else if (i <= { m = { b = bound; b } & MMASK; m } && (q == null)) {
        p.item = item; // offer

        if (arrayCompareAndSet(a, j, null, p)) {
          val end =
            if (timed && m == 0) System.nanoTime() + nanos
            else 0L

          val t = Thread.currentThread() // wait
          var h = p.hash
          var spins = SPINS

          var breakSeen = false
          while (!breakSeen) {
            val v = p.`match`
            if (v != null) {
              p.setMatchRelease(null)
              p.item = null // clear for next use
              p.hash = h
              return v
            } else if (spins > 0) {
              h ^= h << 1; h ^= h >>> 3; h ^= h << 10 // xorshift
              if (h == 0) // initialize hash
                h = SPINS | t.threadId().toInt
              else if (h < 0 && // approx 50% true
                  ({ spins -= 1; spins }
                    & ((SPINS >>> 1) - 1)) == 0)
                Thread.`yield`() // two yields per wait
            } else if (arrayGetAcquire(a, j) != p)
              spins = SPINS // releaser hasn't set match yet
            else if (!t.isInterrupted() && m == 0 &&
                (!timed ||
                { nanos = end - System.nanoTime(); nanos } > 0L)) {
              p.parked = t // minimize window

              if (arrayGetAcquire(a, j) == p) {
                if (nanos == 0L)
                  LockSupport.park(this)
                else
                  LockSupport.parkNanos(this, nanos)
              }
              p.parked = null
            } else if (arrayGetAcquire(a, j) == p &&
                arrayCompareAndSet(a, j, p, null)) {
              if (m != 0) // try to shrink
                atomicBound.compareExchangeStrong(b, b + SEQ - 1)
              p.item = null
              p.hash = h
              i = { p.index >>>= 1; p.index } // descend

              if (Thread.interrupted())
                return null
              if (timed && m == 0 && nanos <= 0L)
                return TIMED_OUT

              breakSeen = true // expired; restart
            }
          }
        } else
          p.item = null // clear offer
      } else {
        if (p.bound != b) { // stale; reset
          p.bound = b
          p.collides = 0
          i =
            if (i != m || m == 0) m
            else m - 1
        } else if ({ c = p.collides; c } < m || m == FULL ||
            atomicBound.compareExchangeStrong(b, b + SEQ + 1)) {

          p.collides = c + 1
          i =
            if (i == 0) m
            else i - 1 // cyclically traverse
        } else
          i = m + 1 // grow

        p.index = i
      }
    }

    null // Should never get here
  }

  /*
   * Exchange function used until arenas enabled. See above for explanation.
   *
   * @param item the item to exchange
   * @param timed true if the wait is timed
   * @param ns if timed, the maximum wait time, else 0L
   * @return the other thread's item; or null if either the arena
   * was enabled or the thread was interrupted before completion; or
   * TIMED_OUT if timed and timed out
   */
  private final def slotExchange(
      item: Object,
      timed: Boolean,
      ns: Long
  ): Object = {
    var nanos = ns

    val p = participant.get()
    val t = Thread.currentThread()
    if (t.isInterrupted()) // preserve interrupt status so caller can recheck
      return null

    var q: Node = null
    var breakSeen = false

    while (!breakSeen) {
      if ({ q = slot; q } != null) {
        if (atomicSlot.compareExchangeStrong(q, null)) {
          val v = q.item
          q.`match` = item
          val w = q.parked
          if (w != null)
            LockSupport.unpark(w)
          return v
        }

        // create arena on contention, but continue until slot null
        if (NCPU > 1 && bound == 0 &&
            atomicBound.compareExchangeStrong(0, SEQ)) {
          arena = new Array[Node]({ (FULL + 2) << ASHIFT })
        }
      } else if (arena != null) {
        return null // caller must reroute to arenaExchange
      } else {
        p.item = item
        if (atomicSlot.compareExchangeStrong(null.asInstanceOf[Node], p))
          breakSeen = true
        else
          p.item = null
      }
    }

    // await release
    var h = p.hash

    val end =
      if (timed) System.nanoTime() + nanos
      else 0L

    var spins =
      if (NCPU > 1) SPINS
      else 1

    var v: Object = null

    breakSeen = false

    while (!breakSeen && { v = p.`match`; v } == null) {
      if (spins > 0) {
        h ^= h << 1; h ^= h >>> 3; h ^= h << 10
        if (h == 0)
          h = SPINS | t.threadId().toInt
        else if (h < 0 && ({ spins -= 1; spins } & ((SPINS >>> 1) - 1)) == 0)
          Thread.`yield`()
      } else if (slot != p)
        spins = SPINS
      else if (!t.isInterrupted() && arena == null &&
          (!timed ||
          { nanos = end - System.nanoTime(); nanos } > 0L)) {
        p.parked = t
        if (slot == p) {
          if (nanos == 0L)
            LockSupport.park(this)
          else
            LockSupport.parkNanos(this, nanos)
        }
        p.parked = null
      } else if (atomicSlot.compareExchangeStrong(p, null)) {
        v =
          if (timed && nanos <= 0L && !t.isInterrupted()) TIMED_OUT
          else null

        breakSeen = true
      }
    }

    p.setMatchRelease(null)
    p.item = null
    p.hash = h
    v
  }

  /*
   * Waits for another thread to arrive at this exchange point (unless
   * the current thread is {@linkplain Thread#interrupt interrupted}),
   * and then transfers the given object to it, receiving its object
   * in return.
   *
   * <p>If another thread is already waiting at the exchange point then
   * it is resumed for thread scheduling purposes and receives the object
   * passed in by the current thread.  The current thread returns immediately,
   * receiving the object passed to the exchange by that other thread.
   *
   * <p>If no other thread is already waiting at the exchange then the
   * current thread is disabled for thread scheduling purposes and lies
   * dormant until one of two things happens:
   * <ul>
   * <li>Some other thread enters the exchange; or
   * <li>Some other thread {@linkplain Thread#interrupt interrupts}
   * the current thread.
   * </ul>
   * <p>If the current thread:
   * <ul>
   * <li>has its interrupted status set on entry to this method; or
   * <li>is {@linkplain Thread#interrupt interrupted} while waiting
   * for the exchange,
   * </ul>
   * then {@link InterruptedException} is thrown and the current thread's
   * interrupted status is cleared.
   *
   * @param x the object to exchange
   * @return the object provided by the other thread
   * @throws InterruptedException if the current thread was
   *         interrupted while waiting
   */

  def exchange(x: V): V = {
    var v: Object = null

    val item =
      if (x == null) NULL_ITEM
      else x // translate null args

    if ((arena != null ||
          ({ v = slotExchange(item, false, 0L); v }) == null) &&
        (Thread.interrupted() ||
        ({ v = arenaExchange(item, false, 0L); v }) == null))
      throw new InterruptedException()

    val result =
      if (v == NULL_ITEM) null
      else v

    result.asInstanceOf[V]
  }

  /*
   * Waits for another thread to arrive at this exchange point (unless
   * the current thread is {@linkplain Thread#interrupt interrupted} or
   * the specified waiting time elapses), and then transfers the given
   * object to it, receiving its object in return.
   *
   * <p>If another thread is already waiting at the exchange point then
   * it is resumed for thread scheduling purposes and receives the object
   * passed in by the current thread.  The current thread returns immediately,
   * receiving the object passed to the exchange by that other thread.
   *
   * <p>If no other thread is already waiting at the exchange then the
   * current thread is disabled for thread scheduling purposes and lies
   * dormant until one of three things happens:
   * <ul>
   * <li>Some other thread enters the exchange; or
   * <li>Some other thread {@linkplain Thread#interrupt interrupts}
   * the current thread; or
   * <li>The specified waiting time elapses.
   * </ul>
   * <p>If the current thread:
   * <ul>
   * <li>has its interrupted status set on entry to this method; or
   * <li>is {@linkplain Thread#interrupt interrupted} while waiting
   * for the exchange,
   * </ul>
   * then {@link InterruptedException} is thrown and the current thread's
   * interrupted status is cleared.
   *
   * <p>If the specified waiting time elapses then {@link
   * TimeoutException} is thrown.  If the time is less than or equal
   * to zero, the method will not wait at all.
   *
   * @param x the object to exchange
   * @param timeout the maximum time to wait
   * @param unit the time unit of the {@code timeout} argument
   * @return the object provided by the other thread
   * @throws InterruptedException if the current thread was
   *         interrupted while waiting
   * @throws TimeoutException if the specified waiting time elapses
   *         before another thread enters the exchange
   */
  def exchange(x: V, timeout: Long, unit: TimeUnit): V = {
    var v: Object = null

    val item =
      if (x == null) NULL_ITEM
      else x

    val ns = unit.toNanos(timeout)

    if ((arena != null ||
          ({ v = slotExchange(item, true, ns); v }) == null) &&
        (Thread.interrupted() ||
        ({ v = arenaExchange(item, true, ns); v } == null)))
      throw new InterruptedException()

    if (v == TIMED_OUT)
      throw new TimeoutException()

    val result =
      if (v == NULL_ITEM) null
      else v

    result.asInstanceOf[V]
  }
}
