/* Ported from JSR-166. Modified for Scala Native.
 * 
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

/* Scala Native Developers Note
 * 
 * The original Doug Lea et al. Java code can be found at URL:
 *   http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/jsr166/src/main/java/util/
 *
 * If you are trying to understand or trace this code, you almost certainly
 * want to study the Oswego Java code.  The original port to Scala Native
 * did some semantic mangling, especially of return statements in methods
 * returning Unit. It also removed some essential comments and
 * elided the blank lines which separate logical sections. Porter's choice,
 * but hard to follow.
 */

import java.io.{ObjectInputStream, ObjectOutputStream, Serializable}
import java.util._
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.{LockSupport, ReentrantLock}
import java.util.function._
import java.util.stream.Stream
import java.{lang => jl, util}

import scala.scalanative.annotation.{align => Contended, safePublish}
import scala.scalanative.libc.stdatomic._
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.unsafe._

// scalafmt: { maxColumn = 120}

@SerialVersionUID(7249069246763182397L)
object ConcurrentHashMap {
  /*
   * Overview:
   *
   * The primary design goal of this hash table is to maintain
   * concurrent readability (typically method get(), but also
   * iterators and related methods) while minimizing update
   * contention. Secondary goals are to keep space consumption about
   * the same or better than java.util.HashMap, and to support high
   * initial insertion rates on an empty table by many threads.
   *
   * This map usually acts as a binned (bucketed) hash table.  Each
   * key-value mapping is held in a Node.  Most nodes are instances
   * of the basic Node class with hash, key, value, and next
   * fields. However, various subclasses exist: TreeNodes are
   * arranged in balanced trees, not lists.  TreeBins hold the roots
   * of sets of TreeNodes. ForwardingNodes are placed at the heads
   * of bins during resizing. ReservationNodes are used as
   * placeholders while establishing values in computeIfAbsent and
   * related methods.  The types TreeBin, ForwardingNode, and
   * ReservationNode do not hold normal user keys, values, or
   * hashes, and are readily distinguishable during search etc.
   * because they have negative hash fields and null key and value
   * fields. (These special nodes are either uncommon or transient,
   * so the impact of carrying around some unused fields is
   * insignificant.)
   *
   * The table is lazily initialized to a power-of-two size upon the
   * first insertion.  Each bin in the table normally contains a
   * list of Nodes (most often, the list has only zero or one Node).
   * Table accesses require volatile/atomic reads, writes, and
   * CASes.  Because there is no other way to arrange this without
   * adding further indirections, we use intrinsics
   * (jdk.internal.misc.Unsafe) operations.
   *
   * We use the top (sign) bit of Node hash fields for control
   * purposes -- it is available anyway because of addressing
   * constraints.  Nodes with negative hash fields are specially
   * handled or ignored in map methods.
   *
   * Insertion (via put or its variants) of the first node in an
   * empty bin is performed by just CASing it to the bin.  This is
   * by far the most common case for put operations under most
   * key/hash distributions.  Other update operations (insert,
   * delete, and replace) require locks.  We do not want to waste
   * the space required to associate a distinct lock object with
   * each bin, so instead use the first node of a bin list itself as
   * a lock. Locking support for these locks relies on builtin
   * "synchronized" monitors.
   *
   * Using the first node of a list as a lock does not by itself
   * suffice though: When a node is locked, any update must first
   * validate that it is still the first node after locking it, and
   * retry if not. Because new nodes are always appended to lists,
   * once a node is first in a bin, it remains first until deleted
   * or the bin becomes invalidated (upon resizing).
   *
   * The main disadvantage of per-bin locks is that other update
   * operations on other nodes in a bin list protected by the same
   * lock can stall, for example when user equals() or mapping
   * functions take a long time.  However, statistically, under
   * random hash codes, this is not a common problem.  Ideally, the
   * frequency of nodes in bins follows a Poisson distribution
   * (http://en.wikipedia.org/wiki/Poisson_distribution) with a
   * parameter of about 0.5 on average, given the resizing threshold
   * of 0.75, although with a large variance because of resizing
   * granularity. Ignoring variance, the expected occurrences of
   * list size k are (exp(-0.5) * pow(0.5, k) / factorial(k)). The
   * first values are:
   *
   * 0:    0.60653066
   * 1:    0.30326533
   * 2:    0.07581633
   * 3:    0.01263606
   * 4:    0.00157952
   * 5:    0.00015795
   * 6:    0.00001316
   * 7:    0.00000094
   * 8:    0.00000006
   * more: less than 1 in ten million
   *
   * Lock contention probability for two threads accessing distinct
   * elements is roughly 1 / (8 * #elements) under random hashes.
   *
   * Actual hash code distributions encountered in practice
   * sometimes deviate significantly from uniform randomness.  This
   * includes the case when N > (1<<30), so some keys MUST collide.
   * Similarly for dumb or hostile usages in which multiple keys are
   * designed to have identical hash codes or ones that differs only
   * in masked-out high bits. So we use a secondary strategy that
   * applies when the number of nodes in a bin exceeds a
   * threshold. These TreeBins use a balanced tree to hold nodes (a
   * specialized form of red-black trees), bounding search time to
   * O(log N).  Each search step in a TreeBin is at least twice as
   * slow as in a regular list, but given that N cannot exceed
   * (1<<64) (before running out of addresses) this bounds search
   * steps, lock hold times, etc., to reasonable constants (roughly
   * 100 nodes inspected per operation worst case) so long as keys
   * are Comparable (which is very common -- String, Long, etc.).
   * TreeBin nodes (TreeNodes) also maintain the same "next"
   * traversal pointers as regular nodes, so can be traversed in
   * iterators in the same way.
   *
   * The table is resized when occupancy exceeds a percentage
   * threshold (nominally, 0.75, but see below).  Any thread
   * noticing an overfull bin may assist in resizing after the
   * initiating thread allocates and sets up the replacement array.
   * However, rather than stalling, these other threads may proceed
   * with insertions etc.  The use of TreeBins shields us from the
   * worst case effects of overfilling while resizes are in
   * progress.  Resizing proceeds by transferring bins, one by one,
   * from the table to the next table. However, threads claim small
   * blocks of indices to transfer (via field transferIndex) before
   * doing so, reducing contention.  A generation stamp in field
   * sizeCtl ensures that resizings do not overlap. Because we are
   * using power-of-two expansion, the elements from each bin must
   * either stay at same index, or move with a power of two
   * offset. We eliminate unnecessary node creation by catching
   * cases where old nodes can be reused because their next fields
   * won't change.  On average, only about one-sixth of them need
   * cloning when a table doubles. The nodes they replace will be
   * garbage collectible as soon as they are no longer referenced by
   * any reader thread that may be in the midst of concurrently
   * traversing table.  Upon transfer, the old table bin contains
   * only a special forwarding node (with hash field "MOVED") that
   * contains the next table as its key. On encountering a
   * forwarding node, access and update operations restart, using
   * the new table.
   *
   * Each bin transfer requires its bin lock, which can stall
   * waiting for locks while resizing. However, because other
   * threads can join in and help resize rather than contend for
   * locks, average aggregate waits become shorter as resizing
   * progresses.  The transfer operation must also ensure that all
   * accessible bins in both the old and new table are usable by any
   * traversal.  This is arranged in part by proceeding from the
   * last bin (table.length - 1) up towards the first.  Upon seeing
   * a forwarding node, traversals (see class Traverser) arrange to
   * move to the new table without revisiting nodes.  To ensure that
   * no intervening nodes are skipped even when moved out of order,
   * a stack (see class TableStack) is created on first encounter of
   * a forwarding node during a traversal, to maintain its place if
   * later processing the current table. The need for these
   * save/restore mechanics is relatively rare, but when one
   * forwarding node is encountered, typically many more will be.
   * So Traversers use a simple caching scheme to avoid creating so
   * many new TableStack nodes. (Thanks to Peter Levart for
   * suggesting use of a stack here.)
   *
   * The traversal scheme also applies to partial traversals of
   * ranges of bins (via an alternate Traverser constructor)
   * to support partitioned aggregate operations.  Also, read-only
   * operations give up if ever forwarded to a null table, which
   * provides support for shutdown-style clearing, which is also not
   * currently implemented.
   *
   * Lazy table initialization minimizes footprint until first use,
   * and also avoids resizings when the first operation is from a
   * putAll, constructor with map argument, or deserialization.
   * These cases attempt to override the initial capacity settings,
   * but harmlessly fail to take effect in cases of races.
   *
   * The element count is maintained using a specialization of
   * LongAdder. We need to incorporate a specialization rather than
   * just use a LongAdder in order to access implicit
   * contention-sensing that leads to creation of multiple
   * CounterCells.  The counter mechanics avoid contention on
   * updates but can encounter cache thrashing if read too
   * frequently during concurrent access. To avoid reading so often,
   * resizing under contention is attempted only upon adding to a
   * bin already holding two or more nodes. Under uniform hash
   * distributions, the probability of this occurring at threshold
   * is around 13%, meaning that only about 1 in 8 puts check
   * threshold (and after resizing, many fewer do so).
   *
   * TreeBins use a special form of comparison for search and
   * related operations (which is the main reason we cannot use
   * existing collections such as TreeMaps). TreeBins contain
   * Comparable elements, but may contain others, as well as
   * elements that are Comparable but not necessarily Comparable for
   * the same T, so we cannot invoke compareTo among them. To handle
   * this, the tree is ordered primarily by hash value, then by
   * Comparable.compareTo order if applicable.  On lookup at a node,
   * if elements are not comparable or compare as 0 then both left
   * and right children may need to be searched in the case of tied
   * hash values. (This corresponds to the full list search that
   * would be necessary if all elements were non-Comparable and had
   * tied hashes.) On insertion, to keep a total ordering (or as
   * close as is required here) across rebalancings, we compare
   * classes and identityHashCodes as tie-breakers. The red-black
   * balancing code is updated from pre-jdk-collections
   * (http://gee.cs.oswego.edu/dl/classes/collections/RBCell.java)
   * based in turn on Cormen, Leiserson, and Rivest "Introduction to
   * Algorithms" (CLR).
   *
   * TreeBins also require an additional locking mechanism.  While
   * list traversal is always possible by readers even during
   * updates, tree traversal is not, mainly because of tree-rotations
   * that may change the root node and/or its linkages.  TreeBins
   * include a simple read-write lock mechanism parasitic on the
   * main bin-synchronization strategy: Structural adjustments
   * associated with an insertion or removal are already bin-locked
   * (and so cannot conflict with other writers) but must wait for
   * ongoing readers to finish. Since there can be only one such
   * waiter, we use a simple scheme using a single "waiter" field to
   * block writers.  However, readers need never block.  If the root
   * lock is held, they proceed along the slow traversal path (via
   * next-pointers) until the lock becomes available or the list is
   * exhausted, whichever comes first. These cases are not fast, but
   * maximize aggregate expected throughput.
   *
   * Maintaining API and serialization compatibility with previous
   * versions of this class introduces several oddities. Mainly: We
   * leave untouched but unused constructor arguments referring to
   * concurrencyLevel. We accept a loadFactor constructor argument,
   * but apply it only to initial table capacity (which is the only
   * time that we can guarantee to honor it.) We also declare an
   * unused "Segment" class that is instantiated in minimal form
   * only when serializing.
   *
   * Also, solely for compatibility with previous versions of this
   * class, it extends AbstractMap, even though all of its methods
   * are overridden, so it is just useless baggage.
   *
   * This file is organized to make things a little easier to follow
   * while reading than they might otherwise: First the main static
   * declarations and utilities, then fields, then main public
   * methods (with a few factorings of multiple public methods into
   * internal ones), then sizing methods, trees, traversers, and
   * bulk operations.
   */
  /* ---------------- Constants -------------- */
  private final val MAXIMUM_CAPACITY = 1 << 30
  private final val DEFAULT_CAPACITY = 16
  private[concurrent] final val MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8
  private final val DEFAULT_CONCURRENCY_LEVEL = 16
  private final val LOAD_FACTOR = 0.75f
  private[concurrent] final val TREEIFY_THRESHOLD = 8
  private[concurrent] final val UNTREEIFY_THRESHOLD = 6
  private[concurrent] final val MIN_TREEIFY_CAPACITY = 64
  private final val MIN_TRANSFER_STRIDE = 16
  private final val RESIZE_STAMP_BITS = 16
  private final val MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1
  private final val RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS
  /*
   * Encodings for Node hash fields. See above for explanation.
   */
  private[concurrent] final val MOVED = -1 // hash for forwarding nodes
  private[concurrent] final val TREEBIN = -2 // hash for roots of trees
  private[concurrent] final val RESERVED = -3 // hash for transient reservations
  private[concurrent] final val HASH_BITS =
    0x7fffffff // usable bits of normal node hash

  private[concurrent] final val NCPU = Runtime.getRuntime().availableProcessors()

  /* ---------------- Nodes -------------- */
  private[concurrent] class Node[K <: AnyRef, V <: AnyRef] private[concurrent] (
      @safePublish private[concurrent] val hash: Int,
      @safePublish private[concurrent] val key: K,
      @volatile private[concurrent] var `val`: V
  ) extends util.Map.Entry[K, V] {
    @volatile private[concurrent] var next: Node[K, V] = _

    def this(hash: Int, key: K, `val`: V, next: Node[K, V]) = {
      this(hash, key, `val`)
      this.next = next
    }

    override final def getKey(): K = key

    override final def getValue(): V = `val`

    override final def hashCode(): Int = key.hashCode() ^ `val`.hashCode()

    override final def toString(): String = Helpers.mapEntryToString(key, `val`)

    override final def setValue(value: V) =
      throw new UnsupportedOperationException

    override final def equals(_o: Any): Boolean = {
      var o = _o.asInstanceOf[AnyRef]
      var k: AnyRef = null
      var v: AnyRef = null
      var u: AnyRef = null
      var e: util.Map.Entry[_, _] = null
      (o.isInstanceOf[util.Map.Entry[_, _]]) && {
        k = { e = o.asInstanceOf[util.Map.Entry[_, _]]; e }.getKey().asInstanceOf[AnyRef]; k
      } != null && { v = e.getValue().asInstanceOf[AnyRef]; v } != null && ((k eq key) || k.equals(key)) &&
        ((v eq { u = `val`; u }) || v.equals(u))
    }

    private[concurrent] def find(h: Int, k: AnyRef): Node[K, V] = {
      var e = this
      if (k != null) while ({
        var ek: K = null.asInstanceOf[K].asInstanceOf[K]
        if (e.hash == h && (({ ek = e.key; ek } eq k) || (ek != null && k.equals(ek))))
          return e
        e = e.next
        e != null
      }) ()
      null
    }
  }

  /* ---------------- Static utilities -------------- */
  private[concurrent] def spread(h: Int) = (h ^ (h >>> 16)) & HASH_BITS

  private def tableSizeFor(c: Int) = {
    val n = -1 >>> Integer.numberOfLeadingZeros(c - 1)
    if (n < 0) 1
    else if (n >= MAXIMUM_CAPACITY) MAXIMUM_CAPACITY
    else n + 1
  }

  private[concurrent] def comparableClassFor(x: AnyRef): Class[_] = {
    val c = x.getClass()
    if (c == classOf[String]) c
    else
      x match {
        case x: Comparable[_] => c
        case _                => null
      }
  }

  private[concurrent] def compareComparables(
      kc: Class[_],
      k: AnyRef,
      x: AnyRef
  ): Int = {
    if (x == null || (x.getClass ne kc)) 0
    else k.asInstanceOf[Comparable[Any]].compareTo(x)
  }

  /* ---------------- Table element access -------------- */
  /*
   * Atomic access methods are used for table elements as well as
   * elements of in-progress next table while resizing.  All uses of
   * the tab arguments must be null checked by callers.  All callers
   * also paranoically precheck that tab's length is not zero (or an
   * equivalent check), thus ensuring that any index argument taking
   * the form of a hash value anded with (length - 1) is a valid
   * index.  Note that, to be correct wrt arbitrary concurrency
   * errors by users, these checks must operate on local variables,
   * which accounts for some odd-looking inline assignments below.
   * Note that calls to setTabAt always occur within locked regions,
   * and so require only release ordering.
   */
  private[concurrent] def tabAt[K <: AnyRef, V <: AnyRef](
      tab: Array[Node[K, V]],
      i: Int
  ) = tab.at(i).atomic.load(memory_order_acquire)
  // U
  //   .getReferenceAcquire(tab, (i.toLong << ASHIFT) + ABASE)
  //   .asInstanceOf[Node[K, V]]

  private[concurrent] def casTabAt[K <: AnyRef, V <: AnyRef](
      tab: Array[Node[K, V]],
      i: Int,
      c: Node[K, V],
      v: Node[K, V]
  ) = tab.at(i).atomic.compareExchangeStrong(c, v)
  // U.compareAndSetReference(tab, (i.toLong << ASHIFT) + ABASE, c, v)

  private[concurrent] def setTabAt[K <: AnyRef, V <: AnyRef](
      tab: Array[Node[K, V]],
      i: Int,
      v: Node[K, V]
  ): Unit = {
    tab.at(i).atomic.store(v, memory_order_release)
    // U.putReferenceRelease(tab, (i.toLong << ASHIFT) + ABASE, v)
  }

  @SerialVersionUID(2249069246763182397L)
  private[concurrent] class Segment[K <: AnyRef, V <: AnyRef] private[concurrent] (
      private[concurrent] val loadFactor: Float
  ) extends ReentrantLock
      with Serializable {}

  def newKeySet[K <: AnyRef] =
    new KeySetView[K, java.lang.Boolean](new ConcurrentHashMap[K, java.lang.Boolean], java.lang.Boolean.TRUE)

  def newKeySet[K <: AnyRef](initialCapacity: Int) =
    new KeySetView[K, java.lang.Boolean](
      new ConcurrentHashMap[K, java.lang.Boolean](initialCapacity),
      java.lang.Boolean.TRUE
    )

