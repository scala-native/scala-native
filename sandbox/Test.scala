package test

import native.ffi._

@extern
object stdio {
  def puts(str: Ptr[Char8]): Unit = extern
}

object Test {
  def main(args: Array[String]) = {
    val ptr = c"hello, world!"
    ptr(0) = 'H'
    stdio.puts(ptr)
  }
}
