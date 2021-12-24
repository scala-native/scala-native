package scala.scalanative.linker

import org.scalatest._
import scalanative.nir.{Sig, Type, Global, Rt}

class ModuleReachabilitySuite extends ReachabilitySuite {
  val sources = Seq("""
    object Module {
      def meth: Unit = ()
    }
  """)

  val TestClsName = "Test"
  val TestModuleName = "Test$"
  val ModuleClsName = "Module$"
  val ParentClsName = "Parent"
  val ObjectClsName = "java.lang.Object"
  val ScalaMainNonStaticSig =
    Sig.Method("main", Rt.ScalaMainSig.types, Sig.Scope.Public)

  val Test = g(TestClsName)
  val TestModule = g(TestModuleName)
  val TestInit = g(TestModuleName, Sig.Ctor(Seq.empty))
  val TestMain = g(TestClsName, Rt.ScalaMainSig)
  val TestModuleMain = g(TestModuleName, ScalaMainNonStaticSig)
  val Module = g(ModuleClsName)
  val ModuleInit = g(ModuleClsName, Sig.Ctor(Seq.empty))
  val ModuleFoo = g(ModuleClsName, Sig.Method("foo", Seq(Type.Unit)))
  val ModuleBar = g(ModuleClsName, Sig.Field("bar"))
  val ModuleBarSet =
    g(ModuleClsName, Sig.Method("bar_$eq", Seq(Type.Int, Type.Unit)))
  val ModuleBarGet = g(ModuleClsName, Sig.Method("bar", Seq(Type.Int)))
  val Parent = g(ParentClsName)
  val ParentInit = g(ParentClsName, Sig.Ctor(Seq.empty))
  val ParentFoo = g(ParentClsName, Sig.Method("foo", Seq(Type.Unit)))
  val Trait = g("Trait")
  val Object = g(ObjectClsName)
  val ObjectInit = g(ObjectClsName, Sig.Ctor(Seq.empty))

  val commonReachable =
    Seq(Test, TestModule, TestInit, TestMain, TestModuleMain)

  testReachable("unused modules are discarded") {
    val source = """
      object Module

      object Test {
        def main(args: Array[String]): Unit = ()
      }
    """
    val entry = TestMain
    val reachable = Seq(
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  testReachable("unused module vars are discarded") {
    val source = """
      object Module {
        var bar: Int = _
      }

      object Test {
        def main(args: Array[String]): Unit = Module
      }
    """
    val entry = TestMain
    val reachable = Seq(
      Module,
      ModuleInit,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  testReachable("unused module defs are discarded") {
    val source = """
      object Module {
        def foo: Unit = ()
      }

      object Test {
        def main(args: Array[String]): Unit = {
          val x = Module
        }
      }
    """
    val entry = TestMain
    val reachable = Seq(
      Module,
      ModuleInit,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  testReachable("used modules are included") {
    val source = """
      object Module

      object Test {
        def main(args: Array[String]): Unit = {
          val x = Module
        }
      }
    """
    val entry = TestMain
    val reachable = Seq(
      Module,
      ModuleInit,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  testReachable("used module parents are included") {
    val source = """
      class Parent

      object Module extends Parent {
        def foo: Unit = ()
      }

      object Test {
        def main(args: Array[String]): Unit = {
          val x = Module
        }
      }
    """
    val entry = TestMain
    val reachable = Seq(
      Module,
      ModuleInit,
      Parent,
      ParentInit,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  testReachable("used module traits are included") {
    val source = """
      trait Trait

      object Module extends Trait {
        def foo: Unit = ()
      }

      object Test {
        def main(args: Array[String]): Unit = {
          val x = Module
        }
      }
    """
    val entry = TestMain
    val reachable = Seq(
      Module,
      ModuleInit,
      Trait,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  testReachable("module vars are included if written to") {
    val source = """
      object Module {
        var bar: Int = _
      }

      object Test {
        def main(args: Array[String]): Unit = { Module.bar = 42 }
      }
    """
    val entry = TestMain
    val reachable = Seq(
      Module,
      ModuleInit,
      ModuleBar,
      ModuleBarSet,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  testReachable("module vars are included if read from") {
    val source = """
      object Module {
        var bar: Int = _
      }

      object Test {
        def main(args: Array[String]): Unit = Module.bar
      }
    """
    val entry = TestMain
    val reachable = Seq(
      Module,
      ModuleInit,
      ModuleBar,
      ModuleBarGet,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  testReachable("module methods are included if called") {
    val source = """
      object Module {
        def foo: Unit = ()
      }

      object Test {
        def main(args: Array[String]): Unit = Module.foo
      }
    """
    val entry = TestMain
    val reachable = Seq(
      Module,
      ModuleInit,
      ModuleFoo,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }
}
