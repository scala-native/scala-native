package salty.ir

class Fresh {
  private var i: Int = 0
  def apply(prefix: String = "") = {
    val res = Val.Local(prefix + i)
    i += 1
    res
  }
}
