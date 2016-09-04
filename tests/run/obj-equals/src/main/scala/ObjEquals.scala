object ObjEquals {

  case class O(m: Int)

  def main(args: Array[String]): Unit = {
    val a = new O(5)
    val b = new O(5)

    assert((a==b) == true)
  }
}
