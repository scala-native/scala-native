package test

import scalanative.native._

@extern object stdio {
  var __stdoutp: Ptr[_] = extern
  def fputc(ch: Int, stream: Ptr[_]): Int = extern
}
import stdio._

object Test {
  def putc(ch: Char): Unit =
    fputc(ch.toInt, __stdoutp)
  def main(args: Array[String]): Unit = {
    putc('h')
    putc('i')
    putc('\n')
  }
}