  /* ---------------- Special Nodes -------------- */
  private[concurrent] final class ForwardingNode[K <: AnyRef, V <: AnyRef] private[concurrent] (
      private[concurrent] val nextTable: Array[Node[K, V]]
  ) extends Node[K, V](MOVED, null.asInstanceOf[K], null.asInstanceOf[V]) {
    override private[concurrent] def find(h: Int, k: AnyRef): Node[K, V] = {
      // loop to avoid arbitrarily deep recursion on forwarding nodes
      var tab = nextTable
      while (true) {
        var e: Node[K, V] = null
        var n = 0
        if (k == null || tab == null || { n = tab.length; n } == 0 || { e = tabAt(tab, (n - 1) & h); e } == null)
          return null

        var restart = false
        while (!restart) {
          var eh = 0
          var ek: K = null.asInstanceOf[K]
          if ({ eh = e.hash; eh } == h && (({ ek = e.key; ek } eq k) || (ek != null && k.equals(ek))))
            return e
          if (eh < 0) {
            if (e.isInstanceOf[ForwardingNode[_, _]]) {
              tab = e.asInstanceOf[ForwardingNode[K, V]].nextTable
              restart = true
            } else
              return e.find(h, k)
          } else if ({ e = e.next; e } == null)
            return null
        }
      }
      // unreachable
      null
    }
  }

  private[concurrent] final class ReservationNode[K <: AnyRef, V <: AnyRef] private[concurrent]
      extends Node[K, V](RESERVED, null.asInstanceOf[K], null.asInstanceOf[V]) {
    override private[concurrent] def find(
        h: Int,
        k: AnyRef
    ): Node[K, V] = null
  }

