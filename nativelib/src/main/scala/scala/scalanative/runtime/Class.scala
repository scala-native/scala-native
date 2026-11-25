package scala.scalanative.runtime

import java.io.InputStream
import java.lang.ClassLoader
import java.lang.reflect.{Field, Method}
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

import scala.language.implicitConversions

import scala.scalanative.annotation._
import scala.scalanative.runtime.resource.{
  EmbeddedResourceHelper, EmbeddedResourceInputStream
}
import scala.scalanative.runtime.{Array => RuntimeArray, _}
import scala.scalanative.unsafe._

// Emitted as java.lang.Class
private[runtime] final class _Class[A] {
  // Note: All fields are initialized at compile time. There are no _Class constructor calls

  // var rtti: _Class[_Class[?]] = _ // implicitly
  // var lockWord: Object | Long = _ // implicitly, optional
  // Layout defined by codegen.CommonMemoryLayouts.{Rtti, ClassRtti}
  var flags: Int = _ // see _Class.Flags
  var interfacesCount: Int = _
  var interfaces: RawPtr = _ // Ptr[_Class[_]], all implemented traits
  var name: String = _

  // Warning! Fields below are populated if !isInterface()
  var size: Int = _
  var superclassCount: Int = _
  // Classes: Ptr[Int], the reference map used by the GC
  // Arrays: the _Class of the element type, see getComponentType
  var refFieldOffsets: RawPtr = _
  var itablesMask: Int = _
  var itablesShift: Int = _
  var itables: RawPtr = _ // Ptr[{trait: _Class[_], vtable: Ptr[_]}]
  // First entry of the lineage: superclasses from the root, then the class
  // itself, in chunks of LineageSize entries followed by a pointer to the
  // next chunk, see codegen.RuntimeTypeInformation. Has to stay the last
  // field: the first chunk is stored here.
  var lineage: RawPtr = _

  def cast(obj: Object): A =
    obj.asInstanceOf[A]

  def asSubclass[U](clazz: Class[U]): Class[_ <: U] =
    if (clazz.isAssignableFrom(this)) this.asInstanceOf[Class[_ <: U]]
    else throw new ClassCastException(toString())

  def getComponentType(): _Class[_] =
    if (isArray())
      // Array RTTI stores the element type in place of the reference map
      Intrinsics.castRawPtrToObject(refFieldOffsets).asInstanceOf[_Class[_]]
    else null // JVM compliance

  def getName(): String = name

  def getTypeName(): String =
    if (isArray()) getComponentType().getTypeName() + "[]"
    else getName()

  /** @since JDK 12 */
  def componentType(): Class[_] = getComponentType()

  /** @since JDK 12 */
  def arrayType(): Class[_] = ArrayClasses.of(this)

  /** @since JDK 12 */
  def descriptorString(): String =
    if (isPrimitive()) name match {
      case "boolean" => "Z"
      case "byte"    => "B"
      case "char"    => "C"
      case "short"   => "S"
      case "int"     => "I"
      case "long"    => "J"
      case "float"   => "F"
      case "double"  => "D"
      case "void"    => "V"
      case other     => other
    }
    else if (isArray()) name.replace('.', '/')
    else "L" + name.replace('.', '/') + ";"

  def getSimpleName(): String =
    if (isArray()) getComponentType().getSimpleName() + "[]"
    else getClassSimpleName()

  private def getClassSimpleName(): String = {
    val lastDot = name.lastIndexOf('.'.toInt)
    val lastDollar = name.lastIndexOf('$'.toInt)
    if (lastDollar < 0) name.substring(lastDot + 1)
    else getSimpleNameSlowPath(lastDot, lastDollar)
  }

  @noinline private def getSimpleNameSlowPath(
      lastDot: Int,
      lastDollar: Int
  ): String = {
    if (lastDollar < lastDot) name.substring(lastDot + 1)
    else {
      var suffixEnd = lastDollar + 1
      while (suffixEnd < name.length() && {
            val c = name.charAt(suffixEnd)
            c >= '0' && c <= '9'
          }) suffixEnd += 1

      if (suffixEnd < name.length()) name.substring(suffixEnd)
      else {
        val previousDollar = name.lastIndexOf('$'.toInt, lastDollar - 1)
        if (lastDollar == name.length() - 1) {
          if (previousDollar <= lastDot) name.substring(lastDot + 1)
          else name.substring(previousDollar + 1)
        } else if (previousDollar <= lastDot ||
            lastDollar - previousDollar == 5 &&
            name.charAt(previousDollar - 1) == '$' &&
            name.regionMatches(previousDollar + 1, "anon", 0, 4)) ""
        else name.substring(previousDollar + 1, lastDollar)
      }
    }
  }

  def isInterface(): scala.Boolean = (flags & _Class.Flags.IsInterface) != 0

  def isPrimitive(): scala.Boolean = (flags & _Class.Flags.IsPrimitive) != 0

  def isArray(): scala.Boolean = (flags & _Class.Flags.IsArray) != 0

  @inline def isAssignableFrom(that: Class[_]): scala.Boolean = {
    val left = that.asInstanceOf[_Class[_]]
    // unlikely, only possible when operating on Class[_] instances
    if (left.isInterface()) _Class.interfaceIs(left, this)
    else if (_Class.isReferenceArray(this)) _Class.arrayIs(left, this)
    else _Class.classIs(Intrinsics.castObjectToRawPtr(left), this)
  }

  // The class of an object is never an interface: no check on its side
  @inline def isInstance(obj: Object): scala.Boolean =
    obj != null && {
      val cls = Intrinsics.loadRawPtr(Intrinsics.castObjectToRawPtr(obj))
      if (_Class.isReferenceArray(this))
        _Class.arrayIs(
          Intrinsics.castRawPtrToObject(cls).asInstanceOf[_Class[_]],
          this
        )
      else _Class.classIs(cls, this)
    }

  @inline override def equals(other: Any): scala.Boolean =
    other match {
      case other: _Class[_] => this eq other
      case _                => false
    }

  @inline override def hashCode: Int =
    Intrinsics.castRawPtrToLong(Intrinsics.castObjectToRawPtr(this)).##

  override def toString = {
    val name = getName()
    val prefix =
      if (isPrimitive()) ""
      else if (isInterface()) "interface "
      else "class "
    prefix + name
  }

  def getInterfaces(): scala.Array[Class[_]] = {
    val arrayClass =
      Intrinsics.castObjectToRawPtr(classOf[scala.Array[Class[_]]])
    ObjectArray
      .snapshot(interfacesCount, arrayClass, interfaces)
      .asInstanceOf[scala.Array[Class[_]]]
  }

  def getSuperclass(): Class[_ >: A] =
    if (isInterface() || superclassCount == 0) null
    else if (isArray()) classOf[Object].asInstanceOf[Class[_ >: A]]
    else
      _Class.ancestorAt(this, superclassCount - 1).asInstanceOf[Class[_ >: A]]

  // def getField(name: String): Field =
  //   ???

  /** We only have one dummy classloader for JVM compatibility as used to get
   *  resources on Scala Native.
   *  @return
   *    the dummy classloader
   */
  def getClassLoader(): java.lang.ClassLoader =
    ClassLoader.getSystemClassLoader()

  // def getConstructors(): Array[Object] = ???
  // def getDeclaredFields(): Array[Field] = ???
  // def getMethod(
  //     name: java.lang.String,
  //     args: Array[Class[_]]
  // ): java.lang.reflect.Method = ???
  // def getMethods(): Array[Method] = ???

  def getResourceAsStream(
      resourceName: java.lang.String
  ): java.io.InputStream = {
    if (resourceName.isEmpty()) null
    else {
      val absoluteName =
        if (resourceName(0) == '/') {
          resourceName
        } else {
          Paths.get(this.name.replace(".", "/")).getParent() match {
            case null       => s"/$resourceName"
            case parentPath => s"/${parentPath.toString()}/$resourceName"
          }
        }

      val path =
        Paths.get(absoluteName).normalize().toString().replace("\\", "/")

      val absolutePath =
        if (!path.isEmpty() && path(0) != '/') "/" + path
        else path

      EmbeddedResourceHelper.resourceFileIdMap
        .get(absolutePath)
        .map { fileIndex =>
          new EmbeddedResourceInputStream(fileIndex)
        }
        .orNull
    }
  }
}

