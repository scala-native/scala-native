package test

import native.ffi._, stdio._

object Test {
  def main(args: Array[String]) =
    puts(c"hello, world!")
}
