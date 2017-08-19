package java.lang

import java.lang.ref.{Reference, WeakReference}
import java.util.concurrent.atomic.AtomicInteger

// Ported from Harmony

class ThreadLocal[T] {

  import java.lang.ThreadLocal._

  private final val reference: Reference[ThreadLocal[T]] =
    new WeakReference[ThreadLocal[T]](this)

  private final val hash: Int = hashCounter.getAndAdd(0x61c88647 << 1)

  protected def initialValue(): T = null.asInstanceOf[T]

  @SuppressWarnings(Array("unchecked"))
  def get(): T = {
    // Optimized for the fast path
    val currentThread: Thread = Thread.currentThread()
    var vals: Values          = values(currentThread)
    if (vals != null) {
      val table: Array[Object] = vals.getTable
      val index: Int           = hash & vals.getMask
      if (this.reference == table(index)) {
        table(index + 1).asInstanceOf[T]
      }
    } else {
      vals = initializeValues(currentThread)
    }

    vals.getAfterMiss(this).asInstanceOf[T]
  }

  def set(value: T): Unit = {
    val currentThread: Thread = Thread.currentThread()
    var vals: Values          = values(currentThread)
    if (vals == null) {
      vals = initializeValues(currentThread)
    }

    vals.put(this, value.asInstanceOf[Object])
  }

  def remove(): Unit = {
    val currentThread: Thread = Thread.currentThread()
    val vals: Values          = values(currentThread)
    if (vals != null) {
      vals.remove(this)
    }
  }

  def initializeValues(current: Thread): Values = {
    current.localValues = new ThreadLocal.Values()
    current.localValues
  }

  def values(current: Thread): Values = current.localValues
}

object ThreadLocal {

  private val hashCounter: AtomicInteger = new AtomicInteger(0)

  class Values {

    import Values._

    private var table: Array[Object] = null

    private var mask: Int = 0

    private var size: Int = 0

    private var tombstones: Int = 0

    private var maximumLoad: Int = 0

    private var clean: Int = 0

    initializeTable(INITIAL_SIZE)

    def this(fromParent: Values) {
      this()
      table = fromParent.table.clone()
      mask = fromParent.mask
      size = fromParent.size
      tombstones = fromParent.tombstones
      maximumLoad = fromParent.maximumLoad
      clean = fromParent.clean
      inheritValues(fromParent)
    }

    def getTable: Array[Object] = table

    def getMask: Int = mask

    @SuppressWarnings(Array("unchecked"))
    private def inheritValues(fromParent: Values): Unit = {
      val table: Array[Object]    = this.table
      var i: Int                  = this.table.length
      var continue: scala.Boolean = false
      while (i >= 0) {
        continue = false
        val k: Object = table(i)

        if (k == null || k == TOMBSTONE) {
          continue = true
        }

        if (!continue) {
          val reference: Reference[InheritableThreadLocal[Object]] =
            k.asInstanceOf[Reference[InheritableThreadLocal[Object]]]

          val key: InheritableThreadLocal[Object] = reference.get()
          if (key != null) {
            // Replace value with filtered value
            // We should just let exceptions bubble out and tank
            // the thread creation

            table(i + 1) = key.childValue(fromParent.table(i + 1))
          } else {
            table(i) = TOMBSTONE
            table(i + 1) = null
            fromParent.table(i) = TOMBSTONE
            fromParent.table(i + 1) = null

            tombstones += 1
            fromParent.tombstones += 1

            size -= 1
            fromParent.size -= 1
          }
        }
        i -= 2
      }
    }

    private def initializeTable(capacity: Int): Unit = {
      this.table = new Array[Object](capacity << 1)
      this.mask = table.length - 1
      this.clean = 0
      this.maximumLoad = capacity * 2 / 3
    }

    private def cleanUp(): Unit = {
      if (rehash()) return
      if (size == 0) return

      var index: Int           = clean
      val table: Array[Object] = this.table
      var counter              = table.length

      var continue: scala.Boolean = false

      while (counter > 0) {
        continue = false
        val k: Object = table(index)

        if (k == TOMBSTONE || k == null) continue = true

        if (!continue) {
          val reference: Reference[ThreadLocal[_]] =
            k.asInstanceOf[Reference[ThreadLocal[_]]]
          if (reference.get() == null) {
            table(index) = TOMBSTONE
            table(index + 1) = null
            tombstones += 1
            size -= 1
          }
        }

        counter >>= 1
        index = next(index)
      }

      clean = index
    }

