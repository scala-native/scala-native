/*
 * Written by Doug Lea and Martin Buchholz with assistance from members of
 * JCP JSR-166 Expert Group and released to the public domain, as explained
 * at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

/* Ported  to Scala Native
 *
 * Note on code style:
 *
 *   The objective of this port was to match the behavior of the Java JSR-166
 *   code, where matching is defined as passing the corresponding
 *   JSR-166 ConcurrentLinkedDequeTest.scala.
 *
 *   The plentiful use of Java idioms such as assignments within while
 *   condition clauses and 'break' and 'continue' statements leads to some
 *   just plain ugly non-idiomatic Scala code.
 *
 *   In particular the 'skipDeletedPredecessors()' and
 *   'skipDeletedSuccessors()' methods use Finite State Machines (FSM) to
 *   translate Java nested loops with both 'continue' and 'break' statements.
 *
 *   When you are repulsed by the coding style, have a moment of compassion
 *   for the wretched translator.
 *
 *   Once correctness has been established, perhaps future Evolutions can
 *   move through the file going from method to method and making the
 *   code style more pleasant to eyes accustomed  to idiomatic Scala.
 */

import java.util

import java.util.Objects
import java.util.Iterator
import java.util.{Spliterator, Spliterators}

import java.util.function.Consumer

import scala.scalanative.unsafe._
import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.libc.stdatomic.{AtomicRef, PtrToAtomicRef}
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.memory_order.memory_order_relaxed

/** An unbounded concurrent {@linkplain Deque deque} based on linked nodes.
 *  Concurrent insertion, removal, and access operations execute safely across
 *  multiple threads. A {@code ConcurrentLinkedDeque} is an appropriate choice
 *  when many threads will share access to a common collection. Like most other
 *  concurrent collection implementations, this class does not permit the use of
 *  {@code null} elements.
 *
 *  <p>Iterators and spliterators are <a
 *  href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
 *
 *  <p>Beware that, unlike in most collections, the {@code size} method is
 *  <em>NOT</em> a constant-time operation. Because of the asynchronous nature
 *  of these deques, determining the current number of elements requires a
 *  traversal of the elements, and so may report inaccurate results if this
 *  collection is modified during traversal. Additionally, the bulk operations
 *  {@code addAll}, {@code removeAll}, {@code retainAll}, {@code containsAll},
 *  {@code equals}, and {@code toArray} are <em>not</em> guaranteed to be
 *  performed atomically. For example, an iterator operating concurrently with
 *  an {@code addAll} operation might view only some of the added elements.
 *
 *  <p>This class and its iterator implement all of the <em>optional</em>
 *  methods of the {@link Deque} and {@link Iterator} interfaces.
 *
 *  <p>Memory consistency effects: As with other concurrent collections, actions
 *  in a thread prior to placing an object into a {@code ConcurrentLinkedDeque}
 *  <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 *  actions subsequent to the access or removal of that element from the {@code
 *  ConcurrentLinkedDeque} in another thread.
 *
 *  <p>This class is a member of the <a href="{@docRoot
 *  }/../technotes/guides/collections/index.html"> Java Collections
 *  Framework</a>.
 *
 *  @since 1.7
 *  @author
 *    Doug Lea
 *  @author
 *    Martin Buchholz
 *  @param <
 *    E> the type of elements held in this deque
 */
@SerialVersionUID(876323262645176354L)
object ConcurrentLinkedDeque {
  private val PREV_TERMINATOR: Node[AnyRef] = new Node[AnyRef](null)
  PREV_TERMINATOR.next = PREV_TERMINATOR

  private val NEXT_TERMINATOR: Node[AnyRef] = new Node[AnyRef](null)
  NEXT_TERMINATOR.prev = NEXT_TERMINATOR

  private[concurrent] final class Node[E <: AnyRef] private[concurrent] {
    // default constructor for NEXT_TERMINATOR, PREV_TERMINATOR
    @volatile private[concurrent] var prev: Node[E] = null
    @volatile private[concurrent] var item: E = null.asInstanceOf[E]
    @volatile private[concurrent] var next: Node[E] = null

    @alwaysinline private[ConcurrentLinkedDeque] def PREV: AtomicRef[Node[E]] =
      fromRawPtr[Node[E]](classFieldRawPtr(this, "prev")).atomic
    @alwaysinline private[ConcurrentLinkedDeque] def ITEM: AtomicRef[E] =
      fromRawPtr[E](classFieldRawPtr(this, "item")).atomic
    @alwaysinline private[ConcurrentLinkedDeque] def NEXT: AtomicRef[Node[E]] =
      fromRawPtr[Node[E]](classFieldRawPtr(this, "next")).atomic

    /** Constructs a new node. Uses relaxed write because item can only be seen
     *  after publication via casNext or casPrev.
     */
    def this(item: E) = {
      this()
      ITEM.store(item, memory_order_relaxed)
    }

    private[concurrent] def appendRelaxed(next: Node[E]): Unit = {
      // assert next != null;
      // assert this.next == null;
      NEXT.store(next, memory_order_relaxed)
    }

    private[concurrent] def casItem(cmp: E, `val`: E): Boolean = {
      // assert item == cmp || item == null;
      // assert cmp != null;
      // assert val == null;
      ITEM.compareExchangeStrong(cmp, `val`)
    }

    private[concurrent] def lazySetNext(`val`: Node[E]): Unit = {
      this.next = `val`
    }

    private[concurrent] def casNext(cmp: Node[E], `val`: Node[E]): Boolean = {
      // assert next == cmp || next == null;
      // assert cmp != null;
      // assert val == null;
      NEXT.compareExchangeStrong(cmp, `val`)
    }

    private[concurrent] def lazySetPrev(`val`: Node[E]): Unit = {
      this.prev = `val`
    }

    private[concurrent] def casPrev(cmp: Node[E], `val`: Node[E]): Boolean = {
      // assert prev == cmp || prev == null;
      // assert cmp != null;
      // assert val == null;
      PREV.compareExchangeStrong(cmp, `val`)
    }
  }

