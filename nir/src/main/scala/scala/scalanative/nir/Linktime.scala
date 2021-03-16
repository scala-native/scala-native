package scala.scalanative.nir

sealed trait LinktimeCondition {
  def position: Position
}

object LinktimeCondition {

  case class SimpleCondition(name: Global, comparison: Comp, value: Val)(
      implicit val position: Position)
      extends LinktimeCondition

  case class ComplexCondition(
      op: Bin,
      left: LinktimeCondition,
      right: LinktimeCondition)(implicit val position: Position)
      extends LinktimeCondition

  object Tag {
    final val SimpleCondition  = 1
    final val ComplexCondition = 2
  }

}

object Linktime {

  final val Linktime = Global.Top("scala.scalanative.linktime")

  // Artificial function, never actually called.
  // Takes Global for constant struct describing linktime property.
  // Replaced with resolved value at link-time.
  final val PropertyResolveFunctionName: Global.Member =
    Linktime.member(Sig.Method("resolveProperty", Seq(Type.Ptr, Type.Nothing)))

  final def PropertyResolveFunctionTy(retty: Type): Type.Function =
    Type.Function(Seq(Type.Ptr), retty)

  final def PropertyResolveFunction(retty: Type): Val.Global =
    Val.Global(PropertyResolveFunctionName, PropertyResolveFunctionTy(retty))

  def nameToLinktimePropertyName(name: Global): Global.Member = {
    val Global.Member(owner, sig) = name
    require(sig.isMethod, s"Expected method, but received ${sig.unmangled}")

    val Sig.Method(ident, _, _) = sig.unmangled
    owner.member(Sig.Generated(s"${ident}_property"))
  }
}
