package test

sealed abstract class A
final case class Add(x: Int, y: Int) extends A
final case class Mul(x: Int, y: Int) extends A

class Test {
  def foo(x: A) = {
    def loop(x: Int, acc: Int): Int =
      if (x <= 1) acc
      else { println(acc); loop(x - 1, acc * x) }
    loop(5, 1)
  }
}
