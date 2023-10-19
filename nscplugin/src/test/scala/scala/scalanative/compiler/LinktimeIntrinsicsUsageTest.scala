package scala.scalanative.compiler

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.nir._
import scala.scalanative.util.Scope
import scala.scalanative.linker.compileAndLoad
import scala.scalanative.api.CompilationFailedException
import scala.scalanative.NIRCompiler

class LinktimeIntrinsicsUsageTest {

  @Test def requireLiteralForServiceLoader(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object Test {
          |  val x: Class[_] = this.getClass
          |  def fail() = java.util.ServiceLoader.load(x)
          |}""".stripMargin
          )
        )
    )
    assertTrue(
      err.getMessage(),
      err
        .getMessage()
        .contains(
          "first argument of method load needs to be literal constant of class type"
        )
    )
  }

  @Test def setsCorrectDefnAttrs(): Unit = {
    compileAndLoad(
      "Test.scala" ->
        """object Test{
        |  def bias = println() 
        |  def usesServiceLoader1 = {java.util.ServiceLoader.loadInstalled(classOf[Test.type]); ()}
        |  def usesServiceLoader2 = {java.util.ServiceLoader.load(classOf[Test.type]); ()}
        |  def usesServiceLoader3 = {java.util.ServiceLoader.load(classOf[Test.type], null.asInstanceOf[java.lang.ClassLoader]); ()}
        |}""".stripMargin
    ) { defns =>
      val TestModule = Global.Top("Test$")
      val expected: Seq[Global] = Seq(
        TestModule,
        TestModule.member(Sig.Method("bias", Seq(Type.Unit))),
        TestModule.member(Sig.Method("usesServiceLoader1", Seq(Type.Unit))),
        TestModule.member(Sig.Method("usesServiceLoader2", Seq(Type.Unit))),
        TestModule.member(Sig.Method("usesServiceLoader3", Seq(Type.Unit)))
      )
      assertEquals(Set.empty, expected.diff(defns.map(_.name)).toSet)

      val expectedUsingIntrinsics = defns
        .filter(_.name match {
          case Global.Member(TestModule, sig) =>
            sig.unmangled match {
              case Sig.Method(name, _, _) =>
                name.startsWith("usesServiceLoader")
              case _ => false
            }
          case _ => false
        })
        .toSet
      defns.foreach { defn =>
        assertEquals(
          defn.name.toString(),
          expectedUsingIntrinsics.contains(defn),
          defn.attrs.isUsingIntrinsics
        )
      }
    }
  }

}