  /* ---------------- Table Initialization and Resizing -------------- */
  private[concurrent] def resizeStamp(n: Int) =
    Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1))

  /* ---------------- Counter support -------------- */
  @Contended
  private[concurrent] final class CounterCell private[concurrent] (
      @volatile private[concurrent] var value: Long
  ) {
    @inline def CELLVALUE = fromRawPtr[scala.Long](classFieldRawPtr(this, "value")).atomic
  }

  private[concurrent] def untreeify[K <: AnyRef, V <: AnyRef](b: Node[K, V]) = {
    var hd: Node[K, V] = null
    var tl: Node[K, V] = null
    var q = b
    while (q != null) {
      val p = new Node[K, V](q.hash, q.key, q.`val`)
      if (tl == null) hd = p
      else tl.next = p
      tl = p

      q = q.next
    }
    hd
  }

  /* ---------------- TreeNodes -------------- */
  private[concurrent] final class TreeNode[K <: AnyRef, V <: AnyRef] private[concurrent] (
      hash: Int,
      key: K,
      `val`: V,
      next: Node[K, V],
      private[concurrent] var parent: TreeNode[K, V] // red-black tree links
  ) extends Node[K, V](hash, key, `val`, next) {
    private[concurrent] var left: TreeNode[K, V] = _
    private[concurrent] var right: TreeNode[K, V] = _
    private[concurrent] var prev: TreeNode[K, V] = _ // needed to unlink next upon deletion

    private[concurrent] var red = false

    override private[concurrent] def find(h: Int, k: AnyRef) =
      findTreeNode(h, k, null)

    private[concurrent] final def findTreeNode(
        h: Int,
        k: AnyRef,
        _kc: Class[_]
    ): TreeNode[K, V] = {
      var kc = _kc
      if (k != null) {
        var p = this
        while ({
          var ph = 0
          var dir = 0
          var pk: K = null.asInstanceOf[K]
          var q: TreeNode[K, V] = null
          val pl = p.left
          val pr = p.right
          if ({ ph = p.hash; ph } > h) p = pl
          else if (ph < h) p = pr
          else if (({ pk = p.key; pk } eq k) || (pk != null && k.equals(pk)))
            return p
          else if (pl == null) p = pr
          else if (pr == null) p = pl
          else if ((kc != null || { kc = comparableClassFor(k); kc } != null) &&
              { dir = compareComparables(kc, k, pk); dir } != 0)
            p = if (dir < 0) pl else pr
          else if ({ q = pr.findTreeNode(h, k, kc); q } != null)
            return q
          else p = pl
          p != null
        }) ()
      }
      null
    }
  }

  /* ---------------- TreeBins -------------- */
  private[concurrent] object TreeBin { // values for lockState
    private[concurrent] final val WRITER = 1 // set while holding write lock
    private[concurrent] final val WAITER = 2 // set when waiting for write lock
    private[concurrent] final val READER = 4 // increment value for setting read lock

    private[concurrent] def tieBreakOrder(a: AnyRef, b: AnyRef) = {
      var d = 0
      if (a == null || b == null || { d = a.getClass.getName.compareTo(b.getClass.getName); d } == 0)
        d =
          if (System.identityHashCode(a) <= System.identityHashCode(b)) -(1)
          else 1
      d
    }

    /* ------------------------------------------------------------ */
    // Red-black tree methods, all adapted from CLR
    private[concurrent] def rotateLeft[K <: AnyRef, V <: AnyRef](
        _root: TreeNode[K, V],
        p: TreeNode[K, V]
    ) = {
      var root = _root
      var r: TreeNode[K, V] = null
      var pp: TreeNode[K, V] = null
      var rl: TreeNode[K, V] = null
      if (p != null && { r = p.right; r } != null) {
        if ({ rl = { p.right = r.left; p.right }; rl } != null) rl.parent = p
        if ({ pp = { r.parent = p.parent; r.parent }; pp } == null) { root = r; root }.red = false
        else if (pp.left eq p) pp.left = r
        else pp.right = r
        r.left = p
        p.parent = r
      }
      root
    }

    private[concurrent] def rotateRight[K <: AnyRef, V <: AnyRef](
        _root: TreeNode[K, V],
        p: TreeNode[K, V]
    ) = {
      var root = _root
      var l: TreeNode[K, V] = null
      var pp: TreeNode[K, V] = null
      var lr: TreeNode[K, V] = null
      if (p != null && { l = p.left; l } != null) {
        if ({ lr = { p.left = l.right; p.left }; lr } != null) lr.parent = p
        if ({ pp = { l.parent = p.parent; l.parent }; pp } == null) { root = l; root }.red = false
        else if (pp.right eq p) pp.right = l
        else pp.left = l
        l.right = p
        p.parent = l
      }
      root
    }

    private[concurrent] def balanceInsertion[K <: AnyRef, V <: AnyRef](
        _root: TreeNode[K, V],
        _x: TreeNode[K, V]
    ): TreeNode[K, V] = {
      var root = _root
      var x = _x
      x.red = true
      var xp: TreeNode[K, V] = null
      var xpp: TreeNode[K, V] = null
      var xppl: TreeNode[K, V] = null
      var xppr: TreeNode[K, V] = null
      while (true) {
        if ({ xp = x.parent; xp } == null) {
          x.red = false
          return x
        } else if (!xp.red || { xpp = xp.parent; xpp } == null)
          return root
        if (xp eq { xppl = xpp.left; xppl })
          if ({ xppr = xpp.right; xppr } != null && xppr.red) {
            xppr.red = false
            xp.red = false
            xpp.red = true
            x = xpp
          } else {
            if (x eq xp.right) {
              root = rotateLeft(root, { x = xp; x })
              xpp =
                if ({ xp = x.parent; xp } == null) null
                else xp.parent
            }
            if (xp != null) {
              xp.red = false
              if (xpp != null) {
                xpp.red = true
                root = rotateRight(root, xpp)
              }
            }
          }
        else if (xppl != null && xppl.red) {
          xppl.red = false
          xp.red = false
          xpp.red = true
          x = xpp
        } else {
          if (x eq xp.left) {
            root = rotateRight(root, { x = xp; x })
            xpp =
              if ({ xp = x.parent; xp } == null) null
              else xp.parent
          }
          if (xp != null) {
            xp.red = false
            if (xpp != null) {
              xpp.red = true
              root = rotateLeft(root, xpp)
            }
          }
        }
      }
      // unreachable
      null
    }

    private[concurrent] def balanceDeletion[K <: AnyRef, V <: AnyRef](
        _root: TreeNode[K, V],
        _x: TreeNode[K, V]
    ): TreeNode[K, V] = {
      var root = _root
      var x = _x
      var xp: TreeNode[K, V] = null
      var xpl: TreeNode[K, V] = null
      var xpr: TreeNode[K, V] = null
      while (true) {
        if (x == null || (x eq root))
          return root
        else if ({ xp = x.parent; xp } == null) {
          x.red = false
          return x
        } else if (x.red) {
          x.red = false
          return root
        } else if ({ xpl = xp.left; xpl } eq x) {
          if ({ xpr = xp.right; xpr } != null && xpr.red) {
            xpr.red = false
            xp.red = true
            root = rotateLeft(root, xp)
            xpr =
              if ({ xp = x.parent; xp } == null) null
              else xp.right
          }
          if (xpr == null) x = xp
          else {
            val sl = xpr.left
            var sr = xpr.right
            if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
              xpr.red = true
              x = xp
            } else {
              if (sr == null || !sr.red) {
                if (sl != null) sl.red = false
                xpr.red = true
                root = rotateRight(root, xpr)
                xpr =
                  if ({ xp = x.parent; xp } == null) null
                  else xp.right
              }
              if (xpr != null) {
                xpr.red =
                  if (xp == null) false
                  else xp.red
                if ({ sr = xpr.right; sr } != null) sr.red = false
              }
              if (xp != null) {
                xp.red = false
                root = rotateLeft(root, xp)
              }
              x = root
            }
          }
        } else { // symmetric
          if (xpl != null && xpl.red) {
            xpl.red = false
            xp.red = true
            root = rotateRight(root, xp)
            xpl =
              if ({ xp = x.parent; xp } == null) null
              else xp.left
          }
          if (xpl == null) x = xp
          else {
            var sl = xpl.left
            val sr = xpl.right
            if ((sl == null || !sl.red) && (sr == null || !sr.red)) {
              xpl.red = true
              x = xp
            } else {
              if (sl == null || !sl.red) {
                if (sr != null) sr.red = false
                xpl.red = true
                root = rotateLeft(root, xpl)
                xpl =
                  if ({ xp = x.parent; xp } == null) null
                  else xp.left
              }
              if (xpl != null) {
                xpl.red =
                  if (xp == null) false
                  else xp.red
                if ({ sl = xpl.left; sl } != null) sl.red = false
              }
              if (xp != null) {
                xp.red = false
                root = rotateRight(root, xp)
              }
              x = root
            }
          }
        }
      }
      // unreachable
      null
    }

    private[concurrent] def checkInvariants[K <: AnyRef, V <: AnyRef](
        t: TreeNode[K, V]
    ): Boolean = {
      val tp = t.parent
      val tl = t.left
      val tr = t.right
      val tb = t.prev
      val tn = t.next.asInstanceOf[TreeNode[K, V]]
      if (tb != null && (tb.next ne t)) return false
      if (tn != null && (tn.prev ne t)) return false
      if (tp != null && (t ne tp.left) && (t ne tp.right)) return false
      if (tl != null && ((tl.parent ne t) || tl.hash > t.hash)) return false
      if (tr != null && ((tr.parent ne t) || tr.hash < t.hash)) return false
      if (t.red && tl != null && tl.red && tr != null && tr.red) return false
      if (tl != null && !checkInvariants(tl)) return false
      if (tr != null && !checkInvariants(tr)) return false
      true
    }

  }

  private[concurrent] final class TreeBin[K <: AnyRef, V <: AnyRef] private[concurrent] (
      @volatile private[concurrent] var first: TreeNode[K, V]
  ) extends Node[K, V](TREEBIN, null.asInstanceOf[K], null.asInstanceOf[V]) {
    @volatile private[concurrent] var waiter: Thread = _
    @volatile private[concurrent] var lockState = 0

    @inline def LOCKSTATE = fromRawPtr[scala.Int](classFieldRawPtr(this, "lockState")).atomic

    private[concurrent] var root: TreeNode[K, V] = {
      var r: TreeNode[K, V] = null
      var value: TreeNode[K, V] = first
      var next: TreeNode[K, V] = null
      while (value != null) {
        next = value.next.asInstanceOf[TreeNode[K, V]]
        value.left = null
        value.right = null
        if (r == null) {
          value.parent = null
          value.red = false
          r = value
        } else {
          val k = value.key
          val h = value.hash
          var kc: Class[_] = null
          var p = r
          var break = false
          while (!break) {
            var dir = 0
            var ph = 0
            val pk = p.key
            if ({ ph = p.hash; ph } > h) dir = -1
            else if (ph < h) dir = 1
            else if (kc == null && ({ kc = comparableClassFor(k); kc } == null) ||
                ({ dir = compareComparables(kc, k, pk); dir }) == 0)
              dir = TreeBin.tieBreakOrder(k, pk)
            val xp = p
            p =
              if (dir <= 0) p.left
              else p.right
            if (p == null) {
              value.parent = xp
              if (dir <= 0) xp.left = value
              else xp.right = value
              r = TreeBin.balanceInsertion(r, value)
              break = true
            }
          }
        }
        value = next
      }
      assert(TreeBin.checkInvariants(r))
      r
    }

    private final def lockRoot(): Unit = {
      if (!this.LOCKSTATE.compareExchangeStrong(0, TreeBin.WRITER))
        contendedLock() // offload to separate method
    }

    private final def unlockRoot(): Unit = {
      lockState = 0
    }

    private final def contendedLock(): Unit = {
      var waiting = false
      var s = 0
      while (true)
        if (({ s = lockState; s } & ~TreeBin.WAITER) == 0)
          if (this.LOCKSTATE.compareExchangeStrong(s, TreeBin.WRITER)) {
            if (waiting) waiter = null
            return
          } else if ((s & TreeBin.WAITER) == 0)
            if (this.LOCKSTATE.compareExchangeStrong(s, s | TreeBin.WAITER)) {
              waiting = true
              waiter = Thread.currentThread()
            } else if (waiting) LockSupport.park(this)
    }

    override private[concurrent] final def find(
        h: Int,
        k: AnyRef
    ): Node[K, V] = {
      if (k != null) {
        var e: Node[K, V] = first
        while (e != null) {
          var s = 0
          var ek: K = null.asInstanceOf[K]
          if (({ s = lockState; s } & (TreeBin.WAITER | TreeBin.WRITER)) != 0) {
            if (e.hash == h && (({ ek = e.key; ek } eq k) || (ek != null && k.equals(ek))))
              return e
            e = e.next
          } else if (this.LOCKSTATE.compareExchangeStrong(s, s + TreeBin.READER)) {
            var r: TreeNode[K, V] = null
            var p: TreeNode[K, V] = null
            try
              p =
                if ({ r = root; r } == null) null
                else r.findTreeNode(h, k, null)
            finally {
              var w: Thread = null
              if (this.LOCKSTATE.fetchAdd(-TreeBin.READER) == (TreeBin.READER | TreeBin.WAITER) &&
                  { w = waiter; w } != null) LockSupport.unpark(w)
            }
            return p
          }
        }
      }
      null
    }

    private[concurrent] final def putTreeVal(
        h: Int,
        k: K,
        v: V
    ): TreeNode[K, V] = {
      var kc: Class[_] = null
      var searched = false
      var p = root
      var break = false
      while (!break) {
        var dir = 0
        var ph = 0
        var pk: K = null.asInstanceOf[K]
        if (p == null) {
          first = { root = new TreeNode[K, V](h, k, v, null, null); root }
          break = true
        } else if ({ ph = p.hash; ph } > h) dir = -1
        else if (ph < h) dir = 1
        else if (({ pk = p.key; pk } eq k) || (pk != null && k.equals(pk)))
          return p
        else if ((kc == null && { kc = comparableClassFor(k); kc } == null) ||
            { dir = compareComparables(kc, k, pk); dir } == 0) {
          if (!searched) {
            var q: TreeNode[K, V] = null
            var ch: TreeNode[K, V] = null
            searched = true
            if (({ ch = p.left; ch } != null && { q = ch.findTreeNode(h, k, kc); q } != null) ||
                ({ ch = p.right; ch } != null && { q = ch.findTreeNode(h, k, kc); q } != null))
              return q
          }
          dir = TreeBin.tieBreakOrder(k, pk)
        }
        if (!break) {
          val xp = p
          p =
            if (dir <= 0) p.left
            else p.right
          if (p == null) {
            val f = first
            var x = new TreeNode[K, V](h, k, v, f, xp)
            first = x
            if (f != null) f.prev = x
            if (dir <= 0) xp.left = x
            else xp.right = x
            if (!xp.red) x.red = true
            else {
              lockRoot()
              try root = TreeBin.balanceInsertion(root, x)
              finally unlockRoot()
            }
            break = true
          }
        }
      }
      assert(TreeBin.checkInvariants(root))
      null
    }

    private[concurrent] final def removeTreeNode(
        p: TreeNode[K, V]
    ): Boolean = {
      val next = p.next.asInstanceOf[TreeNode[K, V]]
      val pred = p.prev // unlink traversal pointers

      var r: TreeNode[K, V] = null
      var rl: TreeNode[K, V] = null
      if (pred == null) first = next
      else pred.next = next
      if (next != null) next.prev = pred
      if (first == null) {
        root = null
        return true
      }
      if ({ r = root; r } == null || r.right == null || // too small
          { rl = r.left; rl } == null || rl.left == null)
        return true
      lockRoot()
      try {
        var replacement: TreeNode[K, V] = null
        val pl = p.left
        val pr = p.right
        if (pl != null && pr != null) {
          var s = pr
          var sl: TreeNode[K, V] = null
          while ({ sl = s.left; sl } != null) s = sl // find successor
          val c = s.red
          s.red = p.red
          p.red = c // swap colors

          val sr = s.right
          val pp = p.parent
          if (s eq pr) { // p was s's direct parent
            p.parent = s
            s.right = p
          } else {
            val sp = s.parent
            if ({ p.parent = sp; sp } != null)
              if (s eq sp.left) sp.left = p
              else sp.right = p
            if ({ s.right = pr; pr } != null) pr.parent = s
          }
          p.left = null
          if ({ p.right = sr; sr } != null) sr.parent = p
          if ({ s.left = pl; pl } != null) pl.parent = s
          if ({ s.parent = pp; pp } == null) r = s
          else if (p eq pp.left) pp.left = s
          else pp.right = s
          if (sr != null) replacement = sr
          else replacement = p
        } else if (pl != null) replacement = pl
        else if (pr != null) replacement = pr
        else replacement = p
        if (replacement ne p) {
          val pp = { replacement.parent = p.parent; p.parent }
          if (pp == null) r = replacement
          else if (p eq pp.left) pp.left = replacement
          else pp.right = replacement
          p.left = null
          p.right = null
          p.parent = null
        }
        root =
          if (p.red) r
          else TreeBin.balanceDeletion(r, replacement)
        if (p eq replacement) { // detach pointers
          var pp: TreeNode[K, V] = null
          if ({ pp = p.parent; pp } != null) {
            if (p eq pp.left) pp.left = null
            else if (p eq pp.right) pp.right = null
            p.parent = null
          }
        }
      } finally unlockRoot()
      assert(TreeBin.checkInvariants(root))
      false
    }
  }

  /* ----------------Table Traversal -------------- */
  private[concurrent] final class TableStack[K <: AnyRef, V <: AnyRef] {
    private[concurrent] var length = 0
    private[concurrent] var index = 0
    private[concurrent] var tab: Array[Node[K, V]] = _
    private[concurrent] var next: TableStack[K, V] = _
  }

  private[concurrent] class Traverser[K <: AnyRef, V <: AnyRef] private[concurrent] (
      private[concurrent] var tab: Array[Node[K, V]], // current table; updated if resized
      private[concurrent] val baseSize: Int, // initial table size
      private[concurrent] var index: Int,
      private[concurrent] var baseLimit: Int // index bound for initial table
  ) {
    private[concurrent] var baseIndex = index // current index of initial table
    private[concurrent] var _next: Node[K, V] = _ // the next entry to use

    private[concurrent] var stack: TableStack[K, V] = _
    private[concurrent] var spare: TableStack[K, V] = _ // to save/restore on ForwardingNodes

    private[concurrent] final def advance(): Node[K, V] = {
      var e: Node[K, V] = null
      if ({ e = _next; e } != null) e = e.next

      while (true) {
        var t: Array[Node[K, V]] = null
        var i = 0
        var n = 0 // must use locals in checks

        if (e != null)
          return { _next = e; _next }

        if (baseIndex >= baseLimit || { t = tab; t } == null ||
            { n = t.length; n } <= { i = index; i } || i < 0)
          return { _next = null; _next }
        var continue = false
        if ({ e = tabAt(t, i); e } != null && e.hash < 0)
          if (e.isInstanceOf[ForwardingNode[_, _]]) {
            tab = e.asInstanceOf[ForwardingNode[K, V]].nextTable
            e = null
            pushState(t, i, n)
            continue = true
          } else if (e.isInstanceOf[TreeBin[_, _]])
            e = e.asInstanceOf[TreeBin[K, V]].first
          else e = null
        if (!continue) {
          if (stack != null) recoverState(n)
          else if ({ index = i + baseSize; index } >= n)
            index = { baseIndex += 1; baseIndex } // visit upper slots if present
        }
      }
      // unreachable
      null
    }

    private def pushState(
        t: Array[Node[K, V]],
        i: Int,
        _n: Int
    ): Unit = {
      var n = _n
      var s = spare // reuse if possible

      if (s != null) spare = s.next
      else s = new TableStack[K, V]
      s.tab = t
      s.length = n
      s.index = i
      s.next = stack
      stack = s
    }

    private def recoverState(_n: Int): Unit = {
      var n = _n
      var s: TableStack[K, V] = null
      var len = 0
      while ({ s = stack; s } != null && { index += { len = s.length; len }; index } >= n) {
        n = len
        index = s.index
        tab = s.tab
        s.tab = null
        val next = s.next
        s.next = spare // save for reuse

        stack = next
        spare = s
      }
      if (s == null && { index += baseSize; index } >= n)
        index = { baseIndex += 1; baseIndex }
    }
  }

  private[concurrent] abstract class BaseIterator[K <: AnyRef, V <: AnyRef, IterateType <: AnyRef] private[concurrent] (
      tab: Array[Node[K, V]],
      size: Int,
      index: Int,
      limit: Int,
      private[concurrent] val map: ConcurrentHashMap[K, V]
  ) extends Traverser[K, V](tab, size, index, limit)
      with Iterator[IterateType] {
    advance()

    private[concurrent] var lastReturned: Node[K, V] = _

    final def hasNext(): Boolean = _next != null
    final def hasMoreElements(): Boolean = _next != null

    override final def remove(): Unit = {
      var p: Node[K, V] = null
      if ({ p = lastReturned; p } == null) throw new IllegalStateException
      lastReturned = null
      map.replaceNode(p.key, null.asInstanceOf[V], null.asInstanceOf[V])
    }
  }

  private[concurrent] final class KeyIterator[K <: AnyRef, V <: AnyRef] private[concurrent] (
      tab: Array[Node[K, V]],
      size: Int,
      index: Int,
      limit: Int,
      map: ConcurrentHashMap[K, V]
  ) extends BaseIterator[K, V, K](tab, size, index, limit, map)
      with Enumeration[K] {
    override final def next(): K = {
      var p: Node[K, V] = null
      if ({ p = _next; p } == null) throw new NoSuchElementException
      val k = p.key
      lastReturned = p
      advance()
      k
    }

    override final def nextElement(): K = next()
  }

  private[concurrent] final class ValueIterator[K <: AnyRef, V <: AnyRef] private[concurrent] (
      tab: Array[Node[K, V]],
      size: Int,
      index: Int,
      limit: Int,
      map: ConcurrentHashMap[K, V]
  ) extends BaseIterator[K, V, V](tab, size, index, limit, map)
      with Enumeration[V] {
    override final def next(): V = {
      var p: Node[K, V] = null
      if ({ p = _next; p } == null) throw new NoSuchElementException
      val v = p.`val`
      lastReturned = p
      advance()
      v
    }

    override final def nextElement(): V = next()
  }

  private[concurrent] final class EntryIterator[K <: AnyRef, V <: AnyRef] private[concurrent] (
      tab: Array[Node[K, V]],
      size: Int,
      index: Int,
      limit: Int,
      map: ConcurrentHashMap[K, V]
  ) extends BaseIterator[K, V, util.Map.Entry[K, V]](tab, size, index, limit, map) {
    override final def next(): util.Map.Entry[K, V] = {
      var p: Node[K, V] = null
      if ({ p = _next; p } == null) throw new NoSuchElementException
      val k = p.key
      val v = p.`val`
      lastReturned = p
      advance()
      new MapEntry[K, V](k, v, map)
    }
  }

  private[concurrent] final class MapEntry[K <: AnyRef, V <: AnyRef] private[concurrent] (
      private[concurrent] val key: K // non-null
      ,
      private[concurrent] var `val`: V // non-null
      ,
      private[concurrent] val map: ConcurrentHashMap[K, V]
  ) extends util.Map.Entry[K, V] {
    override def getKey(): K = key

    override def getValue(): V = `val`

    override def hashCode(): Int = key.hashCode() ^ `val`.hashCode()

    override def toString(): String = Helpers.mapEntryToString(key, `val`)

    override def equals(_o: Any): Boolean = {
      val o = _o.asInstanceOf[AnyRef]
      var k: AnyRef = null
      var v: AnyRef = null
      var e: util.Map.Entry[_, _] = null
      (o.isInstanceOf[util.Map.Entry[_, _]]) && {
        k = { e = o.asInstanceOf[util.Map.Entry[_, _]]; e }.getKey().asInstanceOf[AnyRef]; k
      } != null && {
        v = e.getValue().asInstanceOf[AnyRef]; v
      } != null &&
        ((k eq key) || k.equals(key)) && ((v eq `val`) || v.equals(`val`))
    }

    override def setValue(value: V): V = {
      if (value == null) throw new NullPointerException
      val v = `val`
      `val` = value
      map.put(key, value)
      v
    }
  }

  private[concurrent] final class KeySpliterator[K <: AnyRef, V <: AnyRef] private[concurrent] (
      tab: Array[Node[K, V]],
      size: Int,
      index: Int,
      limit: Int,
      private[concurrent] var est: Long // size estimate
  ) extends Traverser[K, V](tab, size, index, limit)
      with Spliterator[K] {
    override def trySplit(): KeySpliterator[K, V] = {
      var i = 0
      var f = 0
      var h = 0
      if (({ h = { i = baseIndex; i } + { f = baseLimit; f }; h } >>> 1) <= i) null
      else
        new KeySpliterator[K, V](
          tab,
          baseSize,
          { baseLimit = h; h },
          f,
          { est >>>= 1; est }
        )
    }

    override def forEachRemaining(action: Consumer[_ >: K]): Unit = {
      if (action == null) throw new NullPointerException
      var p: Node[K, V] = null
      while ({ p = advance(); p } != null) action.accept(p.key)
    }

    override def tryAdvance(action: Consumer[_ >: K]): Boolean = {
      if (action == null) throw new NullPointerException
      var p: Node[K, V] = null
      if ({ p = advance(); p } == null)
        return false
      action.accept(p.key)
      true
    }

    override def estimateSize(): Long = est

    override def characteristics(): Int =
      Spliterator.DISTINCT | Spliterator.CONCURRENT | Spliterator.NONNULL
  }

  private[concurrent] final class ValueSpliterator[K <: AnyRef, V <: AnyRef] private[concurrent] (
      tab: Array[Node[K, V]],
      size: Int,
      index: Int,
      limit: Int,
      private[concurrent] var est: Long // size estimate
  ) extends Traverser[K, V](tab, size, index, limit)
      with Spliterator[V] {
    override def trySplit(): ValueSpliterator[K, V] = {
      val i = baseIndex
      val f = baseLimit
      val h = i + f
      if ((h >>> 1) <= i) null
      else
        new ValueSpliterator[K, V](
          tab,
          baseSize,
          { baseLimit = h; baseLimit },
          f,
          { est >>>= 1; est }
        )
    }

    override def forEachRemaining(action: Consumer[_ >: V]): Unit = {
      if (action == null) throw new NullPointerException
      var p: Node[K, V] = null
      while ({ p = advance(); p } != null) action.accept(p.`val`)
    }

    override def tryAdvance(action: Consumer[_ >: V]): Boolean = {
      if (action == null) throw new NullPointerException
      var p: Node[K, V] = null
      if ({ p = advance(); p } == null)
        return false
      action.accept(p.`val`)
      true
    }

    override def estimateSize(): Long = est

    override def characteristics(): Int =
      Spliterator.CONCURRENT | Spliterator.NONNULL
  }

  private[concurrent] final class EntrySpliterator[K <: AnyRef, V <: AnyRef] private[concurrent] (
      tab: Array[Node[K, V]],
      size: Int,
      index: Int,
      limit: Int,
      private[concurrent] var est: Long, // size estimate
      private[concurrent] val map: ConcurrentHashMap[K, V] // To export MapEntry
  ) extends Traverser[K, V](tab, size, index, limit)
      with Spliterator[util.Map.Entry[K, V]] {
    override def trySplit(): EntrySpliterator[K, V] = {
      var i = 0
      var f = 0
      var h = 0
      if (({ h = { i = baseIndex; i } + { f = baseLimit; f }; h } >>> 1) <= i) null
      else
        new EntrySpliterator[K, V](
          tab,
          baseSize,
          { baseLimit = h; h },
          f,
          { est >>>= 1; est },
          map
        )
    }

    override def forEachRemaining(
        action: Consumer[_ >: util.Map.Entry[K, V]]
    ): Unit = {
      if (action == null) throw new NullPointerException
      var p: Node[K, V] = null
      while ({ p = advance(); p } != null)
        action.accept(new MapEntry[K, V](p.key, p.`val`, map))
    }

    override def tryAdvance(action: Consumer[_ >: util.Map.Entry[K, V]]): Boolean = {
      if (action == null) throw new NullPointerException
      var p: Node[K, V] = null
      if ({ p = advance(); p } == null)
        return false
      action.accept(new MapEntry[K, V](p.key, p.`val`, map))
      true
    }

    override def estimateSize(): Long = est

    override def characteristics(): Int =
      Spliterator.DISTINCT | Spliterator.CONCURRENT | Spliterator.NONNULL
  }

  /* ----------------Views -------------- */
  @SerialVersionUID(7249069246763182397L)
  private[concurrent] object CollectionView {
    private val OOME_MSG = "Required array size too large"
  }

  @SerialVersionUID(7249069246763182397L)
  private[concurrent] abstract class CollectionView[K <: AnyRef, V <: AnyRef, E <: AnyRef] private[concurrent] (
      private[concurrent] val map: ConcurrentHashMap[K, V]
  ) extends Collection[E]
      with Serializable {

    def getMap: ConcurrentHashMap[K, V] = map

    override final def clear(): Unit = {
      map.clear()
    }

    override final def size(): Int = map.size()

    override final def isEmpty(): Boolean = map.isEmpty()

    // implementations below rely on concrete classes supplying these
    // abstract methods
    override def iterator(): Iterator[E]

    override def contains(o: Any): Boolean
    override def remove(o: Any): Boolean

    override final def toArray(): Array[AnyRef] = {
      val sz = map.mappingCount
      if (sz > MAX_ARRAY_SIZE)
        throw new OutOfMemoryError(CollectionView.OOME_MSG)
      var n = sz.toInt
      var r = new Array[AnyRef](n)
      var i = 0
      this.forEach {
        case (e: AnyRef) =>
          if (i == n) {
            if (n >= MAX_ARRAY_SIZE)
              throw new OutOfMemoryError(CollectionView.OOME_MSG)
            if (n >= MAX_ARRAY_SIZE - (MAX_ARRAY_SIZE >>> 1) - 1)
              n = MAX_ARRAY_SIZE
            else n += (n >>> 1) + 1
            r = Arrays.copyOf(r, n)
          }
          r(i) = e
          i += 1
      }
      if (i == n) r
      else Arrays.copyOf(r, i)
    }

    override final def toArray[T <: AnyRef](
        a: Array[T]
    ): Array[T] = {
      val sz = map.mappingCount
      if (sz > MAX_ARRAY_SIZE)
        throw new OutOfMemoryError(CollectionView.OOME_MSG)
      val m = sz.toInt
      var r =
        if (a.length >= m) a
        else
          java.lang.reflect.Array
            .newInstance(a.getClass.getComponentType, m)
            .asInstanceOf[Array[T]]
      var n = r.length
      var i = 0
      this.forEach { e =>
        if (i == n) {
          if (n >= MAX_ARRAY_SIZE)
            throw new OutOfMemoryError(CollectionView.OOME_MSG)
          if (n >= MAX_ARRAY_SIZE - (MAX_ARRAY_SIZE >>> 1) - 1)
            n = MAX_ARRAY_SIZE
          else n += (n >>> 1) + 1
          r = Arrays.copyOf(r, n)
        }
        r(i) = e.asInstanceOf[T]
        i += 1
      }
      if ((a eq r) && i < n) {
        r(i) = null.asInstanceOf[T] // null-terminate
        return r
      }
      if (i == n) r
      else Arrays.copyOf(r, i)
    }

    override final def toString(): String = {
      val sb = new jl.StringBuilder()
      sb.append('[')
      val it = iterator()
      var break = false
      if (it.hasNext()) while (!break) {
        val e = it.next().asInstanceOf[AnyRef]
        sb.append(
          if (e eq this) "(this Collection)"
          else e
        )
        if (!it.hasNext()) break = true
        else sb.append(',').append(' ')
      }
      sb.append(']').toString()
    }

    override final def containsAll(c: Collection[_]): Boolean = {
      if (c ne this) {
        val it = c.iterator()
        while (it.hasNext()) {
          val e = it.next().asInstanceOf[AnyRef]
          if (e == null || !contains(e))
            return false
        }
      }
      true
    }

    override def removeAll(c: Collection[_]): Boolean = {
      if (c == null) throw new NullPointerException
      var modified = false
      // Use (c instanceof Set) as a hint that lookup in c is as
      // efficient as this view
      var t: Array[Node[K, V]] = null
      if ({ t = map.table; t } == null)
        return false
      else if (c.isInstanceOf[Set[_]] && c.size() > t.length) {
        val it = iterator()
        while (it.hasNext()) if (c.contains(it.next())) {
          it.remove()
          modified = true
        }
      } else c.forEach { case (e: AnyRef) => modified |= remove(e) }
      modified
    }

    override final def retainAll(c: Collection[_]): Boolean = {
      if (c == null) throw new NullPointerException
      var modified = false
      val it = iterator()
      while (it.hasNext()) if (!c.contains(it.next())) {
        it.remove()
        modified = true
      }
      modified
    }
  }

  @SerialVersionUID(7249069246763182397L)
  class KeySetView[K <: AnyRef, V <: AnyRef] private[concurrent] (
      map: ConcurrentHashMap[K, V],
      private val value: V
  ) extends CollectionView[K, V, K](map)
      with Set[K]
      with Serializable {
// non-public

    def getMappedValue = value

    override def contains(o: Any) = map.containsKey(o)
    override def remove(o: Any) = map.remove(o) != null

    override def iterator() = {
      var t: Array[Node[K, V]] = null
      val m = map
      val f =
        if ({ t = m.table; t } == null) 0
        else t.length
      new KeyIterator[K, V](t, f, 0, f, m)
    }

    override def add(e: K) = {
      var v: V = null.asInstanceOf[V]
      if ({ v = value; v } == null) throw new UnsupportedOperationException
      map.putVal(e, v, true) == null
    }

    override def addAll(c: Collection[_ <: K]) = {
      var added = false
      var v: V = null.asInstanceOf[V]
      if ({ v = value; v } == null) throw new UnsupportedOperationException
      c.forEach { e =>
        if (map.putVal(e, v, true) == null) added = true
      }
      added
    }
    override def hashCode() = {
      var h = 0
      this.forEach { e => h += e.hashCode() }
      h
    }
    override def equals(_o: Any) = {
      val o = _o.asInstanceOf[AnyRef]
      var c: Set[_] = null
      (o.isInstanceOf[Set[_]]) &&
        (({ c = o.asInstanceOf[Set[_]]; c } eq this) ||
        (containsAll(c) && c.containsAll(this)))
    }
    override def spliterator() = {
      var t: Array[Node[K, V]] = null
      val m = map
      val n = m.sumCount
      val f =
        if ({ t = m.table; t } == null) 0
        else t.length
      new KeySpliterator[K, V](
        t,
        f,
        0,
        f,
        if (n < 0L) 0L
        else n
      )
    }
    override def forEach(action: Consumer[_ >: K]): Unit = {
      if (action == null) throw new NullPointerException
      var t: Array[Node[K, V]] = null
      if ({ t = map.table; t } != null) {
        val it = new Traverser[K, V](t, t.length, 0, t.length)
        var p: Node[K, V] = null
        while ({ p = it.advance(); p } != null) action.accept(p.key)
      }
    }
  }

  @SerialVersionUID(2249069246763182397L)
  private[concurrent] final class ValuesView[K <: AnyRef, V <: AnyRef] private[concurrent] (
      map: ConcurrentHashMap[K, V]
  ) extends CollectionView[K, V, V](map)
      with Collection[V]
      with Serializable {
    override final def contains(o: Any): Boolean = map.containsValue(o)
    override final def remove(o: Any): Boolean = {
      if (o != null) {
        val it = iterator()
        while (it.hasNext()) if (o.equals(it.next())) {
          it.remove()
          return true
        }
      }
      false
    }

    override final def iterator(): Iterator[V] = {
      val m = map
      var t: Array[Node[K, V]] = null
      val f =
        if ({ t = m.table; t } == null) 0
        else t.length
      new ValueIterator[K, V](t, f, 0, f, m)
    }

    override final def add(e: V) = throw new UnsupportedOperationException

    override final def addAll(c: Collection[_ <: V]) =
      throw new UnsupportedOperationException

    override def removeAll(c: Collection[_]): Boolean = {
      if (c == null) throw new NullPointerException
      var modified = false
      val it = iterator()
      while (it.hasNext()) if (c.contains(it.next())) {
        it.remove()
        modified = true
      }
      modified
    }

    override def removeIf(filter: Predicate[_ >: V]): Boolean =
      map.removeValueIf(filter)

    override def spliterator(): Spliterator[V] = {
      var t: Array[Node[K, V]] = null
      val m = map
      val n = m.sumCount
      val f =
        if ({ t = m.table; t } == null) 0
        else t.length
      new ValueSpliterator[K, V](
        t,
        f,
        0,
        f,
        if (n < 0L) 0L
        else n
      )
    }

    override def forEach(action: Consumer[_ >: V]): Unit = {
      if (action == null) throw new NullPointerException
      var t: Array[Node[K, V]] = null
      if ({ t = map.table; t } != null) {
        val it = new Traverser[K, V](t, t.length, 0, t.length)
        var p: Node[K, V] = null
        while ({ p = it.advance(); p } != null) action.accept(p.`val`)
      }
    }
  }

  @SerialVersionUID(2249069246763182397L)
  private[concurrent] final class EntrySetView[K <: AnyRef, V <: AnyRef] private[concurrent] (
      map: ConcurrentHashMap[K, V]
  ) extends CollectionView[K, V, util.Map.Entry[K, V]](map)
      with Set[util.Map.Entry[K, V]]
      with Serializable {
    override def contains(_o: Any): Boolean = {
      val o = _o.asInstanceOf[AnyRef]
      var k: AnyRef = null
      var v: AnyRef = null
      var r: AnyRef = null
      var e: util.Map.Entry[_, _] = null
      (o.isInstanceOf[util.Map.Entry[_, _]]) && {
        e = o.asInstanceOf[util.Map.Entry[_, _]];
        k = e.getKey().asInstanceOf[AnyRef]; k
      } != null && { r = map.get(k); r } != null && {
        v = e.getValue().asInstanceOf[AnyRef]; v
      } != null &&
        ((v eq r) || v.equals(r))
    }

    override def remove(o: Any): Boolean = {
      var k: AnyRef = null
      var v: AnyRef = null
      var e: util.Map.Entry[_, _] = null
      (o.isInstanceOf[util.Map.Entry[_, _]]) && {
        e = o.asInstanceOf[util.Map.Entry[_, _]]
        k = e.getKey().asInstanceOf[AnyRef]; k
      } != null && { v = e.getValue().asInstanceOf[AnyRef]; v } != null &&
        map.remove(k, v)
    }

    override def iterator(): Iterator[util.Map.Entry[K, V]] = {
      val m = map
      var t: Array[Node[K, V]] = null
      val f =
        if ({ t = m.table; t } == null) 0
        else t.length
      new EntryIterator[K, V](t, f, 0, f, m)
    }

    override def add(e: util.Map.Entry[K, V]): Boolean =
      map.putVal(e.getKey(), e.getValue(), false) == null

    override def addAll(c: Collection[_ <: util.Map.Entry[K, V]]): Boolean = {
      var added = false
      c.forEach { e => if (add(e)) added = true }
      added
    }

    override def removeIf(
        filter: Predicate[_ >: util.Map.Entry[K, V]]
    ): Boolean = map.removeEntryIf(filter)

    override final def hashCode(): Int = {
      var h = 0
      var t: Array[Node[K, V]] = null
      if ({ t = map.table; t } != null) {
        val it = new Traverser[K, V](t, t.length, 0, t.length)
        var p: Node[K, V] = null
        while ({ p = it.advance(); p } != null) h += p.hashCode()
      }
      h
    }

    override final def equals(_o: Any): Boolean = {
      val o = _o.asInstanceOf[AnyRef]
      var c: Set[_] = null
      (o.isInstanceOf[Set[_]]) &&
        (({ c = o.asInstanceOf[Set[_]]; c } eq this) ||
        (containsAll(c) && c.containsAll(this)))
    }

    override def spliterator(): Spliterator[util.Map.Entry[K, V]] = {
      var t: Array[Node[K, V]] = null
      val m = map
      val n = m.sumCount
      val f =
        if ({ t = m.table; t } == null) 0
        else t.length
      new EntrySpliterator[K, V](
        t,
        f,
        0,
        f,
        if (n < 0L) 0L
        else n,
        m
      )
    }

    override def forEach(action: Consumer[_ >: util.Map.Entry[K, V]]): Unit = {
      if (action == null) throw new NullPointerException
      var t: Array[Node[K, V]] = null
      if ({ t = map.table; t } != null) {
        val it = new Traverser[K, V](t, t.length, 0, t.length)
        var p: Node[K, V] = null
        while ({ p = it.advance(); p } != null)
          action.accept(
            new MapEntry[K, V](p.key, p.`val`, map)
          )
      }
    }
  }

