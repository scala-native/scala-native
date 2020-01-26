package scala.scalanative.cpp.ios

import scalanative.unsafe._
import scalanative.cpp.ios

trait Streambuf {
    def in_avail(): Streamsize
}