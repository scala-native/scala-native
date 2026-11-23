package testlib

sealed trait ADT
object ADT {
  case object SingletonCase extends ADT
  case class ClassCase(x: String)
}
