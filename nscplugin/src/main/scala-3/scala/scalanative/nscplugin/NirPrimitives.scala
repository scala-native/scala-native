package scala.scalanative.nscplugin

import scala.collection.mutable
import dotty.tools.backend.jvm.DottyPrimitives
import dotty.tools.dotc.ast.tpd._
import dotty.tools.dotc.util.ReadOnlyMap
import dotty.tools.dotc.core._
import Names.TermName
import StdNames._
import Types._
import Contexts._
import Symbols._
import Names._
import scala.scalanative.nscplugin.NirPrimitives

object NirPrimitives {
  final val FirstNirPrimitiveCode = 300
  final val THROW = 1 + FirstNirPrimitiveCode

  final val BOXED_UNIT = 1 + THROW
  final val ARRAY_CLONE = 1 + BOXED_UNIT

  final val CQUOTE = 1 + ARRAY_CLONE
  final val STACKALLOC = 1 + CQUOTE

  final val DIV_UINT = 1 + STACKALLOC
  final val DIV_ULONG = 1 + DIV_UINT
  final val REM_UINT = 1 + DIV_ULONG
  final val REM_ULONG = 1 + REM_UINT

  final val UNSIGNED_OF = 1 + REM_ULONG
  final val BYTE_TO_UINT = 1 + UNSIGNED_OF
  final val BYTE_TO_ULONG = 1 + BYTE_TO_UINT
  final val SHORT_TO_UINT = 1 + BYTE_TO_ULONG
  final val SHORT_TO_ULONG = 1 + SHORT_TO_UINT
  final val INT_TO_ULONG = 1 + SHORT_TO_ULONG

  final val UINT_TO_FLOAT = 1 + INT_TO_ULONG
  final val ULONG_TO_FLOAT = 1 + UINT_TO_FLOAT
  final val UINT_TO_DOUBLE = 1 + ULONG_TO_FLOAT
  final val ULONG_TO_DOUBLE = 1 + UINT_TO_DOUBLE

  final val LOAD_BOOL = 1 + ULONG_TO_DOUBLE
  final val LOAD_CHAR = 1 + LOAD_BOOL
  final val LOAD_BYTE = 1 + LOAD_CHAR
  final val LOAD_SHORT = 1 + LOAD_BYTE
  final val LOAD_INT = 1 + LOAD_SHORT
  final val LOAD_LONG = 1 + LOAD_INT
  final val LOAD_FLOAT = 1 + LOAD_LONG
  final val LOAD_DOUBLE = 1 + LOAD_FLOAT
  final val LOAD_RAW_PTR = 1 + LOAD_DOUBLE
  final val LOAD_RAW_SIZE = 1 + LOAD_RAW_PTR
  final val LOAD_OBJECT = 1 + LOAD_RAW_SIZE

  final val STORE_BOOL = 1 + LOAD_OBJECT
  final val STORE_CHAR = 1 + STORE_BOOL
  final val STORE_BYTE = 1 + STORE_CHAR
  final val STORE_SHORT = 1 + STORE_BYTE
  final val STORE_INT = 1 + STORE_SHORT
  final val STORE_LONG = 1 + STORE_INT
  final val STORE_FLOAT = 1 + STORE_LONG
  final val STORE_DOUBLE = 1 + STORE_FLOAT
  final val STORE_RAW_PTR = 1 + STORE_DOUBLE
  final val STORE_RAW_SIZE = 1 + STORE_RAW_PTR
  final val STORE_OBJECT = 1 + STORE_RAW_SIZE

  final val ELEM_RAW_PTR = 1 + STORE_OBJECT

