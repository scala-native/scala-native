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
  var superClass: Class[_ >: A] = _
  // First entry of the inline lineage: superclasses from the root, then the
  // class itself, see codegen.RuntimeTypeInformation
  var lineage: RawPtr = _

  def cast(obj: Object): A =
    obj.asInstanceOf[A]

  def getComponentType(): _Class[_] =
    if (isArray())
      // Array RTTI stores the element type in place of the reference map
      Intrinsics.castRawPtrToObject(refFieldOffsets).asInstanceOf[_Class[_]]
    else null // JVM compliance

  def getName(): String = name

  def getSimpleName(): String = {
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

  def isAssignableFrom(that: Class[_]): scala.Boolean =
    is(that.asInstanceOf[_Class[_]], this)

  def isInstance(obj: Object): scala.Boolean =
    is(obj.getClass.asInstanceOf[_Class[_]], this)

  @alwaysinline private def is(cls: Class[_]): Boolean =
    this eq cls.asInstanceOf[_Class[A]]

  private def is(left: _Class[_], right: _Class[_]): Boolean = {
    // This replicates the logic of the compiler-generated instance check
    // that you would normally get if you do (obj: L).isInstanceOf[R],
    // where rtti for L and R are `left` and `right`.
    if (left eq right) return true

    if (left.isInterface()) {
      // unlikely, only possible when operating on Class[_] instances
      if (right.isInterface()) _Class.checkHasTrait(left, right)
      else false
    } else if (right.isInterface()) _Class.checkImplements(left, right)
    else if (right.superclassCount < _Class.LineageSize) {
      // `right` is in the lineage of `left` at index superclassCount(right)
      val lineage = Intrinsics.classFieldRawPtr(left, "lineage")
      val ancestor = Intrinsics.loadObject(
        Intrinsics.elemRawPtr(lineage, _Class.PtrSize * right.superclassCount)
      )
      ancestor eq right
    } else {
      var ancestor: _Class[_] = left
      var count = left.superclassCount
      val rightCount = right.superclassCount
      while (count > rightCount) {
        ancestor = ancestor.superClass.asInstanceOf[_Class[_]]
        count -= 1
      }
      count == rightCount && (ancestor eq right)
    }
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
    val array =
      if (interfacesCount == 0) scala.Array.emptyObjectArray
      else ObjectArray.snapshot(interfacesCount, interfaces)
    array.asInstanceOf[scala.Array[Class[_]]]
  }
  def getSuperclass(): Class[_ >: A] =
    if (isInterface()) null
    else superClass

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
    final val IsAtHomeSlot = 0x80 // 1 << 7
    final val TraitKeyShift = 8
  }

  // Keep in sync with codegen.CommonMemoryLayouts.LineageSize
  private final val LineageSize = 8
  @alwaysinline private def PtrSize =
    Intrinsics.castRawSizeToInt(Intrinsics.sizeOf[RawPtr])

  /* Looks `trt` up in the itable of class `left` the way generated code
   * does, see codegen.ITable: linear probing from the slot selected by the
   * trait key, until the trait or an empty slot.
   */
  private def checkImplements(left: _Class[_], trt: _Class[_]): Boolean = {
    val mask = left.itablesMask
    val entrySize = 2 * PtrSize
    var pos = ((trt.flags >>> Flags.TraitKeyShift) >>> left.itablesShift) & mask
    if ((trt.flags & Flags.IsAtHomeSlot) != 0) {
      // Implementors hold the trait in this slot, see Metadata.alwaysAtHomeSlot
      val entry = Intrinsics.loadObject(
        Intrinsics.elemRawPtr(left.itables, entrySize * pos)
      )
      return entry eq trt
    }
    var probes = 0
    while (probes <= mask) {
      val entry = Intrinsics.loadObject(
        Intrinsics.elemRawPtr(left.itables, entrySize * pos)
      )
      if (entry eq trt) return true
      if (entry eq null) return false
      pos = (pos + 1) & mask
      probes += 1
    }
    false
  }

  // `interfaces` lists every trait implemented by `left`
  private def checkHasTrait(left: _Class[_], right: _Class[_]): Boolean = {
    val interfaces = left.interfaces
    val ptrSize = Intrinsics.castRawSizeToInt(Intrinsics.sizeOf[Ptr[_]])
    var idx = 0
    while (idx < left.interfacesCount) {
      val interfacePtr = Intrinsics.elemRawPtr(interfaces, ptrSize * idx)
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

private object LinkedClasses {
  import Intrinsics._

  // ConcurrentHashMap does not allow null values
  private val NotFound = new Object
  private val cache = new ConcurrentHashMap[String, AnyRef]

  def forName(className: String): Class[_] =
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

  // The class list section holds a pointer to the RTTI of every class and
  // trait, the RTTI of a type is its java.lang.Class instance
  private def find(className: String): Class[_] = {
    val ptrSize = castRawSizeToInt(sizeOf[RawPtr])
    val section = StaticRoots.classListSection()
    val start = loadRawPtr(section)
    val end = loadRawPtr(elemRawPtr(section, ptrSize))
    val size = castRawPtrToLong(end) - castRawPtrToLong(start)
    var offset = 0L
    while (offset < size) {
      val rtti = loadRawPtr(elemRawPtr(start, castLongToRawSize(offset)))
      if (castRawPtrToLong(rtti) != 0L) {
        val cls = castRawPtrToObject(rtti).asInstanceOf[_Class[_]]
        if (cls.name == className) return cls
      }
      offset += ptrSize
    }
    null
  }
}
