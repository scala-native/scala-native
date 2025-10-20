package scala.scalanative.compiler

import org.junit.Test
import org.junit.Assert.*

import scala.scalanative.nir.*
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

  @Test def emitsStaticObjectMonitorCalls(): Unit = {
    compileAndLoad(
      "Test.scala" ->
        """
        |object Test{  
        |  def main(): Unit = synchronized {
        |   Test.synchronized{
        |     println("")
        |    }
        |  }
        |}""".stripMargin
    ) { defns =>
      val TestModule = Global.Top("Test$")
      val MainMethod =
        TestModule.member(Sig.Method("main", Seq(Type.Unit)))

      val expected: Seq[Global] =
        Seq(TestModule, MainMethod)
      assertEquals(Set.empty, expected.diff(defns.map(_.name)).toSet)

      defns
        .collectFirst {
          case defn: Defn.Define if defn.name == MainMethod => defn
        }
        .foreach { defn =>
          // format: off
          val RuntimePackageCls = Global.Top("scala.scalanative.runtime.package")
          val RuntimePackage    = Global.Top("scala.scalanative.runtime.package$")
          val EnterMonitorSig   = Sig.Method("enterMonitor", Seq(Rt.Object, Type.Unit)).mangled
          val ExitMonitorSig    = Sig.Method("exitMonitor",  Seq(Rt.Object, Type.Unit)).mangled
          val EnterMonitorStaticSig   = Sig.Method("enterMonitor", Seq(Rt.Object, Type.Unit), Sig.Scope.PublicStatic).mangled
          val ExitMonitorStaticSig    = Sig.Method("exitMonitor",  Seq(Rt.Object, Type.Unit), Sig.Scope.PublicStatic).mangled
          object MonitorEnter{
            def unapply(v: Val): Option[Boolean] = v match {
              case Val.Global(Global.Member(RuntimePackage, EnterMonitorSig), _) => Some(false)
              case Val.Global(Global.Member(RuntimePackageCls, EnterMonitorStaticSig), _) => Some(true)
              case _ => None
            }
          }
          object MonitorExit{
            def unapply(v: Val): Option[Boolean] = v match {
              case Val.Global(Global.Member(RuntimePackage, ExitMonitorSig), _) => Some(false)
              case Val.Global(Global.Member(RuntimePackageCls, ExitMonitorStaticSig), _) => Some(true)
              case _ => None
            }
          }
          // format: on
          var monitorEnters, monitorExits = 0
          defn.insts.foreach {
            case inst @ Inst.Let(_, op, _) =>
              op match {
                case Op.Method(MonitorEnter(_) | MonitorExit(_), _) =>
                  fail(s"Unexpected method dispatch: ${inst.show}")
                case Op.Call(_, MonitorEnter(isStatic), args) =>
                  assert(isStatic)
                  monitorEnters += 1
                case Op.Call(_, MonitorExit(isStatic), args) =>
                  assert(isStatic)
                  monitorExits += 1
                case _ => ()
              }
            case _ => ()
          }
          // For each monitor enter there are 2 monitor exits:
          // - first for successfull path before reutrning value
          // - second for erronous path before throwing exception
          // synchronised call is emitted as try-finally block
          assertEquals(2, monitorEnters)
          assertEquals(4, monitorExits)
        }
    }
  }

}
