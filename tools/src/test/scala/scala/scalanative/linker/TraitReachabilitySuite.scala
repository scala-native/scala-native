package scala.scalanative.linker

import org.scalatest._
import scalanative.util.Platform.scalaVersion
import scalanative.nir.{Type, Sig, Global}

class TraitReachabilitySuite extends ReachabilitySuite {
  val Parent = g("Parent")

  // Scala 2.11.x
  val ParentClass = g("Parent$class")
  val ParentClassInit =
    g("Parent$class", Sig.Method("$init$", Seq(Type.Ref(Parent), Type.Unit)))
  val ParentClassFoo =
    g("Parent$class", Sig.Method("foo", Seq(Type.Ref(Parent), Type.Unit)))

  // Scala 2.12.x
  val ParentInit =
    g("Parent", Sig.Method("$init$", Seq(Type.Unit)))
  val ParentFoo =
    g("Parent", Sig.Method("foo", Seq(Type.Unit)))

  val Child          = g("Child")
  val ChildInit      = g("Child", Sig.Ctor(Seq.empty))
  val ChildFoo       = g("Child", Sig.Method("foo", Seq(Type.Unit)))
  val GrandChild     = g("GrandChild")
  val GrandChildInit = g("GrandChild", Sig.Ctor(Seq.empty))
  val GrandChildFoo  = g("GrandChild", Sig.Method("foo", Seq(Type.Unit)))
  val Object         = g("java.lang.Object")
  val ObjectInit     = g("java.lang.Object", Sig.Ctor(Seq.empty))
  val Test           = g("Test$")
  val TestInit       = g("Test$", Sig.Ctor(Seq.empty))
  val TestMain       = g("Test$", Sig.Method("main", Seq(Type.Unit)))
  val TestCallFoo =
    g("Test$", Sig.Method("callFoo", Seq(Type.Ref(Parent), Type.Unit)))

  testReachable("unused traits are discarded") {
    val source = """
      trait Parent
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

  testReachable("inherited trait is included") {
    val source = """
      trait Parent
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
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on trait includes an impl of all implementors (1)") {
    val source = """
      trait Parent {
        def foo: Unit
      }
      class Child extends Parent {
        def foo: Unit = ()
      }

      object Test {
        def callFoo(parent: Parent): Unit =
          parent.foo
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
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on trait includes an impl of all implementors (2)") {
    val source = """
      trait Parent {
        def foo: Unit
      }
      class Child extends Parent {
        def foo: Unit = ()
      }
      class GrandChild extends Child {
        override def foo: Unit = ()
      }

      object Test {
        def callFoo(parent: Parent): Unit =
          parent.foo
        def main: Unit = {
          callFoo(new Child)
          callFoo(new GrandChild)
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
      GrandChild,
      GrandChildInit,
      GrandChildFoo,
      Parent,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on a trait with default implementation includes impl class") {
    val source = """
      trait Parent {
        def foo: Unit = ()
      }
      class Child extends Parent

      object Test {
        def callFoo(parent: Parent): Unit =
          parent.foo
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
      Object,
      ObjectInit
    ) ++ {
      if (scalaVersion.startsWith("2.11")) {
        Seq(
          Parent,
          ParentClass,
          ParentClassInit,
          ParentClassFoo
        )
      } else {
        Seq(
          Parent,
          ParentInit,
          ParentFoo
        )
      }
    }
    (source, entry, reachable)
  }

  testReachable(
    "calling a method on a trait with default implementation discards impl class") {
    val source = """
      trait Parent {
        def foo: Unit = ()
      }
      class Child extends Parent {
        override def foo: Unit = ()
      }

      object Test {
        def callFoo(parent: Parent): Unit =
          parent.foo
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
      Object,
      ObjectInit
    ) ++ {
      if (scalaVersion.startsWith("2.11")) {
        Seq(
          Parent,
          ParentClass,
          ParentClassInit
        )
      } else {
        Seq(
          Parent,
          ParentInit
        )
      }
    }
    (source, entry, reachable)
  }
}
