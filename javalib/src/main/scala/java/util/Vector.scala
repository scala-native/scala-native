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

package java.util

import java.io.*
import java.util.*
import java.lang as jl

@SerialVersionUID(-2767605614048989439L)
object Vector {
  private val DEFAULT_SIZE = 10
}

@SerialVersionUID(-2767605614048989439L)
class Vector[E <: AnyRef](
    initialCapacity: Int,
    protected var capacityIncrement: Int
) extends AbstractList[E]
    with List[E]
    with RandomAccess
    with Cloneable
    with Serializable {
  if (initialCapacity < 0) throw new IllegalArgumentException

  protected var elementCount = 0
  protected var elementData: Array[E] = newElementArray(initialCapacity)

  def this() = this(Vector.DEFAULT_SIZE, 0)

  def this(capacity: Int) = this(capacity, 0)

  def this(collection: Collection[? <: E]) = {
    this(collection.size(), 0)
    val it = collection.iterator()
    while (it.hasNext()) {
      elementData(elementCount) = it.next()
      elementCount += 1
    }
  }

  private def newElementArray(size: Int): Array[E] =
    new Array[AnyRef](size).asInstanceOf[Array[E]]

  override def add(location: Int, obj: E): Unit =
    insertElementAt(obj, location)

  override def add(obj: E): Boolean = synchronized {
    if (elementCount == elementData.length) growByOne()
    elementData(elementCount) = obj
    elementCount += 1
    true
  }

  override def addAll(
      _location: Int,
      collection: Collection[? <: E]
  ): Boolean = synchronized {
    var location = _location
    if (0 <= location && location <= elementCount) {
      val size = collection.size()
      if (size == 0) return false
      val required = size - (elementData.length - elementCount)
      if (required > 0) growBy(required)
      val count = elementCount - location
      if (count > 0)
        System.arraycopy(
          elementData,
          location,
          elementData,
          location + size,
          count
        )
      val it = collection.iterator()
      while (it.hasNext()) {
        elementData(location) = it.next()
        location += 1
      }
      elementCount += size
      return true
    }
    throw new ArrayIndexOutOfBoundsException(location)
  }

  override def addAll(collection: Collection[? <: E]): Boolean = synchronized {
    addAll(elementCount, collection)
  }

  def addElement(obj: E): Unit = synchronized {
    if (elementCount == elementData.length) growByOne()
    elementData(elementCount) = obj
    elementCount += 1
  }

  def capacity: Int = synchronized { elementData.length }

  override def clear(): Unit = removeAllElements()

  override def clone: AnyRef = try
    synchronized {
      val vector = super.clone.asInstanceOf[Vector[E]]
      vector.elementData = elementData.clone
      vector
    }
  catch {
    case e: CloneNotSupportedException => null
  }

  override def contains(obj: Any): Boolean =
    indexOf(obj.asInstanceOf[AnyRef], 0) != -1

  override def containsAll(collection: Collection[?]): Boolean = synchronized {
    super.containsAll(collection)
  }

  def copyInto(elements: Array[AnyRef]): Unit = synchronized {
    System.arraycopy(elementData, 0, elements, 0, elementCount)
  }

  def elementAt(location: Int): E = synchronized {
    if (location < elementCount) elementData(location).asInstanceOf[E]
    else throw new ArrayIndexOutOfBoundsException(location)
  }

  def elements: Enumeration[E] = new Enumeration[E]() {
    private[util] var pos = 0

    override def hasMoreElements(): Boolean = pos < elementCount

    override def nextElement(): E = Vector.this.synchronized {
      if (pos < elementCount) {
        val elem = elementData(pos)
        pos += 1
        elem.asInstanceOf[E]
      } else throw new NoSuchElementException
    }
  }

  def ensureCapacity(minimumCapacity: Int): Unit = synchronized {
    if (elementData.length < minimumCapacity) {
      val next = (if (capacityIncrement <= 0) elementData.length
                  else capacityIncrement) + elementData.length
      grow(
        if (minimumCapacity > next) minimumCapacity
        else next
      )
    }
  }

  override def equals(obj: Any): Boolean = obj match {
    case obj: List[?] =>
      if (this eq obj) return true
      synchronized {
        val list = obj.asInstanceOf[List[?]]
        if (list.size() != elementCount) return false
        var index = 0
        val it = list.iterator()
        while (it.hasNext()) {
          val e1 = elementData({
            index += 1; index - 1
          })
          val e2 = it.next()
          if (!(if (e1 == null) e2 == null
                else e1 == (e2))) return false
        }
      }
      true
    case _ => false
  }

  def firstElement: E = synchronized {
    if (elementCount > 0) return elementData(0).asInstanceOf[E]
    throw new NoSuchElementException
  }

  override def get(location: Int): E = elementAt(location)

  private def grow(newCapacity: Int): Unit = {
    val newData = newElementArray(newCapacity)
    // Assumes elementCount is <= newCapacity
    assert(elementCount <= newCapacity)
    System.arraycopy(elementData, 0, newData, 0, elementCount)
    elementData = newData
  }

  private def growByOne(): Unit = {
    var adding = 0
    if (capacityIncrement <= 0) {
      adding = elementData.length
      if (adding == 0) adding = 1
    } else adding = capacityIncrement
    assert(adding > 0)
    val newData = newElementArray(elementData.length + adding)
    System.arraycopy(elementData, 0, newData, 0, elementCount)
    elementData = newData
  }

  private def growBy(required: Int): Unit = {
    var adding = 0
    if (capacityIncrement <= 0) {
      adding = elementData.length
      if (adding == 0) adding = required
      while (adding < required) adding += adding
    } else {
      adding = (required / capacityIncrement) * capacityIncrement
      if (adding < required) adding += capacityIncrement
    }
    val newData = newElementArray(elementData.length + adding)
    System.arraycopy(elementData, 0, newData, 0, elementCount)
    elementData = newData
  }

  override def hashCode: Int = synchronized {
    var result = 1
    for (i <- 0 until elementCount) {
      result = (31 * result) + (if (elementData(i) == null) 0
                                else elementData(i).hashCode)
    }
    result
  }

  override def indexOf(obj: Any): Int = indexOf(obj, 0)

  def indexOf(obj: Any, location: Int): Int = synchronized {
    var i = location
    while (i < elementCount) {
      if (obj == elementData(i)) return i
      i += 1
    }
    -1
  }

  def insertElementAt(obj: E, location: Int): Unit = synchronized {
    if (0 <= location && location <= elementCount) {
      if (elementCount == elementData.length) growByOne()
      val count = elementCount - location
      if (count > 0)
        System.arraycopy(
          elementData,
          location,
          elementData,
          location + 1,
          count
        )
      elementData(location) = obj
      elementCount += 1
    } else throw new ArrayIndexOutOfBoundsException(location)
  }

  override def isEmpty(): Boolean = synchronized { elementCount == 0 }

  def lastElement: E =
    try synchronized { elementData(elementCount - 1).asInstanceOf[E] }
    catch {
      case e: IndexOutOfBoundsException => throw new NoSuchElementException
    }

  override def lastIndexOf(obj: Any): Int = synchronized {
    lastIndexOf(obj, elementCount - 1)
  }

  def lastIndexOf(obj: Any, location: Int): Int = synchronized {
    if (location < elementCount) {
      var i = location
      while (i >= 0) {
        if (obj == elementData(i)) return i
        i -= 1
      }
      -1
    } else throw new ArrayIndexOutOfBoundsException(location)
  }

  override def remove(location: Int): E = synchronized {
    if (location < elementCount) {
      val result = elementData(location).asInstanceOf[E]
      elementCount -= 1
      val size = elementCount - location
      if (size > 0)
        System.arraycopy(elementData, location + 1, elementData, location, size)
      elementData(elementCount) = null.asInstanceOf[E]
      return result
    }
    throw new ArrayIndexOutOfBoundsException(location)
  }

  override def remove(obj: Any): Boolean = removeElement(obj)

  override def removeAll(collection: Collection[?]): Boolean = synchronized {
    super.removeAll(collection)
  }

  def removeAllElements(): Unit = synchronized {
    for (i <- 0 until elementCount) {
      elementData(i) = null.asInstanceOf[E]
    }
    elementCount = 0
  }

  def removeElement(obj: Any): Boolean = synchronized {
    val index = indexOf(obj.asInstanceOf[AnyRef], 0)
    if (index == -1) false
    else {
      removeElementAt(index)
      true
    }
  }

  def removeElementAt(location: Int): Unit = synchronized {
    if (0 <= location && location < elementCount) {
      elementCount -= 1
      val size = elementCount - location
      if (size > 0)
        System.arraycopy(elementData, location + 1, elementData, location, size)
      elementData(elementCount) = null.asInstanceOf[E]
    } else throw new ArrayIndexOutOfBoundsException(location)
  }

  override protected def removeRange(start: Int, end: Int): Unit = {
    if (start >= 0 && start <= end && end <= elementCount) {
      if (start == end) ()
      else if (end != elementCount) {
        System.arraycopy(
          elementData,
          end,
          elementData,
          start,
          elementCount - end
        )
        val newCount = elementCount - (end - start)
        Arrays.fill(
          elementData.asInstanceOf[Array[AnyRef]],
          newCount,
          elementCount,
          null
        )
        elementCount = newCount
      } else {
        Arrays.fill(
          elementData.asInstanceOf[Array[AnyRef]],
          start,
          elementCount,
          null
        )
        elementCount = start
      }
    } else throw new IndexOutOfBoundsException
  }

  override def retainAll(collection: Collection[?]): Boolean = synchronized {
    super.retainAll(collection)
  }

  override def set(
      location: Int,
      obj: E
  ): E = synchronized {
    if (location < elementCount) {
      val result = elementData(location).asInstanceOf[E]
      elementData(location) = obj
      return result
    }
    throw new ArrayIndexOutOfBoundsException(location)
  }

  def setElementAt(obj: E, location: Int): Unit = synchronized {
    if (location < elementCount) elementData(location) = obj
    else throw new ArrayIndexOutOfBoundsException(location)
  }

  def setSize(length: Int): Unit = synchronized {
    if (length == elementCount) return ensureCapacity(length)
    if (elementCount > length)
      Arrays.fill(
        elementData.asInstanceOf[Array[AnyRef]],
        length,
        elementCount,
        null
      )
    elementCount = length
  }

  override def size(): Int = synchronized(elementCount)

  // TODO: SynchronizedList, SynchronizedRandomAccessList
  // override def subList(start: Int, end: Int): List[E] = synchronized {
  //   new Collections.SynchronizedRandomAccessList[E](
  //     super.subList(start, end),
  //     this
  //   )
  //  }

  override def toArray(): Array[AnyRef] = synchronized {
    val result = new Array[AnyRef](elementCount)
    System.arraycopy(elementData, 0, result, 0, elementCount)
    result
  }

  override def toArray[T <: AnyRef](_contents: Array[T]): Array[T] =
    synchronized {
      val contents =
        if (elementCount > _contents.length)
          java.lang.reflect.Array
            .newInstance(_contents.getClass().getComponentType(), elementCount)
            .asInstanceOf[Array[T]]
        else _contents

      System.arraycopy(elementData, 0, contents, 0, elementCount)
      if (elementCount < contents.length)
        contents(elementCount) = null.asInstanceOf[T]
      contents
    }

  override def toString: String = synchronized {
    if (elementCount == 0) return "[]"
    val length = elementCount - 1
    val buffer = new jl.StringBuilder(elementCount * 16)
    buffer.append('[')
    for (i <- 0 until length) {
      if (elementData(i) eq this)
        buffer.append("(this Collection)")
      else buffer.append(elementData(i))
      buffer.append(", ")

    }
    if (elementData(length) eq this)
      buffer.append("(this Collection)")
    else buffer.append(elementData(length))
    buffer.append(']')
    buffer.toString
  }

  def trimToSize(): Unit = synchronized {
    if (elementData.length != elementCount) grow(elementCount)
  }

  // @throws[IOException]
  // private def writeObject(stream: ObjectOutputStream): Unit = {
  //   stream.defaultWriteObject()
  // }
}
