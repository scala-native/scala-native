package scala.scalanative.runtime

import java.lang.reflect.{Field, Method}
import scala.language.implicitConversions

import scala.scalanative.annotation._
import scala.scalanative.unsafe._
import scala.scalanative.runtime.{Array => RuntimeArray, _}
import scala.scalanative.runtime.resource.EmbeddedResourceInputStream
import scala.scalanative.runtime.resource.EmbeddedResourceHelper
import java.io.InputStream
import java.nio.file.Paths

// Emitted as java.lang.Class
private[runtime] final class _Class[A] {
  // Note: All fields are initialized at compile time. There are no _Class constructor calls

  // var rtti: _Class[_Class[?]] = _ // implicitly
  // var lockWord: Object | Long = _ // implicitly, optional
  var id: Int = _
  var interfacesCount: Int = _ // can be used in the future
  var interfaces: RawPtr = _
  var name: String = _

  // Warning! Fields below are populated if !isInterface()
  var size: Int = _
  var idRangeUntil: Int = _
  var refFieldOffsets: RawPtr = _ // Ptr[Int]
  var itablesCount: Int = _ // actually size - 1 - stores ready to use mask
  var itables: RawPtr = _ // Ptr[CArray[ITableEntry, up to 32]]
  var superClass: Class[_ >: A] = _

  type ITableEntry = CStruct2[Int, Ptr[_]] // {id: Int, vtable: void*}

  def cast(obj: Object): A =
    obj.asInstanceOf[A]

  def getComponentType(): _Class[_] = if (isArray()) {
    if (is(classOf[ObjectArray])) classOf[java.lang.Object] // hot path
    else if (is(classOf[ByteArray])) classOf[scala.Byte]
    else if (is(classOf[CharArray])) classOf[scala.Char]
    else if (is(classOf[IntArray])) classOf[scala.Int]
    else if (is(classOf[LongArray])) classOf[scala.Long]
    else if (is(classOf[FloatArray])) classOf[scala.Float]
    else if (is(classOf[DoubleArray])) classOf[scala.Double]
    else if (is(classOf[BooleanArray])) classOf[scala.Boolean]
    else if (is(classOf[ShortArray])) classOf[scala.Short]
    else if (is(classOf[BlobArray])) classOf[scala.Byte]
    else null // JVM compliance
  } else null // JVM compliance

  def getName(): String = name

  def getSimpleName(): String = {
    val lastDot = name.lastIndexOf('.'.toInt)
    name.substring(lastDot + 1).split('$').last
  }

  // Based on fixed ordering in scala.scalanative.codegen.Metadata.initClassIdsAndRanges
  def isInterface(): scala.Boolean = id < 0
  def isPrimitive(): scala.Boolean = id >= 0 && id <= 8
  // id == 9 is java.lang.Object
  // id == 10 runtime.Array
  // ids 10-20 runtime.Array implementations
  def isArray(): scala.Boolean = id >= 10 && id <= 20

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
    } else if (right.isInterface()) {
      // likely - in most cases we check if class is class or class is trait
      val size = left.itablesCount
      if (size >= 0) {
        // fast-path
        val slot = right.id & size
        val itablePtr = Intrinsics.elemRawPtr(
          left.itables,
          Intrinsics.castRawSizeToInt(Intrinsics.sizeOf[ITableEntry]) * slot
        )
        val itableId = Intrinsics.loadInt(itablePtr)
        itableId == right.id
      } else _Class.checkHasTrait(left, right)
    } else {
      val rightFrom = right.id
      val rightTo = right.idRangeUntil
      val leftId = left.id
      leftId >= rightFrom && leftId <= rightTo
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
    val prefix = if (isInterface()) "interface " else "class "
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

  // def getClassLoader(): java.lang.ClassLoader = ???
  // def getConstructor(args: Array[_Class[_]]): java.lang.reflect.Constructor[_] =
  //   ???
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

  private def checkHasTrait(left: _Class[_], right: _Class[_]): Boolean = {
    var low = 0
    var high = left.interfacesCount - 1
    if (high == -1) return false
    val interfaces = left.interfaces
    val rightId = right.id
    while (low <= high) {
      val idx = (low + high) / 2
      val interfacePtr = Intrinsics.elemRawPtr(
        interfaces,
        Intrinsics.castRawSizeToInt(Intrinsics.sizeOf[Ptr[_]]) * idx
      )
      val interface =
        Intrinsics.loadObject(interfacePtr).asInstanceOf[_Class[_]]
      val interfaceId = interface.id
      if (interfaceId == rightId) return true
      if (interfaceId < rightId) low = idx + 1
      else high = idx - 1
    }
    false
  }

  def forName(name: String): Class[_] =
    LinkedClassesRepository.byName
      .get(name)
      .getOrElse(throw new ClassNotFoundException(name))
      .asInstanceOf[Class[_]]

  def forName(
      name: String,
      init: scala.Boolean,
      loader: ClassLoader
  ): Class[_] = forName(name)
}

private object LinkedClassesRepository {
  // Reachable only from `forName` method
  @noinline private def loadAll: scala.Array[_Class[_]] = intrinsic
  val byName: Map[String, _Class[_]] = loadAll.map { cls =>
    cls.name -> cls
  }.toMap
}