    private def rehash(): scala.Boolean = {
      if (tombstones + size < maximumLoad) return false

      val capacity: Int = table.length >> 1

      var newCapacity: Int = capacity

      if (size > (capacity >> 1)) {
        newCapacity = capacity << 1
      }

      val oldTable: Array[Object] = table
      initializeTable(newCapacity)

      tombstones = 0

      if (size == 0) return true

      var i: Int                  = oldTable.length - 2
      var continue: scala.Boolean = false
      while (i >= 0) {
        continue = false
        val k: Object = oldTable(i)
        if (k == null || k == TOMBSTONE) continue = true

        if (!continue) {
          val reference: Reference[ThreadLocal[_]] =
            k.asInstanceOf[Reference[ThreadLocal[_]]]

          val key: ThreadLocal[_] = reference.get()
          if (key != null) add(key, oldTable(i + 1))
          else size -= 1
        }
        i -= 2
      }

      true
    }

    def add(key: ThreadLocal[_], value: Object): Unit = {
      var index: Int = key.hash & mask
      while (true) {
        val k: Object = table(index)
        if (k == null) {
          table(index) = key.reference
          table(index + 1) = value
          return
        }

        index = next(index)
      }
    }

    def put(key: ThreadLocal[_], value: Object): Unit = {
      cleanUp()

      var firstTombstone: Int = -1

      var index: Int = key.hash & mask
      while (true) {
        val k: Object = table(index)

        if (k == key.reference) {
          table(index + 1) = value
          return
        }

        if (k == null) {
          if (firstTombstone == -1) {
            table(index) = key.reference
            table(index + 1) = value
            size += 1
            return
          }

          table(firstTombstone) = key.reference
          table(firstTombstone + 1) = value
          tombstones -= 1
          size += 1
          return
        }

        if (firstTombstone == -1 && k == TOMBSTONE) firstTombstone = index

        index = next(index)
      }
    }

    def getAfterMiss(key: ThreadLocal[_]): Object = {
      val table: Array[Object] = this.table
      var index: Int           = key.hash & mask

      // If the first slot is empty, the search is over
      if (table(index) == null) {
        val value: Object = key.initialValue().asInstanceOf[Object]

        // If the table is still the same and the slot is still empty...
        if ((this.table == table) && table(index) == null) {
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

      // Keep track of first tombstone. That's where we want to go back
      // and add an entry if necessary
      var firstTombstone: Int = -1

      index = next(index)
      while (true) {
        val reference: Object = table(index)
        if (reference == key.reference) return table(index + 1)

        // If no entry was found...
        if (reference == null) {
          val value: Object = key.initialValue().asInstanceOf[Object]

          // If the table is still the same
          if (this.table == table) {
            // If we passed a tombstone and that slot still
            // contains a tombstone
            if (firstTombstone > -1 && table(firstTombstone) == TOMBSTONE) {
              table(firstTombstone) = key.reference
              table(firstTombstone + 1) = value
              tombstones -= 1
              size += 1

              // No need to clean up here. We aren't filling
              // in a null slot
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

          // The table changed during initialValue().
          put(key, value)
          return value
        }

        if (firstTombstone == -1 && reference == TOMBSTONE)
          // Keep track of this tombstone so we can overwrite it.
          firstTombstone = index

        index = next(index)
      }
      // For the compiler
      null.asInstanceOf[Object]
    }

    def remove(key: ThreadLocal[_]): Unit = {
      cleanUp()

      var index: Int = key.hash & mask
      while (true) {
        val reference: Object = table(index)

        if (reference == key.reference) {
          table(index) = TOMBSTONE
          table(index + 1) = null
          tombstones += 1
          size -= 1
          return
        }

        if (reference == null) return

        index = next(index)
      }
    }

    private def next(index: Int) = (index + 2) & mask

  }

  object Values {

    private final val INITIAL_SIZE: Int = 16

    private final val TOMBSTONE: Object = new Object()

  }

}
