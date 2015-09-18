package test

sealed abstract class A
final case class Add(x: Int, y: Int) extends A
final case class Mul(x: Int, y: Int) extends A

class Test {
  def foo(x: A) = x match {
    case Add(x, y) => x + y
    case Mul(x, y) => x * y
  }
}
