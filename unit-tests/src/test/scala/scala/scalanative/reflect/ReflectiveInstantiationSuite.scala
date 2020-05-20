package scala.scalanative

package reflect

// Ported from Scala.js.

import scala.scalanative.reflect._
import scala.scalanative.reflect.annotation._
import scala.scalanative.unsafe._

object ReflectiveInstantiationSuite extends tests.Suite {
  import ReflectTest.{Accessors, PtrAccessors, VC}

  private final val Prefix = "scala.scalanative.reflect.ReflectTest$"

  private final val NameClassEnableDirect =
    Prefix + "ClassEnableDirect"
  private final val NameClassEnableDirectNoZeroArgCtor =
    Prefix + "ClassEnableDirectNoZeroArgCtor"
  private final val NameObjectEnableDirect =
    Prefix + "ObjectEnableDirect$"
  private final val NameTraitEnableDirect =
    Prefix + "TraitEnableDirect"
  private final val NameAbstractClassEnableDirect =
    Prefix + "AbstractClassEnableDirect"
  private final val NameClassNoPublicConstructorEnableDirect =
    Prefix + "ClassNoPublicConstructorEnableDirect"

  private final val NameInnerClass = {
    Prefix + "ClassWithInnerClassWithEnableReflectiveInstantiation$" +
      "InnerClassWithEnableReflectiveInstantiation"
  }

  private final val NameClassEnableIndirect =
    Prefix + "ClassEnableIndirect"
  private final val NameClassEnableIndirectNoZeroArgCtor =
    Prefix + "ClassEnableIndirectNoZeroArgCtor"
  private final val NameObjectEnableIndirect =
    Prefix + "ObjectEnableIndirect$"
  private final val NameTraitEnableIndirect =
    Prefix + "TraitEnableIndirect"
  private final val NameAbstractClassEnableIndirect =
    Prefix + "AbstractClassEnableIndirect"
  private final val NameClassNoPublicConstructorEnableIndirect =
    Prefix + "ClassNoPublicConstructorEnableIndirect"

  private final val NameClassDisable =
    Prefix + "ClassDisable"
  private final val NameObjectDisable =
    Prefix + "ObjectDisable$"
  private final val NameTraitDisable =
    Prefix + "TraitDisable"

  private final val NameInnerObject = {
    Prefix + "ClassWithInnerObjectWithEnableReflectiveInstantiation$" +
      "InnerObjectWithEnableReflectiveInstantiation"
  }

  private final val NameClassWithPtrArg = {
    Prefix + "ClassWithPtrArg"
  }

  test("testClassRuntimeClass") {
    for {
      name <- Seq(NameClassEnableDirect,
                  NameClassEnableDirectNoZeroArgCtor,
                  NameClassEnableIndirect,
                  NameClassEnableIndirectNoZeroArgCtor)
    } {
      val optClassData = Reflect.lookupInstantiatableClass(name)
      assertTrue(optClassData.isDefined)
      val classData = optClassData.get

      val runtimeClass = optClassData.get.runtimeClass
      assertEquals(name, runtimeClass.getName)
    }
  }

  test("testObjectRuntimeClass") {
    for {
      name <- Seq(NameObjectEnableDirect, NameObjectEnableIndirect)
    } {
      val optClassData = Reflect.lookupLoadableModuleClass(name)
      assertTrue(optClassData.isDefined)
      val classData = optClassData.get

      val runtimeClass = optClassData.get.runtimeClass
      assertEquals(name, runtimeClass.getName)
    }
  }

  test("testClassCannotBeFound") {
    for {
      name <- Seq(
        NameObjectEnableDirect,
        NameTraitEnableDirect,
        NameAbstractClassEnableDirect,
        NameClassNoPublicConstructorEnableDirect,
        NameObjectEnableIndirect,
        NameTraitEnableIndirect,
        NameAbstractClassEnableIndirect,
        NameClassNoPublicConstructorEnableIndirect,
        NameClassDisable,
        NameObjectDisable,
        NameTraitDisable
      )
    } {
      assertFalse(s"$name should not be found",
                  Reflect.lookupInstantiatableClass(name).isDefined)
    }
  }

  test("testObjectCannotBeFound") {
    for {
      name <- Seq(
        NameClassEnableDirect,
        NameClassEnableDirectNoZeroArgCtor,
        NameTraitEnableDirect,
        NameAbstractClassEnableDirect,
        NameClassNoPublicConstructorEnableDirect,
        NameClassEnableIndirect,
        NameTraitEnableIndirect,
        NameAbstractClassEnableIndirect,
        NameClassNoPublicConstructorEnableIndirect,
        NameClassDisable,
        NameObjectDisable,
        NameTraitDisable
      )
    } {
      assertFalse(s"$name should not be found",
                  Reflect.lookupLoadableModuleClass(name).isDefined)
    }
  }

