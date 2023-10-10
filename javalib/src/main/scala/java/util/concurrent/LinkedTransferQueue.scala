/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.{AbstractQueue, Collection, Iterator, Spliterator, function}
import java.util.{Arrays, Objects, Spliterators}
import java.util.{NoSuchElementException}
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport
import scala.scalanative.libc.stdatomic.AtomicRef
import scala.scalanative.libc.stdatomic.memory_order.{
  memory_order_relaxed,
  memory_order_release
}
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}

/** An unbounded {@link TransferQueue} based on linked nodes. This queue orders
 *  elements FIFO (first-in-first-out) with respect to any given producer. The
 *  <em>head</em> of the queue is that element that has been on the queue the
 *  longest time for some producer. The <em>tail</em> of the queue is that
 *  element that has been on the queue the shortest time for some producer.
 *
 *  <p>Beware that, unlike in most collections, the {@code size} method is
 *  <em>NOT</em> a constant-time operation. Because of the asynchronous nature
 *  of these queues, determining the current number of elements requires a
 *  traversal of the elements, and so may report inaccurate results if this
 *  collection is modified during traversal.
 *
 *  <p>Bulk operations that add, remove, or examine multiple elements, such as
 *  {@link #addAll}, {@link #removeIf} or {@link #forEach}, are <em>not</em>
 *  guaranteed to be performed atomically. For example, a {@code forEach}
 *  traversal concurrent with an {@code addAll} operation might observe only
 *  some of the added elements.
 *
 *  <p>This class and its iterator implement all of the <em>optional</em>
 *  methods of the {@link Collection} and {@link Iterator} interfaces.
 *
 *  <p>Memory consistency effects: As with other concurrent collections, actions
 *  in a thread prior to placing an object into a {@code LinkedTransferQueue} <a
 *  href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 *  actions subsequent to the access or removal of that element from the {@code
 *  LinkedTransferQueue} in another thread.
 *
 *  <p>This class is a member of the <a
 *  href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework">
 *  Java Collections Framework</a>.
 *
 *  @since 1.7
 *  @author
 *    Doug Lea
 *  @param [E]
 *    the type of elements held in this queue
 */
