package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.collection.mutable

object NirPrimitives {
  final val BOXED_UNIT = 301
  final val ARRAY_CLONE = 1 + BOXED_UNIT

  final val CQUOTE = 1 + ARRAY_CLONE
  final val STACKALLOC = 1 + CQUOTE

  final val DIV_UINT = 1 + STACKALLOC
  final val DIV_ULONG = 1 + DIV_UINT
  final val REM_UINT = 1 + DIV_ULONG
  final val REM_ULONG = 1 + REM_UINT

  final val BYTE_TO_UINT = 1 + REM_ULONG
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
  final val SIZE_OF = 1 + CLASS_FIELD_RAWPTR
  final val ALIGNMENT_OF = 1 + SIZE_OF
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

  private val nirPrimitives = mutable.Map.empty[Symbol, Int]

  private def initWithPrimitives(addPrimitive: (Symbol, Int) => Unit): Unit = {
    def addPrimitives(alts: Seq[Symbol], tag: Int): Unit =
      alts.foreach(addPrimitive(_, tag))

    addPrimitive(BoxedUnit_UNIT, BOXED_UNIT)
    addPrimitive(Array_clone, ARRAY_CLONE)
    addPrimitive(CQuoteMethod, CQUOTE)
    addPrimitives(StackallocMethods, STACKALLOC)
    addPrimitive(DivUIntMethod, DIV_UINT)
    addPrimitive(DivULongMethod, DIV_ULONG)
    addPrimitive(RemUIntMethod, REM_UINT)
    addPrimitive(RemULongMethod, REM_ULONG)
    addPrimitive(ByteToUIntMethod, BYTE_TO_UINT)
    addPrimitive(ByteToULongMethod, BYTE_TO_ULONG)
    addPrimitive(ShortToUIntMethod, SHORT_TO_UINT)
    addPrimitive(ShortToULongMethod, SHORT_TO_ULONG)
    addPrimitive(IntToULongMethod, INT_TO_ULONG)
    addPrimitive(UIntToFloatMethod, UINT_TO_FLOAT)
    addPrimitive(ULongToFloatMethod, ULONG_TO_FLOAT)
    addPrimitive(UIntToDoubleMethod, UINT_TO_DOUBLE)
    addPrimitive(ULongToDoubleMethod, ULONG_TO_DOUBLE)
    addPrimitive(LoadBoolMethod, LOAD_BOOL)
    addPrimitive(LoadCharMethod, LOAD_CHAR)
    addPrimitive(LoadByteMethod, LOAD_BYTE)
    addPrimitive(LoadShortMethod, LOAD_SHORT)
    addPrimitive(LoadIntMethod, LOAD_INT)
    addPrimitive(LoadLongMethod, LOAD_LONG)
    addPrimitive(LoadFloatMethod, LOAD_FLOAT)
    addPrimitive(LoadDoubleMethod, LOAD_DOUBLE)
    addPrimitive(LoadRawPtrMethod, LOAD_RAW_PTR)
    addPrimitive(LoadRawSizeMethod, LOAD_RAW_SIZE)
    addPrimitive(LoadObjectMethod, LOAD_OBJECT)

    addPrimitive(StoreBoolMethod, STORE_BOOL)
    addPrimitive(StoreCharMethod, STORE_CHAR)
    addPrimitive(StoreByteMethod, STORE_BYTE)
    addPrimitive(StoreShortMethod, STORE_SHORT)
    addPrimitive(StoreIntMethod, STORE_INT)
    addPrimitive(StoreLongMethod, STORE_LONG)
    addPrimitive(StoreFloatMethod, STORE_FLOAT)
    addPrimitive(StoreDoubleMethod, STORE_DOUBLE)
    addPrimitive(StoreRawPtrMethod, STORE_RAW_PTR)
    addPrimitive(StoreRawSizeMethod, STORE_RAW_SIZE)
    addPrimitive(StoreObjectMethod, STORE_OBJECT)

    addPrimitive(ElemRawPtrMethod, ELEM_RAW_PTR)

    addPrimitive(CastRawPtrToObjectMethod, CAST_RAW_PTR_TO_OBJECT)
    addPrimitive(CastObjectToRawPtrMethod, CAST_OBJECT_TO_RAW_PTR)
    addPrimitive(CastIntToFloatMethod, CAST_INT_TO_FLOAT)
    addPrimitive(CastFloatToIntMethod, CAST_FLOAT_TO_INT)
    addPrimitive(CastLongToDoubleMethod, CAST_LONG_TO_DOUBLE)
    addPrimitive(CastDoubleToLongMethod, CAST_DOUBLE_TO_LONG)
    addPrimitive(CastRawPtrToIntMethod, CAST_RAWPTR_TO_INT)
    addPrimitive(CastRawPtrToLongMethod, CAST_RAWPTR_TO_LONG)
    addPrimitive(CastIntToRawPtrMethod, CAST_INT_TO_RAWPTR)
    addPrimitive(CastLongToRawPtrMethod, CAST_LONG_TO_RAWPTR)
    addPrimitive(CastRawSizeToInt, CAST_RAWSIZE_TO_INT)
    addPrimitive(CastRawSizeToLong, CAST_RAWSIZE_TO_LONG)
    addPrimitive(CastRawSizeToLongUnsigned, CAST_RAWSIZE_TO_LONG_UNSIGNED)
    addPrimitive(CastIntToRawSize, CAST_INT_TO_RAWSIZE)
    addPrimitive(CastIntToRawSizeUnsigned, CAST_INT_TO_RAWSIZE_UNSIGNED)
    addPrimitive(CastLongToRawSize, CAST_LONG_TO_RAWSIZE)

    addPrimitives(CFuncPtrApplyMethods, CFUNCPTR_APPLY)
    addPrimitives(CFuncPtrFromFunctionMethods, CFUNCPTR_FROM_FUNCTION)
    addPrimitive(ClassFieldRawPtrMethod, CLASS_FIELD_RAWPTR)
    addPrimitives(SizeOfMethods, SIZE_OF)
    addPrimitives(AlignmentOfMethods, ALIGNMENT_OF)
  }
}