  test("testClassNoArgCtor") {
    for (name <- Seq(NameClassEnableDirect, NameClassEnableIndirect)) {
      val optClassData = Reflect.lookupInstantiatableClass(name)
      assertTrue(optClassData.isDefined)
      val classData = optClassData.get

      val instance = classData.newInstance().asInstanceOf[Accessors]
      assertEquals(-1, instance.x)
      assertEquals(name.stripPrefix(Prefix), instance.y)
    }
  }

  test("testClassNoArgCtorErrorCase") {
    for (name <- Seq(NameClassEnableDirectNoZeroArgCtor,
                     NameClassEnableIndirectNoZeroArgCtor)) {
      val optClassData = Reflect.lookupInstantiatableClass(name)
      assertTrue(optClassData.isDefined)
      val classData = optClassData.get

      expectThrows(classOf[InstantiationException], {
        classData.newInstance()
      })
    }
  }

  test("testClassCtorWithArgs") {
    for (name <- Seq(NameClassEnableDirect,
                     NameClassEnableDirectNoZeroArgCtor,
                     NameClassEnableIndirect,
                     NameClassEnableIndirectNoZeroArgCtor)) {
      val optClassData = Reflect.lookupInstantiatableClass(name)
      assertTrue(optClassData.isDefined)
      val classData = optClassData.get

      val optCtorIntString =
        classData.getConstructor(classOf[Int], classOf[String])
      assertTrue(optCtorIntString.isDefined)
      val instanceIntString =
        optCtorIntString.get.newInstance(543, "foobar").asInstanceOf[Accessors]
      assertEquals(543, instanceIntString.x)
      assertEquals("foobar", instanceIntString.y)

      val optCtorInt = classData.getConstructor(classOf[Int])
      assertTrue(optCtorInt.isDefined)
      val instanceInt =
        optCtorInt.get.newInstance(123).asInstanceOf[Accessors]
      assertEquals(123, instanceInt.x)
      assertEquals(name.stripPrefix(Prefix), instanceInt.y)

      // Value class is seen as its underlying
      val optCtorShort = classData.getConstructor(classOf[Short])
      assertTrue(optCtorShort.isDefined)
      val instanceShort =
        optCtorShort.get.newInstance(21.toShort).asInstanceOf[Accessors]
      assertEquals(42, instanceShort.x)
      assertEquals(name.stripPrefix(Prefix), instanceShort.y)

      // Non-existent
      assertFalse(classData.getConstructor(classOf[Boolean]).isDefined)
      assertFalse(classData.getConstructor(classOf[VC]).isDefined)

      // Non-public
      assertFalse(classData.getConstructor(classOf[String]).isDefined)
      assertFalse(classData.getConstructor(classOf[Double]).isDefined)
    }
  }

  test("testClassCtorWthPtrArg") {
    import ReflectTest.ClassWithPtrArg

    val optClassData = Reflect.lookupInstantiatableClass(NameClassWithPtrArg)
    assertTrue(optClassData.isDefined)
    val classData = optClassData.get

    // test with array of bytes
    Zone { implicit z =>
      val size   = 64
      val buffer = alloc[Byte](size)

      def fn(idx: Int) = size - idx

      for (i <- 0 until size) {
        buffer(i) = fn(i).toByte
      }

      val optCtorPtrInt =
        classData.getConstructor(classOf[Ptr[Byte]], classOf[Int])
      assertTrue(optCtorPtrInt.isDefined)

      val instance =
        optCtorPtrInt.get.newInstance(buffer, size).asInstanceOf[PtrAccessors]
      assertEquals(64, instance.n)

      for (i <- 0 until size) {
        assertEquals(fn(i), instance.p(i))
      }
    }

    // test with null pointer
    val instance = classData.newInstance().asInstanceOf[PtrAccessors]
    assertEquals(-1, instance.n)
    assertEquals(null, instance.p)
  }

  test("testInnerClass") {
    import ReflectTest.ClassWithInnerClassWithEnableReflectiveInstantiation

    val outer = new ClassWithInnerClassWithEnableReflectiveInstantiation(15)

    val optClassData = Reflect.lookupInstantiatableClass(NameInnerClass)
    assertTrue(optClassData.isDefined)
    val classData = optClassData.get

    val optCtorOuterString =
      classData.getConstructor(outer.getClass, classOf[String])
    assertTrue(optCtorOuterString.isDefined)
    val instanceOuterString =
      optCtorOuterString.get.newInstance(outer, "babar").asInstanceOf[Accessors]
    assertEquals(15, instanceOuterString.x)
    assertEquals("babar", instanceOuterString.y)
  }

  test("testLocalClass") {
    @EnableReflectiveInstantiation
    class LocalClassWithEnableReflectiveInstantiation

    val fqcn = classOf[LocalClassWithEnableReflectiveInstantiation].getName
    assertFalse(s"$fqcn should not be found",
                Reflect.lookupInstantiatableClass(fqcn).isDefined)
  }

