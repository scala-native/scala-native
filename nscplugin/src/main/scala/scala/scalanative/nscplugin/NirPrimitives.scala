package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.collection.mutable

object NirPrimitives {
  final val BOXED_UNIT  = 301
  final val ARRAY_CLONE = 1 + BOXED_UNIT

  final val PTR_LOAD   = 1 + ARRAY_CLONE
  final val PTR_STORE  = 1 + PTR_LOAD
  final val PTR_ADD    = 1 + PTR_STORE
  final val PTR_SUB    = 1 + PTR_ADD
  final val PTR_APPLY  = 1 + PTR_SUB
  final val PTR_UPDATE = 1 + PTR_APPLY

  final val FUN_PTR_CALL = 1 + PTR_UPDATE
  final val FUN_PTR_FROM = 1 + FUN_PTR_CALL

  final val SIZEOF     = 1 + FUN_PTR_FROM
  final val TYPEOF     = 1 + SIZEOF
  final val CQUOTE     = 1 + TYPEOF
  final val CCAST      = 1 + CQUOTE
  final val STACKALLOC = 1 + CCAST
  final val DIV_UINT   = 1 + STACKALLOC
  final val DIV_ULONG  = 1 + DIV_UINT
  final val REM_UINT   = 1 + DIV_ULONG
  final val REM_ULONG  = 1 + REM_UINT
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

  def isPtrOp(code: Int): Boolean =
    code >= PTR_LOAD && code <= PTR_UPDATE

  def isFunPtrOp(code: Int): Boolean =
    code == FUN_PTR_CALL || code == FUN_PTR_FROM

  private val nirPrimitives = mutable.Map.empty[Symbol, Int]

  private def initWithPrimitives(addPrimitive: (Symbol, Int) => Unit): Unit = {
    addPrimitive(BoxedUnit_UNIT, BOXED_UNIT)
    addPrimitive(Array_clone, ARRAY_CLONE)
    addPrimitive(PtrLoadMethod, PTR_LOAD)
    addPrimitive(PtrStoreMethod, PTR_STORE)
    addPrimitive(PtrAddMethod, PTR_ADD)
    addPrimitive(PtrSubMethod, PTR_SUB)
    addPrimitive(PtrApplyMethod, PTR_APPLY)
    addPrimitive(PtrUpdateMethod, PTR_UPDATE)
    FunctionPtrApply.foreach(addPrimitive(_, FUN_PTR_CALL))
    FunctionPtrFrom.foreach(addPrimitive(_, FUN_PTR_FROM))
    addPrimitive(SizeofMethod, SIZEOF)
    addPrimitive(TypeofMethod, TYPEOF)
    addPrimitive(CQuoteMethod, CQUOTE)
    addPrimitive(CCastMethod, CCAST)
    addPrimitive(StackallocMethod, STACKALLOC)
    addPrimitive(DivUIntMethod, DIV_UINT)
    addPrimitive(DivULongMethod, DIV_ULONG)
    addPrimitive(RemUIntMethod, REM_UINT)
    addPrimitive(RemULongMethod, REM_ULONG)
  }
}
