package scala.scalanative.runtime

import scala.scalanative.unsafe._

object GCInfo {

  sealed trait GCType
  case class Commix() extends GCType
  case class Immix() extends GCType
  case class None() extends GCType
  case class Boehm() extends GCType
  case class Unknown() extends GCType

  def getType(): GCType =
    fromCString(GC.getType()) match {
      case "boehm"  => Boehm()
      case "immix"  => Immix()
      case "commix" => Commix()
      case "none"   => None()
      case _        => Unknown()
    }
}
