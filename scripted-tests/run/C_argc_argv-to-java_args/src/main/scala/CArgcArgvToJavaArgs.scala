object CArgcArgvToJavaArgs {
  def main(args: Array[String]) {
    val len = args.length

    assert(len == 3)
    assert(args(0).equals("hello"))
    assert(args(1).equals("hola"))
    assert(args(2).equals("salut"))
  }
}
