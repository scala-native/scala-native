package scala.scalanative
package profiling

sealed trait Event
object Event {
  case class Call(ptr: Long, argc: Int)            extends Event
  case class Load(ptr: Long)                       extends Event
  case class Store(ptr: Long)                      extends Event
  case class Classalloc(typeid: Int, name: String) extends Event
  case class Method(actualTypeid: Int, scopeTypeid: Int, name: String)
      extends Event
  case class As(fromTypeId: Int, toTypeId: Int, obj: Long) extends Event
  case class Is(typeId: Int, expected: Int, obj: Long)     extends Event
  case class Box(toTypeId: Int)                            extends Event
  case class Unbox(fromTypeId: Int, obj: Long)             extends Event
}
