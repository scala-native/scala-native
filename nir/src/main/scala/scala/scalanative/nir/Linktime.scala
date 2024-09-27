package scala.scalanative.nir

sealed trait LinktimeCondition {
  def position: SourcePosition
}

object LinktimeCondition {

  case class SimpleCondition(
      propertyName: String,
      comparison: Comp,
      value: Val
  )(implicit val position: SourcePosition)
      extends LinktimeCondition

  case class ComplexCondition(
      op: Bin,
      left: LinktimeCondition,
      right: LinktimeCondition
  )(implicit val position: SourcePosition)
      extends LinktimeCondition

  object Tag {
    final val SimpleCondition = 1
    final val ComplexCondition = 2
  }

}

private[scalanative] object Linktime {

  final val Linktime = Global.Top("scala.scalanative.linktime")

  // Artificial function, never actually called.
  // Takes Global for constant struct describing linktime property.
  // Replaced with resolved value at link-time.
  final val PropertyResolveFunctionName: Global.Member =
    Linktime.member(
      Sig.Method("resolveProperty", Seq(Rt.String, Rt.RuntimeNothing))
    )

  final def PropertyResolveFunctionTy(retty: Type): Type.Function =
    Type.Function(Seq(Rt.String), retty)

  final def PropertyResolveFunction(retty: Type): Val.Global =
    Val.Global(PropertyResolveFunctionName, PropertyResolveFunctionTy(retty))
}
