object Test {
  var z = 0

  def main(args: Array[String]): Unit = {
    val x = 42
    val y = x
    z = args.size
    val z2 = z + 1
    println(z2)

    val a = "Foo".toLowerCase()
    println(a)
    println(z)
  }
}
