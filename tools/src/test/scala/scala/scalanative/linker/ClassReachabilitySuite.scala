package scala.scalanative.linker

import org.scalatest._
import scalanative.nir.Global

class ClassReachabilitySuite extends ReachabilitySuite {
  val Test         = g("Test$")
  val TestInit     = g("Test$", "init")
  val TestMain     = g("Test$", "main_unit")
  val TestCallFoo  = g("Test$", "callFoo_Parent_unit")
  val Parent       = g("Parent")
  val ParentInit   = g("Parent", "init")
  val ParentFoo    = g("Parent", "foo_unit")
  val ParentBar    = g("Parent", "field.bar")
  val ParentBarSet = g("Parent", "bar$underscore$=_i32_unit")
  val ParentBarGet = g("Parent", "bar_i32")
  val Child        = g("Child")
  val ChildInit    = g("Child", "init")
  val ChildFoo     = g("Child", "foo_unit")
  val Object       = g("java.lang.Object")
  val ObjectInit   = g("java.lang.Object", "init")

  testReachable("unused classes are discarded") {
    val source = """
      class Parent
      class Child extends Parent

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

  testReachable("unused class methods are discarded") {
    val source = """
      class Parent {
        def foo: Unit = ()
      }

      object Test {
        def main: Unit = new Parent
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Parent,
      ParentInit,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("unused class vars are discarded") {
    val source = """
      class Parent {
        var bar: Int = _
      }

      object Test {
        def main: Unit = new Parent
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Parent,
      ParentInit,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("class without parent allocated") {
    val source = """
      class Parent
      class Child extends Parent

      object Test {
        def main: Unit = {
          new Parent
        }
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Parent,
      ParentInit,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("allocating a class includes both the class and its parent") {
    val source = """
      class Parent
      class Child extends Parent

      object Test {
        def main: Unit = new Child
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Child,
      ChildInit,
      Parent,
      ParentInit,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on parent, with neither child nor parent allocated, discards both impls") {
    val source = """
      class Parent {
        def foo: Unit = ()
      }
      class Child extends Parent {
        override def foo: Unit = ()
      }

      object Test {
        def callFoo(obj: Parent): Unit =
          obj.foo
        def main: Unit =
          callFoo(null)
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      TestCallFoo,
      Parent,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on parent, with only child allocated, discards parent impl") {
    val source = """
      class Parent {
        def foo: Unit = ()
      }
      class Child extends Parent {
        override def foo: Unit = ()
      }

      object Test {
        def callFoo(obj: Parent): Unit =
          obj.foo
        def main: Unit =
          callFoo(new Child)
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      TestCallFoo,
      Child,
      ChildInit,
      ChildFoo,
      Parent,
      ParentInit,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on parent, with only parent allocated, discards child impl") {
    val source = """
      class Parent {
        def foo: Unit = ()
      }
      class Child extends Parent {
        override def foo: Unit = ()
      }

      object Test {
        def callFoo(obj: Parent): Unit =
          obj.foo
        def main: Unit =
          callFoo(new Parent)
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      TestCallFoo,
      Parent,
      ParentInit,
      ParentFoo,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on parent, with both child and parent allocated, loads both impls") {
    val source = """
      class Parent {
        def foo: Unit = ()
      }
      class Child extends Parent {
        override def foo: Unit = ()
      }

      object Test {
        def callFoo(obj: Parent): Unit =
          obj.foo
        def main: Unit = {
          callFoo(new Parent)
          callFoo(new Child)
        }
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      TestCallFoo,
      Child,
      ChildInit,
      ChildFoo,
      Parent,
      ParentInit,
      ParentFoo,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("class vars are included if written to") {
    val source = """
      class Parent {
        var bar: Int = _
      }

      object Test {
        def main: Unit = {
          val p = new Parent
          p.bar = 42
        }
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Parent,
      ParentInit,
      ParentBar,
      ParentBarSet,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable("class vars are included if read from") {
    val source = """
      class Parent {
        var bar: Int = _
      }

      object Test {
        def main: Unit = {
          val p = new Parent
          p.bar
        }
      }
    """
    val entry  = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      TestMain,
      Parent,
      ParentInit,
      ParentBar,
      ParentBarGet,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }
}