  final val CAST_RAW_PTR_TO_OBJECT = 1 + ELEM_RAW_PTR
  final val CAST_OBJECT_TO_RAW_PTR = 1 + CAST_RAW_PTR_TO_OBJECT
  final val CAST_INT_TO_FLOAT = 1 + CAST_OBJECT_TO_RAW_PTR
  final val CAST_FLOAT_TO_INT = 1 + CAST_INT_TO_FLOAT
  final val CAST_LONG_TO_DOUBLE = 1 + CAST_FLOAT_TO_INT
  final val CAST_DOUBLE_TO_LONG = 1 + CAST_LONG_TO_DOUBLE
  final val CAST_RAWPTR_TO_INT = 1 + CAST_DOUBLE_TO_LONG
  final val CAST_RAWPTR_TO_LONG = 1 + CAST_RAWPTR_TO_INT
  final val CAST_INT_TO_RAWPTR = 1 + CAST_RAWPTR_TO_LONG
  final val CAST_LONG_TO_RAWPTR = 1 + CAST_INT_TO_RAWPTR
  final val CAST_RAWSIZE_TO_INT = 1 + CAST_LONG_TO_RAWPTR
  final val CAST_RAWSIZE_TO_LONG = 1 + CAST_RAWSIZE_TO_INT
  final val CAST_RAWSIZE_TO_LONG_UNSIGNED = 1 + CAST_RAWSIZE_TO_LONG
  final val CAST_INT_TO_RAWSIZE = 1 + CAST_RAWSIZE_TO_LONG_UNSIGNED
  final val CAST_INT_TO_RAWSIZE_UNSIGNED = 1 + CAST_INT_TO_RAWSIZE
  final val CAST_LONG_TO_RAWSIZE = 1 + CAST_INT_TO_RAWSIZE_UNSIGNED

  final val CFUNCPTR_FROM_FUNCTION = 1 + CAST_LONG_TO_RAWSIZE
  final val CFUNCPTR_APPLY = 1 + CFUNCPTR_FROM_FUNCTION

  final val CLASS_FIELD_RAWPTR = 1 + CFUNCPTR_APPLY
  final val SIZE_OF = CLASS_FIELD_RAWPTR + 1
  final val ALIGNMENT_OF = SIZE_OF + 1

  final val REFLECT_SELECTABLE_SELECTDYN = ALIGNMENT_OF + 1
  final val REFLECT_SELECTABLE_APPLYDYN = REFLECT_SELECTABLE_SELECTDYN + 1

  final val SAFEZONE_ALLOC = 1 + REFLECT_SELECTABLE_APPLYDYN

  final val LastNirPrimitiveCode = SAFEZONE_ALLOC

  def isNirPrimitive(code: Int): Boolean =
    code >= FirstNirPrimitiveCode && code <= LastNirPrimitiveCode

  def isRawPtrOp(code: Int): Boolean =
    code >= LOAD_BOOL && code <= ELEM_RAW_PTR

  def isRawPtrLoadOp(code: Int): Boolean =
    code >= LOAD_BOOL && code <= LOAD_OBJECT

  def isRawPtrStoreOp(code: Int): Boolean =
    code >= STORE_BOOL && code <= STORE_OBJECT

  def isRawPtrCastOp(code: Int): Boolean =
    code >= CAST_RAW_PTR_TO_OBJECT && code <= CAST_LONG_TO_RAWPTR

  def isRawSizeCastOp(code: Int): Boolean =
    code >= CAST_RAWSIZE_TO_INT && code <= CAST_LONG_TO_RAWSIZE

  def isUnsignedOp(code: Int): Boolean =
    code >= DIV_UINT && code <= ULONG_TO_DOUBLE
}

class NirPrimitives(using ctx: Context) extends DottyPrimitives(ctx) {
  import NirPrimitives._
  private lazy val nirPrimitives: ReadOnlyMap[Symbol, Int] = initNirPrimitives

  override def getPrimitive(sym: Symbol): Int =
    nirPrimitives.getOrElse(sym, super.getPrimitive(sym))

  override def getPrimitive(app: Apply, tpe: Type)(using Context): Int =
    nirPrimitives.getOrElse(app.fun.symbol, super.getPrimitive(app, tpe))

