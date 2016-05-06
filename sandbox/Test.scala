package test

import scalanative.native._

@extern object std {
  def puts(ptr: Ptr[CChar]): Unit = extern
}

class C

object Test {
  def main(args: Array[String]): Unit = {
    std.puts(c"hi")
  }
}
