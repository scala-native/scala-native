package test

class C {
  def foo(x: Any): Boolean = x.asInstanceOf[String].isInstanceOf[String]
}
