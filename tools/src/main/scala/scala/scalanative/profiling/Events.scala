package scala.scalanative
package profiling

sealed trait Event
object Event {
  case class Call(typeid: Int, methid: Int) extends Event
  case object Load                          extends Event
  case object Store                         extends Event
  case class Classalloc(typeid: Int)        extends Event
  case class Method(actualTypeid: Int, scopeTypeid: Int, methid: Int)
      extends Event
  case class As(fromTypeId: Int, toTypeId: Int) extends Event
  case class Is(typeId: Int, expected: Int)     extends Event
  case class Box(toTypeId: Int)                 extends Event
  case class Unbox(fromTypeId: Int)             extends Event
}
