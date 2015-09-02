package salty.tools
package linker

import java.io._
import java.nio._
import java.nio.channels._

import salty.ir._, Deserializers.RichGet

class Linker(val scope: Scope, entry: Stat)
