package scala.scalanative.linker

import org.scalatest._
import scalanative.nir.Global

class ClassReachabilitySuite extends ReachabilitySuite {
  val sources = Seq("""
    class Parent {
      def meth1: Unit = ()
      def meth2: Unit = ()
    }

    class Child extends Parent {
      override def meth2: Unit = ()
    }
  """)

  val Parent      = g("Parent")
  val ParentInit  = g("Parent", "init")
  val ParentMeth1 = g("Parent", "meth1_unit")
  val ParentMeth2 = g("Parent", "meth2_unit")
  val Child       = g("Child")
  val ChildInit   = g("Child", "init")
  val ChildMeth2  = g("Child", "meth2_unit")
  val Object      = g("java.lang.Object")
  val ObjectInit  = g("java.lang.Object", "init")

  testReachable("parent class")(Parent)(Parent, Object)
  testReachable("parent meth1")(ParentMeth1)(Parent, ParentMeth1, Object)
  testReachable("parent meth2")(ParentMeth2)(Parent, ParentMeth2, Object)
  testReachable("parent init")(ParentInit)(Parent,
                                           ParentInit,
                                           Object,
                                           ObjectInit)
  testReachable("child class")(Child)(Child, Parent, Object)
  testReachable("child meth2")(ChildMeth2)(Child,
                                           ChildMeth2,
                                           Parent,
                                           ParentMeth2,
                                           Object)
  testReachable("child init")(ChildInit)(Child,
                                         ChildInit,
                                         Parent,
                                         ParentInit,
                                         Object,
                                         ObjectInit)
  testReachable("child init + parent meth2")(ChildInit, ParentMeth2)(
    Child,
    ChildInit,
    ChildMeth2,
    Parent,
    ParentInit,
    ParentMeth2,
    Object,
    ObjectInit)
}