  private val HOPS = 2

  /** A customized variant of Spliterators.IteratorSpliterator */
  private[concurrent] object CLDSpliterator {
    private[concurrent] val MAX_BATCH = 1 << 25 // max batch array size;
  }

  private[concurrent] final class CLDSpliterator[
      E <: AnyRef
  ] private[concurrent] (
      private[concurrent]
      val queue: ConcurrentLinkedDeque[E]
  ) extends Spliterator[E] {
    private[concurrent] var current: Node[E] =
      null // current node; null until initialized
    private[concurrent] var batch = 0 // batch size for splits
    private[concurrent] var exhausted = false // true when no more nodes

    override def trySplit(): Spliterator[E] = {
      var p: Node[E] = null
      val q = this.queue
      val b = batch
      val n =
        if (b <= 0) 1
        else if (b >= CLDSpliterator.MAX_BATCH) CLDSpliterator.MAX_BATCH
        else b + 1
      if (!exhausted && ({ p = current; p } != null || {
            p = q.first; p
          } != null)) {
        if (p.item == null && (p eq { p = p.next; p })) current = {
          p = q.first; p
        }
        if (p != null && p.next != null) {
          val a = new Array[AnyRef](n)
          var i = 0

          while (p != null && i < n) {
            if ({ a(i) = p.item; a(i) } != null) i += 1
            if (p eq { p = p.next; p }) p = q.first
          }

          if ({ current = p; current } == null) exhausted = true
          if (i > 0) {
            batch = i
            return Spliterators.spliterator[E](
              a,
              0,
              i,
              Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT
            )
          }
        }
      }
      null
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      var p: Node[E] = null
      if (action == null) throw new NullPointerException
      val q = this.queue
      if (!exhausted && ({ p = current; p } != null) || ({
            p = q.first; p
          } != null)) {
        exhausted = true
        while (p != null) {
          val e = p.item
          if (p eq { p = p.next; p }) p = q.first
          if (e != null) action.accept(e)
        }
      }
    }

    override def tryAdvance(action: Consumer[_ >: E]): Boolean = {
      var p: Node[E] = null
      if (action == null) throw new NullPointerException
      val q = this.queue
      if (!exhausted && ({ p = current; p } != null) || ({
            p = q.first; p
          } != null)) {
        var e: E = null.asInstanceOf[E]

        while (e == null && p != null) {
          e = p.item
          if (p eq { p = p.next; p }) p = q.first
        }

        if ({ current = p; current } == null) exhausted = true
        if (e != null) {
          action.accept(e)
          return true
        }
      }
      false
    }

    override def estimateSize(): Long = java.lang.Long.MAX_VALUE

    override def characteristics(): Int =
      Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT
  }
}

