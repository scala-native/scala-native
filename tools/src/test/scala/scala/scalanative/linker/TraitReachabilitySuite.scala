package scala.scalanative
package linker

import scala.scalanative.NativePlatform

import org.junit.Test
import org.junit.Assert.*

class TraitReachabilitySuite extends ReachabilitySuite {
  val TestClsName = "Test"
  val TestModuleName = "Test$"
  val ChildClsName = "Child"
  val GrandChildClsName = "GrandChild"
  val ParentClsName = "Parent"
  val ParentClassClsName = "Parent$class"
  val ObjectClsName = "java.lang.Object"
  val ScalaMainNonStaticSig =
    nir.Sig.Method("main", nir.Rt.ScalaMainSig.types, nir.Sig.Scope.Public)

  val Parent: nir.Global.Top = g(ParentClsName)
  // Scala 2.12.x
  val ParentInit: nir.Global =
    g(ParentClsName, nir.Sig.Method("$init$", Seq(nir.Type.Unit)))
  val ParentMain: nir.Global = g(ParentClsName, ScalaMainNonStaticSig)
  val ParentFoo: nir.Global =
    g(ParentClsName, nir.Sig.Method("foo", Seq(nir.Type.Unit)))

  val Child: nir.Global = g(ChildClsName)
  val ChildInit: nir.Global = g(ChildClsName, nir.Sig.Ctor(Seq.empty))
  val ChildFoo: nir.Global =
    g(ChildClsName, nir.Sig.Method("foo", Seq(nir.Type.Unit)))
  val GrandChild: nir.Global = g(GrandChildClsName)
  val GrandChildInit: nir.Global = g(GrandChildClsName, nir.Sig.Ctor(Seq.empty))
  val GrandChildFoo: nir.Global =
    g(GrandChildClsName, nir.Sig.Method("foo", Seq(nir.Type.Unit)))
  val Object: nir.Global = g(ObjectClsName)
  val ObjectInit: nir.Global = g(ObjectClsName, nir.Sig.Ctor(Seq.empty))
  val Test: nir.Global = g(TestClsName)
  val TestModule: nir.Global = g(TestModuleName)
  val TestInit: nir.Global = g(TestModuleName, nir.Sig.Ctor(Seq.empty))
  val TestMain: nir.Global = g(TestClsName, nir.Rt.ScalaMainSig)
  val TestModuleMain: nir.Global = g(TestModuleName, ScalaMainNonStaticSig)
  val TestCallFoo: nir.Global =
    g(
      TestModuleName,
      nir.Sig.Method("callFoo", Seq(nir.Type.Ref(Parent), nir.Type.Unit))
    )
  val commonReachable =
    Seq(Test, TestModule, TestInit, TestMain, TestModuleMain)

  @Test def unusedTrait(): Unit = testReachable() {
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

  @Test def inheritedTrait(): Unit = testReachable() {
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

  @Test def traitMethodImplementors(): Unit = testReachable() {
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

  @Test def traitMethodImplementors2(): Unit =
    testReachable() {
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

  @Test def traitMethodDefaultImplementation(): Unit = testReachable() {
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

  @Test def traitMethodDefaultImplementation2(): Unit = testReachable() {
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
  @Test def inheritedMainMethod(): Unit = testReachable() {
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
