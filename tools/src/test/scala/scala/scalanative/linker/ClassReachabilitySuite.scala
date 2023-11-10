package scala.scalanative
package linker

import org.junit.Test
import org.junit.Assert._

class ClassReachabilitySuite extends ReachabilitySuite {
  val TestClsName = "Test"
  val TestModuleName = "Test$"
  val ChildClsName = "Child"
  val ParentClsName = "Parent"
  val ObjectClsName = "java.lang.Object"
  val ScalaMainNonStaticSig =
    nir.Sig.Method("main", nir.Rt.ScalaMainSig.types, nir.Sig.Scope.Public)

  val Parent = g(ParentClsName)
  val ParentInit = g(ParentClsName, nir.Sig.Ctor(Seq.empty))
  val ParentFoo = g(ParentClsName, nir.Sig.Method("foo", Seq(nir.Type.Unit)))
  val ParentBar = g(ParentClsName, nir.Sig.Field("bar"))
  val ParentBarSet =
    g(
      ParentClsName,
      nir.Sig.Method("bar_$eq", Seq(nir.Type.Int, nir.Type.Unit))
    )
  val ParentBarGet = g(ParentClsName, nir.Sig.Method("bar", Seq(nir.Type.Int)))
  val ParentMain = g(ParentClsName, ScalaMainNonStaticSig)
  val Child = g(ChildClsName)
  val ChildInit = g(ChildClsName, nir.Sig.Ctor(Seq.empty))
  val ChildFoo = g(ChildClsName, nir.Sig.Method("foo", Seq(nir.Type.Unit)))
  val Object = g(ObjectClsName)
  val ObjectInit = g(ObjectClsName, nir.Sig.Ctor(Seq.empty))
  val Test = g(TestClsName)
  val TestModule = g(TestModuleName)
  val TestInit = g(TestModuleName, nir.Sig.Ctor(Seq.empty))
  val TestMain = g(TestClsName, nir.Rt.ScalaMainSig)
  val TestModuleMain = g(TestModuleName, ScalaMainNonStaticSig)
  val TestCallFoo =
    g(
      TestModuleName,
      nir.Sig.Method("callFoo", Seq(nir.Type.Ref(Parent), nir.Type.Unit))
    )

  val commonReachable =
    Seq(Test, TestModule, TestInit, TestMain, TestModuleMain)

  @Test def unusedClasses(): Unit = testReachable() {
    val source = """
      class Parent
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

  @Test def unusedMethods(): Unit = testReachable() {
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
      Parent,
      ParentInit,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  @Test def unusedVars(): Unit =
    testReachable() {
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
        Parent,
        ParentInit,
        Object,
        ObjectInit
      )
      (source, entry, commonReachable ++ reachable)
    }

  @Test def classWithoutParentAllocation(): Unit =
    testReachable() {
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
        Parent,
        ParentInit,
        Object,
        ObjectInit
      )
      (source, entry, commonReachable ++ reachable)
    }

  @Test def allocatingClass(): Unit = testReachable() {
    val source = """
      class Parent
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
      ParentInit,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  @Test def callParentMethodUnallocated(): Unit = testReachable() {
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
      TestCallFoo,
      Parent,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  @Test def callParentMethodChildAllocated(): Unit = testReachable() {
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
      TestCallFoo,
      Child,
      ChildInit,
      ChildFoo,
      Parent,
      ParentInit,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  @Test def callParentMethodParentAllocated(): Unit = testReachable() {
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
      TestCallFoo,
      Parent,
      ParentInit,
      ParentFoo,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  @Test def callParentMethodBothAllocated(): Unit = testReachable() {
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
    (source, entry, commonReachable ++ reachable)
  }

  @Test def classVarWritten(): Unit = testReachable() {
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
      Parent,
      ParentInit,
      ParentBar,
      ParentBarSet,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  @Test def classVarsRead(): Unit = testReachable() {
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
      Parent,
      ParentInit,
      ParentBar,
      ParentBarGet,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable ++ reachable)
  }

  // Issue #805
  @Test def inheritedMainMethod(): Unit = testReachable() {
    val source = """
      abstract class Parent {
        def main(args: Array[String]): Unit = ()
      }

      object Test extends Parent
      """
    val entry = TestMain
    val reachable = Seq(
      Parent,
      ParentInit,
      ParentMain,
      Object,
      ObjectInit
    )
    (source, entry, commonReachable.diff(Seq(TestModuleMain)) ++ reachable)
  }
}
