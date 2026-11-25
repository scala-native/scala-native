package scala.scalanative.runtime

import java.util.concurrent.ConcurrentHashMap

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.runtime.Intrinsics._

/** Classes of arrays by component type.
 *
 *  The linker defines a class for each array type the program uses. Other array
 *  types get a copy of `ObjectArray`'s RTTI on first use, keeping its lineage
 *  and methods.
 */
object ArrayClasses {

  def of(component: Class[_]): Class[_] = {
    val cls = component.asInstanceOf[_Class[_]]
    if (cls.isPrimitive()) primitiveArrayClass(cls)
    else if (is(cls, classOf[Object])) classOf[ObjectArray]
    else {
      val slot = cacheSlot(cls)
      val cached = cache(slot)
      if (cached != null && (cached.getComponentType() eq component)) cached
      else {
        val known = classes.get(component)
        val arrayClass =
          if (known != null) known
          else classes.computeIfAbsent(component, _ => create(cls))
        if (isLinked(arrayClass)) cache(slot) = arrayClass
        arrayClass
      }
    }
  }

  /** Called by compiled type tests against arrays with trait components. */
  def isSubclass(cls: Class[_], arrayClass: Class[_]): Boolean =
    _Class.arrayIs(
      cls.asInstanceOf[_Class[_]],
      arrayClass.asInstanceOf[_Class[_]]
    )

  @alwaysinline private[runtime] def dimensions(cls: _Class[_]): Int =
    if (cls.isArray())
      (cls.flags & _Class.Flags.ArrayDimensionsMask) >>>
        _Class.Flags.ArrayDimensionsShift
    else 0

  private[runtime] final val MaxDimensions = 255

  // Seeded with the linker's classes, from the class list
  private lazy val classes: ConcurrentHashMap[Class[_], Class[_]] = {
    val map = new ConcurrentHashMap[Class[_], Class[_]]
    LinkedClasses.foreach { cls =>
      if (_Class.isReferenceArray(cls) && !is(cls, classOf[ObjectArray]))
        map.put(cls.getComponentType(), cls)
    }
    map
  }

  /* Racy direct-mapped cache, entries validated by their component. Holds
   * only immutable linker classes; runtime ones are published by `classes`.
   */
  private final val CacheSize = 256
  private val cache = new scala.Array[Class[_]](CacheSize)

  @alwaysinline private def cacheSlot(cls: _Class[_]): Int = {
    val address = castRawPtrToLong(castObjectToRawPtr(cls))
    ((address ^ (address >>> 12)) >>> 4).toInt & (CacheSize - 1)
  }

  // Runtime-created classes have ObjectArray in their own lineage slot
  @alwaysinline private def isLinked(arrayClass: Class[_]): Boolean = {
    val cls = arrayClass.asInstanceOf[_Class[_]]
    _Class.ancestorAt(cls, cls.superclassCount) eq cls
  }

  @alwaysinline private def is(cls: _Class[_], other: Class[_]): Boolean =
    (cls: AnyRef) eq (other: AnyRef)

  private def primitiveArrayClass(component: _Class[_]): Class[_] =
    if (is(component, classOf[scala.Int])) classOf[IntArray]
    else if (is(component, classOf[scala.Long])) classOf[LongArray]
    else if (is(component, classOf[scala.Double])) classOf[DoubleArray]
    else if (is(component, classOf[scala.Float])) classOf[FloatArray]
    else if (is(component, classOf[scala.Byte])) classOf[ByteArray]
    else if (is(component, classOf[scala.Char])) classOf[CharArray]
    else if (is(component, classOf[scala.Short])) classOf[ShortArray]
    else if (is(component, classOf[scala.Boolean])) classOf[BooleanArray]
    else
      throw new IllegalArgumentException(
        "Cannot create an array of " + component.getName()
      )

  private def create(component: _Class[_]): Class[_] = {
    val dims = dimensions(component) + 1
    if (dims > MaxDimensions)
      throw new IllegalArgumentException(
        "Cannot create an array type with more than " + MaxDimensions +
          " dimensions"
      )
    val template = classOf[ObjectArray].asInstanceOf[_Class[_]]
    val size = StaticRoots.objectArrayClassSize()
    val rawptr = StaticRoots.allocRuntimeClass(size)
    if (castRawPtrToLong(rawptr) == 0L)
      throw new OutOfMemoryError("Cannot allocate the class of an array")
    ffi.memcpy(rawptr, castObjectToRawPtr(template), size)
    val cls = castRawPtrToObject(rawptr).asInstanceOf[_Class[_]]
    if (isMultithreadingEnabled)
      storeRawPtr(
        elemRawPtr(rawptr, MemoryLayout.Rtti.LockWordOffset),
        castLongToRawPtr(0L)
      )
    cls.flags = (template.flags & ~_Class.Flags.ArrayDimensionsMask) |
      (dims << _Class.Flags.ArrayDimensionsShift)
    cls.refFieldOffsets = castObjectToRawPtr(component)
    cls.name =
      if (component.isArray()) "[" + component.getName()
      else "[L" + component.getName() + ";"
    cls.asInstanceOf[Class[_]]
  }
}
