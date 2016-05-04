package test

import scalanative.native._

/*@extern object std {
  def puts(ptr: Ptr[CChar]): Unit = extern
  def malloc(size: Word): Ptr[_] = extern
}
import std._*/

class C

object Test {
  def main(args: Array[String]): Unit = {
    val word1 = cast[Word](new C)
    val word2 = cast[Word](new C)
    word1 == word2

    /*val ptr = cast[Ptr[CChar]](malloc(3))
    !ptr = 'h'
    !(ptr + 1) = 'i'
    !(ptr + 2) = '\0'
    puts(ptr)*/
  }
}
