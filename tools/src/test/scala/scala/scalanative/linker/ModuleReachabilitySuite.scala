package scala.scalanative.linker

import org.scalatest._
import scalanative.nir.Global

class ModuleReachabilitySuite extends ReachabilitySuite {
  val sources = Seq("""
    object Module {
      def meth: Unit = ()
    }
  """)

  val Module     = g("Module$")
  val ModuleInit = g("Module$", "init")
  val ModuleMeth = g("Module$", "meth_unit")
  val Object     = g("java.lang.Object")
  val ObjectInit = g("java.lang.Object", "init")

  testReachable("module constructor")(Module)(Module,
                                              ModuleInit,
                                              Object,
                                              ObjectInit)
  testReachable("module member")(ModuleMeth)(Module,
                                             ModuleInit,
                                             ModuleMeth,
                                             Object,
                                             ObjectInit)
}
