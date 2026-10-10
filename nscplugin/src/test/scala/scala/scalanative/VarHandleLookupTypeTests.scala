package scala.scalanative

// Generated from VarHandleLookupTypeTests.scala.gyb; edit the template.
// format: off
import org.junit.Test

class VarHandleBooleanLookupTypeTests extends VarHandleLookupCompilerTestSupport {
  private def primitive(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: Boolean = true
       |  def current: Boolean = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  private def boxed(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: java.lang.Boolean = java.lang.Boolean.valueOf(true)
       |  def current: java.lang.Boolean = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  @Test def primitiveAcceptsClassOfPrimitive(): Unit =
    compiles(primitive("classOf[Boolean]"))

  @Test def primitiveAcceptsWrapperTYPE(): Unit =
    compiles(primitive("java.lang.Boolean.TYPE"))

  @Test def primitiveRejectsBoxedClass(): Unit =
    rejected(primitive("classOf[java.lang.Boolean]"), "type does not match")

  @Test def boxedAcceptsBoxedClass(): Unit =
    compiles(boxed("classOf[java.lang.Boolean]"))

  @Test def boxedRejectsClassOfPrimitive(): Unit =
    rejected(boxed("classOf[Boolean]"), "type does not match")

  @Test def boxedRejectsWrapperTYPE(): Unit =
    rejected(boxed("java.lang.Boolean.TYPE"), "type does not match")
}
class VarHandleByteLookupTypeTests extends VarHandleLookupCompilerTestSupport {
  private def primitive(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: Byte = 37.toByte
       |  def current: Byte = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  private def boxed(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: java.lang.Byte = java.lang.Byte.valueOf(37.toByte)
       |  def current: java.lang.Byte = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  @Test def primitiveAcceptsClassOfPrimitive(): Unit =
    compiles(primitive("classOf[Byte]"))

  @Test def primitiveAcceptsWrapperTYPE(): Unit =
    compiles(primitive("java.lang.Byte.TYPE"))

  @Test def primitiveRejectsBoxedClass(): Unit =
    rejected(primitive("classOf[java.lang.Byte]"), "type does not match")

  @Test def boxedAcceptsBoxedClass(): Unit =
    compiles(boxed("classOf[java.lang.Byte]"))

  @Test def boxedRejectsClassOfPrimitive(): Unit =
    rejected(boxed("classOf[Byte]"), "type does not match")

  @Test def boxedRejectsWrapperTYPE(): Unit =
    rejected(boxed("java.lang.Byte.TYPE"), "type does not match")
}
class VarHandleShortLookupTypeTests extends VarHandleLookupCompilerTestSupport {
  private def primitive(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: Short = 37.toShort
       |  def current: Short = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  private def boxed(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: java.lang.Short = java.lang.Short.valueOf(37.toShort)
       |  def current: java.lang.Short = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  @Test def primitiveAcceptsClassOfPrimitive(): Unit =
    compiles(primitive("classOf[Short]"))

  @Test def primitiveAcceptsWrapperTYPE(): Unit =
    compiles(primitive("java.lang.Short.TYPE"))

  @Test def primitiveRejectsBoxedClass(): Unit =
    rejected(primitive("classOf[java.lang.Short]"), "type does not match")

  @Test def boxedAcceptsBoxedClass(): Unit =
    compiles(boxed("classOf[java.lang.Short]"))

  @Test def boxedRejectsClassOfPrimitive(): Unit =
    rejected(boxed("classOf[Short]"), "type does not match")

  @Test def boxedRejectsWrapperTYPE(): Unit =
    rejected(boxed("java.lang.Short.TYPE"), "type does not match")
}
class VarHandleCharLookupTypeTests extends VarHandleLookupCompilerTestSupport {
  private def primitive(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: Char = 37.toChar
       |  def current: Char = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  private def boxed(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: java.lang.Character = java.lang.Character.valueOf(37.toChar)
       |  def current: java.lang.Character = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  @Test def primitiveAcceptsClassOfPrimitive(): Unit =
    compiles(primitive("classOf[Char]"))

  @Test def primitiveAcceptsWrapperTYPE(): Unit =
    compiles(primitive("java.lang.Character.TYPE"))

  @Test def primitiveRejectsBoxedClass(): Unit =
    rejected(primitive("classOf[java.lang.Character]"), "type does not match")

  @Test def boxedAcceptsBoxedClass(): Unit =
    compiles(boxed("classOf[java.lang.Character]"))

  @Test def boxedRejectsClassOfPrimitive(): Unit =
    rejected(boxed("classOf[Char]"), "type does not match")

  @Test def boxedRejectsWrapperTYPE(): Unit =
    rejected(boxed("java.lang.Character.TYPE"), "type does not match")
}
class VarHandleIntLookupTypeTests extends VarHandleLookupCompilerTestSupport {
  private def primitive(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: Int = 37
       |  def current: Int = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  private def boxed(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: java.lang.Integer = java.lang.Integer.valueOf(37)
       |  def current: java.lang.Integer = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  @Test def primitiveAcceptsClassOfPrimitive(): Unit =
    compiles(primitive("classOf[Int]"))

  @Test def primitiveAcceptsWrapperTYPE(): Unit =
    compiles(primitive("java.lang.Integer.TYPE"))

  @Test def primitiveRejectsBoxedClass(): Unit =
    rejected(primitive("classOf[java.lang.Integer]"), "type does not match")

  @Test def boxedAcceptsBoxedClass(): Unit =
    compiles(boxed("classOf[java.lang.Integer]"))

  @Test def boxedRejectsClassOfPrimitive(): Unit =
    rejected(boxed("classOf[Int]"), "type does not match")

  @Test def boxedRejectsWrapperTYPE(): Unit =
    rejected(boxed("java.lang.Integer.TYPE"), "type does not match")
}
class VarHandleLongLookupTypeTests extends VarHandleLookupCompilerTestSupport {
  private def primitive(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: Long = 37L
       |  def current: Long = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  private def boxed(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: java.lang.Long = java.lang.Long.valueOf(37L)
       |  def current: java.lang.Long = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  @Test def primitiveAcceptsClassOfPrimitive(): Unit =
    compiles(primitive("classOf[Long]"))

  @Test def primitiveAcceptsWrapperTYPE(): Unit =
    compiles(primitive("java.lang.Long.TYPE"))

  @Test def primitiveRejectsBoxedClass(): Unit =
    rejected(primitive("classOf[java.lang.Long]"), "type does not match")

  @Test def boxedAcceptsBoxedClass(): Unit =
    compiles(boxed("classOf[java.lang.Long]"))

  @Test def boxedRejectsClassOfPrimitive(): Unit =
    rejected(boxed("classOf[Long]"), "type does not match")

  @Test def boxedRejectsWrapperTYPE(): Unit =
    rejected(boxed("java.lang.Long.TYPE"), "type does not match")
}
class VarHandleFloatLookupTypeTests extends VarHandleLookupCompilerTestSupport {
  private def primitive(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: Float = 37.0f
       |  def current: Float = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  private def boxed(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: java.lang.Float = java.lang.Float.valueOf(37.0f)
       |  def current: java.lang.Float = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  @Test def primitiveAcceptsClassOfPrimitive(): Unit =
    compiles(primitive("classOf[Float]"))

  @Test def primitiveAcceptsWrapperTYPE(): Unit =
    compiles(primitive("java.lang.Float.TYPE"))

  @Test def primitiveRejectsBoxedClass(): Unit =
    rejected(primitive("classOf[java.lang.Float]"), "type does not match")

  @Test def boxedAcceptsBoxedClass(): Unit =
    compiles(boxed("classOf[java.lang.Float]"))

  @Test def boxedRejectsClassOfPrimitive(): Unit =
    rejected(boxed("classOf[Float]"), "type does not match")

  @Test def boxedRejectsWrapperTYPE(): Unit =
    rejected(boxed("java.lang.Float.TYPE"), "type does not match")
}
class VarHandleDoubleLookupTypeTests extends VarHandleLookupCompilerTestSupport {
  private def primitive(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: Double = 37.0d
       |  def current: Double = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  private def boxed(token: String): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value: java.lang.Double = java.lang.Double.valueOf(37.0d)
       |  def current: java.lang.Double = value
       |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
       |}""".stripMargin

  @Test def primitiveAcceptsClassOfPrimitive(): Unit =
    compiles(primitive("classOf[Double]"))

  @Test def primitiveAcceptsWrapperTYPE(): Unit =
    compiles(primitive("java.lang.Double.TYPE"))

  @Test def primitiveRejectsBoxedClass(): Unit =
    rejected(primitive("classOf[java.lang.Double]"), "type does not match")

  @Test def boxedAcceptsBoxedClass(): Unit =
    compiles(boxed("classOf[java.lang.Double]"))

  @Test def boxedRejectsClassOfPrimitive(): Unit =
    rejected(boxed("classOf[Double]"), "type does not match")

  @Test def boxedRejectsWrapperTYPE(): Unit =
    rejected(boxed("java.lang.Double.TYPE"), "type does not match")
}
