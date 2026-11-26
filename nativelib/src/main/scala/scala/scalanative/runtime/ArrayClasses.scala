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

  @inline def of(component: Class[_]): Class[_] = {
    val cls = component.asInstanceOf[_Class[_]]
    val slot = cacheSlot(cls)
    val cached = cache(slot)
    if (cached != null && (componentOf(cached) eq cls)) cached
    else lookup(cls, slot)
  }

  @noinline private def lookup(cls: _Class[_], slot: Int): Class[_] = {
    val arrayClass: Class[_] =
      if (cls.isPrimitive()) primitiveArrayClass(cls)
      else if (is(cls, classOf[Object])) classOf[ObjectArray]
      else {
        val component = cls.asInstanceOf[Class[_]]
        val known = classes.get(component)
        if (known != null) known
        else classes.computeIfAbsent(component, _ => create(cls))
      }
    // Release: readers load the class through this pointer (a dependent load,
    // ordered on every supported architecture), so they see it initialized
    ffi.stdatomic.atomic_store_intptr(
      cache.asInstanceOf[ObjectArray].atRawUnsafe(slot),
      castObjectToRawPtr(arrayClass),
      ffi.stdatomic.memory_order.memory_order_release
    )
    arrayClass
  }

  // Array RTTI stores the component class in place of the reference map
  @alwaysinline private def componentOf(arrayClass: Class[_]): AnyRef =
    castRawPtrToObject(arrayClass.asInstanceOf[_Class[_]].refFieldOffsets)

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

  // RTTI size of ObjectArray, set with `classes`
  private var objectArrayClassSize: RawSize = _

  // Seeded with the linker's classes, from their sn_arrayclasses entries
  private lazy val classes: ConcurrentHashMap[Class[_], Class[_]] = {
    val map = new ConcurrentHashMap[Class[_], Class[_]]
    val ptrSize = _Class.PtrSize
    val entrySize = 2 * ptrSize // {RTTI, RTTI size}
    val section = StaticRoots.arrayClassesSection()
    val start = loadRawPtr(section)
    val end = loadRawPtr(elemRawPtr(section, castIntToRawSize(ptrSize)))
    val size = castRawPtrToLong(end) - castRawPtrToLong(start)
    var offset = 0L
    while (offset < size) {
      val entry = elemRawPtr(start, castLongToRawSize(offset))
      val rtti = loadRawPtr(entry)
      if (castRawPtrToLong(rtti) != 0L) {
        val cls = castRawPtrToObject(rtti).asInstanceOf[Class[_]]
        if (is(cls.asInstanceOf[_Class[_]], classOf[ObjectArray]))
          objectArrayClassSize = loadRawSize(
            elemRawPtr(entry, castIntToRawSize(ptrSize))
          )
        else map.put(cls.getComponentType(), cls)
      }
      offset += entrySize
    }
    map
  }

  /* Racy direct-mapped cache of all lookups, entries validated by their
   * component and published with a release store.
   */
  private final val CacheSize = 256
  private val cache = new scala.Array[Class[_]](CacheSize)

  @alwaysinline private def cacheSlot(cls: _Class[_]): Int = {
    val address = castRawPtrToLong(castObjectToRawPtr(cls))
    ((address ^ (address >>> 12)) >>> 4).toInt & (CacheSize - 1)
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
    val size = objectArrayClassSize
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
