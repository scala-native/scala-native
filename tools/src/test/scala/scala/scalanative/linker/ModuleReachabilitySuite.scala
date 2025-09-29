package scala.scalanative
package linker

import org.junit.Assert._
import org.junit.Test

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
    nir.Sig.Method("main", nir.Rt.ScalaMainSig.types, nir.Sig.Scope.Public)

  val Test = g(TestClsName)
  val TestModule = g(TestModuleName)
  val TestInit = g(TestModuleName, nir.Sig.Ctor(Seq.empty))
  val TestMain = g(TestClsName, nir.Rt.ScalaMainSig)
  val TestModuleMain = g(TestModuleName, ScalaMainNonStaticSig)
  val Module = g(ModuleClsName)
  val ModuleInit = g(ModuleClsName, nir.Sig.Ctor(Seq.empty))
  val ModuleFoo = g(ModuleClsName, nir.Sig.Method("foo", Seq(nir.Type.Unit)))
  val ModuleBar = g(ModuleClsName, nir.Sig.Field("bar"))
  val ModuleBarSet =
    g(
      ModuleClsName,
      nir.Sig.Method("bar_$eq", Seq(nir.Type.Int, nir.Type.Unit))
    )
  val ModuleBarGet = g(ModuleClsName, nir.Sig.Method("bar", Seq(nir.Type.Int)))
  val Parent = g(ParentClsName)
  val ParentInit = g(ParentClsName, nir.Sig.Ctor(Seq.empty))
  val ParentFoo = g(ParentClsName, nir.Sig.Method("foo", Seq(nir.Type.Unit)))
  val Trait = g("Trait")
  val Object = g(ObjectClsName)
  val ObjectInit = g(ObjectClsName, nir.Sig.Ctor(Seq.empty))

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
