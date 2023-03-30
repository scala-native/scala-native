// Ported from Harmony
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang

import java.lang.ref.{Reference, WeakReference}
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier
import java.util.Objects

object ThreadLocal {

  /** Hash counter. */
  private val hashCounter = new AtomicInteger(0)

  def withInitial[T <: AnyRef](supplier: Supplier[_ <: T]): ThreadLocal[T] =
    new SuppliedThreadLocal(supplier)

  private[lang] class SuppliedThreadLocal[T <: AnyRef](
      supplier: Supplier[_ <: T]
  ) extends ThreadLocal[T] {
    Objects.requireNonNull(supplier)
    override protected def initialValue(): T = supplier.get()
  }

  /** Per-thread map of ThreadLocal instances to values. */
  private[lang] object Values {

    /** Size must always be a power of 2.
     */
    private val INITIAL_SIZE = 16

    private def DefaultCapacity = INITIAL_SIZE << 1
    private def DefaultMask = DefaultCapacity - 1
    private def DefaultMaximumLoad = DefaultCapacity * 2 / 3

    /** Placeholder for deleted entries. */
    private case object TOMBSTONE

    /** Placeholder used when thread local values are not allowed */
    object Unsupported extends Values(Array.empty, -1, 0)
  }

  /** Constructs a new, empty instance. */
  private[lang] class Values(
      /** Map entries. Contains alternating keys (ThreadLocal) and values. The
       *  length is always a power of 2.
       */
      var table: Array[AnyRef] = new Array[AnyRef](Values.DefaultCapacity),
      /** Used to turn hashes into indices. */
      var mask: Int = Values.DefaultMask,
      /** Maximum number of live entries and tombstones. */
      var maximumLoad: Int = Values.DefaultMaximumLoad
  ) {

    /** Number of live entries. */
    private[lang] var size = 0

    /** Number of tombstones. */
    private var tombstones = 0

    /** Points to the next cell to clean up. */
    private var clean = 0

    /** Used for InheritableThreadLocals.
     */
    def this(fromParent: ThreadLocal.Values) = {
      this(fromParent.table.clone(), fromParent.mask, fromParent.maximumLoad)
      this.size = fromParent.size
      this.tombstones = fromParent.tombstones
      this.clean = fromParent.clean
      inheritValues(fromParent)
    }

    /** Inherits values from a parent thread.
     */
    private def inheritValues(fromParent: ThreadLocal.Values): Unit = {
      // Transfer values from parent to child thread.
      val table = this.table
      for (i <- table.length - 2 to 0 by -2) {
        val k = table(i)
        // The table can only contain null, tombstones and references.
        k match {
          case reference: Reference[
                InheritableThreadLocal[AnyRef]
              ] @unchecked =>
            // Raw type enables us to pass in an Object below.
            val key = reference.get()
            key match {
              case null =>
                table(i) = Values.TOMBSTONE
                table(i + 1) = null
                fromParent.table(i) = Values.TOMBSTONE
                fromParent.table(i + 1) = null
                tombstones += 1
                fromParent.tombstones += 1
                size -= 1
                fromParent.size -= 1
              case _ =>
                // Replace value with filtered value.
                // We should just let exceptions bubble out and tank
                // the thread creation
                table(i + 1) = key.getChildValue(fromParent.table(i + 1))
            }
          case _ => ()
        }
      }
    }

    /** Creates a new, empty table with the given capacity.
     */
    private def initializeTable(capacity: Int): Unit = {
      this.table = new Array[AnyRef](capacity << 1)
      this.mask = table.length - 1
      this.clean = 0
      this.maximumLoad = capacity * 2 / 3 // 2/3
    }

    /** Cleans up after garbage-collected thread locals.
     */
    private def cleanUp(): Unit = {
      // If we rehashed, we needn't clean up (clean up happens as a side effect).
      if (rehash()) return
      // No live entries == nothing to clean.
      if (size == 0) return

      // Clean log(table.length) entries picking up where we left off last time.
      var index = clean
      val table = this.table
      var counter = table.length
      while (counter > 0) {
        table(index) match {
          case reference: Reference[ThreadLocal[_]] @unchecked =>
            if (reference.get() == null) { // This thread local was reclaimed by the garbage collector.
              table(index) = Values.TOMBSTONE
              table(index + 1) = null
              tombstones += 1
              size -= 1
            }

          case _ => () // on to next entry
        }
        counter >>= 1
        index = next(index)
      }
      // Point cursor to next index.
      clean = index
    }

    /** Rehashes the table, expanding or contracting it as necessary. Gets rid
     *  of tombstones. Returns true if a rehash occurred. We must rehash every
     *  time we fill a null slot; we depend on the presence of null slots to end
     *  searches (otherwise, we'll infinitely loop).
     */
    private def rehash(): Boolean = {
      if (tombstones + size < maximumLoad) return false
      val capacity = table.length >> 1

      // Default to the same capacity. This will create a table of the
      // same size and move over the live entries, analogous to a
      // garbage collection. This should only happen if you churn a
      // bunch of thread local garbage (removing and reinserting
      // the same thread locals over and over will overwrite tombstones
      // and not fill up the table).
      var newCapacity = capacity
      if (size > (capacity >> 1)) {
        // More than 1/2 filled w/ live entries. Double size.
        newCapacity = capacity << 1
      }
      val oldTable = this.table
      // Allocate new table.
      initializeTable(newCapacity)
      // We won't have any tombstones after this.
      this.tombstones = 0
      // If we have no live entries, we can quit here.
      if (size == 0) return true

      // Move over entries.
      for (i <- oldTable.length - 2 to 0 by -2) oldTable(i) match {
        case reference: Reference[ThreadLocal[_]] @unchecked =>
          val key = reference.get()
          if (key != null) {
            // Entry is still live. Move it over.
            add(key, oldTable(i + 1))
          } else size -= 1
        case _ => ()
      }
      true
    }

    /** Adds an entry during rehashing. Compared to put(), this method doesn't
     *  have to clean up, check for existing entries, account for tombstones,
     *  etc.
     */
    private[lang] def add(key: ThreadLocal[_], value: AnyRef): Unit = {
      var index = key.hash & mask
      while (true) {
        val k = table(index)
        if (k == null) {
          table(index) = key.reference
          table(index + 1) = value
          return
        }
        index = next(index)
      }
    }

    /** Sets entry for given ThreadLocal to given value, creating an entry if
     *  necessary.
     */
    private[lang] def put(key: ThreadLocal[_], value: AnyRef): Unit = {
      cleanUp()
      // Keep track of first tombstone. That's where we want to go back
      // and add an entry if necessary.
      var firstTombstone = -1
      var index = key.hash & mask
      while (true) {
        val k = table(index)
        if (k eq key.reference) { // Replace existing entry.
          table(index + 1) = value
          return
        }
        if (k == null) {
          if (firstTombstone == -1) { // Fill in null slot.
            table(index) = key.reference
            table(index + 1) = value
            size += 1
            return
          }
          // Go back and replace first tombstone.
          table(firstTombstone) = key.reference
          table(firstTombstone + 1) = value
          tombstones -= 1
          size += 1
          return
        }
        // Remember first tombstone.
        if (firstTombstone == -1 && (k eq Values.TOMBSTONE))
          firstTombstone = index

        index = next(index)
      }
    }

    /** Gets value for given ThreadLocal after not finding it in the first slot.
     */
    private[lang] def getAfterMiss(key: ThreadLocal[_ <: AnyRef]): AnyRef = {
      val table = this.table
      var index = key.hash & mask
      // If the first slot is empty, the search is over.
      if (table(index) == null) {
        val value = key.initialValue()
        // If the table is still the same and the slot is still empty...
        if ((this.table eq table) && table(index) == null) {
          table(index) = key.reference
          table(index + 1) = value
          size += 1
          cleanUp()
          return value
        }
        // The table changed during initialValue().
        put(key, value)
        return value
      }
      var firstTombstone = -1
      // Continue search.
      index = next(index)
      while (true) {
        val reference = table(index)
        if (reference eq key.reference) return table(index + 1)
        // If no entry was found...
        if (reference == null) {
          val value = key.initialValue()
          // If the table is still the same...
          if (this.table eq table) { // If we passed a tombstone and that slot still
            // contains a tombstone...
            if (firstTombstone > -1 &&
                (table(firstTombstone) eq Values.TOMBSTONE)) {
              table(firstTombstone) = key.reference
              table(firstTombstone + 1) = value
              tombstones -= 1
              size += 1
              // No need to clean up here. We aren't filling
              // in a null slot.
              return value
            }
            // If this slot is still empty...
            if (table(index) == null) {
              table(index) = key.reference
              table(index + 1) = value
              size += 1
              cleanUp()
              return value
            }
          }
          put(key, value)
          return value
        }
        if (firstTombstone == -1 && (reference eq Values.TOMBSTONE)) { // Keep track of this tombstone so we can overwrite it.
          firstTombstone = index
        }

        index = next(index)
      }
      null // unreachable
    }

    /** Removes entry for the given ThreadLocal.
     */
    private[lang] def remove(key: ThreadLocal[_]): Unit = {
      cleanUp()
      var index = key.hash & mask
      while ({ true }) {
        val reference = table(index)
        if (reference eq key.reference) { // Success!
          table(index) = Values.TOMBSTONE
          table(index + 1) = null
          tombstones += 1
          size -= 1
          return
        }
        if (reference == null) { // No entry found.
          return
        }

        index = next(index)
      }
    }

    /** Gets the next index. If we're at the end of the table, we wrap back
     *  around to 0.
     */
    private def next(index: Int) = (index + 2) & mask
  }
}

