package test

class A // 3..9
  class B extends A // 7..9
    class C extends B // 8..9
      class E extends C // 9..9
  class D extends A // 4..6
    class G extends D // 5..5
    class F extends D // 6..6

object Test { // 2
  def main(args: Array[String]): Unit = {
    new A
    new B
    new C
    new D
    new E
    new F
    new G
    ()
  }
}