@SerialVersionUID(-3223113410248163686L) class LinkedTransferQueue[E <: AnyRef]
    extends AbstractQueue[E]
    with TransferQueue[E]
    with Serializable {
  import LinkedTransferQueue._

  /*
   * *** Overview of Dual Queues with Slack ***
   *
   * Dual Queues, introduced by Scherer and Scott
   * (http://www.cs.rochester.edu/~scott/papers/2004_DISC_dual_DS.pdf)
   * are (linked) queues in which nodes may represent either data or
   * requests.  When a thread tries to enqueue a data node, but
   * encounters a request node, it instead "matches" and removes it;
   * and vice versa for enqueuing requests. Blocking Dual Queues
   * arrange that threads enqueuing unmatched requests block until
   * other threads provide the match. Dual Synchronous Queues (see
   * Scherer, Lea, & Scott
   * http://www.cs.rochester.edu/u/scott/papers/2009_Scherer_CACM_SSQ.pdf)
   * additionally arrange that threads enqueuing unmatched data also
   * block.  Dual Transfer Queues support all of these modes, as
   * dictated by callers.
   *
   * A FIFO dual queue may be implemented using a variation of the
   * Michael & Scott (M&S) lock-free queue algorithm
   * (http://www.cs.rochester.edu/~scott/papers/1996_PODC_queues.pdf).
   * It maintains two pointer fields, "head", pointing to a
   * (matched) node that in turn points to the first actual
   * (unmatched) queue node (or null if empty); and "tail" that
   * points to the last node on the queue (or again null if
   * empty). For example, here is a possible queue with four data
   * elements:
   *
   *  head                tail
   *    |                   |
   *    v                   v
   *    M -> U -> U -> U -> U
   *
   * The M&S queue algorithm is known to be prone to scalability and
   * overhead limitations when maintaining (via CAS) these head and
   * tail pointers. This has led to the development of
   * contention-reducing variants such as elimination arrays (see
   * Moir et al http://portal.acm.org/citation.cfm?id=1074013) and
   * optimistic back pointers (see Ladan-Mozes & Shavit
   * http://people.csail.mit.edu/edya/publications/OptimisticFIFOQueue-journal.pdf).
   * However, the nature of dual queues enables a simpler tactic for
   * improving M&S-style implementations when dual-ness is needed.
   *
   * In a dual queue, each node must atomically maintain its match
   * status. While there are other possible variants, we implement
   * this here as: for a data-mode node, matching entails CASing an
   * "item" field from a non-null data value to null upon match, and
   * vice-versa for request nodes, CASing from null to a data
   * value. (Note that the linearization properties of this style of
   * queue are easy to verify -- elements are made available by
   * linking, and unavailable by matching.) Compared to plain M&S
   * queues, this property of dual queues requires one additional
   * successful atomic operation per enq/deq pair. But it also
   * enables lower cost variants of queue maintenance mechanics. (A
   * variation of this idea applies even for non-dual queues that
   * support deletion of interior elements, such as
   * j.u.c.ConcurrentLinkedQueue.)
   *
   * Once a node is matched, its match status can never again
   * change.  We may thus arrange that the linked list of them
   * contain a prefix of zero or more matched nodes, followed by a
   * suffix of zero or more unmatched nodes. (Note that we allow
   * both the prefix and suffix to be zero length, which in turn
   * means that we do not use a dummy header.)  If we were not
   * concerned with either time or space efficiency, we could
   * correctly perform enqueue and dequeue operations by traversing
   * from a pointer to the initial node; CASing the item of the
   * first unmatched node on match and CASing the next field of the
   * trailing node on appends.  While this would be a terrible idea
   * in itself, it does have the benefit of not requiring ANY atomic
   * updates on head/tail fields.
   *
   * We introduce here an approach that lies between the extremes of
   * never versus always updating queue (head and tail) pointers.
   * This offers a tradeoff between sometimes requiring extra
   * traversal steps to locate the first and/or last unmatched
   * nodes, versus the reduced overhead and contention of fewer
   * updates to queue pointers. For example, a possible snapshot of
   * a queue is:
   *
   *  head           tail
   *    |              |
   *    v              v
   *    M -> M -> U -> U -> U -> U
   *
   * The best value for this "slack" (the targeted maximum distance
   * between the value of "head" and the first unmatched node, and
   * similarly for "tail") is an empirical matter. We have found
   * that using very small constants in the range of 1-3 work best
   * over a range of platforms. Larger values introduce increasing
   * costs of cache misses and risks of long traversal chains, while
   * smaller values increase CAS contention and overhead.
   *
   * Dual queues with slack differ from plain M&S dual queues by
   * virtue of only sometimes updating head or tail pointers when
   * matching, appending, or even traversing nodes; in order to
   * maintain a targeted slack.  The idea of "sometimes" may be
   * operationalized in several ways. The simplest is to use a
   * per-operation counter incremented on each traversal step, and
   * to try (via CAS) to update the associated queue pointer
   * whenever the count exceeds a threshold. Another, that requires
   * more overhead, is to use random number generators to update
   * with a given probability per traversal step.
   *
   * In any strategy along these lines, because CASes updating
   * fields may fail, the actual slack may exceed targeted slack.
   * However, they may be retried at any time to maintain targets.
   * Even when using very small slack values, this approach works
   * well for dual queues because it allows all operations up to the
   * point of matching or appending an item (hence potentially
   * allowing progress by another thread) to be read-only, thus not
   * introducing any further contention.  As described below, we
   * implement this by performing slack maintenance retries only
   * after these points.
   *
   * As an accompaniment to such techniques, traversal overhead can
   * be further reduced without increasing contention of head
   * pointer updates: Threads may sometimes shortcut the "next" link
   * path from the current "head" node to be closer to the currently
   * known first unmatched node, and similarly for tail. Again, this
   * may be triggered with using thresholds or randomization.
   *
   * These ideas must be further extended to avoid unbounded amounts
   * of costly-to-reclaim garbage caused by the sequential "next"
   * links of nodes starting at old forgotten head nodes: As first
   * described in detail by Boehm
   * (http://portal.acm.org/citation.cfm?doid=503272.503282), if a GC
   * delays noticing that any arbitrarily old node has become
   * garbage, all newer dead nodes will also be unreclaimed.
   * (Similar issues arise in non-GC environments.)  To cope with
   * this in our implementation, upon CASing to advance the head
   * pointer, we set the "next" link of the previous head to point
   * only to itself; thus limiting the length of chains of dead nodes.
   * (We also take similar care to wipe out possibly garbage
   * retaining values held in other Node fields.)  However, doing so
   * adds some further complexity to traversal: If any "next"
   * pointer links to itself, it indicates that the current thread
   * has lagged behind a head-update, and so the traversal must
   * continue from the "head".  Traversals trying to find the
   * current tail starting from "tail" may also encounter
   * self-links, in which case they also continue at "head".
   *
   * It is tempting in slack-based scheme to not even use CAS for
   * updates (similarly to Ladan-Mozes & Shavit). However, this
   * cannot be done for head updates under the above link-forgetting
   * mechanics because an update may leave head at a detached node.
   * And while direct writes are possible for tail updates, they
   * increase the risk of long retraversals, and hence long garbage
   * chains, which can be much more costly than is worthwhile
   * considering that the cost difference of performing a CAS vs
   * write is smaller when they are not triggered on each operation
   * (especially considering that writes and CASes equally require
   * additional GC bookkeeping ("write barriers") that are sometimes
   * more costly than the writes themselves because of contention).
   *
   * *** Overview of implementation ***
   *
   * We use a threshold-based approach to updates, with a slack
   * threshold of two -- that is, we update head/tail when the
   * current pointer appears to be two or more steps away from the
   * first/last node. The slack value is hard-wired: a path greater
   * than one is naturally implemented by checking equality of
   * traversal pointers except when the list has only one element,
   * in which case we keep slack threshold at one. Avoiding tracking
   * explicit counts across method calls slightly simplifies an
   * already-messy implementation. Using randomization would
   * probably work better if there were a low-quality dirt-cheap
   * per-thread one available, but even ThreadLocalRandom is too
   * heavy for these purposes.
   *
   * With such a small slack threshold value, it is not worthwhile
   * to augment this with path short-circuiting (i.e., unsplicing
   * interior nodes) except in the case of cancellation/removal (see
   * below).
   *
   * All enqueue/dequeue operations are handled by the single method
   * "xfer" with parameters indicating whether to act as some form
   * of offer, put, poll, take, or transfer (each possibly with
   * timeout). The relative complexity of using one monolithic
   * method outweighs the code bulk and maintenance problems of
   * using separate methods for each case.
   *
   * Operation consists of up to two phases. The first is implemented
   * in method xfer, the second in method awaitMatch.
   *
   * 1. Traverse until matching or appending (method xfer)
   *
   *    Conceptually, we simply traverse all nodes starting from head.
   *    If we encounter an unmatched node of opposite mode, we match
   *    it and return, also updating head (by at least 2 hops) to
   *    one past the matched node (or the node itself if it's the
   *    pinned trailing node).  Traversals also check for the
   *    possibility of falling off-list, in which case they restart.
   *
   *    If the trailing node of the list is reached, a match is not
   *    possible.  If this call was untimed poll or tryTransfer
   *    (argument "how" is NOW), return empty-handed immediately.
   *    Else a new node is CAS-appended.  On successful append, if
   *    this call was ASYNC (e.g. offer), an element was
   *    successfully added to the end of the queue and we return.
   *
   *    Of course, this naive traversal is O(n) when no match is
   *    possible.  We optimize the traversal by maintaining a tail
   *    pointer, which is expected to be "near" the end of the list.
   *    It is only safe to fast-forward to tail (in the presence of
   *    arbitrary concurrent changes) if it is pointing to a node of
   *    the same mode, even if it is dead (in this case no preceding
   *    node could still be matchable by this traversal).  If we
   *    need to restart due to falling off-list, we can again
   *    fast-forward to tail, but only if it has changed since the
   *    last traversal (else we might loop forever).  If tail cannot
   *    be used, traversal starts at head (but in this case we
   *    expect to be able to match near head).  As with head, we
   *    CAS-advance the tail pointer by at least two hops.
   *
   * 2. Await match or cancellation (method awaitMatch)
   *
   *    Wait for another thread to match node; instead cancelling if
   *    the current thread was interrupted or the wait timed out. To
   *    improve performance in common single-source / single-sink
   *    usages when there are more tasks than cores, an initial
   *    Thread.yield is tried when there is apparently only one
   *    waiter.  In other cases, waiters may help with some
   *    bookkeeping, then park/unpark.
   *
   * ** Unlinking removed interior nodes **
   *
   * In addition to minimizing garbage retention via self-linking
   * described above, we also unlink removed interior nodes. These
   * may arise due to timed out or interrupted waits, or calls to
   * remove(x) or Iterator.remove.  Normally, given a node that was
   * at one time known to be the predecessor of some node s that is
   * to be removed, we can unsplice s by CASing the next field of
   * its predecessor if it still points to s (otherwise s must
   * already have been removed or is now offlist). But there are two
   * situations in which we cannot guarantee to make node s
   * unreachable in this way: (1) If s is the trailing node of list
   * (i.e., with null next), then it is pinned as the target node
   * for appends, so can only be removed later after other nodes are
   * appended. (2) We cannot necessarily unlink s given a
   * predecessor node that is matched (including the case of being
   * cancelled): the predecessor may already be unspliced, in which
   * case some previous reachable node may still point to s.
   * (For further explanation see Herlihy & Shavit "The Art of
   * Multiprocessor Programming" chapter 9).  Although, in both
   * cases, we can rule out the need for further action if either s
   * or its predecessor are (or can be made to be) at, or fall off
   * from, the head of list.
   *
   * Without taking these into account, it would be possible for an
   * unbounded number of supposedly removed nodes to remain reachable.
   * Situations leading to such buildup are uncommon but can occur
   * in practice; for example when a series of short timed calls to
   * poll repeatedly time out at the trailing node but otherwise
   * never fall off the list because of an untimed call to take() at
   * the front of the queue.
   *
   * When these cases arise, rather than always retraversing the
   * entire list to find an actual predecessor to unlink (which
   * won't help for case (1) anyway), we record the need to sweep the
   * next time any thread would otherwise block in awaitMatch. Also,
   * because traversal operations on the linked list of nodes are a
   * natural opportunity to sweep dead nodes, we generally do so,
   * including all the operations that might remove elements as they
   * traverse, such as removeIf and Iterator.remove.  This largely
   * eliminates long chains of dead interior nodes, except from
   * cancelled or timed out blocking operations.
   *
   * Note that we cannot self-link unlinked interior nodes during
   * sweeps. However, the associated garbage chains terminate when
   * some successor ultimately falls off the head of the list and is
   * self-linked.
   */

  /** A node from which the first live (non-matched) node (if any) can be
   *  reached in O(1) time. Invariants:
   *    - all live nodes are reachable from head via .next
   *    - head != null
   *    - (tmp = head).next != tmp || tmp != head Non-invariants:
   *    - head may or may not be live
   *    - it is permitted for tail to lag behind head, that is, for tail to not
   *      be reachable from head!
   */
  @volatile private[concurrent] var head: Node = _

  /** A node from which the last node on list (that is, the unique node with
   *  node.next == null) can be reached in O(1) time. Invariants:
   *    - the last node is always reachable from tail via .next
   *    - tail != null Non-invariants:
   *    - tail may or may not be live
   *    - it is permitted for tail to lag behind head, that is, for tail to not
   *      be reachable from head!
   *    - tail.next may or may not be self-linked.
   */
  @volatile private[concurrent] var tail: Node = _

  /** The number of apparent failures to unsplice cancelled nodes */
  @volatile private[concurrent] var needSweep: Boolean = _

  private val tailAtomic = new AtomicRef[Node](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "tail"))
  )
  private val headAtomic = new AtomicRef[Node](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "head"))
  )

  private def casTail(cmp: Node, `val`: Node) =
    tailAtomic.compareExchangeStrong(cmp, `val`)
  private def casHead(cmp: Node, `val`: Node) =
    headAtomic.compareExchangeStrong(cmp, `val`)

  /** Tries to CAS pred.next (or head, if pred is null) from c to p. Caller must
   *  ensure that we're not unlinking the trailing node.
   */
  private def tryCasSuccessor(pred: Node, c: Node, p: Node) = {
    if (pred != null) {
      pred.casNext(c, p)
    } else if (casHead(c, p)) {
      c.selfLink()
      true
    } else {
      false
    }
  }

  /** Collapses dead (matched) nodes between pred and q.
   *  @param pred
   *    the last known live node, or null if none
   *  @param c
   *    the first dead node
   *  @param p
   *    the last dead node
   *  @param q
   *    p.next: the next live node, or null if at end
   *  @return
   *    pred if pred still alive and CAS succeeded; else p
   */
  private def skipDeadNodes(pred: Node, c: Node, p: Node, q: Node): Node = {
    var _q = q
    if (_q == null) {
      // Never unlink trailing node.
      if (c == p) return pred
      _q = p;
    }
    if (tryCasSuccessor(pred, c, _q) && (pred == null || !pred.isMatched()))
      pred
    else p
  }

  /** Collapses dead (matched) nodes from h (which was once head) to p. Caller
   *  ensures all nodes from h up to and including p are dead.
   */
  private def skipDeadNodesNearHead(h: Node, p: Node): Unit = {
    var _p = p
    var continueLoop = true
    while (continueLoop) {
      val q = _p.next
      if (q == null) continueLoop = false
      else if (!q.isMatched()) { _p = q; continueLoop = false }
      else {
        if (_p == q) return
        _p = q
      }
    }
    if (casHead(h, _p))
      h.selfLink()
  }

  /** Implements all queuing methods. See above for explanation.
   *
   *  @param e
   *    the item or null for take
   *  @param haveData
   *    true if this is a put, else a take
   *  @param how
   *    NOW, ASYNC, SYNC, or TIMED
   *  @param nanos
   *    timeout in nanosecs, used only if mode is TIMED
   *  @return
   *    an item if matched, else e
   *  @throws NullPointerException
   *    if haveData mode but e is null
   */
  private def xfer(e: E, haveData: Boolean, how: Int, nanos: Long): E = {
    if (haveData && (e == null))
      throw new NullPointerException()

    var restart = true
    var s: Node = null
    var t: Node = null
    var h: Node = null
    while (true) {
      val old_t = t
      t = tail
      var innerBreak = false
      var p = if (old_t != t && t.isData == haveData) t else { h = head; h }
      while (!innerBreak) {
        var skipRest = false
        var item: Object = null
        if (p.isData != haveData && haveData == {
              item = p.item; item == null
            }) {
          if (h == null) h = head
          if (p.tryMatch(item, e)) {
            if (h != p) skipDeadNodesNearHead(h, p)
            return item.asInstanceOf[E]
          }
        }
        val q = p.next
        if (q == null) {
          if (how == NOW) return e
          if (s == null) s = new Node(e)
          if (!p.casNext(null, s)) skipRest = true
          if (!skipRest) {
            if (p != t) casTail(t, s)
            if (how == ASYNC) return e
            return awaitMatch(s, p, e, (how == TIMED), nanos)
          }
        }
        if (!skipRest) {
          val old_p = p
          p = q
          if (old_p == p) innerBreak = true
        }
      }
    }
    ???
  }

  /** Possibly blocks until node s is matched or caller gives up.
   *
   *  @param s
   *    the waiting node
   *  @param pred
   *    the predecessor of s, or null if unknown (the null case does not occur
   *    in any current calls but may in possible future extensions)
   *  @param e
   *    the comparison value for checking match
   *  @param timed
   *    if true, wait only until timeout elapses
   *  @param nanos
   *    timeout in nanosecs, used only if timed is true
   *  @return
   *    matched item, or e if unmatched on interrupt or timeout
   */
  private def awaitMatch(
      s: Node,
      pred: Node,
      e: E,
      timed: Boolean,
      nanos: Long
  ): E = {
    var _nanos = nanos
    val isData = s.isData
    val deadline = if (timed) System.nanoTime() + _nanos else 0
    val w = Thread.currentThread()
    var stat = -1
    var item: Object = s.item
    var continueLoop = true
    while (item == e && continueLoop) {
      if (needSweep) sweep()
      else if ((timed && _nanos <= 0) || w.isInterrupted()) {
        if (s.casItem(e, if (e == null) s else null)) {
          unsplice(pred, s) // cancelled
          return e
        }
      } else if (stat <= 0) {
        if (pred != null && pred.next == s) {
          if (stat < 0 &&
              (pred.isData != isData || pred.isMatched())) {
            stat = 0 // yield once if first
            Thread.`yield`()
          } else {
            stat = 1
            s.waiter = w // enable unpark
          }
        } // else signal in progress
      } else if ({ item = s.item; item != e }) {
        continueLoop = false
      } else if (!timed) {
        LockSupport.setCurrentBlocker(this)
        try {
          ForkJoinPool.managedBlock(s)
        } catch {
          case cannotHappen: InterruptedException => {}
        }
        LockSupport.setCurrentBlocker(null)
      } else {
        _nanos = deadline - System.nanoTime()
        if (_nanos > SPIN_FOR_TIMEOUT_THRESHOLD)
          LockSupport.parkNanos(this, _nanos)
      }

      item = s.item
    }
    if (stat == 1)
      s.waiterAtomic.store(null)
    if (!isData)
      s.itemAtomic.store(s) // self-link to avoid garbage
    item.asInstanceOf[E]
  }

  /* -------------- Traversal methods -------------- */

  /** Returns the first unmatched data node, or null if none. Callers must
   *  recheck if the returned node is unmatched before using.
   */
  final def firstDataNode(): Node = {
    var first: Node = null
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false

      var h = head
      var p = h
      var innerBreak = false
      while (p != null && !innerBreak) {
        if (p.item != null) {
          if (p.isData) {
            first = p
            innerBreak = true
          }
        } else if (!p.isData) {
          innerBreak = true
        }
        if (!innerBreak) {
          val q = p.next
          if (q == null) innerBreak = true
          if (!innerBreak && p == q) {
            restartFromHead = true; innerBreak = true
          }
          if (!innerBreak) p = q
        }
      }
    }
    first
  }

  /** Traverses and counts unmatched nodes of the given mode. Used by methods
   *  size and getWaitingConsumerCount.
   */
  private def countOfMode(data: Boolean): Int = {
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false
      var count = 0
      var p = head
      var innerBreak = false
      while (p != null && !innerBreak) {
        if (!p.isMatched()) {
          if (p.isData != data)
            return 0
          count += 1
          if (count == Integer.MAX_VALUE)
            innerBreak = true // @see Collection.size()
        }
        if (!innerBreak) {
          val q = p.next
          if (p == q) {
            innerBreak = true
            restartFromHead = true
          } else
            p = q
        }
      }
      if (!restartFromHead) return count
    }
    ???
  }

  override def toString(): String = {
    var a: Array[String] = null

    var restartFromHead = true;
    while (restartFromHead) {
      restartFromHead = false

      var charLength = 0
      var size = 0

      var p = head
      var innerBreak = false
      while (p != null && !innerBreak) {
        val item = p.item
        if (p.isData) {
          if (item != null) {
            if (a == null)
              a = Array.fill(4)("")
            else if (size == a.length)
              a = Arrays.copyOf(a, 2 * size)
            val s = item.toString()
            a(size) = s
            size += 1
            charLength += s.length()
          }
        } else if (item == null) {
          innerBreak = true
        }
        if (!innerBreak) {
          val q = p.next
          if (p == q) {
            restartFromHead = true
            innerBreak = true
          } else p = q
        }
      }

      if (!restartFromHead) {
        if (size == 0)
          return "[]"

        return Helpers.toString(a.asInstanceOf[Array[AnyRef]], size, charLength)
      }
    }
    ???
  }

  private def toArrayInternal(a: Array[Object]): Array[Object] = {
    var x = a

    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false

      var size = 0

      var p = head
      var innerBreak = false
      while (p != null && !innerBreak) {
        val item = p.item
        if (p.isData) {
          if (item != null) {
            if (x == null)
              x = Array.fill[Object](4)(null)
            else if (size == x.length)
              x = Arrays.copyOf(x, 2 * (size + 4))
            x(size) = item
            size += 1
          }
        } else if (item == null) {
          innerBreak = true
        }
        if (!innerBreak) {
          val q = p.next
          if (p == q) {
            restartFromHead = true
            innerBreak = true
          }
          p = q
        }
      }
      if (!restartFromHead) {
        if (x == null)
          return Array()
        else if (a != null && size <= a.length) {
          if (a != x)
            System.arraycopy(x, 0, a, 0, size)
          if (size < a.length)
            a(size) = null
          return a
        }
        return (if (size == x.length) x else Arrays.copyOf(x, size))
      }
    }
    ???
  }

  /** Returns an array containing all of the elements in this queue, in proper
   *  sequence.
   *
   *  <p>The returned array will be "safe" in that no references to it are
   *  maintained by this queue. (In other words, this method must allocate a new
   *  array). The caller is thus free to modify the returned array.
   *
   *  <p>This method acts as bridge between array-based and collection-based
   *  APIs.
   *
   *  @return
   *    an array containing all of the elements in this queue
   */
  override def toArray(): Array[Object] = toArrayInternal(null)

  /** Returns an array containing all of the elements in this queue, in proper
   *  sequence; the runtime type of the returned array is that of the specified
   *  array. If the queue fits in the specified array, it is returned therein.
   *  Otherwise, a new array is allocated with the runtime type of the specified
   *  array and the size of this queue.
   *
   *  <p>If this queue fits in the specified array with room to spare (i.e., the
   *  array has more elements than this queue), the element in the array
   *  immediately following the end of the queue is set to {@code null}.
   *
   *  <p>Like the {@link #toArray()} method, this method acts as bridge between
   *  array-based and collection-based APIs. Further, this method allows precise
   *  control over the runtime type of the output array, and may, under certain
   *  circumstances, be used to save allocation costs.
   *
   *  <p>Suppose {@code x} is a queue known to contain only strings. The
   *  following code can be used to dump the queue into a newly allocated array
   *  of {@code String}:
   *
   *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
   *
   *  Note that {@code toArray(new Object[0])} is identical in function to
   *  {@code toArray()}.
   *
   *  @param a
   *    the array into which the elements of the queue are to be stored, if it
   *    is big enough; otherwise, a new array of the same runtime type is
   *    allocated for this purpose
   *  @return
   *    an array containing all of the elements in this queue
   *  @throws ArrayStoreException
   *    if the runtime type of the specified array is not a supertype of the
   *    runtime type of every element in this queue
   *  @throws NullPointerException
   *    if the specified array is null
   */
  override def toArray[T <: AnyRef](
      a: Array[T]
  ): Array[T] = {
    java.util.Objects.requireNonNull(a)
    toArrayInternal(a.asInstanceOf[Array[Object]])
      .asInstanceOf[Array[T]]
  }

  /** Weakly-consistent iterator.
   *
   *  Lazily updated ancestor is expected to be amortized O(1) remove(), but
   *  O(n) in the worst case, when lastRet is concurrently deleted.
   */
  final class Itr extends Iterator[E] {
    private var nextNode: Node = null // next node to return item for)
    private var nextItem: E = null.asInstanceOf[E] // the corresponding item)
    private var lastRet: Node = null // last returned node, to support remove)
    private var ancestor: Node = null // Helps unlink lastRet on remove())

    /** Moves to next node after pred, or first node if pred null.
     */
    private def advance(pred: Node): Unit = {
      var _pred = pred
      var p = if (_pred == null) head else _pred.next
      var c = p
      var innerBreak = false
      while (p != null && !innerBreak) {
        val item = p.item
        if (item != null && p.isData) {
          nextNode = p
          nextItem = item.asInstanceOf[E]
          if (c != p)
            tryCasSuccessor(_pred, c, p)
          return
        } else if (!p.isData && item == null) {
          innerBreak = true
        }
        if (!innerBreak) {
          if (c != p && {
                val old_c = c
                c = p
                !tryCasSuccessor(_pred, old_c, c)
              }) {
            _pred = p
            p = p.next
            c = p
          } else {
            val q = p.next
            if (p == q) {
              _pred = null
              p = head
              c = p
            } else {
              p = q
            }
          }
        }
      }
      nextItem = null.asInstanceOf[E]
      nextNode = null
    }

    advance(null)

    final override def hasNext(): Boolean = nextNode != null

    final override def next() = {
      var p = nextNode
      if (p == null)
        throw new NoSuchElementException()
      val e = nextItem
      lastRet = p
      advance(lastRet)
      e
    }

    override def forEachRemaining(
        action: function.Consumer[_ >: E]
    ): Unit = {
      Objects.requireNonNull(action)
      var q: Node = null
      var p = nextNode
      while (p != null) {
        action.accept(nextItem)
        q = p
        advance(q)
        p = nextNode
      }
      if (q != null)
        lastRet = q
    }

    override def remove(): Unit = {
      val lastRet = this.lastRet
      if (lastRet == null)
        throw new IllegalStateException()
      this.lastRet = null
      if (lastRet.item == null)
        return

      var pred = ancestor
      var p = if (pred == null) head else pred.next
      var c = p
      var q: Node = null
      var innerBreak = false
      while (p != null && !innerBreak) {
        if (p == lastRet) {
          val item = p.item
          if (item != null)
            p.tryMatch(item, null)
          q = p.next
          if (q == null) q = p
          if (c != q)
            tryCasSuccessor(pred, c, q)
          ancestor = pred
          return
        }
        val item = p.item
        val pAlive = item != null && p.isData
        if (pAlive) {
          // exceptionally, nothing to do
        } else if (!p.isData && item == null) {
          innerBreak = true
        }
        if (!innerBreak) {
          if ((c != p && {
                val old_c = c
                c = p
                !tryCasSuccessor(pred, old_c, c)
              }) || pAlive) {
            pred = p
            p = p.next
            c = p
          } else {
            val q = p.next
            if (p == q) {
              pred = null
              p = head
              c = p
            } else {
              p = q
            }
          }
        }
      }
    }
  }

  /** A customized variant of Spliterators.IteratorSpliterator */
  final class LTQSpliterator extends Spliterator[E] {
    var _current: Node = null
    var batch = 0
    var exhausted = false

    def trySplit(): Spliterator[E] = {
      var p = current()
      if (p == null) return null
      var q = p.next
      if (q == null) return null

      var i = 0
      batch = Math.min(batch + 1, LTQSpliterator.MAX_BATCH)
      var n = batch
      var a: Array[Object] = null
      var continueLoop = true
      while (continueLoop) {
        val item = p.item
        if (p.isData) {
          if (item != null) {
            if (a == null)
              a = Array.fill[Object](n)(null)
            a(i) = item
            i += 1
          }
        } else if (item == null) {
          p = null
          continueLoop = false
        }
        if (continueLoop) {
          if (p == q)
            p = firstDataNode()
          else
            p = q
        }
        if (p == null) continueLoop = false
        if (continueLoop) {
          q = p.next
          if (q == null) continueLoop = false
        }
        if (i >= n) continueLoop = false
      }
      setCurrent(p)
      if (i == 0)
        null
      else
        Spliterators.spliterator(
          a,
          0,
          i,
          (Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT)
        )
    }

    override def forEachRemaining(
        action: function.Consumer[_ >: E]
    ): Unit = {
      Objects.requireNonNull(action)
      val p = current()
      if (p != null) {
        _current = null
        exhausted = true
        forEachFrom(action, p)
      }
    }

    override def tryAdvance(action: function.Consumer[_ >: E]): Boolean = {
      Objects.requireNonNull(action)
      var p = current()
      while (p != null) {
        var e: E = null.asInstanceOf[E]
        var continueLoop = true
        while (continueLoop) {
          val item = p.item
          val isData = p.isData
          val q = p.next
          p = if (p == q) head else q
          if (isData) {
            if (item != null) {
              e = item.asInstanceOf[E]
              continueLoop = false
            }
          } else if (item == null) {
            p = null
          }
          if (p == null) continueLoop = false
        }
        setCurrent(p)
        if (e != null) {
          action.accept(e)
          return true
        }
      }
      false
    }

    private def setCurrent(p: Node): Unit = {
      _current = p
      if (_current == null)
        exhausted = true
    }

    private def current(): Node = {
      var p = _current
      if (p == null && !exhausted) {
        p = firstDataNode()
        setCurrent(p)
      }
      p
    }

    override def estimateSize() = Long.MaxValue

    override def characteristics(): Int =
      Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT
  }

  object LTQSpliterator {
    val MAX_BATCH = 1 << 25
  }

  /** Returns a {@link Spliterator} over the elements in this queue.
   *
   *  <p>The returned spliterator is <a
   *  href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   *  <p>The {@code Spliterator} reports {@link Spliterator#CONCURRENT}, {@link
   *  Spliterator#ORDERED}, and {@link Spliterator#NONNULL}.
   *
   *  @implNote
   *    The {@code Spliterator} implements {@code trySplit} to permit limited
   *    parallelism.
   *
   *  @return
   *    a {@code Spliterator} over the elements in this queue
   *  @since 1.8
   */
  override def spliterator(): Spliterator[E] = new LTQSpliterator()

  /* -------------- Removal methods -------------- */

  /** Unsplices (now or later) the given deleted/cancelled node with the given
   *  predecessor.
   *
   *  @param pred
   *    a node that was at one time known to be the predecessor of s
   *  @param s
   *    the node to be unspliced
   */
  def unsplice(pred: Node, s: Node): Unit = {
    // assert pred != null;
    // assert pred != s;
    // assert s != null;
    // assert s.isMatched();
    // assert (SWEEP_THRESHOLD & (SWEEP_THRESHOLD - 1)) == 0;
    s.waiter = null; // disable signals
    /*
     * See above for rationale. Briefly: if pred still points to
     * s, try to unlink s.  If s cannot be unlinked, because it is
     * trailing node or pred might be unlinked, and neither pred
     * nor s are head or offlist, set needSweep;
     */
    if (pred != null && pred.next == s) {
      val n = s.next
      if (n == null || (n != s && pred.casNext(s, n) && pred.isMatched())) {
        var continueLoop = true
        while (continueLoop) {
          val h = head
          if (h == pred || h == s)
            return
          if (!h.isMatched())
            continueLoop = false
          if (continueLoop) {
            val hn = h.next
            if (hn == null)
              return
            if (hn != h && casHead(h, hn))
              h.selfLink()
          }
        }
        if (pred.next != pred && s.next != s)
          needSweep = true
      }
    }
  }

  /** Unlinks matched (typically cancelled) nodes encountered in a traversal
   *  from head.
   */
  private def sweep(): Unit = {
    needSweep = false
    var p = head
    var continueLoop = true
    while (p != null && continueLoop) {
      val s = p.next
      if (s == null) continueLoop = false
      if (continueLoop) {
        if (!s.isMatched())
          // Unmatched nodes are never self-linked
          p = s
        else {
          val n = s.next
          if (n == null) // trailing node is pinned
            continueLoop = false
          else if (s == n) // stale
            // No need to also check for p == s, since that implies s == n
            p = head
          else p.casNext(s, n)
        }
      }
    }
  }

  /* -------------- Constructors -------------- */

  /** Creates a {@code LinkedTransferQueue} initially containing the elements of
   *  the given collection, added in traversal order of the collection's
   *  iterator.
   *
   *  @param c
   *    the collection of elements to initially contain
   *  @throws NullPointerException
   *    if the specified collection or any of its elements are null
   */
  def this(c: Collection[_ <: E]) = {
    this()
    var h: Node = null
    var t: Node = null
    val it = c.iterator()
    while (it.hasNext()) {
      val e = it.next()
      val newNode = new Node(Objects.requireNonNull(e))
      if (h == null) {
        t = newNode
        h = t
      } else {
        t.appendRelaxed(newNode)
        t = newNode
      }
    }
    if (h == null) {
      t = new Node()
      h = t
    }
    head = h
    tail = t
  }

  head = new Node()
  tail = head

  /* -------------------- Other ------------------- */

  /** Inserts the specified element at the tail of this queue. As the queue is
   *  unbounded, this method will never block.
   *
   *  @throws NullPointerException
   *    if the specified element is null
   */
  override def put(e: E): Unit = xfer(e, true, ASYNC, 0)

  /** Inserts the specified element at the tail of this queue. As the queue is
   *  unbounded, this method will never block or return {@code false}.
   *
   *  @return
   *    {@code true} (as specified by {@link
   *    BlockingQueue#offer(Object,long,TimeUnit) BlockingQueue.offer})
   *  @throws NullPointerException
   *    if the specified element is null
   */
  override def offer(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    xfer(e, true, ASYNC, 0)
    true
  }

  /** Inserts the specified element at the tail of this queue. As the queue is
   *  unbounded, this method will never return {@code false}.
   *
   *  @return
   *    {@code true} (as specified by {@link Queue#offer})
   *  @throws NullPointerException
   *    if the specified element is null
   */
  override def offer(e: E) = {
    xfer(e, true, ASYNC, 0)
    true
  }

  /** Inserts the specified element at the tail of this queue. As the queue is
   *  unbounded, this method will never throw {@link IllegalStateException} or
   *  return {@code false}.
   *
   *  @return
   *    {@code true} (as specified by {@link Collection#add})
   *  @throws NullPointerException
   *    if the specified element is null
   */
  override def add(e: E): Boolean = {
    xfer(e, true, ASYNC, 0)
    true
  }

  /** Transfers the element to a waiting consumer immediately, if possible.
   *
   *  <p>More precisely, transfers the specified element immediately if there
   *  exists a consumer already waiting to receive it (in {@link #take} or timed
   *  {@link #poll(long,TimeUnit) poll}), otherwise returning {@code false}
   *  without enqueuing the element.
   *
   *  @throws NullPointerException
   *    if the specified element is null
   */
  override def tryTransfer(e: E): Boolean = {
    return xfer(e, true, NOW, 0L) == null
  }

  /** Transfers the element to a consumer, waiting if necessary to do so.
   *
   *  <p>More precisely, transfers the specified element immediately if there
   *  exists a consumer already waiting to receive it (in {@link #take} or timed
   *  {@link #poll(long,TimeUnit) poll}), else inserts the specified element at
   *  the tail of this queue and waits until the element is received by a
   *  consumer.
   *
   *  @throws NullPointerException
   *    if the specified element is null
   */
  override def transfer(e: E): Unit = {
    if (xfer(e, true, SYNC, 0L) != null) {
      Thread.interrupted() // failure possible only due to interrupt
      throw new InterruptedException()
    }
  }

  /** Transfers the element to a consumer if it is possible to do so before the
   *  timeout elapses.
   *
   *  <p>More precisely, transfers the specified element immediately if there
   *  exists a consumer already waiting to receive it (in {@link #take} or timed
   *  {@link #poll(long,TimeUnit) poll}), else inserts the specified element at
   *  the tail of this queue and waits until the element is received by a
   *  consumer, returning {@code false} if the specified wait time elapses
   *  before the element can be transferred.
   *
   *  @throws NullPointerException
   *    if the specified element is null
   */
  override def tryTransfer(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    if (xfer(e, true, TIMED, unit.toNanos(timeout)) == null)
      true
    else if (!Thread.interrupted())
      false
    else throw new InterruptedException()
  }

  override def take(): E = {
    val e = xfer(null.asInstanceOf[E], false, SYNC, 0L)
    if (e != null)
      e
    else {
      Thread.interrupted()
      throw new InterruptedException()
    }
  }

  override def poll(timeout: Long, unit: TimeUnit): E = {
    val e = xfer(null.asInstanceOf[E], false, TIMED, unit.toNanos(timeout))
    if (e != null || !Thread.interrupted())
      e
    else throw new InterruptedException()
  }

  override def poll(): E = xfer(null.asInstanceOf[E], false, NOW, 0L)

  /** @throws NullPointerException
   *    {@inheritDoc}
   *  @throws IllegalArgumentException
   *    {@inheritDoc}
   */
  override def drainTo(c: Collection[_ >: E]): Int = {
    Objects.requireNonNull(c)
    if (c == this)
      throw new IllegalArgumentException()
    var n = 0
    var e = poll()
    while (e != null) {
      c.add(e)
      n += 1
      e = poll()
    }
    n
  }

  /** @throws NullPointerException
   *    {@inheritDoc}
   *  @throws IllegalArgumentException
   *    {@inheritDoc}
   */
  override def drainTo(c: Collection[_ >: E], maxElements: Int): Int = {
    Objects.requireNonNull(c)
    if (c == this)
      throw new IllegalArgumentException()
    var n = 0
    var innerBreak = false
    while (n < maxElements && !innerBreak) {
      val e = poll()
      if (e == null)
        innerBreak = true
      else {
        c.add(e)
        n += 1
      }
    }
    n
  }

  /** Returns an iterator over the elements in this queue in proper sequence.
   *  The elements will be returned in order from first (head) to last (tail).
   *
   *  <p>The returned iterator is <a
   *  href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   *  @return
   *    an iterator over the elements in this queue in proper sequence
   */
  override def iterator(): Iterator[E] = new Itr()

  override def peek(): E = {
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false
      var p = head
      var innerBreak = false
      while (p != null && !innerBreak) {
        val item = p.item
        if (p.isData) {
          if (item != null) {
            return item.asInstanceOf[E]
          }
        } else if (item == null) {
          innerBreak = true
        }
        if (!innerBreak) {
          val q = p.next
          if (p == q) {
            restartFromHead = true
            innerBreak = true
          } else p = q
        }
      }
      if (!restartFromHead) return null.asInstanceOf[E]
    }
    ???
  }

  /** Returns {@code true} if this queue contains no elements.
   *
   *  @return
   *    {@code true} if this queue contains no elements
   */
  override def isEmpty(): Boolean = firstDataNode() == null

  override def hasWaitingConsumer(): Boolean = {
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false
      var p = head
      var innerBreak = false
      while (p != null && !innerBreak) {
        val item = p.item
        if (p.isData) {
          if (item != null) {
            innerBreak = true
          }
        } else if (item == null) {
          return true
        }
        if (!innerBreak) {
          val q = p.next
          if (p == q) {
            restartFromHead = true
            innerBreak = true
          } else p = q
        }
      }
      if (!restartFromHead) return false
    }
    ???
  }

  /** Returns the number of elements in this queue. If this queue contains more
   *  than {@code Integer.MAX_VALUE} elements, returns {@code
   *  Integer.MAX_VALUE}.
   *
   *  <p>Beware that, unlike in most collections, this method is <em>NOT</em> a
   *  constant-time operation. Because of the asynchronous nature of these
   *  queues, determining the current number of elements requires an O(n)
   *  traversal.
   *
   *  @return
   *    the number of elements in this queue
   */
  override def size(): Int = countOfMode(true)

  override def getWaitingConsumerCount(): Int = countOfMode(false)

  /** Removes a single instance of the specified element from this queue, if it
   *  is present. More formally, removes an element {@code e} such that {@code
   *  o.equals(e)}, if this queue contains one or more such elements. Returns
   *  {@code true} if this queue contained the specified element (or
   *  equivalently, if this queue changed as a result of the call).
   *
   *  @param o
   *    element to be removed from this queue, if present
   *  @return
   *    {@code true} if this queue changed as a result of the call
   */
  override def remove(o: Any): Boolean = {
    if (o == null) return false
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false

      var p = head
      var pred: Node = null
      var innerBreak = false
      while (p != null && !innerBreak) {
        var q = p.next
        val item = p.item
        var skipRest = false
        if (item != null) {
          if (p.isData) {
            if (item.equals(o) && p.tryMatch(item, null)) {
              skipDeadNodes(pred, p, p, q)
              return true
            }
            pred = p
            p = q
            skipRest = true
          }
        } else if (!p.isData) innerBreak = true
        if (!skipRest && !innerBreak) {
          var c = p
          var cBreak = false
          while (!cBreak) {
            if (q == null || !q.isMatched()) {
              pred = skipDeadNodes(pred, c, p, q)
              p = q
              cBreak = true
            } else {
              val old_p = p
              p = q
              if (old_p == p) {
                innerBreak = true
                cBreak = true
                restartFromHead = true
              }
            }
            q = p.next
          }
        }
      }
    }
    false
  }

  /** Returns {@code true} if this queue contains the specified element. More
   *  formally, returns {@code true} if and only if this queue contains at least
   *  one element {@code e} such that {@code o.equals(e)}.
   *
   *  @param o
   *    object to be checked for containment in this queue
   *  @return
   *    {@code true} if this queue contains the specified element
   */
  override def contains(o: Any): Boolean = {
    if (o == null) return false

    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false

      var p = head
      var pred: Node = null
      var pLoopBreak = false
      while (p != null && !pLoopBreak) {
        var q = p.next
        val item = p.item
        var pLoopSkip = false
        if (item != null) {
          if (p.isData) {
            if (o.equals(item))
              return true
            pred = p
            p = q
            pLoopSkip = true
          }
        } else if (!p.isData) pLoopBreak = true
        if (!pLoopSkip && !pLoopBreak) {
          val c = p
          var qLoopBreak = false
          while (!qLoopBreak) {
            if (q == null || !q.isMatched()) {
              pred = skipDeadNodes(pred, c, p, q)
              p = q
              qLoopBreak = true
            }
            if (!qLoopBreak) {
              val old_p = p
              p = q
              if (old_p == p) {
                pLoopBreak = true
                qLoopBreak = true
                restartFromHead = true
              }
              q = p.next
            }
          }
        }
      }
      if (!restartFromHead) return false
    }
    ???
  }

  /** Always returns {@code Integer.MAX_VALUE} because a {@code
   *  LinkedTransferQueue} is not capacity constrained.
   *
   *  @return
   *    {@code Integer.MAX_VALUE} (as specified by {@link
   *    BlockingQueue#remainingCapacity()})
   */
  override def remainingCapacity(): Int = Integer.MAX_VALUE

  // No ObjectInputStream in ScalaNative
  // private def writeObject(s: java.io.ObjectOutputStream): Unit
  // private def readObject(s: java.io.ObjectInputStream): Unit

  /** @throws NullPointerException
   *    {@inheritDoc}
   */
  override def removeIf(filter: function.Predicate[_ >: E]): Boolean = {
    Objects.requireNonNull(filter)
    bulkRemove(filter)
  }

  /** @throws NullPointerException
   *    {@inheritDoc}
   */
  override def removeAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    bulkRemove(e => c.contains(e))
  }

  /** @throws NullPointerException
   *    {@inheritDoc}
   */
  override def retainAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    bulkRemove(e => !c.contains(e))
  }

  override def clear(): Unit = bulkRemove(_ => true)

  /** Implementation of bulk remove methods. */
  private def bulkRemove(filter: function.Predicate[_ >: E]): Boolean = {
    var removed = false

    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false

      var hops = MAX_HOPS
      // c will be CASed to collapse intervening dead nodes between
      // pred (or head if null) and p.
      var p = head
      var c = p
      var pred: Node = null
      var innerBreak = false
      while (p != null && !innerBreak) {
        val q = p.next
        val item = p.item
        var pAlive = item != null && p.isData
        if (pAlive) {
          if (filter.test(item.asInstanceOf[E])) {
            if (p.tryMatch(item, null))
              removed = true
            pAlive = false
          }
        } else if (!p.isData && item == null)
          innerBreak = true
        if (!innerBreak) {
          if (pAlive || q == null || { hops -= 1; hops } == 0) {
            // p might already be self-linked here, but if so:
            // - CASing head will surely fail
            // - CASing pred's next will be useless but harmless.
            val old_c = c
            if ((c != p && {
                  c = p; !tryCasSuccessor(pred, old_c, c)
                }) || pAlive) {
              // if CAS failed or alive, abandon old pred
              hops = MAX_HOPS
              pred = p
              c = q
            }
          } else if (p == q) {
            innerBreak = true
            restartFromHead = true
          }
          p = q
        }
      }
    }
    removed
  }

  /** Runs action on each element found during a traversal starting at p. If p
   *  is null, the action is not run.
   */
  def forEachFrom(action: function.Consumer[_ >: E], p: Node): Unit = {
    var _p = p
    var pred: Node = null
    var continueLoop = true
    while (_p != null && continueLoop) {
      var q = _p.next
      val item = _p.item
      var continueRest = true
      if (item != null) {
        if (_p.isData) {
          action.accept(item.asInstanceOf[E])
          pred = _p
          _p = q
          continueRest = false
        }
      } else if (!_p.isData)
        continueLoop = false
      if (continueLoop && continueRest) {
        var c = _p
        var continueInner = true
        while (continueInner) {
          if (q == null || !q.isMatched()) {
            pred = skipDeadNodes(pred, c, _p, q)
            _p = q
            continueInner = false
          }
          if (continueInner) {
            val old_p = _p
            _p = q
            if (_p == old_p) { pred = null; _p = head; continueInner = false }
            q = _p.next
          }
        }
      }
    }
  }

  /** @throws NullPointerException
   *    {@inheritDoc}
   */
  override def forEach(action: function.Consumer[_ >: E]): Unit = {
    Objects.requireNonNull(action)
    forEachFrom(action, head)
  }
}

