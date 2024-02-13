package scala.scalanative.compiler

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.nir._
import scala.scalanative.util.Scope
import scala.scalanative.linker.compileAndLoad
import scala.scalanative.api.CompilationFailedException
import scala.scalanative.NIRCompiler

class MethodCallTest {

  @Test def emitsMethodDispatchForAbstractMethods(): Unit = {
    compileAndLoad(
      "Test.scala" ->
        """
        |trait Foo{
        |  def bar(v1: String, v2: Option[String] = None): String
        |}
        |object Foo extends Foo{
        |  override def bar(v1: String, v2: Option[String] = None): String = ???
        |}
        |class FooCls extends Foo{
        |  override def bar(v1: String, v2: Option[String] = None): String = ???
        |}
        |object Test{  
        |  def testModule(): Unit = {
        |     Foo.bar("object")
        |     Foo.bar("object", Some("opt"))
        |  }
        |  def testClass(): Unit = {
        |    val foo = new FooCls()
        |    foo.bar("cls")
        |    foo.bar("cls", Some("opt"))
        |  }
        |}""".stripMargin
    ) { defns =>
      val TestModule = Global.Top("Test$")
      val TestModuleMethod =
        TestModule.member(Sig.Method("testModule", Seq(Type.Unit)))
      val TestClassMethod =
        TestModule.member(Sig.Method("testClass", Seq(Type.Unit)))
      val FooModule = Type.Ref(Global.Top("Foo$"))
      val FooClass = Type.Ref(Global.Top("FooCls"))

      val expected: Seq[Global] =
        Seq(TestModule, TestModuleMethod, TestClassMethod)
      assertEquals(Set.empty, expected.diff(defns.map(_.name)).toSet)

      defns.foreach {
        case defn: Defn.Define if defn.name == TestModuleMethod =>
          defn.insts.collect {
            case Inst.Let(
                  _,
                  op @ Op.Call(Type.Function(Seq(FooModule, _*), _), fn, _),
                  _
                ) =>
              assert(fn.isInstanceOf[Val.Local], op.show)
          }
        case defn: Defn.Define if defn.name == TestClassMethod =>
          defn.insts.collect {
            case Inst.Let(
                  _,
                  op @ Op.Call(Type.Function(Seq(FooClass, _*), _), fn, _),
                  _
                ) =>
              fn match {
                case Val.Global(Global.Member(FooClass.name, sig), _) =>
                  assert(sig.isCtor)
                case _ => assert(fn.isInstanceOf[Val.Local], op.show)
              }
          }
        case _ => ()
      }
    }
  }

}
