package scala.scalanative

import scala.scalanative.nir.Global
import scala.scalanative.nir.Type

object NirGenSymbols {
  val serializable = Global.Top("java.io.Serializable")

  val jlClass    = Global.Top("java.lang.Class")
  val jlClassRef = Type.Ref(jlClass)

  val jlObject    = Global.Top("java.lang.Object")
  val jlObjectRef = Type.Ref(jlObject)

  val tuple2    = Global.Top("scala.Tuple2")
  val tuple2Ref = Type.Ref(tuple2)

  val srAbstractFunction0 =
    Global.Top("scala.runtime.AbstractFunction0")
  val srAbstractFunction1 =
    Global.Top("scala.runtime.AbstractFunction1")
}
