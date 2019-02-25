package scala.scalanative.linker

import org.scalatest._
import scalanative.nir.{Sig, Type, Global}

class ClassReachabilitySuite extends ReachabilitySuite {
  val Parent       = g("Parent")
  val ParentInit   = g("Parent", Sig.Ctor(Seq.empty))
  val ParentFoo    = g("Parent", Sig.Method("foo", Seq(Type.Unit)))
  val ParentBar    = g("Parent", Sig.Field("bar"))
  val ParentBarSet = g("Parent", Sig.Method("bar_=", Seq(Type.Int, Type.Unit)))
  val ParentBarGet = g("Parent", Sig.Method("bar", Seq(Type.Int)))
  val Child        = g("Child")
  val ChildInit    = g("Child", Sig.Ctor(Seq.empty))
  val ChildFoo     = g("Child", Sig.Method("foo", Seq(Type.Unit)))
  val Object       = g("java.lang.Object")
  val ObjectInit   = g("java.lang.Object", Sig.Ctor(Seq.empty))
  val Test         = g("Test$")
  val TestInit     = g("Test$", Sig.Ctor(Seq.empty))
  val TestMain     = g("Test$", Sig.Method("main", Seq(Type.Unit)))
  val TestCallFoo =
    g("Test$", Sig.Method("callFoo", Seq(Type.Ref(Parent), Type.Unit)))

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
