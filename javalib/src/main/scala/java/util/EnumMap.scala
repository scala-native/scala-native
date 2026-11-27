/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Ported from Apache Harmony

package java.util

import java.lang.{_Enum => Enum}

class EnumMap[K <: Enum[K], V] private (
    private val keyType: Class[K],
    private val keys: Array[AnyRef], // the key universe, by ordinal
    private val vals: Array[AnyRef], // masked values, null = absent
    private var count: Int
) extends AbstractMap[K, V]
    with java.io.Serializable
    with Cloneable {
  import EnumMap._

  private def this(keyType: Class[K], universe: Array[AnyRef]) =
    this(keyType, universe, new Array[AnyRef](universe.length), 0)

  def this(keyType: Class[K]) = this(keyType, EnumMap.universe(keyType))

  def this(m: EnumMap[K, _ <: V]) =
    this(m.keyType, m.keys, m.vals.clone(), m.count)

  def this(m: Map[K, _ <: V]) = {
    this(
      EnumMap.keyTypeOf[K](m),
      EnumMap.universe(EnumMap.keyTypeOf[K](m))
    )
    putAll(m)
  }

  private def isValidKey(key: Any): Boolean =
    key != null && keyType.isInstance(key)

  private def ordinalOf(key: Any): Int = key.asInstanceOf[Enum[_]].ordinal()

  private def checkKey(key: Any): Unit = {
    if (key == null) throw new NullPointerException()
    if (!keyType.isInstance(key))
      throw new ClassCastException(
        s"${key.getClass().getName()} != ${keyType.getName()}"
      )
  }

  override def size(): Int = count

  override def containsKey(key: Any): Boolean =
    isValidKey(key) && {
      vals(ordinalOf(key)) != null
    }

  override def containsValue(value: Any): Boolean = {
    val masked = mask(value)
    var i = 0
    while (i < vals.length) {
      if (vals(i) != null && vals(i).equals(masked)) return true
      i += 1
    }
    false
  }

  override def get(key: Any): V =
    if (!isValidKey(key)) null.asInstanceOf[V]
    else {
      unmask[V](vals(ordinalOf(key)))
    }

  override def put(key: K, value: V): V = {
    checkKey(key)
    val i = ordinalOf(key)
    val old = vals(i)
    vals(i) = mask(value)
    if (old == null) count += 1
    unmask[V](old)
  }

  override def remove(key: Any): V =
    if (!isValidKey(key)) null.asInstanceOf[V]
    else {
      val i = ordinalOf(key)
      if (vals(i) == null) null.asInstanceOf[V]
      else removeAt(i)
    }

  private def removeAt(i: Int): V = {
    val old = vals(i)
    vals(i) = null
    count -= 1
    unmask[V](old)
  }

  override def putAll(m: Map[_ <: K, _ <: V]): Unit = {
    val it: Iterator[_ <: Map.Entry[_ <: K, _ <: V]] =
      m.entrySet().iterator()
    while (it.hasNext()) {
      val e = it.next()
      put(e.getKey(), e.getValue())
    }
  }

  override def clear(): Unit = {
    Arrays.fill(vals, null)
    count = 0
  }

  override def entrySet(): Set[Map.Entry[K, V]] =
    new AbstractSet[Map.Entry[K, V]] {
      def size(): Int = count
      def iterator(): Iterator[Map.Entry[K, V]] = new EntryIterator
      override def clear(): Unit = EnumMap.this.clear()
      override def contains(o: Any): Boolean = o match {
        case e: Map.Entry[_, _] =>
          containsKey(e.getKey()) &&
            Objects.equals(get(e.getKey()), e.getValue())
        case _ => false
      }
      override def remove(o: Any): Boolean =
        contains(o) && {
          EnumMap.this.remove(o.asInstanceOf[Map.Entry[_, _]].getKey())
          true
        }
    }

  override def equals(o: Any): Boolean = o match {
    case other: EnumMap[_, _] if other.keyType ne keyType =>
      count == 0 && other.count == 0
    case _ => super.equals(o)
  }

  override def clone(): EnumMap[K, V] =
    new EnumMap[K, V](keyType, keys, vals.clone(), count)

  private class EntryIterator extends Iterator[Map.Entry[K, V]] {
    // Rescans the slots, so it sees mappings changed after its creation
    private var nextIndex = 0
    private var last: Entry = null

    def hasNext(): Boolean = {
      while (nextIndex < vals.length && vals(nextIndex) == null)
        nextIndex += 1
      nextIndex < vals.length
    }

    def next(): Map.Entry[K, V] = {
      if (!hasNext()) throw new NoSuchElementException()
      last = new Entry(nextIndex)
      nextIndex += 1
      last
    }

    override def remove(): Unit = {
      if (last == null) throw new IllegalStateException()
      if (vals(last.index) != null) removeAt(last.index)
      last.index = -1
      last = null
    }
  }

  private class Entry(var index: Int) extends Map.Entry[K, V] {
    private def checkValid(): Unit =
      if (index < 0) throw new IllegalStateException("Entry was removed")

    def getKey(): K = {
      checkValid()
      keys(index).asInstanceOf[K]
    }
    def getValue(): V = {
      checkValid()
      unmask[V](vals(index))
    }
    def setValue(v: V): V = {
      checkValid()
      val old = vals(index)
      vals(index) = mask(v)
      if (old == null) count += 1
      unmask[V](old)
    }
    override def equals(o: Any): Boolean =
      if (index < 0) super.equals(o)
      else
        o match {
          case e: Map.Entry[_, _] =>
            Objects.equals(getKey(), e.getKey()) &&
              Objects.equals(getValue(), e.getValue())
          case _ => false
        }
    override def hashCode(): Int =
      if (index < 0) super.hashCode()
      else Objects.hashCode(getKey()) ^ Objects.hashCode(getValue())
    override def toString(): String =
      if (index < 0) super.toString()
      else s"${getKey()}=${getValue()}"
  }
}

private object EnumMap {
  private def universe(keyType: Class[_]): Array[AnyRef] = {
    val constants: Array[_ <: AnyRef] = keyType.getEnumConstants()
    if (constants == null)
      throw new NullPointerException(s"$keyType is not an enum class")
    constants.asInstanceOf[Array[AnyRef]]
  }

  // Stands for a null value, null in the table means no mapping
  private object NullValue {
    override def hashCode(): Int = 0
    override def toString(): String = "java.util.EnumMap.NullValue"
  }

  private def mask(v: Any): AnyRef =
    if (v == null) NullValue else v.asInstanceOf[AnyRef]

  private def unmask[V](v: AnyRef): V =
    (if (v eq NullValue) null else v).asInstanceOf[V]

  private def keyTypeOf[K <: Enum[K]](m: Map[K, _]): Class[K] = m match {
    case em: EnumMap[_, _] => em.keyType.asInstanceOf[Class[K]]
    case _                 =>
      if (m.isEmpty())
        throw new IllegalArgumentException("Specified map is empty")
      m.keySet().iterator().next().getDeclaringClass()
  }
}
