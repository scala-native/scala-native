package test

import native.ffi._

object Test {
  def main(args: Array[String]): Unit =
    stdio.puts(c"Hello, world")
}
