package java.lang

import java.lang.reflect.{Field, Method}
import scala.language.implicitConversions

import scalanative.annotation._
import scalanative.unsafe._
import scalanative.runtime.{Array => _, _}
import java.io.InputStream
import java.lang.resource.EncodedResourceInputStream
import java.lang.resource.EmbeddedResourceHelper
import java.util.Base64
import java.nio.file.Paths

// These two methods are generated at link-time by the toolchain
// using current closed-world knowledge of classes and traits in
// the current application.
@extern
object rtti {
  def __check_class_has_trait(classId: Int, traitId: Int): scala.Boolean =
    extern
  def __check_trait_has_trait(leftId: Int, rightId: Int): scala.Boolean =
    extern
}
import rtti._

final class _Class[A] {
  var id: Int = _
  var traitId: Int = _
  var name: String = _
  var size: Int = _
  var idRangeUntil: Int = _

  def cast(obj: Object): A =
    obj.asInstanceOf[A]

  def getComponentType(): _Class[_] = {
    if (is(classOf[BooleanArray])) classOf[scala.Boolean]
    else if (is(classOf[CharArray])) classOf[scala.Char]
    else if (is(classOf[ByteArray])) classOf[scala.Byte]
    else if (is(classOf[ShortArray])) classOf[scala.Short]
    else if (is(classOf[IntArray])) classOf[scala.Int]
    else if (is(classOf[LongArray])) classOf[scala.Long]
    else if (is(classOf[FloatArray])) classOf[scala.Float]
    else if (is(classOf[DoubleArray])) classOf[scala.Double]
    else classOf[java.lang.Object]
  }

  def getName(): String =
    name

  def getSimpleName(): String =
    getName().split('.').last.split('$').last

  def isArray(): scala.Boolean =
    is(classOf[BooleanArray]) ||
      is(classOf[CharArray]) ||
      is(classOf[ByteArray]) ||
      is(classOf[ShortArray]) ||
      is(classOf[IntArray]) ||
      is(classOf[LongArray]) ||
      is(classOf[FloatArray]) ||
      is(classOf[DoubleArray]) ||
      is(classOf[ObjectArray])

  def isAssignableFrom(that: Class[_]): scala.Boolean =
    is(that.asInstanceOf[_Class[_]], this)

  def isInstance(obj: Object): scala.Boolean =
    is(obj.getClass.asInstanceOf[_Class[_]], this)

  @alwaysinline private def is(cls: Class[_]): Boolean =
    this eq cls.asInstanceOf[_Class[A]]

  private def is(left: _Class[_], right: _Class[_]): Boolean =
    // This replicates the logic of the compiler-generated instance check
    // that you would normally get if you do (obj: L).isInstanceOf[R],
    // where rtti for L and R are `left` and `right`.
    if (!left.isInterface()) {
      if (!right.isInterface()) {
        val rightFrom = right.id
        val rightTo = right.idRangeUntil
        val leftId = left.id
        leftId >= rightFrom && leftId <= rightTo
      } else {
        __check_class_has_trait(left.id, -right.id - 1)
      }
    } else {
      if (!right.isInterface()) {
        false
      } else {
        __check_trait_has_trait(-left.id - 1, -right.id - 1)
      }
    }

  def isInterface(): scala.Boolean =
    id < 0

  def isPrimitive(): scala.Boolean =
    is(classOf[PrimitiveBoolean]) ||
      is(classOf[PrimitiveChar]) ||
      is(classOf[PrimitiveByte]) ||
      is(classOf[PrimitiveShort]) ||
      is(classOf[PrimitiveInt]) ||
      is(classOf[PrimitiveLong]) ||
      is(classOf[PrimitiveFloat]) ||
      is(classOf[PrimitiveDouble]) ||
      is(classOf[PrimitiveUnit])

  @inline override def equals(other: Any): scala.Boolean =
    other match {
      case other: _Class[_] =>
        this eq other
      case _ =>
        false
    }

  @inline override def hashCode: Int =
    Intrinsics.castRawPtrToLong(Intrinsics.castObjectToRawPtr(this)).##

  override def toString = {
    val name = getName()
    val prefix = if (isInterface()) "interface " else "class "
    prefix + name
  }

  @stub
  def getInterfaces(): Array[_Class[_]] =
    ???
  @stub
  def getSuperclass(): Class[_ >: A] =
    ???
  @stub
  def getField(name: String): Field =
    ???
  @stub
  def getClassLoader(): java.lang.ClassLoader = ???
  @stub
  def getConstructor(args: Array[_Class[_]]): java.lang.reflect.Constructor[_] =
    ???
  @stub
  def getConstructors(): Array[Object] = ???
  @stub
  def getDeclaredFields(): Array[Field] = ???
  @stub
  def getMethod(
      name: java.lang.String,
      args: Array[Class[_]]
  ): java.lang.reflect.Method = ???
  @stub
  def getMethods(): Array[Method] = ???

  def getResourceAsStream(name: java.lang.String): java.io.InputStream = {
    val absoluteName =
      if (name(0) == '/') {
        name
      } else {
        Option(Paths.get(this.name.replaceAll("\\.", "/")).getParent()) match {
          case Some(parentPath) => s"/${parentPath.toString()}/$name"
          case None             => s"/$name"
        }
      }

    val path =
      Paths.get(absoluteName).normalize().toString().replaceAll("\\\\", "/")

    val absolutePath =
      if (!path.isEmpty() && path(0) != '/') "/" + path
      else path

    EmbeddedResourceHelper.resourceFileIdMap.get(absolutePath) match {
      case Some(fileIndex) =>
        Base64.getDecoder().wrap(new EncodedResourceInputStream(fileIndex))
      case None =>
        null
    }
  }
}

object _Class {
  @alwaysinline private[java] implicit def _class2class[A](
      cls: _Class[A]
  ): Class[A] =
    cls.asInstanceOf[Class[A]]
  @alwaysinline private[java] implicit def class2_class[A](
      cls: Class[A]
  ): _Class[A] =
    cls.asInstanceOf[_Class[A]]

  @stub
  def forName(name: String): Class[_] = ???
  @stub
  def forName(
      name: String,
      init: scala.Boolean,
      loader: ClassLoader
  ): Class[_] = ???
}
