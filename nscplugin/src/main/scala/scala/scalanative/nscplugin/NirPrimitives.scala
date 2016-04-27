package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.collection.mutable

object NirPrimitives {
  final val ANY_GETCLASS = 301

  final val MONITOR_NOTIFY    = 1 + ANY_GETCLASS
  final val MONITOR_NOTIFYALL = 1 + MONITOR_NOTIFY
  final val MONITOR_WAIT      = 1 + MONITOR_NOTIFYALL

  final val ARRAY_CLONE = 1 + MONITOR_WAIT
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
    addPrimitive(Any_getClass, ANY_GETCLASS)

    addPrimitive(Object_notify,    MONITOR_NOTIFY)
    addPrimitive(Object_notifyAll, MONITOR_NOTIFYALL)
    addPrimitive(Object_wait,      MONITOR_WAIT)

    addPrimitive(Array_clone, ARRAY_CLONE)
  }
}
