/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.collection
package mutable

import scala.annotation.tailrec
import scala.collection.immutable.{List, Nil, ::}
import java.lang.{IllegalArgumentException, IndexOutOfBoundsException}
import scala.collection.generic.DefaultSerializable
import scala.runtime.Statics.releaseFence

/** A `Buffer` implementation backed by a list. It provides constant time
  *  prepend and append. Most other operations are linear.
  *
  *  @see [[http://docs.scala-lang.org/overviews/collections/concrete-mutable-collection-classes.html#list-buffers "Scala's Collection Library overview"]]
  *  section on `List Buffers` for more information.
  *
  *  @tparam A    the type of this list buffer's elements.
  *
  *  @define Coll `ListBuffer`
  *  @define coll list buffer
  *  @define orderDependent
  *  @define orderDependentFold
  *  @define mayNotTerminateInf
  *  @define willNotTerminateInf
  */
class ListBuffer[A]
  extends AbstractBuffer[A]
     with SeqOps[A, ListBuffer, ListBuffer[A]]
     with StrictOptimizedSeqOps[A, ListBuffer, ListBuffer[A]]
     with ReusableBuilder[A, immutable.List[A]]
     with IterableFactoryDefaults[A, ListBuffer]
     with DefaultSerializable {

  private var first: List[A] = Nil
  private var last0: ::[A] = null
  private[this] var aliased = false
  private[this] var len = 0

  private type Predecessor[A0] = ::[A0] /*| Null*/

  def iterator = first.iterator

  override def iterableFactory: SeqFactory[ListBuffer] = ListBuffer

  @throws[IndexOutOfBoundsException]
  def apply(i: Int) = first.apply(i)

  def length = len
  override def knownSize = len

  override def isEmpty: Boolean = len == 0

  private def copyElems(): Unit = {
    val buf = ListBuffer.from(this)
    first = buf.first
    last0 = buf.last0
    aliased = false
  }

  private def ensureUnaliased() = if (aliased) copyElems()

  // Avoids copying where possible.
  override def toList: List[A] = {
    aliased = nonEmpty
    // We've accumulated a number of mutations to `List.tail` by this stage.
    // Make sure they are visible to threads that the client of this ListBuffer might be about
    // to share this List with.
    releaseFence()
    first
  }

  def result(): immutable.List[A] = toList

  /** Prepends the elements of this buffer to a given list
    *
    *  @param xs   the list to which elements are prepended
    */
  def prependToList(xs: List[A]): List[A] = {
    if (isEmpty) xs
    else {
      ensureUnaliased()
      last0.next = xs
      toList
    }
  }

  def clear(): Unit = {
    first = Nil
    len = 0
    last0 = null
    aliased = false
  }

  final def addOne(elem: A): this.type = {
    ensureUnaliased()
    val last1 = new ::[A](elem, Nil)
    if (len == 0) first = last1 else last0.next = last1
    last0 = last1
    len += 1
    this
  }

  // Overridden for performance
  override final def addAll(xs: IterableOnce[A]): this.type = {
    val it = xs.iterator
    if (it.hasNext) {
      ensureUnaliased()
      val last1 = new ::[A](it.next(), Nil)
      if (len == 0) first = last1 else last0.next = last1
      last0 = last1
      len += 1
      while (it.hasNext) {
        val last1 = new ::[A](it.next(), Nil)
        last0.next = last1
        last0 = last1
        len += 1
      }
    }
    this
  }

  override def subtractOne(elem: A): this.type = {
    ensureUnaliased()
    if (isEmpty) {}
    else if (first.head == elem) {
      first = first.tail
      reduceLengthBy(1)
    }
    else {
      var cursor = first
      while (!cursor.tail.isEmpty && cursor.tail.head != elem) {
        cursor = cursor.tail
      }
      if (!cursor.tail.isEmpty) {
        val z = cursor.asInstanceOf[::[A]]
        if (z.next == last0)
          last0 = z
        z.next = cursor.tail.tail
        reduceLengthBy(1)
      }
    }
    this
  }

  /** Reduce the length of the buffer, and null out last0
    *  if this reduces the length to 0.
    */
  private def reduceLengthBy(num: Int): Unit = {
    len -= num
    if (len <= 0)   // obviously shouldn't be < 0, but still better not to leak
      last0 = null
  }

  private def locate(i: Int): Predecessor[A] =
    if (i == 0) null
    else if (i == len) last0
    else {
      var j = i - 1
      var p = first
      while (j > 0) {
        p = p.tail
        j -= 1
      }
      p.asInstanceOf[Predecessor[A]]
    }

  private def getNext(p: Predecessor[A]): List[A] =
    if (p == null) first else p.next

  def update(idx: Int, elem: A): Unit = {
    ensureUnaliased()
    if (idx < 0 || idx >= len) throw new IndexOutOfBoundsException(s"$idx is out of bounds (min 0, max ${len-1})")
    if (idx == 0) {
      val newElem = new :: (elem, first.tail)
      if (last0 eq first) {
        last0 = newElem
      }
      first = newElem
    } else {
      // `p` can not be `null` because the case where `idx == 0` is handled above
      val p = locate(idx)
      val newElem = new :: (elem, p.tail.tail)
      if (last0 eq p.tail) {
        last0 = newElem
      }
      p.asInstanceOf[::[A]].next = newElem
    }
  }

  def insert(idx: Int, elem: A): Unit = {
    ensureUnaliased()
    if (idx < 0 || idx > len) throw new IndexOutOfBoundsException(s"$idx is out of bounds (min 0, max ${len-1})")
    if (idx == len) addOne(elem)
    else {
      val p = locate(idx)
      val nx = elem :: getNext(p)
      if(p eq null) first = nx else p.next = nx
      len += 1
    }
  }

  def prepend(elem: A): this.type = {
    insert(0, elem)
    this
  }

  private def insertAfter(p: Predecessor[A], it: Iterator[A]): Predecessor[A] = {
    var prev = p
    val follow = getNext(prev)
    while (it.hasNext) {
      len += 1
      val next = (it.next() :: follow).asInstanceOf[::[A]]
      if(prev eq null) first = next else prev.next = next
      prev = next
    }
    if ((prev ne null) && prev.next.isEmpty) last0 = prev
    prev
  }

  def insertAll(idx: Int, elems: IterableOnce[A]): Unit = {
    ensureUnaliased()
    val it = elems.iterator
    if (it.hasNext) {
      ensureUnaliased()
      if (idx < 0 || idx > len) throw new IndexOutOfBoundsException(s"$idx is out of bounds (min 0, max ${len-1})")
      if (idx == len) ++=(elems)
      else insertAfter(locate(idx), it)
    }
  }

  def remove(idx: Int): A = {
    ensureUnaliased()
    if (idx < 0 || idx >= len) throw new IndexOutOfBoundsException(s"$idx is out of bounds (min 0, max ${len-1})")
    val p = locate(idx)
    val nx = getNext(p)
    if(p eq null) {
      first = nx.tail
      if(first.isEmpty) last0 = null
    } else {
      if(last0 eq nx) last0 = p
      p.next = nx.tail
    }
    len -= 1
    nx.head
  }

  def remove(idx: Int, count: Int): Unit =
    if (count > 0) {
      ensureUnaliased()
      if (idx < 0 || idx + count > len) throw new IndexOutOfBoundsException(s"$idx to ${idx + count} is out of bounds (min 0, max ${len-1})")
      removeAfter(locate(idx), count)
    } else if (count < 0) {
      throw new IllegalArgumentException("removing negative number of elements: " + count)
    }

  private def removeAfter(prev: Predecessor[A], n: Int) = {
    @tailrec def ahead(p: List[A], n: Int): List[A] =
      if (n == 0) p else ahead(p.tail, n - 1)
    val nx = ahead(getNext(prev), n)
    if(prev eq null) first = nx else prev.next = nx
    if(nx.isEmpty) last0 = prev
    len -= n
  }

  def mapInPlace(f: A => A): this.type = {
    ensureUnaliased()
    val buf = new ListBuffer[A]
    for (elem <- this) buf += f(elem)
    first = buf.first
    last0 = buf.last0
    this
  }

  def flatMapInPlace(f: A => IterableOnce[A]): this.type = {
    var src = first
    var dst: List[A] = null
    last0 = null
    len = 0
    while(!src.isEmpty) {
      val it = f(src.head).iterator
      while(it.hasNext) {
        val v = new ::(it.next(), Nil)
        if(dst eq null) dst = v else last0.next = v
        last0 = v
        len += 1
      }
      src = src.tail
    }
    first = if(dst eq null) Nil else dst
    this
  }

  def filterInPlace(p: A => Boolean): this.type = {
    ensureUnaliased()
    var prev: Predecessor[A] = null
    var cur: List[A] = first
    while (!cur.isEmpty) {
      val follow = cur.tail
      if (!p(cur.head)) {
        if(prev eq null) first = follow
        else prev.next = follow
        len -= 1
      } else {
        prev = cur.asInstanceOf[Predecessor[A]]
      }
      cur = follow
    }
    last0 = prev
    this
  }

  def patchInPlace(from: Int, patch: collection.IterableOnce[A], replaced: Int): this.type = {
    val i = math.min(math.max(from, 0), length)
    val n = math.min(math.max(replaced, 0), length)
    ensureUnaliased()
    val p = locate(i)
    removeAfter(p, math.min(n, len - i))
    insertAfter(p, patch.iterator)
    this
  }

  /**
   * Selects the last element.
   *
   * Runs in constant time.
   *
   * @return The last element of this $coll.
   * @throws NoSuchElementException If the $coll is empty.
   */
  override def last: A = if (last0 eq null) throw new NoSuchElementException("last of empty ListBuffer") else last0.head

  /**
   * Optionally selects the last element.
   *
   * Runs in constant time.
   *
   * @return the last element of this $coll$ if it is nonempty, `None` if it is empty.
   */
  override def lastOption: Option[A] = if (last0 eq null) None else Some(last0.head)

  override protected[this] def stringPrefix = "ListBuffer"

}

@SerialVersionUID(3L)
object ListBuffer extends StrictOptimizedSeqFactory[ListBuffer] {

  def from[A](coll: collection.IterableOnce[A]): ListBuffer[A] = new ListBuffer[A] ++= coll

  def newBuilder[A]: Builder[A, ListBuffer[A]] = new GrowableBuilder(empty[A])

  def empty[A]: ListBuffer[A] = new ListBuffer[A]
}
