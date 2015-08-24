package test

class C {
  private[this] var x: Int = _
  def foo(): Unit =
    this.x = b()
  def b() = 2
}
