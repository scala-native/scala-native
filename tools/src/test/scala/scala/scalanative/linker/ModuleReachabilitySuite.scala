package scala.scalanative.linker

import org.scalatest._
import scalanative.nir.Global

class ModuleReachabilitySuite extends ReachabilitySuite {
  val sources = Seq("""
    object Module {
      def meth: Unit = ()
    }
  """)

  val Test         = g("Test$")
  val TestInit     = g("Test$", "init")
  val TestMain     = g("Test$", "main_unit")
  val Module       = g("Module$")
  val ModuleInit   = g("Module$", "init")
  val ModuleFoo    = g("Module$", "foo_unit")
  val ModuleBar    = g("Module$", "field.bar")
  val ModuleBarSet = g("Module$", "bar$underscore$=_i32_unit")
  val ModuleBarGet = g("Module$", "bar_i32")
  val Parent       = g("Parent")
  val ParentInit   = g("Parent", "init")
  val ParentFoo    = g("Parent", "foo_unit")
  val Trait        = g("Trait")
  val Object       = g("java.lang.Object")
  val ObjectInit   = g("java.lang.Object", "init")

  testReachable("unused modules are discarded") {
    val source = """
      object Module

      object Test {
        def main: Unit = ()
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("unused module vars are discarded") {
    val source = """
      object Module {
        var bar: Int = _
      }

      object Test {
        def main: Unit = Module
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Module,
      ModuleInit,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("unused module defs are discarded") {
    val source = """
      object Module {
        def foo: Unit = ()
      }

      object Test {
        def main: Unit = Module
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Module,
      ModuleInit,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("used modules are included") {
    val source = """
      object Module

      object Test {
        def main: Unit = Module
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Module,
      ModuleInit,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("used module parents are included") {
    val source = """
      class Parent

      object Module extends Parent {
        def foo: Unit = ()
      }

      object Test {
        def main: Unit = Module
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Module,
      ModuleInit,
      Parent,
      ParentInit,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("used module traits are included") {
    val source = """
      trait Trait

      object Module extends Trait {
        def foo: Unit = ()
      }

      object Test {
        def main: Unit = Module
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Module,
      ModuleInit,
      Trait,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("module vars are included if written to") {
    val source = """
      object Module {
        var bar: Int = _
      }

      object Test {
        def main: Unit = { Module.bar = 42 }
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Module,
      ModuleInit,
      ModuleBar,
      ModuleBarSet,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("module vars are included if read from") {
    val source = """
      object Module {
        var bar: Int = _
      }

      object Test {
        def main: Unit = Module.bar
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Module,
      ModuleInit,
      ModuleBar,
      ModuleBarGet,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("module methods are included if called") {
    val source = """
      object Module {
        def foo: Unit = ()
      }

      object Test {
        def main: Unit = Module.foo
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Module,
      ModuleInit,
      ModuleFoo,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }
}
