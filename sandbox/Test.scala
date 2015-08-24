package test

class C {
  def foo(x: Any): Any = x match {
    case x: Int => x
    case x: Long => x
    case _ => 0
  }
}
