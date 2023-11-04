package scala.scalanative.linker

import org.junit.Test
import org.junit.Assert._

import scalanative.nir.{Sig, Type, Rt}

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

  @Test def unusedModules(): Unit = testReachable() {
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

  @Test def unusedModuleVars(): Unit = testReachable() {
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

  @Test def unusedModuleDefs(): Unit = testReachable() {
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

  @Test def usedModules(): Unit = testReachable() {
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

  @Test def usedModuleParents(): Unit = testReachable() {
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

  @Test def usedModuleTraits(): Unit = testReachable() {
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

  @Test def moduleVarsWrite(): Unit = testReachable() {
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

  @Test def moduleVarsRead(): Unit = testReachable() {
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

  @Test def moduleMethodsCall(): Unit = testReachable() {
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