  test("testObjectLoad") {
    for (name <- Seq(NameObjectEnableDirect, NameObjectEnableIndirect)) {
      val optClassData = Reflect.lookupLoadableModuleClass(name)
      assertTrue(optClassData.isDefined)
      val classData = optClassData.get

      val instance = classData.loadModule().asInstanceOf[Accessors]
      assertEquals(101, instance.x)
      assertEquals(name.stripPrefix(Prefix), instance.y)
    }
  }

  test("testInnerObjectWithEnableReflectiveInstantiation_issue_3228") {
    assertFalse(Reflect.lookupLoadableModuleClass(NameInnerObject).isDefined)
    assertFalse(Reflect.lookupInstantiatableClass(NameInnerObject).isDefined)
  }

  test("testLocalClassWithReflectiveInstantiationInLambda_issue_3227") {
    // Test that the presence of the following code does not prevent linking
    { () =>
      @EnableReflectiveInstantiation
      class Foo
    }
  }

}

object ReflectTest {
  trait Accessors {
    val x: Int
    val y: String
  }

  trait PtrAccessors {
    val p: Ptr[Byte]
    val n: Int
  }

  final class VC(val self: Short) extends AnyVal

  // Entities with directly enabled reflection

  @EnableReflectiveInstantiation
  class ClassWithPtrArg(val p: Ptr[Byte], val n: Int) extends PtrAccessors {
    def this() = {
      this(null, -1)
    }
  }

  @EnableReflectiveInstantiation
  class ClassEnableDirect(val x: Int, val y: String) extends Accessors {
    def this(x: Int) = this(x, "ClassEnableDirect")
    def this() = this(-1)
    def this(vc: VC) = this(vc.self.toInt * 2)

    protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  @EnableReflectiveInstantiation
  class ClassEnableDirectNoZeroArgCtor(val x: Int, val y: String)
      extends Accessors {
    def this(x: Int) = this(x, "ClassEnableDirectNoZeroArgCtor")
    def this(vc: VC) = this(vc.self.toInt * 2)

    protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  @EnableReflectiveInstantiation
  object ObjectEnableDirect extends Accessors {
    val x = 101
    val y = "ObjectEnableDirect$"
  }

  @EnableReflectiveInstantiation
  trait TraitEnableDirect extends Accessors

  @EnableReflectiveInstantiation
  abstract class AbstractClassEnableDirect(val x: Int, val y: String)
      extends Accessors {

    def this(x: Int) = this(x, "AbstractClassEnableDirect")
    def this() = this(-1)
    def this(vc: VC) = this(vc.self.toInt * 2)

    protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  @EnableReflectiveInstantiation
  class ClassNoPublicConstructorEnableDirect private (val x: Int, val y: String)
      extends Accessors {

    protected def this(y: String) = this(-5, y)
  }

  class ClassWithInnerClassWithEnableReflectiveInstantiation(_x: Int) {
    @EnableReflectiveInstantiation
    class InnerClassWithEnableReflectiveInstantiation(_y: String)
        extends Accessors {
      val x = _x
      val y = _y
    }
  }

  // Entities with reflection enabled by inheritance

  @EnableReflectiveInstantiation
  trait EnablingTrait

  class ClassEnableIndirect(val x: Int, val y: String)
      extends EnablingTrait
      with Accessors {

    def this(x: Int) = this(x, "ClassEnableIndirect")
    def this() = this(-1)
    def this(vc: VC) = this(vc.self.toInt * 2)

    protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  class ClassEnableIndirectNoZeroArgCtor(val x: Int, val y: String)
      extends EnablingTrait
      with Accessors {
    def this(x: Int) = this(x, "ClassEnableIndirectNoZeroArgCtor")
    def this(vc: VC) = this(vc.self.toInt * 2)

    protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  object ObjectEnableIndirect extends EnablingTrait with Accessors {
    val x = 101
    val y = "ObjectEnableIndirect$"
  }

  trait TraitEnableIndirect extends EnablingTrait with Accessors

  abstract class AbstractClassEnableIndirect(val x: Int, val y: String)
      extends EnablingTrait
      with Accessors {

    def this(x: Int) = this(x, "AbstractClassEnableIndirect")
    def this() = this(-1)
    def this(vc: VC) = this(vc.self.toInt * 2)

    protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  class ClassNoPublicConstructorEnableIndirect private (val x: Int,
                                                        val y: String)
      extends EnablingTrait
      with Accessors {

    protected def this(y: String) = this(-5, y)
  }

  // Entities with reflection disabled

  class ClassDisable(val x: Int, val y: String) extends Accessors

  object ObjectDisable extends Accessors {
    val x = 101
    val y = "ObjectDisable$"
  }

  trait TraitDisable extends Accessors

  // Regression cases

  class ClassWithInnerObjectWithEnableReflectiveInstantiation {
    @EnableReflectiveInstantiation
    object InnerObjectWithEnableReflectiveInstantiation
  }
}