private[runtime] object _Class {
  @alwaysinline private[runtime] implicit def _class2class[A](
      cls: _Class[A]
  ): Class[A] =
    cls.asInstanceOf[Class[A]]
  @alwaysinline private[runtime] implicit def class2_class[A](
      cls: Class[A]
  ): _Class[A] =
    cls.asInstanceOf[_Class[A]]

  /** Bits of _Class.flags, keep in sync with
   *  codegen.RuntimeTypeInformation.Flags
   */
  private[runtime] object Flags {
    // Literal constants, inlined at use sites without loading the module
    final val IsInterface = 0x1 // 1 << 0
    final val IsPrimitive = 0x2 // 1 << 1
    final val IsArray = 0x4 // 1 << 2
    // Arrays only, bits 3-4: how the GC scans the elements
    final val ArrayScanMask = 0x18 // 3 << 3
    final val ArrayScanReferences = 0x8 // 1 << 3
    final val TraitKeyShift = 8
    // Arrays only, bits 8-15: number of dimensions
    final val ArrayDimensionsShift = 8
    final val ArrayDimensionsMask = 0xff00 // 0xff << 8
  }

  // Excludes BlobArray
  @alwaysinline private[runtime] def isReferenceArray(cls: _Class[_]) =
    (cls.flags & (Flags.IsArray | Flags.ArrayScanMask)) ==
      (Flags.IsArray | Flags.ArrayScanReferences)

  // Arrays of references are covariant (JLS 4.10.3)
  @noinline private[runtime] def arrayIs(
      left: _Class[_],
      right: _Class[_]
  ): Boolean =
    (left eq right) || (isReferenceArray(left) &&
    right.getComponentType().isAssignableFrom(left.getComponentType()))

  // Keep in sync with codegen.CommonMemoryLayouts.LineageSize
  private final val LineageSize = 8
  @alwaysinline private[runtime] def PtrSize =
    Intrinsics.castRawSizeToInt(Intrinsics.sizeOf[RawPtr])

  /* Address in class `cls` of the field found at `field` in `like`. Classes
   * read from memory are kept as raw pointers: typing them as _Class would
   * check their type on every load.
   */
  @alwaysinline private def fieldOf(
      cls: RawPtr,
      like: _Class[_],
      field: RawPtr
  ): RawPtr =
    Intrinsics.elemRawPtr(
      cls,
      Intrinsics.castLongToRawSize(
        Intrinsics.castRawPtrToLong(field) -
          Intrinsics.castRawPtrToLong(Intrinsics.castObjectToRawPtr(like))
      )
    )

  @alwaysinline private def same(ptr: RawPtr, cls: _Class[_]): Boolean =
    Intrinsics.castRawPtrToLong(ptr) ==
      Intrinsics.castRawPtrToLong(Intrinsics.castObjectToRawPtr(cls))

  /* Whether instances of class `cls` are instances of `right`, a class or a
   * trait, the way generated instance tests decide it. Inlined: the lineage
   * entry or the itable slot where the lookup starts decide most tests.
   */
  @inline private[runtime] def classIs(
      cls: RawPtr,
      right: _Class[_]
  ): Boolean = {
    import Intrinsics._
    val rightFlags = right.flags
    if ((rightFlags & Flags.IsInterface) != 0) {
      val mask =
        loadInt(fieldOf(cls, right, classFieldRawPtr(right, "itablesMask")))
      val shift =
        loadInt(fieldOf(cls, right, classFieldRawPtr(right, "itablesShift")))
      val itables =
        loadRawPtr(fieldOf(cls, right, classFieldRawPtr(right, "itables")))
      val pos = ((rightFlags >>> Flags.TraitKeyShift) >>> shift) & mask
      val entry = loadRawPtr(elemRawPtr(itables, 2 * PtrSize * pos))
      // Another trait in this slot: the lookup continues out of line
      same(entry, right) ||
        (castRawPtrToLong(entry) != 0L && implementsDisplaced(cls, right, pos))
    } else {
      // `right` is in the lineage of `cls` at index superclassCount(right)
      val rightCount = right.superclassCount
      if (rightCount < LineageSize) {
        val lineage = fieldOf(cls, right, classFieldRawPtr(right, "lineage"))
        same(loadRawPtr(elemRawPtr(lineage, PtrSize * rightCount)), right)
      } else deepClassIs(cls, right, rightCount)
    }
  }

  @noinline private def deepClassIs(
      cls: RawPtr,
      right: _Class[_],
      rightCount: Int
  ): Boolean = {
    val left = Intrinsics.castRawPtrToObject(cls).asInstanceOf[_Class[_]]
    ancestorAt(left, rightCount) eq right
  }

  // An interface is a subtype of itself, of the traits it extends and of
  // java.lang.Object
  @noinline private[runtime] def interfaceIs(
      left: _Class[_],
      right: _Class[_]
  ): Boolean =
    (left eq right) || {
      if (right.isInterface()) checkHasTrait(left, right)
      else (right: AnyRef) eq classOf[Object]
    }

  /* The superclass of class `cls` with `depth` superclasses, `cls` itself
   * for its own count, null for a larger one. Past the chunk stored in the
   * class, the last slot of each chunk points to the next one.
   */
  private[runtime] def ancestorAt(cls: _Class[_], depth: Int): _Class[_] = {
    var chunk = Intrinsics.classFieldRawPtr(cls, "lineage")
    var index = depth
    while (index >= LineageSize) {
      chunk = Intrinsics.loadRawPtr(
        Intrinsics.elemRawPtr(chunk, PtrSize * LineageSize)
      )
      if (Intrinsics.castRawPtrToLong(chunk) == 0L) return null
      index -= LineageSize
    }
    Intrinsics
      .loadObject(Intrinsics.elemRawPtr(chunk, PtrSize * index))
      .asInstanceOf[_Class[_]]
  }

  /* Continues the lookup of `trt` in the itable of class `left` after slot
   * `start`, which holds another trait, see codegen.ITable: linear probing
   * until the trait or an empty slot.
   */
  @noinline private def implementsDisplaced(
      cls: RawPtr,
      trt: _Class[_],
      start: Int
  ): Boolean = {
    val left = Intrinsics.castRawPtrToObject(cls).asInstanceOf[_Class[_]]
    val mask = left.itablesMask
    val entrySize = 2 * PtrSize
    var pos = start
    var probes = 0
    while (probes < mask) {
      pos = (pos + 1) & mask
      val entry = Intrinsics.loadObject(
        Intrinsics.elemRawPtr(left.itables, entrySize * pos)
      )
      if (entry eq trt) return true
      if (entry eq null) return false
      probes += 1
    }
    false
  }

  // `interfaces` lists every trait implemented by `left`
  private def checkHasTrait(left: _Class[_], right: _Class[_]): Boolean = {
    val interfaces = left.interfaces
    var idx = 0
    while (idx < left.interfacesCount) {
      val interfacePtr = Intrinsics.elemRawPtr(interfaces, PtrSize * idx)
      if (Intrinsics.loadObject(interfacePtr) eq right) return true
      idx += 1
    }
    false
  }

  def forName(name: String): Class[_] = LinkedClasses.forName(name)

  def forName(
      name: String,
      init: scala.Boolean,
      loader: ClassLoader
  ): Class[_] = forName(name)

  /** @since JDK 22 */
  def forPrimitiveName(primitiveName: String): Class[_] =
    primitiveName match {
      case "boolean" => java.lang.Boolean.TYPE
      case "byte"    => java.lang.Byte.TYPE
      case "char"    => java.lang.Character.TYPE
      case "double"  => java.lang.Double.TYPE
      case "float"   => java.lang.Float.TYPE
      case "int"     => java.lang.Integer.TYPE
      case "long"    => java.lang.Long.TYPE
      case "short"   => java.lang.Short.TYPE
      case "void"    => java.lang.Void.TYPE
      case null      => throw new NullPointerException()
      case _         => null
    }
}

