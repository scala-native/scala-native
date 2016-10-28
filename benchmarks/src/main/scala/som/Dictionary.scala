package som

import som.Dictionary._

class Dictionary[K <: CustomHash, V <: AnyRef](size: Int = INITIAL_CAPACITY) {
  private var buckets = new Array[Entry[K, V]](size)
  private var _size   = 0

  class Entry[K, V](val hash: Int,
                    val key: K,
                    var value: V,
                    var next: Entry[K, V]) {
    def match_(hash: Int, key: K): Boolean =
      this.hash == hash && key.equals(this.key)
  }

  def size() = _size

  def isEmpty() = _size == 0

  private def getBucketIdx(hash: Int): Int =
    (buckets.length - 1) & hash

  private def getBucket(hash: Int): Entry[K, V] =
    buckets(getBucketIdx(hash))

  def at(key: K): V = {
    val _hash = hash(key)
    var e     = getBucket(_hash)

    while (e != null) {
      if (e.match_(_hash, key)) {
        return e.value
      }
      e = e.next
    }

    null.asInstanceOf[V]
  }

  def containsKey(key: K): Boolean = {
    val _hash = hash(key)
    var e     = getBucket(_hash)

    while (e != null) {
      if (e.match_(_hash, key)) {
        return true
      }
      e = e.next
    }
    return false
  }

  def atPut(key: K, value: V): Unit = {
    var _hash   = hash(key)
    var i       = getBucketIdx(_hash)
    var current = buckets(i)

    if (current == null) {
      buckets(i) = newEntry(key, value, _hash)
      _size += 1
    } else {
      insertBucketEntry(key, value, _hash, current)
    }

    if (_size > buckets.length) {
      resize()
    }
  }

  def newEntry(key: K, value: V, hash: Int): Entry[K, V] =
    new Entry(hash, key, value, null)

  def insertBucketEntry(key: K, value: V, hash: Int, head: Entry[K, V]): Unit = {
    var current = head

    while (true) {
      if (current.match_(hash, key)) {
        current.value = value
        return
      }
      if (current.next == null) {
        _size += 1
        current.next = newEntry(key, value, hash)
        return
      }
      current = current.next
    }
  }

  def resize(): Unit = {
    val oldStorage = buckets
    val newStorage = new Array[Entry[K, V]](oldStorage.length * 2)
    buckets = newStorage
    transferEntries(oldStorage)
  }

  def transferEntries(oldStorage: Array[Entry[K, V]]): Unit = {
    (0 until oldStorage.length).foreach { i =>
      val current = oldStorage(i)
      if (current != null) {
        oldStorage(i) = null

        if (current.next == null) {
          buckets(current.hash & (buckets.length - 1)) = current
        } else {
          splitBucket(oldStorage, i, current)
        }
      }
    }
  }

  def splitBucket(oldStorage: Array[Entry[K, V]],
                  i: Int,
                  head: Entry[K, V]): Unit = {
    var loHead: Entry[K, V] = null
    var loTail: Entry[K, V] = null
    var hiHead: Entry[K, V] = null
    var hiTail: Entry[K, V] = null
    var current             = head

    while (current != null) {
      if ((current.hash & oldStorage.length) == 0) {
        if (loTail == null) {
          loHead = current
        } else {
          loTail.next = current
        }
        loTail = current
      } else {
        if (hiTail == null) {
          hiHead = current
        } else {
          hiTail.next = current
        }
        hiTail = current
      }
      current = current.next
    }

    if (loTail != null) {
      loTail.next = null
      buckets(i) = loHead
    }
    if (hiTail != null) {
      hiTail.next = null
      buckets(i + oldStorage.length) = hiHead
    }
  }

  def removeAll(): Unit = {
    buckets = new Array[Entry[K, V]](buckets.length)
    _size = 0
  }

  def getKeys(): Vector[K] = {
    val keys = new Vector[K](_size)
    (0 until buckets.length).foreach { i =>
      var current = buckets(i)
      while (current != null) {
        keys.append(current.key)
        current = current.next
      }
    }
    keys
  }

  def getValues(): Vector[V] = {
    val values = new Vector[V](_size)
    (0 until buckets.length).foreach { i =>
      var current = buckets(i)
      while (current != null) {
        values.append(current.value)
        current = current.next
      }
    }
    values
  }
}

object Dictionary {
  final val INITIAL_CAPACITY = 16

  def hash[K <: CustomHash](key: K): Int = {
    if (key == null) {
      return 0
    }
    val _hash = key.customHash()
    return _hash ^ _hash >>> 16
  }
}