class ThreadLocal[T <: AnyRef]() {
  import ThreadLocal.Values.Unsupported

  /** Returns the value of this variable for the current thread. If an entry
   *  doesn't yet exist for this variable on this thread, this method will
   *  create an entry, populating the value with the result of [[initialValue]].
   */
  def get(): T = {
    // Optimized for the fast path.
    val currentThread = Thread.currentThread()
    val values = this.values(currentThread) match {
      case Unsupported => return initialValue()
      case null        => initializeValues(currentThread)
      case values =>
        assert(values != null)
        val table = values.table
        val index = hash & values.mask
        if (this.reference eq table(index))
          return table(index + 1).asInstanceOf[T]
        values
    }
    values.getAfterMiss(this).asInstanceOf[T]
  }

  /** Provides the initial value of this variable for the current thread. The
   *  default implementation returns `null`.
   */
  protected def initialValue(): T = null.asInstanceOf[T]

  /** Sets the value of this variable for the current thread. If set to null,
   *  the value will be set to null and the underlying entry will still be
   *  present.
   */
  def set(value: T): Unit = {
    val currentThread = Thread.currentThread()
    val values = this.values(currentThread) match {
      case Unsupported => throw new UnsupportedOperationException()
      case null        => initializeValues(currentThread)
      case values      => values
    }
    values.put(this, value)
  }

  /** Removes the entry for this variable in the current thread. If this call is
   *  followed by a [[get]] before a [[set]], [[get]] will call [[initialValue]]
   *  and create a new entry with the resulting value.
   */
  def remove(): Unit = {
    val currentThread = Thread.currentThread()
    val values = this.values(currentThread)
    if (values != null && values != Unsupported) values.remove(this)
  }

  /** Gets Values instance for this thread and variable type.
   */
  protected[lang] def values(current: Thread): ThreadLocal.Values =
    current.threadLocals

  protected[lang] def initializeValues(current: Thread): ThreadLocal.Values = {
    val instance = new ThreadLocal.Values()
    current.threadLocals = instance
    instance
  }

  /** Weak reference to this thread local instance. */
  final private val reference = new WeakReference[ThreadLocal[T]](this)

  /** Internal hash. We deliberately don't bother with #hashCode(). Hashes must
   *  be even. This ensures that the result of (hash & (table.length - 1))
   *  points to a key and not a value.
   *
   *  We increment by Doug Lea's Magic Number(TM) (*2 since keys are in every
   *  other bucket) to help prevent clustering.
   */
  final private val hash = ThreadLocal.hashCounter.getAndAdd(0x61c88647 << 1)
}
