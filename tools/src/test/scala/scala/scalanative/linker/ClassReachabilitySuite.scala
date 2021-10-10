package scala.scalanative.linker

import org.scalatest._
import scalanative.nir.{Sig, Type, Global, Rt}

class ClassReachabilitySuite extends ReachabilitySuite {
  val TestClsName = "Test$"
  val ChildClsName = "Child"
  val ParentClsName = "Parent"
  val ObjectClsName = "java.lang.Object"

  val Parent = g(ParentClsName)
  val ParentInit = g(ParentClsName, Sig.Ctor(Seq.empty))
  val ParentFoo = g(ParentClsName, Sig.Method("foo", Seq(Type.Unit)))
  val ParentBar = g(ParentClsName, Sig.Field("bar"))
  val ParentBarSet =
    g(ParentClsName, Sig.Method("bar_=", Seq(Type.Int, Type.Unit)))
  val ParentBarGet = g(ParentClsName, Sig.Method("bar", Seq(Type.Int)))
  val ParentMain = g(ParentClsName, Rt.ScalaMainSig)
  val Child = g(ChildClsName)
  val ChildInit = g(ChildClsName, Sig.Ctor(Seq.empty))
  val ChildFoo = g(ChildClsName, Sig.Method("foo", Seq(Type.Unit)))
  val Object = g(ObjectClsName)
  val ObjectInit = g(ObjectClsName, Sig.Ctor(Seq.empty))
  val Test = g(TestClsName)
  val TestInit = g(TestClsName, Sig.Ctor(Seq.empty))
  val TestMain = g(TestClsName, Rt.ScalaMainSig)
  val TestCallFoo =
    g(TestClsName, Sig.Method("callFoo", Seq(Type.Ref(Parent), Type.Unit)))

  testReachable("unused classes are discarded") {
    val source = """
      class Parent
      class Child extends Parent

      object Test {
        def main(args: Array[String]): Unit = ()
      }
    """
    val entry = TestMain
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
        def main(args: Array[String]): Unit = new Parent
      }
    """
    val entry = TestMain
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
        def main(args: Array[String]): Unit = new Parent
      }
    """
    val entry = TestMain
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
        def main(args: Array[String]): Unit = {
          new Parent
        }
      }
    """
    val entry = TestMain
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
        def main(args: Array[String]): Unit = new Child
      }
    """
    val entry = TestMain
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
    "calling a method on parent, with neither child nor parent allocated, discards both impls"
  ) {
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
        def main(args: Array[String]): Unit =
          callFoo(null)
      }
    """
    val entry = TestMain
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
    "calling a method on parent, with only child allocated, discards parent impl"
  ) {
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
        def main(args: Array[String]): Unit =
          callFoo(new Child)
      }
    """
    val entry = TestMain
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
    "calling a method on parent, with only parent allocated, discards child impl"
  ) {
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
        def main(args: Array[String]): Unit =
          callFoo(new Parent)
      }
    """
    val entry = TestMain
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
    "calling a method on parent, with both child and parent allocated, loads both impls"
  ) {
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
        def main(args: Array[String]): Unit = {
          callFoo(new Parent)
          callFoo(new Child)
        }
      }
    """
    val entry = TestMain
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
        def main(args: Array[String]): Unit = {
          val p = new Parent
          p.bar = 42
        }
      }
    """
    val entry = TestMain
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
        def main(args: Array[String]): Unit = {
          val p = new Parent
          p.bar
        }
      }
    """
    val entry = TestMain
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

  // Issue #805
  testReachable("inherited main methods are reachable") {
    val source = """
      abstract class Parent {
        def main(args: Array[String]): Unit = ()
      }

      object Test extends Parent
      """
    val entry = TestMain
    val reachable = Seq(
      Test,
      TestInit,
      Parent,
      ParentInit,
      ParentMain,
      Object,
      ObjectInit
    )
    (source, entry, reachable)
  }
}
