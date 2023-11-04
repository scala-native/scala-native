package scala.scalanative

object NirGenSymbols {

  val serializable = nir.Global.Top("java.io.Serializable")

  val jlClass = nir.Global.Top("java.lang.Class")

  val jlClassRef = nir.Type.Ref(jlClass)

  val jlObject = nir.Global.Top("java.lang.Object")

  val jlObjectRef = nir.Type.Ref(jlObject)

  val tuple2 = nir.Global.Top("scala.Tuple2")

  val tuple2Ref = nir.Type.Ref(tuple2)

  val srAbstractFunction0 =
    nir.Global.Top("scala.runtime.AbstractFunction0")

  val srAbstractFunction1 =
    nir.Global.Top("scala.runtime.AbstractFunction1")

}