  override def isPrimitive(sym: Symbol): Boolean = {
    nirPrimitives.contains(sym) || super.isPrimitive(sym)
  }
  override def isPrimitive(tree: Tree): Boolean = {
    nirPrimitives.contains(tree.symbol) || super.isPrimitive(tree)
  }

  private def initNirPrimitives(using Context): ReadOnlyMap[Symbol, Int] = {
    val defnNir = NirDefinitions.get
    val primitives = MutableSymbolMap[Int]()

    def addPrimitive(s: Symbol, code: Int) = {
      assert(!(primitives contains s), "Duplicate primitive " + s)
      assert(s.exists, s"Empty symbol with code $code")
      primitives(s) = code
    }

    def addPrimitives(alts: Seq[Symbol], tag: Int) =
      alts.foreach(addPrimitive(_, tag))

    // scalafmt: { maxColumn = 120}
    addPrimitive(defn.throwMethod, THROW)
    addPrimitive(defn.BoxedUnit_UNIT, BOXED_UNIT)
    addPrimitive(defn.Array_clone, ARRAY_CLONE)
    addPrimitive(defnNir.CQuote_c, CQUOTE)
    addPrimitives(defnNir.Intrinsics_stackallocAlts, STACKALLOC)
    addPrimitive(defnNir.IntrinsicsInternal_stackalloc, STACKALLOC)
    addPrimitive(defnNir.Intrinsics_divUInt, DIV_UINT)
    addPrimitive(defnNir.Intrinsics_divULong, DIV_ULONG)
    addPrimitive(defnNir.Intrinsics_remUInt, REM_UINT)
    addPrimitive(defnNir.Intrinsics_remULong, REM_ULONG)
    addPrimitives(defnNir.Intrinsics_unsignedOfAlts, UNSIGNED_OF)
    addPrimitive(defnNir.Intrinsics_byteToUInt, BYTE_TO_UINT)
    addPrimitive(defnNir.Intrinsics_byteToULong, BYTE_TO_ULONG)
    addPrimitive(defnNir.Intrinsics_shortToUInt, SHORT_TO_UINT)
    addPrimitive(defnNir.Intrinsics_shortToULong, SHORT_TO_ULONG)
    addPrimitive(defnNir.Intrinsics_intToULong, INT_TO_ULONG)
    addPrimitive(defnNir.Intrinsics_uintToFloat, UINT_TO_FLOAT)
    addPrimitive(defnNir.Intrinsics_ulongToFloat, ULONG_TO_FLOAT)
    addPrimitive(defnNir.Intrinsics_uintToDouble, UINT_TO_DOUBLE)
    addPrimitive(defnNir.Intrinsics_ulongToDouble, ULONG_TO_DOUBLE)
    addPrimitive(defnNir.Intrinsics_loadBool, LOAD_BOOL)
    addPrimitive(defnNir.Intrinsics_loadChar, LOAD_CHAR)
    addPrimitive(defnNir.Intrinsics_loadByte, LOAD_BYTE)
    addPrimitive(defnNir.Intrinsics_loadShort, LOAD_SHORT)
    addPrimitive(defnNir.Intrinsics_loadInt, LOAD_INT)
    addPrimitive(defnNir.Intrinsics_loadLong, LOAD_LONG)
    addPrimitive(defnNir.Intrinsics_loadFloat, LOAD_FLOAT)
    addPrimitive(defnNir.Intrinsics_loadDouble, LOAD_DOUBLE)
    addPrimitive(defnNir.Intrinsics_loadRawPtr, LOAD_RAW_PTR)
    addPrimitive(defnNir.Intrinsics_loadRawSize, LOAD_RAW_SIZE)
    addPrimitive(defnNir.Intrinsics_loadObject, LOAD_OBJECT)
    addPrimitive(defnNir.Intrinsics_storeBool, STORE_BOOL)
    addPrimitive(defnNir.Intrinsics_storeChar, STORE_CHAR)
    addPrimitive(defnNir.Intrinsics_storeByte, STORE_BYTE)
    addPrimitive(defnNir.Intrinsics_storeShort, STORE_SHORT)
    addPrimitive(defnNir.Intrinsics_storeInt, STORE_INT)
    addPrimitive(defnNir.Intrinsics_storeLong, STORE_LONG)
    addPrimitive(defnNir.Intrinsics_storeFloat, STORE_FLOAT)
    addPrimitive(defnNir.Intrinsics_storeDouble, STORE_DOUBLE)
    addPrimitive(defnNir.Intrinsics_storeRawPtr, STORE_RAW_PTR)
    addPrimitive(defnNir.Intrinsics_storeRawSize, STORE_RAW_SIZE)
    addPrimitive(defnNir.Intrinsics_storeObject, STORE_OBJECT)
    addPrimitive(defnNir.Intrinsics_elemRawPtr, ELEM_RAW_PTR)
    addPrimitive(defnNir.Intrinsics_castRawPtrToObject, CAST_RAW_PTR_TO_OBJECT)
    addPrimitive(defnNir.Intrinsics_castObjectToRawPtr, CAST_OBJECT_TO_RAW_PTR)
    addPrimitive(defnNir.Intrinsics_castIntToFloat, CAST_INT_TO_FLOAT)
    addPrimitive(defnNir.Intrinsics_castFloatToInt, CAST_FLOAT_TO_INT)
    addPrimitive(defnNir.Intrinsics_castLongToDouble, CAST_LONG_TO_DOUBLE)
    addPrimitive(defnNir.Intrinsics_castDoubleToLong, CAST_DOUBLE_TO_LONG)
    addPrimitive(defnNir.Intrinsics_castRawPtrToInt, CAST_RAWPTR_TO_INT)
    addPrimitive(defnNir.Intrinsics_castRawPtrToLong, CAST_RAWPTR_TO_LONG)
    addPrimitive(defnNir.Intrinsics_castIntToRawPtr, CAST_INT_TO_RAWPTR)
    addPrimitive(defnNir.Intrinsics_castLongToRawPtr, CAST_LONG_TO_RAWPTR)
    addPrimitive(defnNir.Intrinsics_castRawSizeToInt, CAST_RAWSIZE_TO_INT)
    addPrimitive(defnNir.Intrinsics_castRawSizeToLong, CAST_RAWSIZE_TO_LONG)
    addPrimitive(defnNir.Intrinsics_castRawSizeToLongUnsigned, CAST_RAWSIZE_TO_LONG_UNSIGNED)
    addPrimitive(defnNir.Intrinsics_castIntToRawSize, CAST_INT_TO_RAWSIZE)
    addPrimitive(defnNir.Intrinsics_castIntToRawSizeUnsigned, CAST_INT_TO_RAWSIZE_UNSIGNED)
    addPrimitive(defnNir.Intrinsics_castLongToRawSize, CAST_LONG_TO_RAWSIZE)
    addPrimitives(defnNir.CFuncPtr_apply, CFUNCPTR_APPLY)
    addPrimitives(defnNir.CFuncPtr_fromScalaFunction, CFUNCPTR_FROM_FUNCTION)
    addPrimitive(defnNir.Intrinsics_classFieldRawPtr, CLASS_FIELD_RAWPTR)
    addPrimitive(defnNir.IntrinsicsInternal_sizeOf, SIZE_OF)
    addPrimitive(defnNir.IntrinsicsInternal_alignmentOf, ALIGNMENT_OF)
    addPrimitive(
      defnNir.ReflectSelectable_selectDynamic,
      REFLECT_SELECTABLE_SELECTDYN
    )
    addPrimitive(
      defnNir.ReflectSelectable_applyDynamic,
      REFLECT_SELECTABLE_APPLYDYN
    )
    defnNir.RuntimeSafeZoneAllocator_allocate.foreach(addPrimitive(_, SAFEZONE_ALLOC))
    primitives
  }
}
