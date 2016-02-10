package test

class C {
  override def equals(other: Any) = false
  override def hashCode = 0
  override def toString = "hi"
}

object Test {
  def main(args: Array[String]): Unit = new C
}