@SerialVersionUID(876323262645176354L)
class ConcurrentLinkedDeque[E <: AnyRef]
/** Constructs an empty deque.
 */
    extends util.AbstractCollection[E]
    with util.Deque[E]
    with Serializable {

  import ConcurrentLinkedDeque._

  /** A node from which the first node on list (that is, the unique node p with
   *  p.prev == null && p.next != p) can be reached in O(1) time. Invariants:
   *    - the first node is always O(1) reachable from head via prev links
   *    - all live nodes are reachable from the first node via succ()
   *    - head != null
   *    - (tmp = head).next != tmp || tmp != head
   *    - head is never gc-unlinked (but may be unlinked) Non-invariants:
   *    - head.item may or may not be null
   *    - head may not be reachable from the first or last node, or from tail
   */
  @volatile
  @transient private var head: Node[E] = new Node[E]

  /** A node from which the last node on list (that is, the unique node p with
   *  p.next == null && p.prev != p) can be reached in O(1) time. Invariants:
   *    - the last node is always O(1) reachable from tail via next links
   *    - all live nodes are reachable from the last node via pred()
   *    - tail != null
   *    - tail is never gc-unlinked (but may be unlinked) Non-invariants:
   *    - tail.item may or may not be null
   *    - tail may not be reachable from the first or last node, or from head
   */
  @volatile
  @transient private var tail: Node[E] = head

  @alwaysinline private def HEAD: AtomicRef[Node[E]] =
    fromRawPtr[Node[E]](classFieldRawPtr(this, "head")).atomic
  @alwaysinline private def TAIL: AtomicRef[Node[E]] =
    fromRawPtr[Node[E]](classFieldRawPtr(this, "tail")).atomic

  private[concurrent] def prevTerminator = PREV_TERMINATOR.asInstanceOf[Node[E]]

  private[concurrent] def nextTerminator = NEXT_TERMINATOR.asInstanceOf[Node[E]]

  /** Links e as first element.
   */
  private def linkFirst(e: E): Unit = {
    val newNode = new Node[E](Objects.requireNonNull(e))

    while (true) {
      var restart = false
      while (!restart) {
        restart = false

        var h = head
        var p = h
        var q: Node[E] = null
        // Check for head updates every other hop.
        while (true)
          if ({ q = p.prev; q } != null && { p = q; q = p.prev; q } != null)
            // If p == q, we are sure to follow head instead.
            p =
              if (h ne { h = head; h }) h
              else q
          else if (p.next eq p) // PREV_TERMINATOR
            restart = true
          else {
            // p is first node
            newNode.lazySetNext(p) // CAS piggyback
            if (p.casPrev(null, newNode)) {
              // Successful CAS is the linearization point
              // for e to become an element of this deque,
              // and for newNode to become "live".
              if (p ne h)
                casHead(h, newNode) // Failure is OK.// hop two nodes at a time
              return
            }
          }
      }
    }
  }

  /** Links e as last element.
   */
  private def linkLast(e: E): Unit = {

    val newNode = new ConcurrentLinkedDeque.Node[E](Objects.requireNonNull(e))

    var restartFromTail = false
    while (!restartFromTail) {
      restartFromTail = false

      while (true) {
        var t = tail
        var p = t
        var q: ConcurrentLinkedDeque.Node[E] = null
        while (true) {
          // Check for tail updates every other hop.
          if (({ q = p.next; q } != null) && ({ p = q; q = p.next; q } != null))
            // If p == q, we are sure to follow tail instead.
            p =
              if (t ne { t = tail; t }) t
              else q
          else if (p.prev eq p) // NEXT_TERMINATOR
            restartFromTail = true
          else {
            // p is last node
            newNode.lazySetPrev(p) // CAS piggyback

            if (p.casNext(null, newNode)) {
              // Successful CAS is the linearization point
              // for e to become an element of this deque,
              // and for newNode to become "live".

              // hop two nodes at a time
              if (p ne t)
                casTail(t, newNode) // Failure is OK.

              return
            }
          }
        }
      }
    }
  }

  private[concurrent] def unlink(x: Node[E]): Unit = {
    // assert x != null;
    // assert x.item == null;
    // assert x != PREV_TERMINATOR;
    // assert x != NEXT_TERMINATOR;

    val prev = x.prev
    val next = x.next
    if (prev == null) {
      unlinkFirst(x, next)
    } else if (next == null) {
      unlinkLast(x, prev)
    } else {
      // Unlink interior node.
      //
      // This is the common case, since a series of polls at the
      // same end will be "interior" removes, except perhaps for
      // the first one, since end nodes cannot be unlinked.
      //
      // At any time, all active nodes are mutually reachable by
      // following a sequence of either next or prev pointers.
      //
      // Our strategy is to find the unique active predecessor
      // and successor of x.  Try to fix up their links so that
      // they point to each other, leaving x unreachable from
      // active nodes.  If successful, and if x has no live
      // predecessor/successor, we additionally try to gc-unlink,
      // leaving active nodes unreachable from x, by rechecking
      // that the status of predecessor and successor are
      // unchanged and ensuring that x is not reachable from
      // tail/head, before setting x's prev/next links to their
      // logical approximate replacements, self/TERMINATOR.

      var activePred: Node[E] = null
      var activeSucc: Node[E] = null
      var isFirst = false
      var isLast = false
      var hops = 1

      // Find active predecessor
      var p = prev
      var breakNow = false

      while (!breakNow) {
        if (p.item != null) {
          activePred = p
          isFirst = false
          breakNow = true
        }

        if (!breakNow) {
          val q = p.prev

          if (q == null) {
            if (p.next eq p)
              return

            activePred = p
            isFirst = true
            breakNow = true
          } else if (p eq q) {
            return
          } else {
            p = q
          }

          if (!breakNow)
            hops += 1
        }
      }

      // Find active successor
      var p2 = next
      breakNow = false
      while (!breakNow) {
        if (p2.item != null) {
          activeSucc = p2
          isLast = false
          breakNow = true
        } else {
          val q = p2.next
          if (q == null) {
            if (p2.prev eq p2)
              return

            activeSucc = p2
            isLast = true
            breakNow = true
          } else if (p2 eq q) {
            return
          } else {
            p2 = q
          }
        }

        if (!breakNow)
          hops += 1
      }

      // TODO: better HOP heuristics
      if (hops < HOPS && (isFirst | isLast))
        return

      // Squeeze out deleted nodes between activePred and
      // activeSucc, including x.

      skipDeletedSuccessors(activePred)
      skipDeletedPredecessors(activeSucc)

      // Try to gc-unlink, if possible
      if ((isFirst | isLast) &&
          // Recheck expected state of predecessor and successor
          (activePred.next eq activeSucc) && (activeSucc.prev eq activePred)
          && (if (isFirst)
                activePred.prev == null
              else
                activePred.item != null) && (if (isLast)
                                               activeSucc.next == null
                                             else
                                               activeSucc.item != null)) {

        updateHead() // Ensure x is not reachable from head
        updateTail() // Ensure x is not reachable from tail

        // Finally, actually gc-unlink
        x.lazySetPrev(
          if (isFirst) prevTerminator
          else x
        )
        x.lazySetNext(
          if (isLast) nextTerminator
          else x
        )
      }
    }
  }

  /** Unlinks non-null first node.
   */
  private def unlinkFirst(first: Node[E], next: Node[E]): Unit = {
    // assert first != null;
    // assert next != null;
    // assert first.item == null;
    var o: Node[E] = null
    var p = next
    var q: Node[E] = null

    while (true)
      if (p.item != null || { q = p.next; q } == null) {
        if (o != null && (p.prev ne p) && first.casNext(next, p)) {
          skipDeletedPredecessors(p)
          if (first.prev == null && (p.next == null || p.item != null) &&
              (p.prev eq first)) {
            updateHead() // Ensure o is not reachable from head
            updateTail() // Ensure o is not reachable from tail

            // Finally, actually gc-unlink
            o.lazySetNext(o)
            o.lazySetPrev(prevTerminator)
          }
        }
        return
      } else if (p eq q) return
      else {
        o = p
        p = q
      }
  }

  /** Unlinks non-null last node.
   */
  private def unlinkLast(last: Node[E], prev: Node[E]): Unit = {
    // assert last != null;
    // assert prev != null;
    // assert last.item == null;
    var o: Node[E] = null
    var p = prev
    var q: Node[E] = null
    while (true) {
      if (p.item != null || { q = p.prev; q } == null) {
        if (o != null && (p.next ne p) && last.casPrev(prev, p)) {
          skipDeletedSuccessors(p)

          if (last.next == null && (p.prev == null || p.item != null) &&
              (p.next eq last)) {
            updateHead() // Ensure o is not reachable from head
            updateTail() // Ensure o is not reachable from tail

            // Finally, actually gc-unlink
            o.lazySetPrev(o)
            o.lazySetNext(nextTerminator)
          }
        }
        return
      } else if (p eq q) return
      else {
        o = p
        p = q
      }
    }
  }

  /** Guarantees that any node which was unlinked before a call to this method
   *  will be unreachable from head after it returns. Does not guarantee to
   *  eliminate slack, only that head will point to a node that was active while
   *  this method was running.
   */
  private final def updateHead(): Unit = {
    // Either head already points to an active node, or we keep
    // trying to cas it to the first node until it does.

    var h, p, q = null.asInstanceOf[Node[E]]

    var restartFromHead = true

    while (restartFromHead) {
      restartFromHead = false

      while ({ h = head; h }.item == null && { p = h.prev; p } != null) {
        while (!restartFromHead) {
          if ({ q = p.prev; q } == null || { p = q; q = p.prev; q } == null) {
            // It is possible that p is PREV_TERMINATOR,
            // but if so, the CAS is guaranteed to fail.
            if (casHead(h, p)) return
            else restartFromHead = true
          } else if (h ne head) {
            restartFromHead = true
          } else {
            p = q
          }
        }
      }
    }
  }

  private final def updateTail(): Unit = {
    // Either tail already points to an active node, or we keep
    // trying to cas it to the last node until it does.

    var t, p, q = null.asInstanceOf[Node[E]]

    var restartFromTail = true

    while (restartFromTail) {
      restartFromTail = false

      while ({ t = tail; t }.item == null && { p = t.next; p } != null)
        while (true)
          if ({ q = p.next; q } == null || { p = q; q = p.next; q } == null) {
            // It is possible that p is NEXT_TERMINATOR,
            // but if so, the CAS is guaranteed to fail.
            if (casTail(t, p)) return
            else restartFromTail = true
          } else if (t ne tail) restartFromTail = true
          else p = q
    }
  }

  private object SkipDeletedStates {
    final val State_BEGIN = 1
    final val State_WHILE_ACTIVE = 2
    final val State_FIND_ACTIVE = 3

    final val State_WHILE_ACTIVE_TEST = 98
    final val State_DONE = 99
  }

  private def skipDeletedPredecessors(x: Node[E]): Unit = {
    import SkipDeletedStates._

    // A Finite State Machine (FSM) to closely follow Java break/continue logic
    var state = State_BEGIN
    while (state != State_DONE) {
      var prev = x.prev
      var p = prev // Dummy value, will get overwritten.

      state match {
        case State_BEGIN =>
          val prev = x.prev
          // assert next != null;
          // assert x != NEXT_TERMINATOR;
          // assert x != PREV_TERMINATOR;

          p = prev
          state = State_FIND_ACTIVE

        case State_FIND_ACTIVE =>
          var findActive = true

          while (findActive) {
            findActive = true
            if (p.item != null) {
              findActive = false // state stays State_FIND_ACTIVE
            } else {
              val q = p.prev
              if (q == null) {
                findActive = false

                if (p.next eq p)
                  state = State_WHILE_ACTIVE
              } else if (p eq q) {
                findActive = false
                state = State_WHILE_ACTIVE
              } else {
                p = q
              }

              // found active CAS target
              if (state != State_WHILE_ACTIVE) {
                if (prev == p || x.casPrev(prev, p))
                  return
              }

              state = State_WHILE_ACTIVE_TEST
            }
          }

        case State_WHILE_ACTIVE_TEST =>
          state =
            if ((x.item != null || x.next == null)) State_WHILE_ACTIVE_TEST
            else State_DONE

        case unknown =>
          state = State_DONE
      }
    }
  }

  private def skipDeletedSuccessors(x: Node[E]): Unit = {
    import SkipDeletedStates._

    // A Finite State Machine (FSM) to closely follow Java break/continue logic
    var state = State_BEGIN
    while (state != State_DONE) {
      var next = x.next
      var p = next // Dummy value, will get overwritten. Avoid Scala 2/3 quirks

      state match {
        case State_BEGIN =>
          val next = x.next
          // assert next != null;
          // assert x != NEXT_TERMINATOR;
          // assert x != PREV_TERMINATOR;

          p = next
          state = State_FIND_ACTIVE

        case State_FIND_ACTIVE =>
          var findActive = true

          while (findActive) {
            findActive = true
            if (p.item != null) {
              findActive = false // state stays State_FIND_ACTIVE
            } else {
              val q = p.next
              if (q == null) {
                findActive = false

                if (p.next eq p)
                  state = State_WHILE_ACTIVE
              } else if (p eq q) {
                findActive = false
                state = State_WHILE_ACTIVE
              } else {
                p = q
              }

              // found active CAS target
              if (state != State_WHILE_ACTIVE) {
                if (next == p || x.casNext(next, p))
                  return
              }

              state = State_WHILE_ACTIVE_TEST
            }
          }

        case State_WHILE_ACTIVE_TEST =>
          state =
            if ((x.item != null || x.prev == null)) State_WHILE_ACTIVE_TEST
            else State_DONE

        case unknown =>
          state = State_DONE
      }
    }
  }

  /** Returns the successor of p, or the first node if p.next has been linked to
   *  self, which will only be true if traversing with a stale pointer that is
   *  now off the list.
   */
  private[concurrent] final def succ(p: Node[E]) = {
    // TODO: should we skip deleted nodes here?
    val q = p.next
    if (p eq q) first
    else q
  }

  /** Returns the predecessor of p, or the last node if p.prev has been linked
   *  to self, which will only be true if traversing with a stale pointer that
   *  is now off the list.
   */
  private[concurrent] final def pred(p: Node[E]): Node[E] = {
    val q = p.prev
    if (p eq q) last
    else q
  }

  /** Returns the first node, the unique node p for which: p.prev == null &&
   *  p.next != p The returned node may or may not be logically deleted.
   *  Guarantees that head is set to the returned node.
   */

  private[concurrent] def first: Node[E] = {
    while (true) {
      var restartFromHead = true

      while (restartFromHead) {
        restartFromHead = false

        var h = head
        var p = h
        var q: ConcurrentLinkedDeque.Node[E] = null

        while (true) {
          // Check for head updates every other hop.
          if (({ q = p.prev; q } != null) && ({ p = q; q = p.prev; q } != null))
            // If p == q, we are sure to follow head instead.
            p =
              if (h ne { h = head; h }) h
              else q
          else if ((p eq h) || casHead(h, p)) return p
          else restartFromHead = true
        }
      }
    }
    // unreachable
    null
  }

  /** Returns the last node, the unique node p for which: p.next == null &&
   *  p.prev != p The returned node may or may not be logically deleted.
   *  Guarantees that tail is set to the returned node.
   */
  private[concurrent] def last: Node[E] = {

    while (true) {
      var restart = false

      while (!restart) {
        var t = tail
        var p = t
        var q: Node[E] = null
        // Check for tail updates every other hop.

        while (true)
          if ({ q = p.next; q } != null && { p = q; q = p.next; q } != null)
            // If p == q, we are sure to follow tail instead.
            p =
              if (t ne { t = tail; t }) t
              else q
          else if ((p eq t) || casTail(t, p)) return p
          else restart = true
      }
    }

    // unreachable
    null
  }

  /** Returns element unless it is null, in which case throws
   *  NoSuchElementException.
   *
   *  @param v
   *    the element
   *  @return
   *    the element
   */
  // Minor convenience utilities
  private def screenNullResult(v: E) = {
    if (v == null)
      throw new NoSuchElementException
    v
  }

  /** Constructs a deque initially containing the elements of the given
   *  collection, added in traversal order of the collection's iterator.
   *
   *  @param c
   *    the collection of elements to initially contain throws
   *    NullPointerException if the specified collection or any of its elements
   *    are null
   */

  def this(c: util.Collection[_ <: E]) = {
    this()
    var h, t: Node[E] = null

    c.forEach { e =>
      {
        val newNode = new Node[E](Objects.requireNonNull(e))

        if (h == null) {
          h = newNode
          t = newNode
        } else {
          t.lazySetNext(newNode)
          newNode.lazySetPrev(t)
          t = newNode
        }
      }
    }

    initHeadTail(h, t)
  }

  /** Initializes head and tail, ensuring invariants hold.
   */
  private def initHeadTail(_h: Node[E], _t: Node[E]): Unit = {
    var h = _h
    var t = _t

    if (h == t) {
      if (h == null) {
        h = new Node[E]()
        t = h
      } else {
        // Avoid edge case of a single Node with non-null item.
        val newNode = new Node[E]()
        t.lazySetNext(newNode)
        newNode.lazySetPrev(t)
        t = newNode
      }
    }
    head = h
    tail = t
  }

  /** Inserts the specified element at the front of this deque. As the deque is
   *  unbounded, this method will never throw IllegalStateException.
   *
   *  throws NullPointerException if the specified element is null
   */
  override def addFirst(e: E): Unit =
    linkFirst(e)

  /** Inserts the specified element at the end of this deque. As the deque is
   *  unbounded, this method will never throw IllegalStateException.
   *
   *  <p>This method is equivalent to add().
   *
   *  throws NullPointerException if the specified element is null
   */
  override def addLast(e: E): Unit =
    linkLast(e)

  /** Inserts the specified element at the front of this deque. As the deque is
   *  unbounded, this method will never return {@code false}.
   *
   *  @return
   *    {@code true} (as specified by {@link Deque# offerFirst}) throws
   *    NullPointerException if the specified element is null
   */
  override def offerFirst(e: E): Boolean = {
    linkFirst(e)
    true
  }

  /** Inserts the specified element at the end of this deque. As the deque is
   *  unbounded, this method will never return {@code false}.
   *
   *  <p>This method is equivalent to add().
   *
   *  @return
   *    {@code true} (as specified by {@link Deque# offerLast}) throws
   *    NullPointerException if the specified element is null
   */
  override def offerLast(e: E): Boolean = {
    linkLast(e)
    true
  }

  override def peekFirst(): E = {
    var p = first
    while (p != null) {
      val item = p.item
      if (item != null) return item
      p = succ(p)
    }
    null.asInstanceOf[E]
  }

  override def peekLast(): E = {
    var p = last
    while (p != null) {
      val item = p.item
      if (item != null) return item
      p = pred(p)
    }
    null.asInstanceOf[E]
  }

  /** throws NoSuchElementException
   */
  override def getFirst(): E = screenNullResult(peekFirst())

  /** throws NoSuchElementException
   */
  override def getLast(): E = screenNullResult(peekLast())

  override def pollFirst(): E = {
    var p = first

    while (p != null) {
      val item = p.item

      if (item != null && p.casItem(item, null.asInstanceOf[E])) {
        unlink(p)
        return item
      }

      p = succ(p)
    }

    null.asInstanceOf[E]
  }

  override def pollLast(): E = {
    var p = last

    while (p != null) {
      val item = p.item
      if (item != null && p.casItem(item, null.asInstanceOf[E])) {
        unlink(p)
        return item
      }

      p = pred(p)
    }

    null.asInstanceOf[E]
  }

  /** throws NoSuchElementException
   */
  override def removeFirst(): E = screenNullResult(pollFirst())

  /** throws NoSuchElementException
   */
  override def removeLast(): E = screenNullResult(pollLast())

  /** Inserts the specified element at the tail of this deque. As the deque is
   *  unbounded, this method will never return {@code false}.
   *
   *  @return
   *    {@code true} (as specified by {@link Queue# offer}) throws
   *    NullPointerException if the specified element is null
   */
  // *** Queue and stack methods ***
  override def offer(e: E): Boolean = offerLast(e)

  /** Inserts the specified element at the tail of this deque. As the deque is
   *  unbounded, this method will never throw IllegalStateException or return
   *  {@code false}.
   *
   *  @return
   *    {@code true} (as specified by {@link Collection# add}) throws
   *    NullPointerException if the specified element is null
   */
  override def add(e: E): Boolean = offerLast(e)

  override def poll(): E = pollFirst()

  override def peek(): E = peekFirst()

  /** @throws NoSuchElementException
   */
  override def remove(): E = removeFirst()

  /** @throws NoSuchElementException
   */
  override def pop(): E = removeFirst()

  /** @throws NoSuchElementException
   */
  override def element(): E = getFirst()

  /** throws NullPointerException
   */
  override def push(e: E): Unit = addFirst(e)

  /** Removes the first occurrence of the specified element from this deque. If
   *  the deque does not contain the element, it is unchanged. More formally,
   *  removes the first element {@code e} such that {@code o.equals(e)} (if such
   *  an element exists). Returns {@code true} if this deque contained the
   *  specified element (or equivalently, if this deque changed as a result of
   *  the call).
   *
   *  @param o
   *    element to be removed from this deque, if present
   *  @return
   *    {@code true} if the deque contained the specified element throws
   *    NullPointerException if the specified element is null
   */
  override def removeFirstOccurrence(o: Any): Boolean = {
    Objects.requireNonNull(o)
    var p = first
    while (p != null) {
      val item = p.item
      if (item != null && o == item && p.casItem(item, null.asInstanceOf[E])) {
        unlink(p)
        return true
      }
      p = succ(p)
    }
    false
  }

  /** Removes the last occurrence of the specified element from this deque. If
   *  the deque does not contain the element, it is unchanged. More formally,
   *  removes the last element {@code e} such that {@code o.equals(e)} (if such
   *  an element exists). Returns {@code true} if this deque contained the
   *  specified element (or equivalently, if this deque changed as a result of
   *  the call).
   *
   *  @param o
   *    element to be removed from this deque, if present
   *  @return
   *    {@code true} if the deque contained the specified element throws
   *    NullPointerException if the specified element is null
   */
  override def removeLastOccurrence(o: Any): Boolean = {
    Objects.requireNonNull(o)
    var p = last
    while (p != null) {
      val item = p.item
      if (item != null && o == item && p.casItem(item, null.asInstanceOf[E])) {
        unlink(p)
        return true
      }
      p = pred(p)
    }
    false
  }

  /** Returns {@code true} if this deque contains the specified element. More
   *  formally, returns {@code true} if and only if this deque contains at least
   *  one element {@code e} such that {@code o.equals(e)}.
   *
   *  @param o
   *    element whose presence in this deque is to be tested
   *  @return
   *    {@code true} if this deque contains the specified element
   */
  override def contains(o: Any): Boolean = {
    if (o != null) {
      var p = first
      while (p != null) {
        val item = p.item
        if (item != null && o == item) return true
        p = succ(p)
      }
    }
    false
  }

  /** Returns {@code true} if this collection contains no elements.
   *
   *  @return
   *    {@code true} if this collection contains no elements
   */
  override def isEmpty(): Boolean =
    peekFirst() == null

  /** Returns the number of elements in this deque. If this deque contains more
   *  than {@code Integer.MAX_VALUE} elements, it returns {@code
   *  Integer.MAX_VALUE}.
   *
   *  <p>Beware that, unlike in most collections, this method is <em>NOT</em> a
   *  constant-time operation. Because of the asynchronous nature of these
   *  deques, determining the current number of elements requires traversing
   *  them all to count them. Additionally, it is possible for the size to
   *  change during execution of this method, in which case the returned result
   *  will be inaccurate. Thus, this method is typically not very useful in
   *  concurrent applications.
   *
   *  @return
   *    the number of elements in this deque
   */
  override def size(): Int = {
    while (true) {
      var count = 0
      var p = first
      var restart = false
      while (p != null && !restart) {
        if (p.item != null) {
          count += 1
          // @see Collection.size()
          if (count == Integer.MAX_VALUE) return count
        }
        if (p eq { p = p.next; p }) restart = true
      }
      if (!restart) return count
    }
    // unreachable
    -1
  }

  /** Removes the first occurrence of the specified element from this deque. If
   *  the deque does not contain the element, it is unchanged. More formally,
   *  removes the first element {@code e} such that {@code o.equals(e)} (if such
   *  an element exists). Returns {@code true} if this deque contained the
   *  specified element (or equivalently, if this deque changed as a result of
   *  the call).
   *
   *  <p>This method is equivalent to removeFirstOccurrence(Object)}.
   *
   *  @param o
   *    element to be removed from this deque, if present
   *  @return
   *    {@code true} if the deque contained the specified element throws
   *    NullPointerException if the specified element is null
   */
  override def remove(o: Any): Boolean = removeFirstOccurrence(o)

  /** Appends all of the elements in the specified collection to the end of this
   *  deque, in the order that they are returned by the specified collection's
   *  iterator. Attempts to {@code addAll} of a deque to itself result in {@code
   *  IllegalArgumentException}.
   *
   *  @param c
   *    the elements to be inserted into this deque
   *  @return
   *    {@code true} if this deque changed as a result of the call throws
   *    NullPointerException if the specified collection or any of its elements
   *    are null throws IllegalArgumentException if the collection is this deque
   */
  override def addAll(c: util.Collection[_ <: E]): Boolean = {
    if (c eq this)
      throw new IllegalArgumentException // As historically specified in AbstractQueue#addAll
    // Copy c into a private chain of Nodes
    var beginningOfTheEnd: Node[E] = null
    var last: Node[E] = null

    c.forEach { e =>
      val newNode = new Node[E](Objects.requireNonNull(e))
      if (beginningOfTheEnd == null) beginningOfTheEnd = {
        last = newNode; last
      }
      else {
        last.lazySetNext(newNode)
        newNode.lazySetPrev(last)
        last = newNode
      }
    }
    if (beginningOfTheEnd == null) return false
    // Atomically append the chain at the tail of this collection
    var restart = false
    while (!restart) {
      restart = false

      var t = tail
      var p = t
      var q: Node[E] = null
      // Check for tail updates every other hop.
      while (true)
        if ({ q = p.next; q } != null && { p = q; q = p.next; q } != null)
          // If p == q, we are sure to follow tail instead.
          p =
            if (t ne { t = tail; t }) t
            else q
        else if (p.prev eq p) // NEXT_TERMINATOR
          restart = true
        else {
          // p is last node
          beginningOfTheEnd.lazySetPrev(p) // CAS piggyback
          if (p.casNext(null, beginningOfTheEnd)) {
            // Successful CAS is the linearization point
            // for all elements to be added to this deque.
            if (!casTail(t, last)) {
              // Try a little harder to update tail,
              // since we may be adding many elements.
              t = tail
              if (last.next == null) casTail(t, last)
            }
            return true
          }
        }
    }

    // unreachable
    false
  }

  /** Removes all of the elements from this deque.
   */
  override def clear(): Unit = {
    while (pollFirst() != null) {}
  }

  override def toString: String = {
    var a: Array[String] = null
    while (true) {
      var charLength = 0
      var size = 0
      var p = first
      var restart = false
      while (p != null && !restart) {
        val item = p.item
        if (item != null) {
          if (a == null) a = new Array[String](4)
          else if (size == a.length) a = util.Arrays.copyOf(a, 2 * size)
          val s = item.toString
          a({
            size += 1; size - 1
          }) = s
          charLength += s.length
        }
        if (p eq { p = p.next; p }) restart = true
      }
      if (!restart) {
        if (size == 0) return "[]"
        return Helpers.toString(a.asInstanceOf[Array[AnyRef]], size, charLength)
      }
    }
    // unreachable
    null
  }

  private def toArrayInternal(a: Array[AnyRef]): Array[AnyRef] = {
    var x = a
    var restartFromHead = true
    while (true) {
      var size = 0
      var p = first
      restartFromHead = false
      while (p != null && !restartFromHead) {
        val item = p.item
        if (item != null) {
          if (x == null) x = new Array[AnyRef](4)
          else if (size == x.length) x = util.Arrays.copyOf(x, 2 * (size + 4))
          x(size) = item
          size += 1
        }
        if (p eq { p = p.next; p }) restartFromHead = true
      }
      if (!restartFromHead) {
        if (x == null) return new Array[AnyRef](0)
        else if (a != null && size <= a.length) {
          if (a ne x) System.arraycopy(x, 0, a, 0, size)
          if (size < a.length) a(size) = null
          return a
        }
        return if (size == x.length) x
        else util.Arrays.copyOf(x, size)
      }
    }
    a // unreachable, but compiler requires a return value.
  }

  /** Returns an array containing all of the elements in this deque, in proper
   *  sequence (from first to last element).
   *
   *  <p>The returned array will be "safe" in that no references to it are
   *  maintained by this deque. (In other words, this method must allocate a new
   *  array). The caller is thus free to modify the returned array.
   *
   *  <p>This method acts as bridge between array-based and collection-based
   *  APIs.
   *
   *  @return
   *    an array containing all of the elements in this deque
   */
  override def toArray(): Array[AnyRef] = toArrayInternal(null)

  /** Returns an array containing all of the elements in this deque, in proper
   *  sequence (from first to last element); the runtime type of the returned
   *  array is that of the specified array. If the deque fits in the specified
   *  array, it is returned therein. Otherwise, a new array is allocated with
   *  the runtime type of the specified array and the size of this deque.
   *
   *  <p>If this deque fits in the specified array with room to spare (i.e., the
   *  array has more elements than this deque), the element in the array
   *  immediately following the end of the deque is set to {@code null}.
   *
   *  <p>Like the toArray() method, this method acts as bridge between
   *  array-based and collection-based APIs. Further, this method allows precise
   *  control over the runtime type of the output array, and may, under certain
   *  circumstances, be used to save allocation costs.
   *
   *  <p>Suppose {@code x} is a deque known to contain only strings. The
   *  following code can be used to dump the deque into a newly allocated array
   *  of {@code String}:
   *
   *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
   *
   *  Note that {@code toArray(new Object[0])} is identical in function to
   *  {@code toArray()}.
   *
   *  @param a
   *    the array into which the elements of the deque are to be stored, if it
   *    is big enough; otherwise, a new array of the same runtime type is
   *    allocated for this purpose
   *  @return
   *    an array containing all of the elements in this deque throws
   *    ArrayStoreException if the runtime type of the specified array is not a
   *    supertype of the runtime type of every element in this deque throws
   *    NullPointerException if the specified array is null
   */

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    Objects.requireNonNull(a)
    toArrayInternal(a.asInstanceOf[Array[AnyRef]]).asInstanceOf[Array[T]]
  }

  /** Returns an iterator over the elements in this deque in proper sequence.
   *  The elements will be returned in order from first (head) to last (tail).
   *
   *  <p>The returned iterator is <a
   *  href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   *  @return
   *    an iterator over the elements in this deque in proper sequence
   */

  override def iterator(): Iterator[E] =
    new Itr()

  /** Returns an iterator over the elements in this deque in reverse sequential
   *  order. The elements will be returned in order from last (tail) to first
   *  (head).
   *
   *  <p>The returned iterator is <a
   *  href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   *  @return
   *    an iterator over the elements in this deque in reverse order
   */
  override def descendingIterator(): Iterator[E] =
    new DescendingItr

  private abstract class AbstractItr() extends Iterator[E] {

    /** Next node to return item for.
     */
    private var nextNode: Node[E] = null

    /** nextItem holds on to item fields because once we claim that an element
     *  exists in hasNext(), we must return it in the following next() call even
     *  if it was in the process of being removed when hasNext() was called.
     */
    private var nextItem: E = null.asInstanceOf[E]

    /** Node returned by most recent call to next. Needed by remove. Reset to
     *  null if this element is deleted by a call to remove.
     */
    private var lastRet: Node[E] = null

    private[concurrent] def startNode: Node[E]

    private[concurrent] def nextNode(p: Node[E]): Node[E]

    advance() // customize vars after constructor has initially set them.

    /** Sets nextNode and nextItem to next valid node, or to null if no such.
     */
    private def advance(): Unit = {
      lastRet = nextNode
      var p =
        if (nextNode == null) startNode
        else nextNode(nextNode)

      var breakNow = false
      while (!breakNow) {
        if (p == null) {
          // might be at active end or TERMINATOR node; both are OK
          nextNode = null.asInstanceOf[Node[E]]
          nextItem = null.asInstanceOf[E]
          breakNow = true
        } else {
          val item = p.item
          if (item != null) {
            nextNode = p
            nextItem = item
            breakNow = true
          }
          p = nextNode(p)
        }
      }
    }

    override def hasNext(): Boolean =
      nextItem != null

    override def next(): E = {
      val item = nextItem
      if (item == null) throw new NoSuchElementException
      advance()
      item
    }

    override def remove(): Unit = {
      val l = lastRet
      if (l == null) throw new IllegalStateException
      l.item = null.asInstanceOf[E]
      unlink(l)
      lastRet = null.asInstanceOf[Node[E]]
    }
  }

  /** Forward iterator */
  private class Itr extends AbstractItr with Iterator[E] {
    override private[concurrent] def startNode = first

    override private[concurrent] def nextNode(p: Node[E]) = succ(p)
  }

  /** Descending iterator */
