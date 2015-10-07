package salty.ir

sealed abstract class Shape {
  final def holes: Int = this match {
    case Shape.Hole         => 1
    case Shape.Ref(shape)   => shape.holes
    case Shape.Slice(shape) => shape.holes
  }

  final override def toString = this match {
    case Shape.Hole         => "•"
    case Shape.Ref(shape)   => s"$shape!"
    case Shape.Slice(shape) => s"$shape[]"
  }
}
object Shape {
  final case object Hole extends Shape
  final case class Ref(of: Shape) extends Shape
  final case class Slice(of: Shape) extends Shape
  // TODO: Ptr(t)
  // TODO: Func(ret, args)
  // TODO: Struct(fields)
  // TODO: Array(t, n)
}