@SerialVersionUID(-3223113410248163686L) object LinkedTransferQueue {

  /** The number of nanoseconds for which it is faster to spin rather than to
   *  use timed park. A rough estimate suffices. Using a power of two minus one
   *  simplifies some comparisons.
   */
  final val SPIN_FOR_TIMEOUT_THRESHOLD = 1023L

  /** The maximum number of estimated removal failures (sweepVotes) to tolerate
   *  before sweeping through the queue unlinking cancelled nodes that were not
   *  unlinked upon initial removal. See above for explanation. The value must
   *  be at least two to avoid useless sweeps when removing trailing nodes.
   */
  final val SWEEP_THRESHOLD = 32

  /** Tolerate this many consecutive dead nodes before CAS-collapsing. Amortized
   *  cost of clear() is (1 + 1/MAX_HOPS) CASes per element.
   */
  private final val MAX_HOPS = 8

  /** Queue nodes. Uses Object, not E, for items to allow forgetting them after
   *  use. Writes that are intrinsically ordered wrt other accesses or CASes use
   *  simple relaxed forms.
   */
  @SerialVersionUID(-3223113410248163686L) final class Node private (
      val isData: Boolean // false if this is a request node
  ) extends ForkJoinPool.ManagedBlocker {
    @volatile var item: Object =
      null // initially non-null if isData; CASed to match
    @volatile var next: Node = null
    @volatile var waiter: Thread = _ // null when not waiting for a match

    val nextAtomic = new AtomicRef[Node](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "next"))
    )
    val itemAtomic = new AtomicRef[Object](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "item"))
    )
    val waiterAtomic = new AtomicRef[Object](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "waiter"))
    )

    /** Constructs a data node holding item if item is non-null, else a request
     *  node. Uses relaxed write because item can only be seen after
     *  piggy-backing publication via CAS.
     */
    def this(item: Object) = {
      this(item != null)
      itemAtomic.store(item)
    }

    /** Constructs a (matched data) dummy node. */
    def this() = {
      this(true)
    }

    def casNext(cmp: Node, `val`: Node) =
      nextAtomic.compareExchangeStrong(cmp, `val`)
    def casItem(cmp: Object, `val`: Object) =
      itemAtomic.compareExchangeStrong(cmp, `val`)

    /** Links node to itself to avoid garbage retention. Called only after
     *  CASing head field, so uses relaxed write.
     */
    def selfLink(): Unit =
      nextAtomic.store(this, memory_order_release)

    def appendRelaxed(next: Node): Unit =
      nextAtomic.store(next, memory_order_relaxed)

    /** Returns true if this node has been matched, including the case of
     *  artificial matches due to cancellation.
     */
    def isMatched(): Boolean = isData == (item == null)

    /** Tries to CAS-match this node; if successful, wakes waiter. */
    def tryMatch(cmp: Object, `val`: Object) = {
      if (casItem(cmp, `val`)) {
        LockSupport.unpark(waiter)
        true
      } else { false }
    }

    /** Returns true if a node with the given mode cannot be appended to this
     *  node because this node is unmatched and has opposite data mode.
     */
    def cannotPrecede(haveData: Boolean) = {
      val d = isData
      d != haveData && d != (item == null)
    }

    def isReleasable() =
      (isData == (item == null)) || Thread.currentThread().isInterrupted()

    def block() = {
      while (!isReleasable()) LockSupport.park()
      true
    }
  }

  /* Possible values for "how" argument in xfer method. */
  private final val NOW: Int = 0; // for untimed poll, tryTransfer
  private final val ASYNC: Int = 1; // for offer, put, add
  private final val SYNC: Int = 2; // for transfer, take
  private final val TIMED: Int = 3; // for timed poll, tryTransfer

  // Reduce the risk of rare disastrous classloading in first call to
  // LockSupport.park: https://bugs.openjdk.java.net/browse/JDK-8074773
  locally { val _ = LockSupport.getClass }
}