//  private class DescendingItr[E] extends ConcurrentLinkedDeque.AbstractItr[E] {
  private class DescendingItr extends AbstractItr with Iterator[E] {

    override private[concurrent] def startNode: Node[E] = last

    override private[concurrent] def nextNode(p: Node[E]): Node[E] = pred(p)
  }

  /** Returns a Spliterator over the elements in this deque.
   *
   *  <p>The returned spliterator is <i>weakly consistent</i></a>.
   *
   *  <p>The {@code Spliterator} reports Spliterator#CONCURRENT,
   *  Spliterator#ORDERED, and Spliterator#NONNULL .
   *
   *  Implementation Note: The {@code Spliterator} implements {@code trySplit}
   *  to permit limited parallelism.
   *  @return
   *    a {@code Spliterator} over the elements in this deque
   *  @since 1.8
   */
  override def spliterator() = new CLDSpliterator[E](this)

  private def casHead(cmp: Node[E], `val`: Node[E]): Boolean =
    HEAD.compareExchangeStrong(cmp, `val`)

  private def casTail(cmp: Node[E], `val`: Node[E]): Boolean =
    TAIL.compareExchangeStrong(cmp, `val`)

// No support for ObjectInputStream in Scala Native
//  private def writeObject(s: ObjectOutputStream): Unit
//  private def readObject(s: ObjectInputStream): Unit
}
