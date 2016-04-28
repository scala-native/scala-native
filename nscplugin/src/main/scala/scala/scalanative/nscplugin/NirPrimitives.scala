package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.collection.mutable

object NirPrimitives {
  final val ARRAY_CLONE = 301
}

abstract class NirPrimitives {
  val global: Global

  type ThisNirGlobalAddons = NirGlobalAddons {
    val global: NirPrimitives.this.global.type
  }

  val nirAddons: ThisNirGlobalAddons

  import global._
  import definitions._
  import rootMirror._
  import scalaPrimitives._
  import nirAddons._
  import nirDefinitions._
  import NirPrimitives._

  def init(): Unit =
    initWithPrimitives(addPrimitive)

  def initPrepJSPrimitives(): Unit = {
    nirPrimitives.clear()
    initWithPrimitives(nirPrimitives.put)
  }

  def isNirPrimitive(sym: Symbol): Boolean =
    nirPrimitives.contains(sym)

  def isNirPrimitive(code: Int): Boolean =
    code >= 300 && code < 360

  private val nirPrimitives = mutable.Map.empty[Symbol, Int]

  private def initWithPrimitives(addPrimitive: (Symbol, Int) => Unit): Unit = {
    //addPrimitive(Array_clone, ARRAY_CLONE)
  }
}
