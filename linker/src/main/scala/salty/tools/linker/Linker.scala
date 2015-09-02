package salty.tools.linker

import java.io._
import java.nio._
import java.nio.channels._

import salty.ir._, Deserializers.RichGet

class Linker(val classpath: Classpath, entry: Name) {
  def link: Stat = ???
}
