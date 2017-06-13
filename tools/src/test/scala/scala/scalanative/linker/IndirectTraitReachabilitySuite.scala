package scala.scalanative.linker

import org.scalatest._
import scalanative.nir.Global

class IndirectTraitReachabilitySuite extends ReachabilitySuite {
  val sources = Seq("""
    trait Indirect {
      def meth: Unit
    }
    class Parent {
      def meth: Unit = ()
    }
    class Child extends Parent with Indirect
  """)

  val Parent       = g("Parent")
  val ParentInit   = g("Parent", "init")
  val ParentMeth   = g("Parent", "meth_unit")
  val Child        = g("Child")
  val ChildInit    = g("Child", "init")
  val Object       = g("java.lang.Object")
  val ObjectInit   = g("java.lang.Object", "init")
  val Indirect     = g("Indirect")
  val IndirectMeth = g("Indirect", "meth_unit")

  testReachable("child init + trait meth")(ChildInit, IndirectMeth)(
    Child,
    ChildInit,
    Parent,
    ParentInit,
    ParentMeth,
    Object,
    ObjectInit,
    Indirect,
    IndirectMeth)
}
