package a

class A {
  var x = 2
  def foo(capture: A) = {
    def factorial(n: Int): Int =  {
      @scala.annotation.tailrec
      def loop(n: Int)(acc: Int): Int = {
        println(capture)
        loop(n - 1)(n * acc);
      }
      return loop(n)(1);
    }
    factorial(3)
  }
}
