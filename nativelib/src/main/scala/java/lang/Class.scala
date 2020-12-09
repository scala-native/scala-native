package java.lang

import java.lang.reflect.{Field, Method}
import scala.language.implicitConversions

import scalanative.annotation._
import scalanative.unsafe._
import scalanative.runtime.{Array => _, _}

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

/** @param rawty - Pointer with underlying Rt.Type info */
final class _Class[A](val rawty: RawPtr) {
  @alwaysinline private def ty: Ptr[Type] =
    fromRawPtr[Type](rawty)

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
    ty.name

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
    is(that.asInstanceOf[_Class[_]].ty, ty)

  def isInstance(obj: Object): scala.Boolean =
    is(obj.getClass.asInstanceOf[_Class[_]].ty, ty)

  @alwaysinline private def is(cls: Class[_]): Boolean =
    this eq cls.asInstanceOf[_Class[A]]

  private def is(left: Ptr[Type], right: Ptr[Type]): Boolean =
    // This replicates the logic of the compiler-generated instance check
    // that you would normally get if you do (obj: L).isInstanceOf[R],
    // where rtti for L and R are `left` and `right`.
    if (left.isClass) {
      if (right.isClass) {
        val rightCls  = right.asInstanceOf[Ptr[ClassType]]
        val rightFrom = rightCls.id
        val rightTo   = rightCls.idRangeUntil
        val leftId    = left.id
        leftId >= rightFrom && leftId <= rightTo
      } else {
        __check_class_has_trait(left.id, -right.id - 1)
      }
    } else {
      if (right.isClass) {
        false
      } else {
        __check_trait_has_trait(-left.id - 1, -right.id - 1)
      }
    }

  def isInterface(): scala.Boolean =
    !ty.isClass

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
        rawty == other.rawty
      case _ =>
        false
    }

  @inline override def hashCode: Int =
    Intrinsics.castRawPtrToLong(rawty).##

  override def toString = {
    val name   = getName()
    val prefix = if (ty.isClass) "class " else "interface "
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
  def getConstructor(args: Array[Object]): java.lang.reflect.Constructor[_] =
    ???
  @stub
  def getConstructors(): Array[Object] = ???
  @stub
  def getDeclaredFields(): Array[Field] = ???
  @stub
  def getMethod(name: java.lang.String,
                args: Array[Class[_]]): java.lang.reflect.Method = ???
  @stub
  def getMethods(): Array[Method] = ???
  @stub
  def getResourceAsStream(name: java.lang.String): java.io.InputStream = ???
}

object _Class {
  @alwaysinline private[java] implicit def _class2class[A](
      cls: _Class[A]): Class[A] =
    cls.asInstanceOf[Class[A]]
  @alwaysinline private[java] implicit def class2_class[A](
      cls: Class[A]): _Class[A] =
    cls.asInstanceOf[_Class[A]]

  @stub
  def forName(name: String): Class[_] = ???
  @stub
  def forName(name: String,
              init: scala.Boolean,
              loader: ClassLoader): Class[_] = ???
}
