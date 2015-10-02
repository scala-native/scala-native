package test

abstract class Abstract {
  def foo: Int
}

class Concrete extends Abstract {
  def foo = 2
}

object Module {
  private[this] var x: Abstract = _
  def main = {
    x = new Concrete
    x.foo
  }
}