// -------------------------------------------------------
  private[concurrent] abstract class BulkTask[K <: AnyRef, V <: AnyRef, R] private[concurrent] (
      par: BulkTask[K, V, _],
      private[concurrent] var batch: Int, // split control
      i: Int,
      f: Int,
      t: Array[Node[K, V]]
  ) extends CountedCompleter[R](par) {
    private[concurrent] var tab: Array[Node[K, V]] = t // same as Traverser
    private[concurrent] var next: Node[K, V] = _
    private[concurrent] var stack: TableStack[K, V] = _
    private[concurrent] var spare: TableStack[K, V] = _
    private[concurrent] var index = i
    private[concurrent] var baseIndex = i
    private[concurrent] var baseLimit = 0
    private[concurrent] final var baseSize = 0

    if (t == null) {
      this.baseSize = 0
      this.baseLimit = 0
    } else if (par == null) {
      this.baseSize = t.length
      this.baseLimit = t.length
    } else {
      this.baseLimit = f
      this.baseSize = par.baseSize
    }

    private[concurrent] final def advance(): Node[K, V] = {
      var e: Node[K, V] = null
      if ({ e = next; e } != null) e = e.next

      while (true) {
        var t: Array[Node[K, V]] = null
        var i = 0
        var n = 0
        if (e != null)
          return { next = e; e }
        if (baseIndex >= baseLimit || { t = tab; t } == null || { n = t.length; n } <= { i = index; i } || i < 0)
          return { next = null; null }
        var continue = false
        if ({ e = tabAt[K, V](t, i); e } != null && e.hash < 0)
          if (e.isInstanceOf[ForwardingNode[_, _]]) {
            tab = e.asInstanceOf[ForwardingNode[K, V]].nextTable
            e = null
            pushState(t, i, n)
            continue = true
          } else if (e.isInstanceOf[TreeBin[_, _]])
            e = e.asInstanceOf[TreeBin[K, V]].first
          else e = null
        if (!continue) {
          if (stack != null) recoverState(n)
          else if ({ index = i + baseSize; index } >= n)
            index = { baseIndex += 1; baseIndex }
        }
      }
      null // unreachable
    }

    private def pushState(
        t: Array[Node[K, V]],
        i: Int,
        n: Int
    ): Unit = {
      var s = spare
      if (s != null) spare = s.next
      else s = new TableStack[K, V]
      s.tab = t
      s.length = n
      s.index = i
      s.next = stack
      stack = s
    }

    private def recoverState(_n: Int): Unit = {
      var n = _n
      var s: TableStack[K, V] = null
      var len = 0
      while ({ s = stack; s } != null && { index += { len = s.length; len }; index } >= n) {
        n = len
        index = s.index
        tab = s.tab
        s.tab = null
        val next = s.next
        s.next = spare // save for reuse

        stack = next
        spare = s
      }
      if (s == null && { index += baseSize; index } >= n) index = {
        baseIndex += 1; baseIndex
      }
    }
  }

  /*
   * Task classes. Coded in a regular but ugly format/style to
   * simplify checks that each variant differs in the right way from
   * others. The null screenings exist because compilers cannot tell
   * that we've already null-checked task arguments, so we force
   * simplest hoisted bypass to help avoid convoluted traps.
   */
  private[concurrent] final class ForEachKeyTask[K <: AnyRef, V <: AnyRef] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val action: Consumer[_ >: K]
  ) extends BulkTask[K, V, Void](p, b, i, f, t) {
    override final def compute(): Unit = {
      val action: Consumer[_ >: K] = this.action
      if (action != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          new ForEachKeyTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            action
          ).fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) action.accept(p.key)
        propagateCompletion()
      }
    }
  }

  private[concurrent] final class ForEachValueTask[K <: AnyRef, V <: AnyRef] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val action: Consumer[_ >: V]
  ) extends BulkTask[K, V, Void](p, b, i, f, t) {
    override final def compute(): Unit = {
      val action: Consumer[_ >: V] = this.action
      if (action != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          new ForEachValueTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            action
          ).fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) action.accept(p.`val`)
        propagateCompletion()
      }
    }
  }

  private[concurrent] final class ForEachEntryTask[K <: AnyRef, V <: AnyRef] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val action: Consumer[_ >: util.Map.Entry[K, V]]
  ) extends BulkTask[K, V, Void](p, b, i, f, t) {
    override final def compute(): Unit = {
      val action: Consumer[_ >: util.Map.Entry[K, V]] = this.action
      if (action != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          new ForEachEntryTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            action
          ).fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) action.accept(p)
        propagateCompletion()
      }
    }
  }

  private[concurrent] final class ForEachMappingTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val action: BiConsumer[_ >: K, _ >: V]
  ) extends BulkTask[K, V, Void](p, b, i, f, t) {
    override final def compute(): Unit = {
      val action: BiConsumer[_ >: K, _ >: V] = this.action
      if (action != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          new ForEachMappingTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            action
          ).fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) action.accept(p.key, p.`val`)
        propagateCompletion()
      }
    }
  }

  private[concurrent] final class ForEachTransformedKeyTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val transformer: Function[_ >: K, _ <: U],
      private[concurrent] val action: Consumer[_ >: U]
  ) extends BulkTask[K, V, Void](p, b, i, f, t) {
    override final def compute(): Unit = {
      val transformer: Function[_ >: K, _ <: U] = this.transformer
      val action: Consumer[_ >: U] = this.action
      if (transformer != null && action != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          new ForEachTransformedKeyTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            transformer,
            action
          ).fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) {
          var u: U = null.asInstanceOf[U].asInstanceOf[U]
          if ({ u = transformer.apply(p.key); u } != null) action.accept(u)
        }
        propagateCompletion()
      }
    }
  }

  private[concurrent] final class ForEachTransformedValueTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val transformer: Function[_ >: V, _ <: U],
      private[concurrent] val action: Consumer[_ >: U]
  ) extends BulkTask[K, V, Void](p, b, i, f, t) {
    override final def compute(): Unit = {
      val transformer: Function[_ >: V, _ <: U] = this.transformer
      val action: Consumer[_ >: U] = this.action
      if (transformer != null && action != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          new ForEachTransformedValueTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            transformer,
            action
          ).fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) {
          var u: U = null.asInstanceOf[U].asInstanceOf[U]
          if ({ u = transformer.apply(p.`val`); u } != null) action.accept(u)
        }
        propagateCompletion()
      }
    }
  }

  private[concurrent] final class ForEachTransformedEntryTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val transformer: Function[
        util.Map.Entry[K, V],
        _ <: U
      ],
      private[concurrent] val action: Consumer[_ >: U]
  ) extends BulkTask[K, V, Void](p, b, i, f, t) {
    override final def compute(): Unit = {
      val transformer: Function[util.Map.Entry[K, V], _ <: U] = this.transformer
      val action: Consumer[_ >: U] = this.action
      if (transformer != null && action != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          new ForEachTransformedEntryTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            transformer,
            action
          ).fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) {
          var u: U = null.asInstanceOf[U]
          if ({ u = transformer.apply(p); u } != null) action.accept(u)
        }
        propagateCompletion()
      }
    }
  }

  private[concurrent] final class ForEachTransformedMappingTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val transformer: BiFunction[_ >: K, _ >: V, _ <: U],
      private[concurrent] val action: Consumer[_ >: U]
  ) extends BulkTask[K, V, Void](p, b, i, f, t) {
    override final def compute(): Unit = {
      val transformer: BiFunction[_ >: K, _ >: V, _ <: U] = this.transformer
      val action: Consumer[_ >: U] = this.action
      if (transformer != null && action != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          new ForEachTransformedMappingTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            transformer,
            action
          ).fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) {
          var u: U = null.asInstanceOf[U]
          if ({ u = transformer.apply(p.key, p.`val`); u } != null) action.accept(u)
        }
        propagateCompletion()
      }
    }
  }

  private[concurrent] final class SearchKeysTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val searchFunction: Function[_ >: K, _ <: U],
      private[concurrent] val result: AtomicReference[U]
  ) extends BulkTask[K, V, U](p, b, i, f, t) {
    override final def getRawResult(): U = result.get()

    override final def compute(): Unit = {
      val searchFunction: Function[_ >: K, _ <: U] = this.searchFunction
      val result: AtomicReference[U] = this.result
      if (searchFunction != null && result != null) {
        val i = baseIndex
        var f = 0
        var h = 0

        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          if (result.get() != null)
            return

          addToPendingCount(1)
          new SearchKeysTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            searchFunction,
            result
          ).fork()
        }
        var break = false
        while (!break && result.get() == null) {
          var u: U = null.asInstanceOf[U]
          var p: Node[K, V] = null
          if ({ p = advance(); p } == null) {
            propagateCompletion()
            break = true
          } else if ({ u = searchFunction.apply(p.key); u } != null) {
            if (result.compareAndSet(null.asInstanceOf[U], u))
              quietlyCompleteRoot()
            break = true
          }
        }
      }
    }
  }

  private[concurrent] final class SearchValuesTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val searchFunction: Function[_ >: V, _ <: U],
      private[concurrent] val result: AtomicReference[U]
  ) extends BulkTask[K, V, U](p, b, i, f, t) {
    override final def getRawResult(): U = result.get()

    override final def compute(): Unit = {
      val searchFunction: Function[_ >: V, _ <: U] = this.searchFunction
      val result: AtomicReference[U] = this.result
      if (searchFunction != null && result != null) {
        val i = baseIndex
        var f = 0
        var h = 0

        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          if (result.get() != null)
            return

          addToPendingCount(1)
          new SearchValuesTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            searchFunction,
            result
          ).fork()
        }
        var break = false
        while (!break && result.get() == null) {
          var u: U = null.asInstanceOf[U]
          var p: Node[K, V] = null
          if ({ p = advance(); p } == null) {
            propagateCompletion()
            break = true
          } else if ({ u = searchFunction.apply(p.`val`); u } != null) {
            if (result.compareAndSet(null.asInstanceOf[U], u))
              quietlyCompleteRoot()
            break = true
          }
        }
      }
    }
  }

  private[concurrent] final class SearchEntriesTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val searchFunction: Function[
        util.Map.Entry[K, V],
        _ <: U
      ],
      private[concurrent] val result: AtomicReference[U]
  ) extends BulkTask[K, V, U](p, b, i, f, t) {
    override final def getRawResult(): U = result.get()

    override final def compute(): Unit = {
      val searchFunction: Function[util.Map.Entry[K, V], _ <: U] = this.searchFunction
      val result: AtomicReference[U] = this.result
      if (searchFunction != null && result != null) {
        val i = baseIndex
        var f = 0
        var h = 0

        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          if (result.get() != null)
            return

          addToPendingCount(1)
          new SearchEntriesTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            searchFunction,
            result
          ).fork()
        }
        var break = false
        while (!break && result.get() == null) {
          var u: U = null.asInstanceOf[U]
          var p: Node[K, V] = null
          if ({ p = advance(); p } == null) {
            propagateCompletion()
            break = true
          } else if ({ u = searchFunction.apply(p); u } != null) {
            if (result.compareAndSet(null.asInstanceOf[U], u))
              quietlyCompleteRoot()
            return
          }
        }
      }
    }
  }

  private[concurrent] final class SearchMappingsTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val searchFunction: BiFunction[
        _ >: K,
        _ >: V,
        _ <: U
      ],
      private[concurrent] val result: AtomicReference[U]
  ) extends BulkTask[K, V, U](p, b, i, f, t) {
    override final def getRawResult(): U = result.get()

    override final def compute(): Unit = {
      val searchFunction: BiFunction[_ >: K, _ >: V, _ <: U] = this.searchFunction
      val result: AtomicReference[U] = this.result
      if (searchFunction != null && result != null) {
        val i = baseIndex
        var f = 0
        var h = 0

        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          if (result.get() != null)
            return

          addToPendingCount(1)
          new SearchMappingsTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            searchFunction,
            result
          ).fork()
        }
        var break = false
        while (!break && result.get() == null) {
          var u: U = null.asInstanceOf[U]
          var p: Node[K, V] = null
          if ({ p = advance(); p } == null) {
            propagateCompletion()
            break = true
          } else if ({ u = searchFunction.apply(p.key, p.`val`); u } != null) {
            if (result.compareAndSet(null.asInstanceOf[U], u))
              quietlyCompleteRoot()
            break = true
          }
        }
      }
    }
  }

  private[concurrent] final class ReduceKeysTask[K <: AnyRef, V <: AnyRef] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: ReduceKeysTask[K, V],
      private[concurrent] val reducer: BiFunction[_ >: K, _ >: K, _ <: K]
  ) extends BulkTask[K, V, K](p, b, i, f, t) {
    private[concurrent] var result: K = _
    private[concurrent] var rights: ReduceKeysTask[K, V] = _

    override final def getRawResult(): K = result

    override final def compute(): Unit = {
      val reducer: BiFunction[_ >: K, _ >: K, _ <: K] = this.reducer
      if (reducer != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new ReduceKeysTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            reducer
          )
          rights.fork()
        }
        var r: K = null.asInstanceOf[K]
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) {
          val u = p.key
          r =
            if (r == null) u
            else if (u == null) r
            else reducer.apply(r, u)
        }
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[ReduceKeysTask[K, V]]
          var s = t.rights
          while (s != null) {
            var tr: K = null.asInstanceOf[K]
            var sr: K = null.asInstanceOf[K]
            if ({ sr = s.result; sr } != null)
              t.result =
                if (({ tr = t.result; tr } == null)) sr
                else reducer.apply(tr, sr)
            t.rights = s.nextRight
            s = s.nextRight
          }
          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class ReduceValuesTask[K <: AnyRef, V <: AnyRef] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: ReduceValuesTask[
        K,
        V
      ],
      private[concurrent] val reducer: BiFunction[_ >: V, _ >: V, _ <: V]
  ) extends BulkTask[K, V, V](p, b, i, f, t) {
    private[concurrent] var result: V = _
    private[concurrent] var rights: ReduceValuesTask[K, V] = _

    override final def getRawResult(): V = result

    override final def compute(): Unit = {
      var reducer: BiFunction[_ >: V, _ >: V, _ <: V] = null
      if ({ reducer = this.reducer; reducer } != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new ReduceValuesTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            reducer
          )
          rights.fork()
        }
        var r: V = null.asInstanceOf[V]
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) {
          val v = p.`val`
          r =
            if (r == null) v
            else reducer.apply(r, v)
        }
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[ReduceValuesTask[K, V]]
          var s = t.rights
          while (s != null) {
            var tr: V = null.asInstanceOf[V]
            var sr: V = null.asInstanceOf[V]
            if ({ sr = s.result; sr } != null)
              t.result =
                if (({ tr = t.result; tr } == null)) sr
                else reducer.apply(tr, sr)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class ReduceEntriesTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: ReduceEntriesTask[
        K,
        V
      ],
      private[concurrent] val reducer: BiFunction[
        util.Map.Entry[K, V],
        util.Map.Entry[K, V],
        _ <: util.Map.Entry[K, V]
      ]
  ) extends BulkTask[K, V, util.Map.Entry[K, V]](p, b, i, f, t) {
    private[concurrent] var result: util.Map.Entry[K, V] = _
    private[concurrent] var rights: ReduceEntriesTask[K, V] = _

    override final def getRawResult(): util.Map.Entry[K, V] = result

    override final def compute(): Unit = {
      var reducer: BiFunction[
        util.Map.Entry[K, V],
        util.Map.Entry[K, V],
        _ <: util.Map.Entry[K, V]
      ] = null
      if ({ reducer = this.reducer; reducer } != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new ReduceEntriesTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            reducer
          )
          rights.fork()
        }
        var r: util.Map.Entry[K, V] = null
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r =
            if (r == null) p
            else reducer.apply(r, p)
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[ReduceEntriesTask[K, V]]
          var s = t.rights
          while (s != null) {
            var tr: util.Map.Entry[K, V] = null
            var sr: util.Map.Entry[K, V] = null
            if ({ sr = s.result; sr } != null)
              t.result =
                if (({ tr = t.result; tr } == null)) sr
                else reducer.apply(tr, sr)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceKeysTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceKeysTask[
        K,
        V,
        U
      ],
      private[concurrent] val transformer: Function[_ >: K, _ <: U],
      private[concurrent] val reducer: BiFunction[_ >: U, _ >: U, _ <: U]
  ) extends BulkTask[K, V, U](p, b, i, f, t) {
    private[concurrent] var result: U = _
    private[concurrent] var rights: MapReduceKeysTask[K, V, U] = _

    override final def getRawResult(): U = result

    override final def compute(): Unit = {
      var transformer: Function[_ >: K, _ <: U] = null
      var reducer: BiFunction[_ >: U, _ >: U, _ <: U] = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceKeysTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            reducer
          )
          rights.fork()
        }
        var r: U = null.asInstanceOf[U]
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) {
          var u: U = null.asInstanceOf[U]
          if ({ u = transformer.apply(p.key); u } != null)
            r =
              if (r == null) u
              else reducer.apply(r, u)
        }
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceKeysTask[K, V, U]]
          var s = t.rights
          while (s != null) {
            var tr: U = null.asInstanceOf[U]
            var sr: U = null.asInstanceOf[U]
            if ({ sr = s.result; sr } != null)
              t.result =
                if (({ tr = t.result; tr } == null)) sr
                else reducer.apply(tr, sr)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceValuesTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceValuesTask[
        K,
        V,
        U
      ],
      private[concurrent] val transformer: Function[_ >: V, _ <: U],
      private[concurrent] val reducer: BiFunction[_ >: U, _ >: U, _ <: U]
  ) extends BulkTask[K, V, U](p, b, i, f, t) {
    private[concurrent] var result: U = _
    private[concurrent] var rights: MapReduceValuesTask[K, V, U] = _

    override final def getRawResult(): U = result

    override final def compute(): Unit = {
      var transformer: Function[_ >: V, _ <: U] = null
      var reducer: BiFunction[_ >: U, _ >: U, _ <: U] = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceValuesTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            reducer
          )
          rights.fork()
        }
        var r: U = null.asInstanceOf[U]
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) {
          var u: U = null.asInstanceOf[U]
          if ({ u = transformer.apply(p.`val`); u } != null)
            r =
              if (r == null) u
              else reducer.apply(r, u)
        }
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceValuesTask[K, V, U]]
          var s = t.rights
          while (s != null) {
            var tr: U = null.asInstanceOf[U]
            var sr: U = null.asInstanceOf[U]
            if ({ sr = s.result; sr } != null)
              t.result =
                if (({ tr = t.result; tr } == null)) sr
                else reducer.apply(tr, sr)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceEntriesTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceEntriesTask[
        K,
        V,
        U
      ],
      private[concurrent] val transformer: Function[
        util.Map.Entry[K, V],
        _ <: U
      ],
      private[concurrent] val reducer: BiFunction[_ >: U, _ >: U, _ <: U]
  ) extends BulkTask[K, V, U](p, b, i, f, t) {
    private[concurrent] var result: U = null.asInstanceOf[U]
    private[concurrent] var rights: MapReduceEntriesTask[K, V, U] = null

    override final def getRawResult(): U = result

    override final def compute(): Unit = {
      var transformer: Function[util.Map.Entry[K, V], _ <: U] = null
      var reducer: BiFunction[_ >: U, _ >: U, _ <: U] = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceEntriesTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            reducer
          )
          rights.fork()
        }
        var r: U = null.asInstanceOf[U]
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) {
          var u: U = null.asInstanceOf[U]
          if ({ u = transformer.apply(p); u } != null)
            r =
              if (r == null) u
              else reducer.apply(r, u)
        }
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceEntriesTask[K, V, U]]
          var s = t.rights
          while (s != null) {
            var tr: U = null.asInstanceOf[U]
            var sr: U = null.asInstanceOf[U]
            if ({ sr = s.result; sr } != null)
              t.result =
                if (({ tr = t.result; tr } == null)) sr
                else reducer.apply(tr, sr)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceMappingsTask[
      K <: AnyRef,
      V <: AnyRef,
      U <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceMappingsTask[
        K,
        V,
        U
      ],
      private[concurrent] val transformer: BiFunction[_ >: K, _ >: V, _ <: U],
      private[concurrent] val reducer: BiFunction[_ >: U, _ >: U, _ <: U]
  ) extends BulkTask[K, V, U](p, b, i, f, t) {
    private[concurrent] var result: U = null.asInstanceOf[U]
    private[concurrent] var rights: MapReduceMappingsTask[K, V, U] = null

    override final def getRawResult(): U = result

    override final def compute(): Unit = {
      val transformer: BiFunction[_ >: K, _ >: V, _ <: U] = this.transformer
      val reducer: BiFunction[_ >: U, _ >: U, _ <: U] = this.reducer
      if (transformer != null && reducer != null) {
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceMappingsTask[K, V, U](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            reducer
          )
          rights.fork()
        }
        var r: U = null.asInstanceOf[U]
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null) {
          var u: U = null.asInstanceOf[U]
          if ({ u = transformer.apply(p.key, p.`val`); u } != null)
            r =
              if (r == null) u
              else reducer.apply(r, u)
        }
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceMappingsTask[K, V, U]]
          var s = t.rights
          while (s != null) {
            var tr: U = null.asInstanceOf[U]
            var sr: U = null.asInstanceOf[U]
            if ({ sr = s.result; sr } != null)
              t.result =
                if (({ tr = t.result; tr } == null)) sr
                else reducer.apply(tr, sr)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceKeysToDoubleTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceKeysToDoubleTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToDoubleFunction[_ >: K],
      private[concurrent] val basis: Double,
      private[concurrent] val reducer: DoubleBinaryOperator
  ) extends BulkTask[K, V, Double](p, b, i, f, t) {
    private[concurrent] var result = .0
    private[concurrent] var rights: MapReduceKeysToDoubleTask[K, V] = null

    override final def getRawResult(): Double = result

    override final def compute(): Unit = {
      var transformer: ToDoubleFunction[_ >: K] = null
      var reducer: DoubleBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceKeysToDoubleTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceKeysToDoubleTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsDouble(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceValuesToDoubleTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceValuesToDoubleTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToDoubleFunction[_ >: V],
      private[concurrent] val basis: Double,
      private[concurrent] val reducer: DoubleBinaryOperator
  ) extends BulkTask[K, V, Double](p, b, i, f, t) {
    private[concurrent] var result = .0
    private[concurrent] var rights: MapReduceValuesToDoubleTask[K, V] = null

    override final def getRawResult(): Double = result

    override final def compute(): Unit = {
      var transformer: ToDoubleFunction[_ >: V] = null
      var reducer: DoubleBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceValuesToDoubleTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.`val`))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceValuesToDoubleTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsDouble(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceEntriesToDoubleTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceEntriesToDoubleTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToDoubleFunction[
        util.Map.Entry[K, V]
      ],
      private[concurrent] val basis: Double,
      private[concurrent] val reducer: DoubleBinaryOperator
  ) extends BulkTask[K, V, Double](p, b, i, f, t) {
    private[concurrent] var result = .0
    private[concurrent] var rights: MapReduceEntriesToDoubleTask[K, V] = null

    override final def getRawResult(): Double = result

    override final def compute(): Unit = {
      var transformer: ToDoubleFunction[util.Map.Entry[K, V]] = null
      var reducer: DoubleBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceEntriesToDoubleTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsDouble(r, transformer.applyAsDouble(p))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceEntriesToDoubleTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsDouble(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceMappingsToDoubleTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceMappingsToDoubleTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToDoubleBiFunction[_ >: K, _ >: V],
      private[concurrent] val basis: Double,
      private[concurrent] val reducer: DoubleBinaryOperator
  ) extends BulkTask[K, V, Double](p, b, i, f, t) {
    private[concurrent] var result = .0
    private[concurrent] var rights: MapReduceMappingsToDoubleTask[K, V] = null

    override final def getRawResult(): Double = result

    override final def compute(): Unit = {
      var transformer: ToDoubleBiFunction[_ >: K, _ >: V] = null
      var reducer: DoubleBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceMappingsToDoubleTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key, p.`val`))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t = c
            .asInstanceOf[MapReduceMappingsToDoubleTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsDouble(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceKeysToLongTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceKeysToLongTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToLongFunction[_ >: K],
      private[concurrent] val basis: Long,
      private[concurrent] val reducer: LongBinaryOperator
  ) extends BulkTask[K, V, Long](p, b, i, f, t) {
    private[concurrent] var result = 0L
    private[concurrent] var rights: MapReduceKeysToLongTask[K, V] = null

    override final def getRawResult(): Long = result

    override final def compute(): Unit = {
      var transformer: ToLongFunction[_ >: K] = null
      var reducer: LongBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceKeysToLongTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsLong(r, transformer.applyAsLong(p.key))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceKeysToLongTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsLong(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceValuesToLongTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceValuesToLongTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToLongFunction[_ >: V],
      private[concurrent] val basis: Long,
      private[concurrent] val reducer: LongBinaryOperator
  ) extends BulkTask[K, V, Long](p, b, i, f, t) {
    private[concurrent] var result = 0L
    private[concurrent] var rights: MapReduceValuesToLongTask[K, V] = null

    override final def getRawResult(): Long = result

    override final def compute(): Unit = {
      var transformer: ToLongFunction[_ >: V] = null
      var reducer: LongBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceValuesToLongTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsLong(r, transformer.applyAsLong(p.`val`))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceValuesToLongTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsLong(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceEntriesToLongTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceEntriesToLongTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToLongFunction[util.Map.Entry[K, V]],
      private[concurrent] val basis: Long,
      private[concurrent] val reducer: LongBinaryOperator
  ) extends BulkTask[K, V, Long](p, b, i, f, t) {
    private[concurrent] var result = 0L
    private[concurrent] var rights: MapReduceEntriesToLongTask[K, V] = null

    override final def getRawResult(): Long = result

    override final def compute(): Unit = {
      var transformer: ToLongFunction[util.Map.Entry[K, V]] = null
      var reducer: LongBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceEntriesToLongTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsLong(r, transformer.applyAsLong(p))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceEntriesToLongTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsLong(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceMappingsToLongTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceMappingsToLongTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToLongBiFunction[_ >: K, _ >: V],
      private[concurrent] val basis: Long,
      private[concurrent] val reducer: LongBinaryOperator
  ) extends BulkTask[K, V, Long](p, b, i, f, t) {
    private[concurrent] var result = 0L
    private[concurrent] var rights: MapReduceMappingsToLongTask[K, V] = null

    override final def getRawResult(): Long = result

    override final def compute(): Unit = {
      var transformer: ToLongBiFunction[_ >: K, _ >: V] = null
      var reducer: LongBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceMappingsToLongTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsLong(r, transformer.applyAsLong(p.key, p.`val`))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceMappingsToLongTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsLong(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceKeysToIntTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceKeysToIntTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToIntFunction[_ >: K],
      private[concurrent] val basis: Int,
      private[concurrent] val reducer: IntBinaryOperator
  ) extends BulkTask[K, V, Integer](p, b, i, f, t) {
    private[concurrent] var result = 0
    private[concurrent] var rights: MapReduceKeysToIntTask[K, V] = null

    override final def getRawResult(): Integer = result

    override final def compute(): Unit = {
      var transformer: ToIntFunction[_ >: K] = null
      var reducer: IntBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceKeysToIntTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsInt(r, transformer.applyAsInt(p.key))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceKeysToIntTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsInt(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceValuesToIntTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceValuesToIntTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToIntFunction[_ >: V],
      private[concurrent] val basis: Int,
      private[concurrent] val reducer: IntBinaryOperator
  ) extends BulkTask[K, V, Integer](p, b, i, f, t) {
    private[concurrent] var result = 0
    private[concurrent] var rights: MapReduceValuesToIntTask[K, V] = null

    override final def getRawResult(): Integer = result

    override final def compute(): Unit = {
      var transformer: ToIntFunction[_ >: V] = null
      var reducer: IntBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceValuesToIntTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsInt(r, transformer.applyAsInt(p.`val`))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceValuesToIntTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsInt(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceEntriesToIntTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceEntriesToIntTask[K, V],
      private[concurrent] val transformer: ToIntFunction[util.Map.Entry[K, V]],
      private[concurrent] val basis: Int,
      private[concurrent] val reducer: IntBinaryOperator
  ) extends BulkTask[K, V, Integer](p, b, i, f, t) {
    private[concurrent] var result = 0
    private[concurrent] var rights: MapReduceEntriesToIntTask[K, V] = null

    override final def getRawResult(): Integer = result

    override final def compute(): Unit = {
      var transformer: ToIntFunction[util.Map.Entry[K, V]] = null
      var reducer: IntBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceEntriesToIntTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsInt(r, transformer.applyAsInt(p))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceEntriesToIntTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsInt(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

  private[concurrent] final class MapReduceMappingsToIntTask[
      K <: AnyRef,
      V <: AnyRef
  ] private[concurrent] (
      p: BulkTask[K, V, _],
      b: Int,
      i: Int,
      f: Int,
      t: Array[Node[K, V]],
      private[concurrent] val nextRight: MapReduceMappingsToIntTask[
        K,
        V
      ],
      private[concurrent] val transformer: ToIntBiFunction[_ >: K, _ >: V],
      private[concurrent] val basis: Int,
      private[concurrent] val reducer: IntBinaryOperator
  ) extends BulkTask[K, V, Integer](p, b, i, f, t) {
    private[concurrent] var result = 0
    private[concurrent] var rights: MapReduceMappingsToIntTask[K, V] = null

    override final def getRawResult(): Integer = result

    override final def compute(): Unit = {
      var transformer: ToIntBiFunction[_ >: K, _ >: V] = null
      var reducer: IntBinaryOperator = null
      if ({ transformer = this.transformer; transformer } != null && { reducer = this.reducer; reducer } != null) {
        var r = this.basis
        val i = baseIndex
        var f = 0
        var h = 0
        while (batch > 0 && { h = ({ f = baseLimit; f } + i); h >>> 1 } > i) {
          addToPendingCount(1)
          rights = new MapReduceMappingsToIntTask[K, V](
            this,
            { batch >>>= 1; batch },
            { baseLimit = h; h },
            f,
            tab,
            rights,
            transformer,
            r,
            reducer
          )
          rights.fork()
        }
        var p: Node[K, V] = null
        while ({ p = advance(); p } != null)
          r = reducer.applyAsInt(r, transformer.applyAsInt(p.key, p.`val`))
        result = r
        var c: CountedCompleter[_] = null
        c = firstComplete()
        while (c != null) {
          val t =
            c.asInstanceOf[MapReduceMappingsToIntTask[K, V]]
          var s = t.rights
          while (s != null) {
            t.result = reducer.applyAsInt(t.result, s.result)
            t.rights = s.nextRight
            s = s.nextRight
          }

          c = c.nextComplete()
        }
      }
    }
  }

// try{
//   // Reduce the risk of rare disastrous classloading in first call to
//   // LockSupport.park: https://bugs.openjdk.java.net/browse/JDK-8074773
//   var ensureLoaded = classOf[LockSupport]
//   // Eager class load observed to help JIT during startup
//   ensureLoaded = classOf[ReservationNode[_, _]]
}

@SerialVersionUID(7249069246763182397L)
class ConcurrentHashMap[K <: AnyRef, V <: AnyRef]()
    extends AbstractMap[K, V]
    with ConcurrentMap[K, V]
    with Serializable {
  import ConcurrentHashMap._

  /* ---------------- Fields -------------- */
  @volatile
  @transient private[concurrent] var table: Array[Node[K, V]] = _

  @volatile
  @transient private var nextTable: Array[Node[K, V]] = _

  @volatile
  @transient private var baseCount = 0L

  @volatile
  @transient private var sizeCtl = 0

  @volatile
  @transient private var transferIndex = 0

  @volatile
  @transient private var cellsBusy = 0

  @volatile
  @transient private var counterCells: Array[CounterCell] = _
  // views
  @transient private var _keySet: KeySetView[K, V] = _
  @transient private var _values: ValuesView[K, V] = _
  @transient private var _entrySet: EntrySetView[K, V] = _

  // Unsafe mechanics
  @inline def SIZECTL = fromRawPtr[scala.Int](classFieldRawPtr(this, "sizeCtl")).atomic
  @inline def TRANSFERINDEX = fromRawPtr[scala.Int](classFieldRawPtr(this, "transferIndex")).atomic
  @inline def BASECOUNT = fromRawPtr[scala.Long](classFieldRawPtr(this, "baseCount")).atomic
  @inline def CELLSBUSY = fromRawPtr[scala.Int](classFieldRawPtr(this, "cellsBusy")).atomic

  def this(initialCapacity: Int, loadFactor: Float, concurrencyLevel: Int) = {
    this()
    if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
      throw new IllegalArgumentException()
    // if (initialCapacity < concurrencyLevel)   // Use at least as many bins
    //     initialCapacity = concurrencyLevel;   // as estimated threads
    val size: Long =
      (1.0 + initialCapacity.max(concurrencyLevel).toLong / loadFactor).toLong
    val cap: Int =
      if (size >= MAXIMUM_CAPACITY.toLong) MAXIMUM_CAPACITY
      else tableSizeFor(size.toInt)
    this.sizeCtl = cap;
  }

  def this(initialCapacity: Int) = {
    this(initialCapacity, ConcurrentHashMap.LOAD_FACTOR, 1)
  }

  def this(m: Map[_ <: K, _ <: V]) = {
    this()
    this.sizeCtl = ConcurrentHashMap.DEFAULT_CAPACITY
    putAll(m)
  }

  def this(initialCapacity: Int, loadFactor: Float) = {
    this(initialCapacity, loadFactor, 1)
  }

  // Original (since JDK1.2) Map methods
  override def size(): Int = {
    val n = sumCount
    if ((n < 0L)) 0
    else if ((n > Integer.MAX_VALUE.toLong)) Integer.MAX_VALUE
    else n.toInt
  }

  override def isEmpty(): Boolean =
    sumCount <= 0L // ignore transient negative values

  override def get(_key: Any): V = {
    val key = _key.asInstanceOf[AnyRef]
    var tab: Array[Node[K, V]] = null
    var e: Node[K, V] = null
    var p: Node[K, V] = null
    var n = 0
    var eh = 0
    var ek: K = null.asInstanceOf[K]
    val h = spread(key.hashCode())
    if ({ tab = table; tab != null } && { n = tab.length; n > 0 } && { e = tabAt(tab, (n - 1) & h); e != null }) {
      if ({ eh = e.hash; eh == h }) {
        if ({ ek = e.key; ek eq key } || (ek != null && key.equals(ek)))
          return e.`val`
      } else if (eh < 0)
        return if ({ p = e.find(h, key); p != null }) p.`val` else null.asInstanceOf[V]
      while ({ e = e.next; e != null }) {
        if (e.hash == h && ({ ek = e.key; ek eq key } || (ek != null && key.equals(ek))))
          return e.`val`
      }
    }
    null.asInstanceOf[V]
  }

  override def containsKey(key: Any): Boolean = get(key) != null
  override def containsValue(_value: Any): Boolean = {
    val value = _value.asInstanceOf[AnyRef]
    if (value == null) throw new NullPointerException
    var t: Array[Node[K, V]] = null
    if ({ t = table; t != null }) {
      val it = new Traverser[K, V](t, t.length, 0, t.length)
      var p: Node[K, V] = null
      while ({ p = it.advance(); p != null }) {
        val v = p.`val`
        if ((v eq value) || (v != null && value.equals(v)))
          return true
      }
    }
    false
  }

  override def put(key: K, value: V): V = putVal(key, value, false)

  private[concurrent] final def putVal(
      key: K,
      value: V,
      onlyIfAbsent: Boolean
  ): V = {
    if (key == null || value == null) throw new NullPointerException
    val hash = spread(key.hashCode())
    var binCount = 0
    var tab = table
    var break = false
    while (!break) {
      var f: Node[K, V] = null
      var n = 0
      var i = 0
      var fh = 0
      var fk: K = null.asInstanceOf[K]
      var fv: V = null.asInstanceOf[V]
      if (tab == null || { n = tab.length; n == 0 })
        tab = initTable()
      else if ({ f = tabAt(tab, { i = (n - 1) & hash; i }); f == null }) {
        if (casTabAt(tab, i, null, new Node[K, V](hash, key, value)))
          break = true
      } else if ({ fh = f.hash; fh == MOVED }) {
        tab = helpTransfer(tab, f)
      } else if (onlyIfAbsent && // check first node without acquiring lock
          fh == hash &&
          ({ fk = f.key; fk eq key } || (fk != null && key.equals(fk))) && { fv = f.`val`; fv != null }) {
        return fv
      } else {
        var oldVal: V = null.asInstanceOf[V]
        f.synchronized {
          if (tabAt(tab, i) eq f) if (fh >= 0) {
            binCount = 1
            var e = f
            var break = false
            while (!break) {
              var ek: K = null.asInstanceOf[K]
              if (e.hash == hash && ({ ek = e.key; ek eq key } || (ek != null && key.equals(ek)))) {
                oldVal = e.`val`
                if (!onlyIfAbsent) e.`val` = value
                break = true
              } else {
                val pred = e
                if ({ e = e.next; e == null }) {
                  pred.next = new Node[K, V](hash, key, value)
                  break = true
                }
              }
              if (!break) binCount += 1
            }
          } else if (f.isInstanceOf[TreeBin[_, _]]) {
            var p: Node[K, V] = null
            binCount = 2
            if ({
                  p = f
                    .asInstanceOf[TreeBin[K, V]]
                    .putTreeVal(hash, key, value);
                  p
                } != null) {
              oldVal = p.`val`
              if (!onlyIfAbsent) p.`val` = value
            }
          } else if (f.isInstanceOf[ReservationNode[_, _]]) throw new IllegalStateException("Recursive update")
        }
        if (binCount != 0) {
          if (binCount >= TREEIFY_THRESHOLD)
            treeifyBin(tab, i)
          if (oldVal != null)
            return oldVal
          break = true
        }
      }
    }
    addCount(1L, binCount)
    null.asInstanceOf[V]
  }

  override def putAll(m: Map[_ <: K, _ <: V]): Unit = {
    tryPresize(m.size())
    m.entrySet().forEach { e =>
      putVal(e.getKey(), e.getValue(), false)
    }
  }

  override def remove(key: Any): V = replaceNode(key.asInstanceOf[AnyRef], null.asInstanceOf[V], null.asInstanceOf[V])

  private[concurrent] final def replaceNode(
      key: AnyRef,
      value: V,
      cv: AnyRef
  ): V = {
    val hash = spread(key.hashCode())
    var tab = table
    var break = false
    while (!break) {
      var f: Node[K, V] = null
      var n = 0
      var i = 0
      var fh = 0
      if (tab == null || { n = tab.length; n } == 0 || { f = tabAt(tab, { i = (n - 1) & hash; i }); f == null })
        break = true
      else if ({ fh = f.hash; fh } == MOVED)
        tab = helpTransfer(tab, f)
      else {
        var oldVal: V = null.asInstanceOf[V]
        var validated = false
        f.synchronized {
          if (tabAt(tab, i) eq f) if (fh >= 0) {
            validated = true
            var e = f
            var pred: Node[K, V] = null
            var break = false
            while (!break) {
              var ek: K = null.asInstanceOf[K]
              if (e.hash == hash && (({ ek = e.key; ek } eq key) || (ek != null && key.equals(ek)))) {
                val ev = e.`val`
                if (cv == null || (cv eq ev) || (ev != null && cv.equals(ev))) {
                  oldVal = ev
                  if (value != null) e.`val` = value
                  else if (pred != null) pred.next = e.next
                  else setTabAt(tab, i, e.next)
                }
                break = true
              } else {
                pred = e
                if ({ e = e.next; e } == null) break = true
              }
            }
          } else if (f.isInstanceOf[TreeBin[_, _]]) {
            validated = true
            val t = f.asInstanceOf[TreeBin[K, V]]
            var r: TreeNode[K, V] = null
            var p: TreeNode[K, V] = null
            if ({ r = t.root; r } != null && { p = r.findTreeNode(hash, key, null); p } != null) {
              val pv = p.`val`
              if (cv == null || (cv eq pv) || (pv != null && cv.equals(pv))) {
                oldVal = pv
                if (value != null) p.`val` = value
                else if (t.removeTreeNode(p))
                  ConcurrentHashMap
                    .setTabAt(tab, i, untreeify(t.first))
              }
            }
          } else if (f.isInstanceOf[ReservationNode[_, _]]) throw new IllegalStateException("Recursive update")
        }
        if (validated) {
          if (oldVal != null) {
            if (value == null) addCount(-1L, -1)
            return oldVal
          }
          break = true
        }
      }
    }
    null.asInstanceOf[V]
  }

  override def clear(): Unit = {
    var delta = 0L // negative number of deletions

    var i = 0
    var tab = table
    while (tab != null && i < tab.length) {
      var fh = 0
      val f = tabAt(tab, i)
      if (f == null) i += 1
      else if ({ fh = f.hash; fh } == MOVED) {
        tab = helpTransfer(tab, f)
        i = 0 // restart

      } else
        f.synchronized {
          if (tabAt(tab, i) eq f) {
            var p =
              if (fh >= 0) f
              else if ((f.isInstanceOf[TreeBin[_, _]]))
                (f.asInstanceOf[TreeBin[K, V]]).first
              else null
            while (p != null) {
              delta -= 1
              p = p.next
            }
            setTabAt(
              tab, {
                i += 1; i - 1
              },
              null
            )
          }
        }
    }
    if (delta != 0L) addCount(delta, -1)
  }

  override def keySet(): KeySetView[K, V] = {
    val ks: KeySetView[K, V] = _keySet
    if (ks != null)
      return ks
    _keySet = new KeySetView[K, V](this, null.asInstanceOf[V])
    _keySet
  }

  override def values(): Collection[V] = {
    var vs: ValuesView[K, V] = _values
    if (vs != null)
      return vs
    _values = new ValuesView[K, V](this)
    _values
  }

  override def entrySet(): Set[util.Map.Entry[K, V]] = {
    var es: EntrySetView[K, V] = _entrySet
    if (es != null)
      return es
    _entrySet = new EntrySetView[K, V](this)
    _entrySet
  }

  override def hashCode(): Int = {
    var h = 0
    var t: Array[Node[K, V]] = null
    if ({ t = table; t } != null) {
      val it = new Traverser[K, V](t, t.length, 0, t.length)
      var p: Node[K, V] = null
      while ({ p = it.advance(); p } != null) h += p.key.hashCode() ^ p.`val`.hashCode()
    }
    h
  }

  override def toString(): String = {
    var t: Array[Node[K, V]] = null
    val f =
      if ({ t = table; t } == null) 0
      else t.length
    val it = new Traverser[K, V](t, f, 0, f)
    val sb = new jl.StringBuilder
    sb.append('{')
    var p: Node[K, V] = null
    var break = false
    if ({ p = it.advance(); p } != null) while (!break) {
      val k = p.key
      val v = p.`val`
      sb.append(
        if (k eq this) "(this Map)"
        else k
      )
      sb.append('=')
      sb.append(
        if (v eq this) "(this Map)"
        else v
      )
      if ({ p = it.advance(); p == null }) break = true
      else sb.append(',').append(' ')
    }
    sb.append('}').toString()
  }

  override def equals(_o: Any): Boolean = {
    val o = _o.asInstanceOf[AnyRef]
    if (o ne this) {
      if (!o.isInstanceOf[Map[_, _]])
        return false
      val m = o.asInstanceOf[Map[_, _]]
      var t: Array[Node[K, V]] = null
      val f =
        if ({ t = table; t } == null) 0
        else t.length
      locally {
        val it = new Traverser[K, V](t, f, 0, f)
        var p: Node[K, V] = null
        while ({ p = it.advance(); p } != null) {
          val `val` = p.`val`
          val v = m.get(p.key).asInstanceOf[AnyRef]
          if (v == null || ((v ne `val`) && !v.equals(`val`)))
            return false
        }
      }
      locally {
        val it = m.entrySet().iterator().asInstanceOf[Iterator[Map.Entry[AnyRef, AnyRef]]]
        while (it.hasNext()) {
          val e = it.next()
          var mk: AnyRef = null
          var mv: AnyRef = null
          var v: AnyRef = null
          if ({ mk = e.getKey().asInstanceOf[AnyRef]; mk == null } ||
              { mv = e.getValue().asInstanceOf[AnyRef]; mv == null } ||
              { v = get(mk); v } == null || ((mv ne v) && !(mv.equals(v)))) {
            return false
          }
        }
      }
    }
    true
  }

  @throws[java.io.IOException]
  private def writeObject(s: ObjectOutputStream): Unit = {
    // For serialization compatibility
    // Emulate segment calculation from previous version of this class
    var sshift = 0
    var ssize = 1
    while (ssize < DEFAULT_CONCURRENCY_LEVEL) {
      sshift += 1
      ssize <<= 1
    }
    val segmentShift = 32 - sshift
    val segmentMask = ssize - 1
    val segments =
      new Array[Segment[_, _]](
        DEFAULT_CONCURRENCY_LEVEL
      ).asInstanceOf[Array[Segment[K, V]]]
    for (i <- 0 until segments.length) {
      segments(i) = new Segment[K, V](LOAD_FACTOR)
    }
    val streamFields = s.putFields
    streamFields.put("segments", segments)
    streamFields.put("segmentShift", segmentShift)
    streamFields.put("segmentMask", segmentMask)
    s.writeFields()
    var t: Array[Node[K, V]] = null
    if ({ t = table; t } != null) {
      val it = new Traverser[K, V](t, t.length, 0, t.length)
      var p: Node[K, V] = null
      while ({ p = it.advance(); p } != null) {
        s.writeObject(p.key)
        s.writeObject(p.`val`)
      }
    }
    s.writeObject(null)
    s.writeObject(null)
  }

  @throws[java.io.IOException]
  @throws[ClassNotFoundException]
  private def readObject(s: ObjectInputStream): Unit = {
    /*
     * To improve performance in typical cases, we create nodes
     * while reading, then place in table once size is known.
     * However, we must also validate uniqueness and deal with
     * overpopulated bins while doing so, which requires
     * specialized versions of putVal mechanics.
     */
    sizeCtl = -1 // force exclusion for table construction

    s.defaultReadObject()
    var size = 0L
    var p: Node[K, V] = null

    var break = false
    while (!break) {
      val k = s.readObject.asInstanceOf[K]
      val v = s.readObject.asInstanceOf[V]
      if (k != null && v != null) {
        p = new Node[K, V](
          spread(k.hashCode()),
          k,
          v,
          p
        )
        size += 1
      } else break = true
    }
    if (size == 0L) sizeCtl = 0
    else {
      val ts = (1.0 + size / LOAD_FACTOR).toLong
      val n =
        if (ts >= MAXIMUM_CAPACITY.toLong)
          MAXIMUM_CAPACITY
        else tableSizeFor(ts.toInt)
      val tab =
        new Array[Node[_, _]](n)
          .asInstanceOf[Array[Node[K, V]]]
      val mask = n - 1
      var added = 0L
      while (p != null) {
        var insertAtFront = false
        val next = p.next
        var first: Node[K, V] = null
        val h = p.hash
        val j = h & mask
        if ({ first = tabAt(tab, j); first } == null)
          insertAtFront = true
        else {
          val k = p.key
          if (first.hash < 0) {
            val t = first.asInstanceOf[TreeBin[K, V]]
            if (t.putTreeVal(h, k, p.`val`) == null) added += 1
            insertAtFront = false
          } else {
            var binCount = 0
            insertAtFront = true
            var q: Node[K, V] = null
            var qk: K = null.asInstanceOf[K]
            q = first
            var break = false
            while (q != null) {
              if (q.hash == h && (({ qk = q.key; qk } eq k) || (qk != null && k.equals(qk)))) {
                insertAtFront = false
                break = true
              } else {
                binCount += 1
                q = q.next
              }
            }
            if (insertAtFront && binCount >= TREEIFY_THRESHOLD) {
              insertAtFront = false
              added += 1
              p.next = first
              var hd: TreeNode[K, V] = null
              var tl: TreeNode[K, V] = null
              q = p
              while (q != null) {
                val t = new TreeNode[K, V](
                  q.hash,
                  q.key,
                  q.`val`,
                  null,
                  null
                )
                if ({ t.prev = tl; tl } == null) hd = t
                else tl.next = t
                tl = t

                q = q.next
              }
              setTabAt(
                tab,
                j,
                new TreeBin[K, V](hd)
              )
            }
          }
        }
        if (insertAtFront) {
          added += 1
          p.next = first
          setTabAt(tab, j, p)
        }
        p = next
      }
      table = tab
      sizeCtl = n - (n >>> 2)
      baseCount = added
    }
  }

  // ConcurrentMap methods
  override def putIfAbsent(key: K, value: V): V = putVal(key, value, true)

  override def remove(key: Any, value: Any): Boolean = {
    if (key == null) throw new NullPointerException
    value != null && replaceNode(key.asInstanceOf[AnyRef], null.asInstanceOf[V], value.asInstanceOf[AnyRef]) != null
  }

  override def replace(key: K, oldValue: V, newValue: V): Boolean = {
    if (key == null || oldValue == null || newValue == null)
      throw new NullPointerException
    replaceNode(key, newValue, oldValue) != null
  }

  override def replace(key: K, value: V): V = {
    if (key == null || value == null) throw new NullPointerException
    replaceNode(key, value, null)
  }

  // Overrides of JDK8+ Map extension method defaults
  override def getOrDefault(key: Any, defaultValue: V): V = {
    var v: V = null.asInstanceOf[V]
    if ({ v = get(key.asInstanceOf[AnyRef]); v } == null) defaultValue
    else v
  }

  override def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit = {
    if (action == null) throw new NullPointerException
    var t: Array[Node[K, V]] = null
    if ({ t = table; t } != null) {
      val it = new Traverser[K, V](t, t.length, 0, t.length)
      var p: Node[K, V] = null
      while ({ p = it.advance(); p } != null) action.accept(p.key, p.`val`)
    }
  }

  override def replaceAll(
      function: BiFunction[_ >: K, _ >: V, _ <: V]
  ): Unit = {
    if (function == null) throw new NullPointerException
    var t: Array[Node[K, V]] = null
    if ({ t = table; t } != null) {
      val it = new Traverser[K, V](t, t.length, 0, t.length)
      var p: Node[K, V] = null
      while ({ p = it.advance(); p } != null) {
        var oldValue = p.`val`
        val key = p.key
        var break = false
        while (!break) {
          val newValue = function.apply(key, oldValue)
          if (newValue == null) throw new NullPointerException
          if (replaceNode(key, newValue, oldValue) != null || { oldValue = get(key); oldValue } == null)
            break = true
        }
      }
    }
  }

  private[concurrent] def removeEntryIf(
      function: Predicate[_ >: util.Map.Entry[K, V]]
  ) = {
    if (function == null) throw new NullPointerException
    var t: Array[Node[K, V]] = null
    var removed = false
    if ({ t = table; t } != null) {
      val it = new Traverser[K, V](t, t.length, 0, t.length)
      var p: Node[K, V] = null
      while ({ p = it.advance(); p } != null) {
        val k = p.key
        val v = p.`val`
        val e = new util.AbstractMap.SimpleImmutableEntry[K, V](k, v)
        if (function.test(e) && replaceNode(k, null.asInstanceOf[V], v) != null) removed = true
      }
    }
    removed
  }

  private[concurrent] def removeValueIf(function: Predicate[_ >: V]) = {
    if (function == null) throw new NullPointerException
    var t: Array[Node[K, V]] = null
    var removed = false
    if ({ t = table; t } != null) {
      val it = new Traverser[K, V](t, t.length, 0, t.length)
      var p: Node[K, V] = null
      while ({ p = it.advance(); p } != null) {
        val k = p.key
        val v = p.`val`
        if (function.test(v) && replaceNode(k, null.asInstanceOf[V], v) != null) removed = true
      }
    }
    removed
  }

  override def computeIfAbsent(
      key: K,
      mappingFunction: Function[_ >: K, _ <: V]
  ): V = {
    if (key == null || mappingFunction == null) throw new NullPointerException
    val h = spread(key.hashCode())
    var `val`: V = null.asInstanceOf[V]
    var binCount = 0
    var tab = table
    var break = false
    while (!break) {
      var f: Node[K, V] = null
      var n = 0
      var i = 0
      var fh = 0
      var fk: K = null.asInstanceOf[K]
      var fv: V = null.asInstanceOf[V]
      if (tab == null || { n = tab.length; n } == 0) tab = initTable()
      else if ({ f = tabAt(tab, { i = (n - 1) & h; i }); f == null }) {
        val r = new ReservationNode[K, V]
        r.synchronized {
          if (casTabAt(tab, i, null, r)) {
            binCount = 1
            var node: Node[K, V] = null
            try
              if ({ `val` = mappingFunction.apply(key); `val` } != null)
                node = new Node[K, V](h, key, `val`)
            finally setTabAt(tab, i, node)
          }
        }
        if (binCount != 0) break = true
      } else if ({ fh = f.hash; fh } == MOVED)
        tab = helpTransfer(tab, f)
      else if (fh == h // check first node without acquiring lock
          && (({ fk = f.key; fk } eq key) || (fk != null && key.equals(fk))) && { fv = f.`val`; fv } != null) {
        return fv
      } else {
        var added = false
        f.synchronized {
          if (tabAt(tab, i) eq f) if (fh >= 0) {
            binCount = 1
            var e = f
            var break = false
            while (!break) {
              var ek: K = null.asInstanceOf[K]
              if (e.hash == h && (({ ek = e.key; ek } eq key) || (ek != null && key.equals(ek)))) {
                `val` = e.`val`
                break = true
              }
              if (!break) {
                val pred = e
                if ({ e = e.next; e == null }) {
                  if ({ `val` = mappingFunction.apply(key); `val` != null }) {
                    if (pred.next != null)
                      throw new IllegalStateException("Recursive update")
                    added = true
                    pred.next = new Node[K, V](h, key, `val`)
                  }
                  break = true
                }
              }
              if (!break) binCount += 1
            }
          } else if (f.isInstanceOf[TreeBin[_, _]]) {
            binCount = 2
            val t = f.asInstanceOf[TreeBin[K, V]]
            var r: TreeNode[K, V] = null
            var p: TreeNode[K, V] = null
            if ({ r = t.root; r } != null && { p = r.findTreeNode(h, key, null); p } != null) `val` = p.`val`
            else if ({ `val` = mappingFunction.apply(key); `val` } != null) {
              added = true
              t.putTreeVal(h, key, `val`)
            }
          } else if (f.isInstanceOf[ReservationNode[_, _]]) throw new IllegalStateException("Recursive update")
        }
        if (binCount != 0) {
          if (binCount >= TREEIFY_THRESHOLD)
            treeifyBin(tab, i)
          if (!added)
            return `val`
          break = true
        }
      }
    }
    if (`val` != null) addCount(1L, binCount)
    `val`
  }

  override def computeIfPresent(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = {
    if (key == null || remappingFunction == null) throw new NullPointerException
    val h = spread(key.hashCode())
    var `val`: V = null.asInstanceOf[V]
    var delta = 0
    var binCount = 0
    var tab = table
    var break = false
    while (!break) {
      var f: Node[K, V] = null
      var n = 0
      var i = 0
      var fh = 0
      if (tab == null || { n = tab.length; n } == 0) tab = initTable()
      else if ({ f = tabAt(tab, { i = (n - 1) & h; i }); f == null })
        break = true
      else if ({ fh = f.hash; fh } == MOVED)
        tab = helpTransfer(tab, f)
      else {
        f.synchronized {
          if (tabAt(tab, i) eq f) if (fh >= 0) {
            binCount = 1
            var e = f
            var pred: Node[K, V] = null
            var break = false
            while (!break) {
              var ek: K = null.asInstanceOf[K]
              if (e.hash == h && (({ ek = e.key; ek } eq key) || (ek != null && key.equals(ek)))) {
                `val` = remappingFunction.apply(key, e.`val`)
                if (`val` != null) e.`val` = `val`
                else {
                  delta = -1
                  val en = e.next
                  if (pred != null) pred.next = en
                  else setTabAt(tab, i, en)
                }
                break = true
              } else {
                pred = e
                if ({ e = e.next; e } == null) break = true
                else binCount += 1
              }
            }
          } else if (f.isInstanceOf[TreeBin[_, _]]) {
            binCount = 2
            val t = f.asInstanceOf[TreeBin[K, V]]
            var r: TreeNode[K, V] = null
            var p: TreeNode[K, V] = null
            if ({ r = t.root; r } != null && { p = r.findTreeNode(h, key, null); p } != null) {
              `val` = remappingFunction.apply(key, p.`val`)
              if (`val` != null) p.`val` = `val`
              else {
                delta = -1
                if (t.removeTreeNode(p))
                  ConcurrentHashMap
                    .setTabAt(tab, i, untreeify(t.first))
              }
            }
          } else if (f.isInstanceOf[ReservationNode[_, _]]) throw new IllegalStateException("Recursive update")
        }
        if (binCount != 0) break = true
      }
    }
    if (delta != 0) addCount(delta.toLong, binCount)
    `val`
  }

  override def compute(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = {
    if (key == null || remappingFunction == null) throw new NullPointerException
    val h = spread(key.hashCode())
    var `val`: V = null.asInstanceOf[V]
    var delta = 0
    var binCount = 0
    var tab = table
    var break = false
    while (!break) {
      var f: Node[K, V] = null
      var n = 0
      var i = 0
      var fh = 0
      if (tab == null || { n = tab.length; n } == 0) tab = initTable()
      else if ({ f = tabAt(tab, { i = (n - 1) & h; i }); f == null }) {
        val r = new ReservationNode[K, V]
        r.synchronized {
          if (casTabAt(tab, i, null, r)) {
            binCount = 1
            var node: Node[K, V] = null
            try
              if ({ `val` = remappingFunction.apply(key, null.asInstanceOf[V]); `val` } != null) {
                delta = 1
                node = new Node[K, V](h, key, `val`)
              }
            finally setTabAt(tab, i, node)
          }
        }
        if (binCount != 0) break = true
      } else if ({ fh = f.hash; fh } == MOVED)
        tab = helpTransfer(tab, f)
      else {
        f.synchronized {
          if (tabAt(tab, i) eq f) if (fh >= 0) {
            binCount = 1
            var e = f
            var pred: Node[K, V] = null
            var break = false
            while (!break) {
              var ek: K = null.asInstanceOf[K]
              if (e.hash == h && (({ ek = e.key; ek } eq key) || (ek != null && key.equals(ek)))) {
                `val` = remappingFunction.apply(key, e.`val`)
                if (`val` != null) e.`val` = `val`
                else {
                  delta = -1
                  val en = e.next
                  if (pred != null) pred.next = en
                  else setTabAt(tab, i, en)
                }
                break = true
              } else {
                pred = e
                if ({ e = e.next; e } == null) {
                  `val` = remappingFunction.apply(key, null.asInstanceOf[V])
                  if (`val` != null) {
                    if (pred.next != null)
                      throw new IllegalStateException("Recursive update")
                    delta = 1
                    pred.next = new Node[K, V](h, key, `val`)
                  }
                  break = true
                } else binCount += 1
              }
            }
          } else if (f.isInstanceOf[TreeBin[_, _]]) {
            binCount = 1
            val t = f.asInstanceOf[TreeBin[K, V]]
            var r: TreeNode[K, V] = null
            var p: TreeNode[K, V] = null
            if ({ r = t.root; r } != null) p = r.findTreeNode(h, key, null)
            else p = null
            val pv =
              if (p == null) null.asInstanceOf[V]
              else p.`val`
            `val` = remappingFunction.apply(key, pv)
            if (`val` != null)
              if (p != null) p.`val` = `val`
              else {
                delta = 1
                t.putTreeVal(h, key, `val`)
              }
            else if (p != null) {
              delta = -1
              if (t.removeTreeNode(p))
                ConcurrentHashMap
                  .setTabAt(tab, i, untreeify(t.first))
            }
          } else if (f.isInstanceOf[ReservationNode[_, _]]) throw new IllegalStateException("Recursive update")
        }
        if (binCount != 0) {
          if (binCount >= TREEIFY_THRESHOLD)
            treeifyBin(tab, i)
          break = true

        }
      }
    }
    if (delta != 0) addCount(delta.toLong, binCount)
    `val`
  }

  override def merge(
      key: K,
      value: V,
      remappingFunction: BiFunction[_ >: V, _ >: V, _ <: V]
  ): V = {
    if (key == null || value == null || remappingFunction == null)
      throw new NullPointerException
    val h = spread(key.hashCode())
    var `val`: V = null.asInstanceOf[V]
    var delta = 0
    var binCount = 0
    var tab = table
    var break = false
    while (!break) {
      var f: Node[K, V] = null
      var n = 0
      var i = 0
      var fh = 0
      if (tab == null || { n = tab.length; n } == 0) tab = initTable()
      else if ({ f = tabAt(tab, { i = (n - 1) & h; i }); f == null }) {
        if (casTabAt(tab, i, null, new Node[K, V](h, key, value))) {
          delta = 1
          `val` = value
          break = true
        }
      } else if ({ fh = f.hash; fh == MOVED })
        tab = helpTransfer(tab, f)
      else {
        f.synchronized {
          if (tabAt(tab, i) eq f) if (fh >= 0) {
            binCount = 1
            var e = f
            var pred: Node[K, V] = null
            var break = false
            while (!break) {
              var ek: K = null.asInstanceOf[K]
              if (e.hash == h && (({ ek = e.key; ek } eq key) || (ek != null && key.equals(ek)))) {
                `val` = remappingFunction.apply(e.`val`, value)
                if (`val` != null) e.`val` = `val`
                else {
                  delta = -1
                  val en = e.next
                  if (pred != null) pred.next = en
                  else setTabAt(tab, i, en)
                }
                break = true
              } else {
                pred = e
                if ({ e = e.next; e } == null) {
                  delta = 1
                  `val` = value
                  pred.next = new Node[K, V](h, key, `val`)
                  break = true
                }
              }
              if (!break) binCount += 1
            }
          } else if (f.isInstanceOf[TreeBin[_, _]]) {
            binCount = 2
            val t = f.asInstanceOf[TreeBin[K, V]]
            val r = t.root
            val p =
              if (r == null) null
              else r.findTreeNode(h, key, null)
            `val` =
              if (p == null) value
              else remappingFunction.apply(p.`val`, value)
            if (`val` != null)
              if (p != null) p.`val` = `val`
              else {
                delta = 1
                t.putTreeVal(h, key, `val`)
              }
            else if (p != null) {
              delta = -1
              if (t.removeTreeNode(p))
                ConcurrentHashMap
                  .setTabAt(tab, i, untreeify(t.first))
            }
          } else if (f.isInstanceOf[ReservationNode[_, _]]) throw new IllegalStateException("Recursive update")
        }
        if (binCount != 0) {
          if (binCount >= TREEIFY_THRESHOLD)
            treeifyBin(tab, i)
          break = true
        }
      }
    }
    if (delta != 0) addCount(delta.toLong, binCount)
    `val`
  }

  // Hashtable legacy methods
  def contains(value: Any): Boolean = containsValue(value)

  def keys: Enumeration[K] = {
    var t: Array[Node[K, V]] = null
    val f =
      if ({ t = table; t } == null) 0
      else t.length
    new KeyIterator[K, V](t, f, 0, f, this)
  }

  def elements: Enumeration[V] = {
    var t: Array[Node[K, V]] = null
    val f =
      if ({ t = table; t } == null) 0
      else t.length
    new ValueIterator[K, V](t, f, 0, f, this)
  }

  // ConcurrentHashMap-only methods
  def mappingCount: Long = {
    val n = sumCount
    if (n < 0L) 0L
    else n // ignore transient negative values

  }

  def keySet(mappedValue: V): KeySetView[K, V] = {
    if (mappedValue == null) throw new NullPointerException
    new KeySetView[K, V](this, mappedValue)
  }

  private final def initTable() = {
    var tab: Array[Node[K, V]] = null
    var sc = 0
    var break = false
    while (!break && ({ tab = table; tab } == null || tab.length == 0))
      if ({ sc = sizeCtl; sc } < 0)
        Thread.`yield`() // lost initialization race; just spin
      else if (this.SIZECTL.compareExchangeStrong(sc, -1)) {
        try
          if ({ tab = table; tab } == null || tab.length == 0) {
            val n =
              if (sc > 0) sc
              else DEFAULT_CAPACITY
            val nt =
              new Array[Node[_, _]](n)
                .asInstanceOf[Array[Node[K, V]]]
            table = nt
            tab = nt
            sc = n - (n >>> 2)
          }
        finally sizeCtl = sc
        break = true
      }
    tab
  }

  private final def addCount(x: Long, check: Int): Unit = {
    var cs: Array[CounterCell] = null
    var b = 0L
    var s = 0L
    if ({ cs = counterCells; cs } != null || !this.BASECOUNT
          .compareExchangeStrong({ b = baseCount; b }, { s = b + x; s })) {
      var c: CounterCell = null
      var v = 0L
      var m = 0
      var uncontended = true
      if (cs == null || { m = cs.length - 1; m < 0 } || { c = cs(ThreadLocalRandom.getProbe() & m); c == null } ||
          { uncontended = c.CELLVALUE.compareExchangeStrong({ v = c.value; v }, v + x); !uncontended }) {
        fullAddCount(x, uncontended)
        return
      }

      if (check <= 1)
        return
      s = sumCount
    }

    if (check >= 0) {
      var tab: Array[Node[K, V]] = null
      var nt: Array[Node[K, V]] = null
      var n = 0
      var sc = 0
      var break = false
      while (!break && (
            s >= { sc = sizeCtl; sc }.toLong && { tab = table; tab } != null && { n = tab.length; n < MAXIMUM_CAPACITY }
          )) {
        val rs = resizeStamp(n) << RESIZE_STAMP_SHIFT
        if (sc < 0) {
          if (sc == rs + MAX_RESIZERS || sc == rs + 1 || { nt = nextTable; nt } == null || transferIndex <= 0)
            break = true
          else if (this.SIZECTL.compareExchangeStrong(sc, sc + 1)) transfer(tab, nt)
        } else if (this.SIZECTL.compareExchangeStrong(sc, rs + 2)) transfer(tab, null)
        if (!break) s = sumCount
      }
    }
  }

  private[concurrent] final def helpTransfer(
      tab: Array[Node[K, V]],
      f: Node[K, V]
  ): Array[Node[K, V]] = {
    var nextTab: Array[Node[K, V]] = null
    var sc = 0
    if (tab != null &&
        f.isInstanceOf[ForwardingNode[_, _]] && {
          nextTab = f.asInstanceOf[ForwardingNode[K, V]].nextTable; nextTab != null
        }) {
      val rs = resizeStamp(tab.length) << RESIZE_STAMP_SHIFT
      var break = false
      while (!break && ((nextTab eq nextTable) && (table eq tab) && { sc = sizeCtl; sc < 0 })) {
        if (sc == rs + MAX_RESIZERS || sc == rs + 1 || transferIndex <= 0)
          break = true
        else if (this.SIZECTL.compareExchangeStrong(sc, sc + 1)) {
          transfer(tab, nextTab)
          break = true
        }
      }
      return nextTab
    }
    table
  }

  private final def tryPresize(size: Int): Unit = {
    val c =
      if (size >= (MAXIMUM_CAPACITY >>> 1))
        MAXIMUM_CAPACITY
      else tableSizeFor(size + (size >>> 1) + 1)
    var sc = 0
    var break = false
    while (!break && { sc = sizeCtl; sc } >= 0) {
      val tab = table
      var n = 0
      if (tab == null || { n = tab.length; n } == 0) {
        n = if (sc > c) sc else c
        if (this.SIZECTL.compareExchangeStrong(sc, -1))
          try
            if (table eq tab) {
              val nt =
                new Array[Node[_, _]](n)
                  .asInstanceOf[Array[Node[K, V]]]
              table = nt
              sc = n - (n >>> 2)
            }
          finally sizeCtl = sc
      } else if (c <= sc || n >= MAXIMUM_CAPACITY)
        break = true
      else if (tab eq table) {
        val rs = resizeStamp(n)
        if (this.SIZECTL.compareExchangeStrong(sc, (rs << RESIZE_STAMP_SHIFT) + 2)) transfer(tab, null)
      }
    }
  }

  private final def transfer(
      tab: Array[Node[K, V]],
      _nextTab: Array[Node[K, V]]
  ): Unit = {
    var nextTab = _nextTab
    val n = tab.length
    var stride = 0
    if ({
      stride =
        if (NCPU > 1) (n >>> 3) / NCPU
        else n;
      stride < MIN_TRANSFER_STRIDE
    })
      stride = MIN_TRANSFER_STRIDE // subdivide range
    if (nextTab == null) { // initiating
      try {
        val nt =
          new Array[Node[_, _]](n << 1)
            .asInstanceOf[Array[Node[K, V]]]
        nextTab = nt
      } catch {
        case ex: Throwable =>
          // try to cope with OOME
          sizeCtl = Integer.MAX_VALUE
          return
      }
      nextTable = nextTab
      transferIndex = n
    }
    val nextn = nextTab.length
    val fwd = new ForwardingNode[K, V](nextTab)
    var advance = true
    var finishing = false // to ensure sweep before committing nextTab

    var i = 0
    var bound = 0
    while (true) {
      var f: Node[K, V] = null
      var fh = 0
      while (advance) {
        var nextIndex = 0
        var nextBound = 0
        if ({ i -= 1; i >= bound } || finishing) advance = false
        else if ({ nextIndex = transferIndex; nextIndex } <= 0) {
          i = -1
          advance = false
        } else if (this.TRANSFERINDEX.compareExchangeStrong(
              nextIndex, {
                nextBound =
                  if (nextIndex > stride) nextIndex - stride
                  else 0
                nextBound
              }
            )) {
          bound = nextBound
          i = nextIndex - 1
          advance = false
        }
      }
      if (i < 0 || i >= n || i + n >= nextn) {
        var sc = 0
        if (finishing) {
          nextTable = null
          table = nextTab
          sizeCtl = (n << 1) - (n >>> 1)
          return
        }
        if (this.SIZECTL.compareExchangeStrong(
              { sc = sizeCtl; sc },
              sc - 1
            )) {

          if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
            return

          finishing = true
          advance = true

          i = n // recheck before commit
        }
      } else if ({ f = tabAt(tab, i); f } == null)
        advance = casTabAt(tab, i, null, fwd)
      else if ({ fh = f.hash; fh } == MOVED)
        advance = true // already processed
      else
        f.synchronized {
          if (tabAt(tab, i) eq f) {
            var ln: Node[K, V] = null
            var hn: Node[K, V] = null
            if (fh >= 0) {
              var runBit = fh & n
              var lastRun = f
              var p = f.next
              while (p != null) {
                val b = p.hash & n
                if (b != runBit) {
                  runBit = b
                  lastRun = p
                }

                p = p.next
              }
              if (runBit == 0) {
                ln = lastRun
                hn = null
              } else {
                hn = lastRun
                ln = null
              }
              p = f
              while (p ne lastRun) {
                val ph = p.hash
                val pk = p.key
                val pv = p.`val`
                if ((ph & n) == 0)
                  ln = new Node[K, V](ph, pk, pv, ln)
                else hn = new Node[K, V](ph, pk, pv, hn)

                p = p.next
              }
              setTabAt(nextTab, i, ln)
              setTabAt(nextTab, i + n, hn)
              setTabAt(tab, i, fwd)
              advance = true
            } else if (f.isInstanceOf[TreeBin[_, _]]) {
              val t = f.asInstanceOf[TreeBin[K, V]]
              var lo: TreeNode[K, V] = null
              var loTail: TreeNode[K, V] = null
              var hi: TreeNode[K, V] = null
              var hiTail: TreeNode[K, V] = null
              var lc = 0
              var hc = 0
              var e: Node[K, V] = t.first
              while (e != null) {
                val h = e.hash
                val p = new TreeNode[K, V](
                  h,
                  e.key,
                  e.`val`,
                  null,
                  null
                )
                if ((h & n) == 0) {
                  if ({ p.prev = loTail; loTail } == null) lo = p
                  else loTail.next = p
                  loTail = p
                  lc += 1
                } else {
                  if ({ p.prev = hiTail; hiTail } == null) hi = p
                  else hiTail.next = p
                  hiTail = p
                  hc += 1
                }

                e = e.next
              }
              ln =
                if (lc <= UNTREEIFY_THRESHOLD)
                  untreeify(lo)
                else if (hc != 0) new TreeBin[K, V](lo)
                else t
              hn =
                if (hc <= UNTREEIFY_THRESHOLD)
                  untreeify(hi)
                else if (lc != 0) new TreeBin[K, V](hi)
                else t
              setTabAt(nextTab, i, ln)
              setTabAt(nextTab, i + n, hn)
              setTabAt(tab, i, fwd)
              advance = true
            } else if (f.isInstanceOf[ReservationNode[_, _]])
              throw new IllegalStateException("Recursive update")
          }
        }
    }
  }

  private[concurrent] final def sumCount = {
    val cs = counterCells
    var sum = baseCount
    if (cs != null) for (c <- cs) {
      if (c != null) sum += c.value
    }
    sum
  }

  // See LongAdder version for explanation
  private final def fullAddCount(x: Long, _wasUncontended: Boolean): Unit = {
    var h = 0
    var wasUncontended = _wasUncontended
    if ({ h = ThreadLocalRandom.getProbe(); h } == 0) {
      ThreadLocalRandom.localInit() // force initialization
      h = ThreadLocalRandom.getProbe()
      wasUncontended = true
    }
    var collide = false // True if last slot nonempty

    var break = false
    while (!break) {
      var cs: Array[CounterCell] = null
      var c: CounterCell = null
      var n = 0
      var v = 0L
      if ({ cs = counterCells; cs } != null && { n = cs.length; n } > 0) {
        if ({ c = cs((n - 1) & h); c } == null) {
          if (cellsBusy == 0) { // Try to attach new Cell
            val r = new CounterCell(x) // Optimistic create
            if (cellsBusy == 0 && this.CELLSBUSY.compareExchangeStrong(0, 1)) {
              var created = false
              try { // Recheck under lock
                var rs: Array[CounterCell] = null
                var m = 0
                var j = 0
                if ({ rs = counterCells; rs } != null && { m = rs.length; m } > 0 &&
                    rs({ j = (m - 1) & h; j }) == null) {
                  rs(j) = r
                  created = true
                }
              } finally cellsBusy = 0
              if (created) break = true
              // continue // Slot is now non-empty
            }
          } else collide = false
        } else if (!wasUncontended) // CAS already known to fail
          wasUncontended = true // Continue after rehash
        else if (c.CELLVALUE.compareExchangeStrong(
              { v = c.value; v },
              v + x
            )) break = true
        else if ((counterCells ne cs) || n >= NCPU)
          collide = false // At max size or stale
        else if (!collide) collide = true
        else if (cellsBusy == 0 && this.CELLSBUSY.compareExchangeStrong(0, 1)) {
          try
            if (counterCells eq cs)
              counterCells = Arrays.copyOf(cs, n << 1) // Expand table unless stale

          finally cellsBusy = 0
          collide = false
          // continue // Retry with expanded table
        } else h = ThreadLocalRandom.advanceProbe(h)
      } else if (cellsBusy == 0 && (counterCells eq cs) &&
          this.CELLSBUSY.compareExchangeStrong(0, 1)) {
        var init = false
        try // Initialize table
          if (counterCells eq cs) {
            val rs = new Array[CounterCell](2)
            rs(h & 1) = new CounterCell(x)
            counterCells = rs
            init = true
          }
        finally cellsBusy = 0
        if (init) break = true
      } else if (this.BASECOUNT.compareExchangeStrong(
            { v = baseCount; v },
            v + x
          )) {
        break = true // Fall back on using base
      }
    }
  }

  /* ---------------- Conversion from/to TreeBins -------------- */
  private final def treeifyBin(
      tab: Array[Node[K, V]],
      index: Int
  ): Unit = {
    var b: Node[K, V] = null
    var n = 0
    if (tab != null)
      if ({ n = tab.length; n } < MIN_TREEIFY_CAPACITY)
        tryPresize(n << 1)
      else if ({ b = tabAt(tab, index); b } != null && b.hash >= 0)
        b.synchronized {
          if (tabAt(tab, index) eq b) {
            var hd: TreeNode[K, V] = null
            var tl: TreeNode[K, V] = null
            var e = b
            while (e != null) {
              val p = new TreeNode[K, V](
                e.hash,
                e.key,
                e.`val`,
                null,
                null
              )
              if ({ p.prev = tl; tl } == null) hd = p
              else tl.next = p
              tl = p

              e = e.next
            }
            setTabAt(
              tab,
              index,
              new TreeBin[K, V](hd)
            )
          }
        }
  }

  // Parallel bulk operations
  private[concurrent] final def batchFor(b: Long): Int = {
    var n = 0L
    if (b == java.lang.Long.MAX_VALUE || { n = sumCount; n <= 1L } || n < b)
      return 0
    val sp = ForkJoinPool.getCommonPoolParallelism() << 2 // slack of 4

    if (b <= 0L || { n /= b; n >= sp }) sp
    else n.toInt
  }

  def forEach(
      parallelismThreshold: Long,
      action: BiConsumer[_ >: K, _ >: V]
  ): Unit = {
    if (action == null) throw new NullPointerException
    new ForEachMappingTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      action
    ).invoke()
  }

  def forEach[U <: AnyRef](
      parallelismThreshold: Long,
      transformer: BiFunction[_ >: K, _ >: V, _ <: U],
      action: Consumer[_ >: U]
  ): Unit = {
    if (transformer == null || action == null) throw new NullPointerException
    new ForEachTransformedMappingTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      transformer,
      action
    ).invoke()
  }

  def search[U <: AnyRef](
      parallelismThreshold: Long,
      searchFunction: BiFunction[_ >: K, _ >: V, _ <: U]
  ): U = {
    if (searchFunction == null) throw new NullPointerException
    new SearchMappingsTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      searchFunction,
      new AtomicReference[U]
    ).invoke()
  }

  def reduce[U <: AnyRef](
      parallelismThreshold: Long,
      transformer: BiFunction[_ >: K, _ >: V, _ <: U],
      reducer: BiFunction[_ >: U, _ >: U, _ <: U]
  ): U = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceMappingsTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      reducer
    ).invoke()
  }

  def reduceToDouble(
      parallelismThreshold: Long,
      transformer: ToDoubleBiFunction[_ >: K, _ >: V],
      basis: Double,
      reducer: DoubleBinaryOperator
  ): Double = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceMappingsToDoubleTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def reduceToLong(
      parallelismThreshold: Long,
      transformer: ToLongBiFunction[_ >: K, _ >: V],
      basis: Long,
      reducer: LongBinaryOperator
  ): Long = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceMappingsToLongTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def reduceToInt(
      parallelismThreshold: Long,
      transformer: ToIntBiFunction[_ >: K, _ >: V],
      basis: Int,
      reducer: IntBinaryOperator
  ): Int = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceMappingsToIntTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def forEachKey(parallelismThreshold: Long, action: Consumer[_ >: K]): Unit = {
    if (action == null) throw new NullPointerException
    new ForEachKeyTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      action
    ).invoke()
  }

  def forEachKey[U <: AnyRef](
      parallelismThreshold: Long,
      transformer: Function[_ >: K, _ <: U],
      action: Consumer[_ >: U]
  ): Unit = {
    if (transformer == null || action == null) throw new NullPointerException
    new ForEachTransformedKeyTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      transformer,
      action
    ).invoke()
  }

  def searchKeys[U <: AnyRef](
      parallelismThreshold: Long,
      searchFunction: Function[_ >: K, _ <: U]
  ): U = {
    if (searchFunction == null) throw new NullPointerException
    new SearchKeysTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      searchFunction,
      new AtomicReference[U]
    ).invoke()
  }

  def reduceKeys(
      parallelismThreshold: Long,
      reducer: BiFunction[_ >: K, _ >: K, _ <: K]
  ): K = {
    if (reducer == null) throw new NullPointerException
    new ReduceKeysTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      reducer
    ).invoke()
  }

  def reduceKeys[U <: AnyRef](
      parallelismThreshold: Long,
      transformer: Function[_ >: K, _ <: U],
      reducer: BiFunction[_ >: U, _ >: U, _ <: U]
  ): U = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceKeysTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      reducer
    ).invoke()
  }

  def reduceKeysToDouble(
      parallelismThreshold: Long,
      transformer: ToDoubleFunction[_ >: K],
      basis: Double,
      reducer: DoubleBinaryOperator
  ): Double = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceKeysToDoubleTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def reduceKeysToLong(
      parallelismThreshold: Long,
      transformer: ToLongFunction[_ >: K],
      basis: Long,
      reducer: LongBinaryOperator
  ): Long = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceKeysToLongTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def reduceKeysToInt(
      parallelismThreshold: Long,
      transformer: ToIntFunction[_ >: K],
      basis: Int,
      reducer: IntBinaryOperator
  ): Int = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceKeysToIntTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def forEachValue(
      parallelismThreshold: Long,
      action: Consumer[_ >: V]
  ): Unit = {
    if (action == null) throw new NullPointerException
    new ForEachValueTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      action
    ).invoke()
  }

  def forEachValue[U <: AnyRef](
      parallelismThreshold: Long,
      transformer: Function[_ >: V, _ <: U],
      action: Consumer[_ >: U]
  ): Unit = {
    if (transformer == null || action == null) throw new NullPointerException
    new ForEachTransformedValueTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      transformer,
      action
    ).invoke()
  }

  def searchValues[U <: AnyRef](
      parallelismThreshold: Long,
      searchFunction: Function[_ >: V, _ <: U]
  ): U = {
    if (searchFunction == null) throw new NullPointerException
    new SearchValuesTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      searchFunction,
      new AtomicReference[U]
    ).invoke()
  }

  def reduceValues(
      parallelismThreshold: Long,
      reducer: BiFunction[_ >: V, _ >: V, _ <: V]
  ): V = {
    if (reducer == null) throw new NullPointerException
    new ReduceValuesTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      reducer
    ).invoke()
  }

  def reduceValues[U <: AnyRef](
      parallelismThreshold: Long,
      transformer: Function[_ >: V, _ <: U],
      reducer: BiFunction[_ >: U, _ >: U, _ <: U]
  ): U = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceValuesTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      reducer
    ).invoke()
  }

  def reduceValuesToDouble(
      parallelismThreshold: Long,
      transformer: ToDoubleFunction[_ >: V],
      basis: Double,
      reducer: DoubleBinaryOperator
  ): Double = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceValuesToDoubleTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def reduceValuesToLong(
      parallelismThreshold: Long,
      transformer: ToLongFunction[_ >: V],
      basis: Long,
      reducer: LongBinaryOperator
  ): Long = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceValuesToLongTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def reduceValuesToInt(
      parallelismThreshold: Long,
      transformer: ToIntFunction[_ >: V],
      basis: Int,
      reducer: IntBinaryOperator
  ): Int = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceValuesToIntTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def forEachEntry(
      parallelismThreshold: Long,
      action: Consumer[_ >: util.Map.Entry[K, V]]
  ): Unit = {
    if (action == null) throw new NullPointerException
    new ForEachEntryTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      action
    ).invoke()
  }

  def forEachEntry[U <: AnyRef](
      parallelismThreshold: Long,
      transformer: Function[util.Map.Entry[K, V], _ <: U],
      action: Consumer[_ >: U]
  ): Unit = {
    if (transformer == null || action == null) throw new NullPointerException
    new ForEachTransformedEntryTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      transformer,
      action
    ).invoke()
  }

  def searchEntries[U <: AnyRef](
      parallelismThreshold: Long,
      searchFunction: Function[util.Map.Entry[K, V], _ <: U]
  ): U = {
    if (searchFunction == null) throw new NullPointerException
    new SearchEntriesTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      searchFunction,
      new AtomicReference[U]
    ).invoke()
  }

  def reduceEntries(
      parallelismThreshold: Long,
      reducer: BiFunction[
        util.Map.Entry[K, V],
        util.Map.Entry[K, V],
        _ <: util.Map.Entry[K, V]
      ]
  ): util.Map.Entry[K, V] = {
    if (reducer == null) throw new NullPointerException
    new ReduceEntriesTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      reducer
    ).invoke()
  }

  def reduceEntries[U <: AnyRef](
      parallelismThreshold: Long,
      transformer: Function[util.Map.Entry[K, V], _ <: U],
      reducer: BiFunction[_ >: U, _ >: U, _ <: U]
  ): U = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceEntriesTask[K, V, U](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      reducer
    ).invoke()
  }

  def reduceEntriesToDouble(
      parallelismThreshold: Long,
      transformer: ToDoubleFunction[util.Map.Entry[K, V]],
      basis: Double,
      reducer: DoubleBinaryOperator
  ): Double = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceEntriesToDoubleTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def reduceEntriesToLong(
      parallelismThreshold: Long,
      transformer: ToLongFunction[util.Map.Entry[K, V]],
      basis: Long,
      reducer: LongBinaryOperator
  ): Long = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceEntriesToLongTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }

  def reduceEntriesToInt(
      parallelismThreshold: Long,
      transformer: ToIntFunction[util.Map.Entry[K, V]],
      basis: Int,
      reducer: IntBinaryOperator
  ): Int = {
    if (transformer == null || reducer == null) throw new NullPointerException
    new MapReduceEntriesToIntTask[K, V](
      null,
      batchFor(parallelismThreshold),
      0,
      0,
      table,
      null,
      transformer,
      basis,
      reducer
    ).invoke()
  }
}
