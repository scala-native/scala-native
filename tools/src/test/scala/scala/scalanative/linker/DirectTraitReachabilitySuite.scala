package scala.scalanative.linker

import org.scalatest._
import scalanative.nir.Global

class DirectTraitReachabilitySuite extends ReachabilitySuite {
  val sources = Seq("""
    trait Parent {
      def meth: Unit
    }

    class Child extends Parent {
      def meth: Unit = ()
    }
  """)

  val Parent     = g("Parent")
  val ParentMeth = g("Parent", "meth_unit")
  val Child      = g("Child")
  val ChildInit  = g("Child", "init")
  val ChildMeth  = g("Child", "meth_unit")
  val Object     = g("java.lang.Object")
  val ObjectInit = g("java.lang.Object", "init")

  testReachable("trait")(Parent)(Parent)
  testReachable("trait method")(ParentMeth)(Parent, ParentMeth)
  testReachable("trait method override")(ChildMeth)(Child,
                                                    ChildMeth,
                                                    Parent,
                                                    ParentMeth,
                                                    Object)
  testReachable("trait inherited")(Child)(Child, Parent, Object)
  testReachable("trait meth + inherited init")(ParentMeth, ChildInit)(
    Parent,
    ParentMeth,
    Child,
    ChildInit,
    ChildMeth,
    Object,
    ObjectInit)
}
