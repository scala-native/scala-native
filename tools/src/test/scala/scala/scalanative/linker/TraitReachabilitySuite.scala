package scala.scalanative
package linker

import scala.scalanative.NativePlatform
import scala.scalanative.nir.{Global, Sig, Type, Rt}

class TraitReachabilitySuite extends ReachabilitySuite {
  val TestClsName = "Test"
  val TestModuleName = "Test$"
  val ChildClsName = "Child"
  val GrandChildClsName = "GrandChild"
  val ParentClsName = "Parent"
  val ParentClassClsName = "Parent$class"
  val ObjectClsName = "java.lang.Object"
  val ScalaMainNonStaticSig =
    Sig.Method("main", Rt.ScalaMainSig.types, Sig.Scope.Public)

  val Parent: Global = g(ParentClsName)
  // Scala 2.12.x
  val ParentInit: Global =
    g(ParentClsName, Sig.Method("$init$", Seq(Type.Unit)))
  val ParentMain: Global = g(ParentClsName, ScalaMainNonStaticSig)
  val ParentFoo: Global = g(ParentClsName, Sig.Method("foo", Seq(Type.Unit)))

  val Child: Global = g(ChildClsName)
  val ChildInit: Global = g(ChildClsName, Sig.Ctor(Seq.empty))
  val ChildFoo: Global = g(ChildClsName, Sig.Method("foo", Seq(Type.Unit)))
  val GrandChild: Global = g(GrandChildClsName)
  val GrandChildInit: Global = g(GrandChildClsName, Sig.Ctor(Seq.empty))
  val GrandChildFoo: Global =
    g(GrandChildClsName, Sig.Method("foo", Seq(Type.Unit)))
  val Object: Global = g(ObjectClsName)
  val ObjectInit: Global = g(ObjectClsName, Sig.Ctor(Seq.empty))
  val Test: Global = g(TestClsName)
  val TestModule: Global = g(TestModuleName)
  val TestInit: Global = g(TestModuleName, Sig.Ctor(Seq.empty))
  val TestMain: Global = g(TestClsName, Rt.ScalaMainSig)
  val TestModuleMain: Global = g(TestModuleName, ScalaMainNonStaticSig)
  val TestCallFoo: Global =
    g(TestModuleName, Sig.Method("callFoo", Seq(Type.Ref(Parent), Type.Unit)))
  val commonReachable =
    Seq(Test, TestModule, TestInit, TestMain, TestModuleMain)

  testReachable("unused traits are discarded") {
    val source = """
      trait Parent
      class Child extends Parent

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

  testReachable("inherited trait is included") {
    val source = """
      trait Parent
      class Child extends Parent

      object Test {
        def main(args: Array[String]): Unit = new Child
      }
    """
    val entry = TestMain
    val reachable = Seq(
      Child,
      ChildInit,
      Parent,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  testReachable(
    "calling a method on trait includes an impl of all implementors (1)"
  ) {
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
        def main(args: Array[String]): Unit =
          callFoo(new Child)
      }
    """
    val entry = TestMain
    val reachable = Seq(
      TestCallFoo,
      Child,
      ChildInit,
      ChildFoo,
      Parent,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  testReachable(
    "calling a method on trait includes an impl of all implementors (2)"
  ) {
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
        def main(args: Array[String]): Unit = {
          callFoo(new Child)
          callFoo(new GrandChild)
        }
      }
    """
    val entry = TestMain
    val reachable = Seq(
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
    (source, entry, commonReachable ++ reachable)
  }

  testReachable(
    "calling a method on a trait with default implementation includes impl class"
  ) {
    val source = """
      trait Parent {
        def foo: Unit = ()
      }
      class Child extends Parent

      object Test {
        def callFoo(parent: Parent): Unit =
          parent.foo
        def main(args: Array[String]): Unit =
          callFoo(new Child)
      }
    """
    val entry = TestMain
    val reachable = Seq(
      TestCallFoo,
      Child,
      ChildInit,
      ChildFoo,
      Object,
      ObjectInit
    ) ++ {
      if (NativePlatform.erasesEmptyTraitConstructor) {
        Seq(Parent, ParentFoo)
      } else {
        Seq(
          Parent,
          ParentInit,
          ParentFoo
        )
      }
    }
    (source, entry, commonReachable ++ reachable)
  }

  testReachable(
    "calling a method on a trait with default implementation discards impl class"
  ) {
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
        def main(args: Array[String]): Unit =
          callFoo(new Child)
      }
    """
    val entry = TestMain
    val reachable = Seq(
      TestCallFoo,
      Child,
      ChildInit,
      ChildFoo,
      Object,
      ObjectInit
    ) ++ {
      if (NativePlatform.erasesEmptyTraitConstructor) {
        Seq(Parent)
      } else {
        Seq(
          Parent,
          ParentInit
        )
      }
    }
    (source, entry, commonReachable ++ reachable)
  }

  // Issue #805
  testReachable("inherited main methods are reachable") {
    val source = """
       trait Parent {
         def main(args: Array[String]): Unit = ()
       }

       object Test extends Parent
       """
    val entry = TestMain
    val reachable =
      Seq(
        Parent,
        Object,
        ObjectInit
      ) ++ {
        if (NativePlatform.erasesEmptyTraitConstructor) {
          Seq(ParentMain, TestModuleMain)
        } else {
          Seq(
            ParentInit,
            ParentMain
          )
        }
      }
    (source, entry, commonReachable.diff(Seq(TestModuleMain)) ++ reachable)
  }
}