private[runtime] object LinkedClasses {
  import Intrinsics._

  // ConcurrentHashMap does not allow null values
  private val NotFound = new Object
  private val cache = new ConcurrentHashMap[String, AnyRef]

  def forName(className: String): Class[_] =
    if (className.startsWith("[")) arrayForName(className)
    else
      cache.computeIfAbsent(
        className,
        name => {
          val cls = find(name)
          if (cls != null) cls else NotFound
        }
      ) match {
        case cls: Class[_] => cls
        case _             => throw new ClassNotFoundException(className)
      }

  // `[` per dimension, then a primitive code or `L<class name>;`
  private def arrayForName(className: String): Class[_] = {
    def notFound() = throw new ClassNotFoundException(className)
    var dims = 0
    while (dims < className.length() && className.charAt(dims) == '[')
      dims += 1
    val element = className.substring(dims)
    val component: Class[_] =
      if (element.length() == 1) element.charAt(0) match {
        case 'Z' => classOf[scala.Boolean]
        case 'B' => classOf[scala.Byte]
        case 'C' => classOf[scala.Char]
        case 'S' => classOf[scala.Short]
        case 'I' => classOf[scala.Int]
        case 'J' => classOf[scala.Long]
        case 'F' => classOf[scala.Float]
        case 'D' => classOf[scala.Double]
        case _   => notFound()
      }
      else if (element.length() > 2 && element.charAt(0) == 'L' &&
          element.charAt(element.length() - 1) == ';' &&
          element.charAt(1) != '[')
        forName(element.substring(1, element.length() - 1))
      else notFound()
    if (dims > ArrayClasses.MaxDimensions) notFound()
    var cls = component
    while (dims > 0) {
      cls = ArrayClasses.of(cls)
      dims -= 1
    }
    cls
  }

  private def find(className: String): Class[_] =
    findClass(_.name == className)

  def foreach(f: _Class[_] => Unit): Unit =
    findClass { cls => f(cls); false }

  // The class list section holds a pointer to the RTTI of every class and
  // trait, the RTTI of a type is its java.lang.Class instance
  private def findClass(p: _Class[_] => Boolean): _Class[_] = {
    val ptrSize = _Class.PtrSize
    val section = StaticRoots.classListSection()
    val start = loadRawPtr(section)
    val end = loadRawPtr(elemRawPtr(section, ptrSize))
    val size = castRawPtrToLong(end) - castRawPtrToLong(start)
    var offset = 0L
    while (offset < size) {
      val rtti = loadRawPtr(elemRawPtr(start, castLongToRawSize(offset)))
      if (castRawPtrToLong(rtti) != 0L) {
        val cls = castRawPtrToObject(rtti).asInstanceOf[_Class[_]]
        if (p(cls)) return cls
      }
      offset += ptrSize
    }
    null
  }
}
