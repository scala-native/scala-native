package java.lang

import scala.scalanative.unsafe._
import scala.scalanative.runtime._
import scala.scalanative.runtime.Intrinsics._

class _Object {
  @inline def __equals(that: _Object): scala.Boolean =
    this eq that

  @inline def __hashCode(): scala.Int = {
    val addr = castRawPtrToLong(castObjectToRawPtr(this))
    addr.toInt ^ (addr >> 32).toInt
  }

  @inline def __toString(): String =
    getClass.getName + "@" + Integer.toHexString(hashCode)

  @inline def __getClass(): _Class[_] = {
    val rtti   = getRawType(this)
    val clsPtr = elemRawPtr(rtti, 16)

    if (loadRawPtr(clsPtr) == null) {
      val newClass = new _Class[Any](rtti)
      storeObject(clsPtr, newClass)
      ClassInstancesRegistry.add(newClass)
    } else {
      loadObject(clsPtr).asInstanceOf[_Class[_]]
    }
  }

  @inline def __notify(): Unit =
    getMonitor(this)._notify()

  @inline def __notifyAll(): Unit =
    getMonitor(this)._notifyAll()

  @inline def __wait(): Unit =
    getMonitor(this)._wait()

  @inline def __wait(timeout: scala.Long): Unit =
    getMonitor(this)._wait(timeout)

  @inline def __wait(timeout: scala.Long, nanos: Int): Unit =
    getMonitor(this)._wait(timeout, nanos)

  @inline def __scala_==(that: _Object): scala.Boolean = {
    // This implementation is only called for classes that don't override
    // equals. Otherwise, whenever equals is overriden, we also update the
    // vtable entry for scala_== to point to the override directly.
    this eq that
  }

  @inline def __scala_## : scala.Int = {
    // This implementation is only called for classes that don't override
    // hashCode. Otherwise, whenever hashCode is overriden, we also update the
    // vtable entry for scala_## to point to the override directly.
    val addr = castRawPtrToLong(castObjectToRawPtr(this))
    addr.toInt ^ (addr >> 32).toInt
  }

  protected def __clone(): _Object = {
    val rawty = getRawType(this)
    val size  = loadInt(elemRawPtr(rawty, sizeof[Type]))
    val clone = GC.alloc(rawty, size)
    val src   = castObjectToRawPtr(this)
    libc.memcpy(clone, src, size)
    castRawPtrToObject(clone).asInstanceOf[_Object]
  }

  protected def __finalize(): Unit = ()
}

/** Registry for created instances of java.lang.Class
 * It's only purpose is to prevent GC from collecting instances of java.lang.Class
 **/
object ClassInstancesRegistry {
  private var instances     = new scala.Array[_Class[_]](512)
  private var lastId        = -1
  @inline def nextId(): Int = { lastId += 1; lastId }

  def add(cls: _Class[_]): _Class[_] = {
    val id = nextId()
    if (instances.length <= id) {
      val newSize: Int = (instances.length * 1.1).toInt
      val newArr       = new scala.Array[_Class[_]](newSize)
      Array.copy(instances, 0, newArr, 0, instances.length)
      instances = newArr
    }
    instances(id) = cls
    cls
  }
}
