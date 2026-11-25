package scala.scalanative.runtime

import java.util.concurrent.ConcurrentHashMap

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.runtime.Intrinsics._

/** Classes of arrays.
 *
 *  Arrays of primitives have their own classes, `IntArray` and the like, and
 *  `ObjectArray` is the class of `Array[Object]`. The linker gives every other
 *  array type used by the program a class of its own, a subclass of
 *  `ObjectArray` named like the JVM names the array class
 *  (`[Ljava.lang.String;`, `[[I`), with its component type and number of
 *  dimensions.
 *
 *  [[of]] finds the class of arrays of a given component, as needed by
 *  `java.lang.reflect.Array.newInstance` and `Class.arrayType`. Array types the
 *  linker could not see, for components known only at runtime, get a class
 *  created on first use: a copy of `ObjectArray`'s RTTI with its own name,
 *  component and dimensions, which keeps `ObjectArray`'s superclasses and
 *  methods. Either way each array type has exactly one class.
 */
object ArrayClasses {

  /** The class of arrays whose components are instances of `component`. */
  def of(component: Class[_]): Class[_] = {
    val cls = component.asInstanceOf[_Class[_]]
    if (cls.isPrimitive()) primitiveArrayClass(cls)
    else if (is(cls, classOf[Object])) classOf[ObjectArray]
    else {
      val known = classes.get(component)
      if (known != null) known
      else classes.computeIfAbsent(component, _ => create(cls))
    }
  }

  /** Number of dimensions of an array class, 0 for other classes. */
  @alwaysinline private[runtime] def dimensions(cls: _Class[_]): Int =
    if (cls.isArray()) (cls.flags >>> DimensionsShift) & 0xff
    else 0

  // Keep in sync with codegen.RuntimeTypeInformation.Flags
  private final val DimensionsShift = 8
  private final val DimensionsMask = 0xff << DimensionsShift
  private final val MaxDimensions = 255

  /* Classes of arrays of references by their component, other than Object[].
   * Starts with the classes defined by the linker, from the class list.
   */
  private lazy val classes: ConcurrentHashMap[Class[_], Class[_]] = {
    val map = new ConcurrentHashMap[Class[_], Class[_]]
    LinkedClasses.foreach { cls =>
      if (_Class.isReferenceArray(cls) && !is(cls, classOf[ObjectArray]))
        map.put(cls.getComponentType(), cls)
    }
    map
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

  /* A copy of the class of Array[Object]: same virtual methods, traits,
   * superclasses and class key, its own name, component type and number of
   * dimensions.
   */
  private def create(component: _Class[_]): Class[_] = {
    val dims = dimensions(component) + 1
    if (dims > MaxDimensions)
      throw new IllegalArgumentException(
        "Cannot create an array type with more than 255 dimensions"
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
    cls.flags = (template.flags & ~DimensionsMask) | (dims << DimensionsShift)
    cls.refFieldOffsets = castObjectToRawPtr(component)
    cls.name =
      if (component.isArray()) "[" + component.getName()
      else "[L" + component.getName() + ";"
    cls.asInstanceOf[Class[_]]
  }
}
