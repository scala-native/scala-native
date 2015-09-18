package test

class Test {
  def bar(x: Int) = x
  def foo(x: Int) = {
    try println("try")
    catch {
      case e: Exception =>
        println("catch")
        throw e
    }
    finally {
      println("finally")
    }
  }
}
