package scala.scalanative.nir

sealed trait LinktimeCondition

object LinktimeCondition {

  case class SimpleCondition(name: Global, comparison: Comp, value: Val)
      extends LinktimeCondition

  case class ComplexCondition(op: Bin,
                              left: LinktimeCondition,
                              right: LinktimeCondition)
      extends LinktimeCondition

  object Tag {
    final val SimpleCondition  = 1
    final val ComplexCondition = 2
  }
}
