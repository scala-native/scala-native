package scala.scalanative
package nir

import Type._

object Nrt {
  private def module(id: String) =
    Module(Global.Val(id))
  private def cls(id: String) =
    Class(Global.Type(id))
  private def nrtcls(id: String) =
    Class(Global.Type("nrt", id))
  private def struct(id: String) =
    Struct(Global.Type("nrt", id))
  private def value(id: String, ty: Type) =
    Val.Global(Global.Val("nrt", id), Ptr(ty))
  private def intrinsic(id: String, args: Seq[Type], ret: Type) =
    Val.Global(Global.Val(("nrt" +: id.split("_")): _*), Ptr(Function(args, ret)))
  private def nullary(id: String, to: Type) =
    intrinsic(id, Seq(), to)
  private def unary(id: String, from: Type, to: Type) =
    intrinsic(id, Seq(from), to)
  private def binary(id: String, from1: Type, from2: Type, to: Type) =
    intrinsic(id, Seq(from1, from2), to)
  private def ternary(id: String, from1: Type, from2: Type, from3: Type, to: Type) =
    intrinsic(id, Seq(from1, from2, from3), to)

  def call(Intr: Val.Global, args: Val*): Op = {
    val Val.Global(_, Ptr(ty)) = Intr
    Op.Call(ty, Intr, args)
  }

  lazy val String = cls("java.lang.String")

  lazy val BoxedUnit       = module("scala.runtime.BoxedUnit")
  lazy val BoxedUnit_value = {
    val instance = BoxedUnit.name + "instance"
    val clsvalue = Val.Global(instance, Ptr(ClassValue(BoxedUnit.name)))
    Val.Bitcast(Class(BoxedUnit.name), clsvalue)
  }

  lazy val Object            = nrtcls("Object")
  lazy val Object_alloc      = binary("Object_alloc"     , Ptr(Type), Size, Object   )
  lazy val Object_init       = unary ("Object_init"      , Object,          Unit     )
  lazy val Object_equals     = binary("Object_equals"    , Object, Object,  Bool     )
  lazy val Object_toString   = unary ("Object_toString"  , Object,          String   )
  lazy val Object_hashCode   = unary ("Object_hashCode"  , Object,          I32      )
  lazy val Object_getType    = unary ("Object_getType"   , Object,          Ptr(Type))
  lazy val Object_getMonitor = unary ("Object_getMonitor", Object,          Monitor  )
  lazy val Object_clone      = unary ("Object_clone"     , Object,          Object   )
  lazy val Object_finalize   = unary ("Object_finalize"  , Object,          Unit     )

  lazy val Monitor           = nrtcls ("Monitor")
  lazy val Monitor_enter     = unary  ("Monitor_enter"    , Monitor,           Unit  )
  lazy val Monitor_exit      = unary  ("Monitor_exit"     , Monitor,           Unit  )
  lazy val Monitor_notify    = unary  ("Monitor_notify"   , Monitor,           Unit  )
  lazy val Monitor_notifyAll = unary  ("Monitor_notifyAll", Monitor,           Unit  )
  lazy val Monitor_wait      = ternary("Monitor_wait"     , Monitor, I64, I32, Unit  )

  lazy val AnyArray        = nrtcls ("AnyArray")
  lazy val AnyArray_apply  = binary ("AnyArray_apply" , AnyArray, I32,         Object)
  lazy val AnyArray_update = ternary("AnyArray_update", AnyArray, I32, Object, Unit  )
  lazy val AnyArray_length = unary  ("AnyArray_length", AnyArray,              I32   )

  lazy val BoolArray        = nrtcls ("BoolArray")
  lazy val BoolArray_alloc  = unary  ("BoolArray_alloc" , I32,                  BoolArray)
  lazy val BoolArray_apply  = binary ("BoolArray_apply" , BoolArray, I32,       Bool     )
  lazy val BoolArray_update = ternary("BoolArray_update", BoolArray, I32, Bool, Unit     )
  lazy val BoolArray_length = unary  ("BoolArray_length", BoolArray,            I32      )

  lazy val CharArray        = nrtcls ("CharArray")
  lazy val CharArray_alloc  = unary  ("CharArray_alloc" , I32,                 CharArray)
  lazy val CharArray_apply  = binary ("CharArray_apply" , CharArray, I32,      I16      )
  lazy val CharArray_update = ternary("CharArray_update", CharArray, I32, I16, Unit     )
  lazy val CharArray_length = unary  ("CharArray_length", CharArray,           I32      )

  lazy val ByteArray        = nrtcls ("ByteArray")
  lazy val ByteArray_alloc  = unary  ("ByteArray_alloc"  ,I32,                ByteArray)
  lazy val ByteArray_apply  = binary ("ByteArray_apply" , ByteArray, I32,     I8       )
  lazy val ByteArray_update = ternary("ByteArray_update", ByteArray, I32, I8, Unit     )
  lazy val ByteArray_length = unary  ("ByteArray_length", ByteArray,          I32      )

  lazy val ShortArray        = nrtcls ("ShortArray")
  lazy val ShortArray_alloc  = unary  ("ShortArray_alloc" , I32,                  ShortArray)
  lazy val ShortArray_apply  = binary ("ShortArray_apply" , ShortArray, I32,      I16       )
  lazy val ShortArray_update = ternary("ShortArray_update", ShortArray, I32, I16, Unit      )
  lazy val ShortArray_length = unary  ("ShortArray_length", ShortArray,           I32       )

  lazy val IntArray        = nrtcls ("IntArray")
  lazy val IntArray_alloc  = unary  ("IntArray_alloc" , I32,                IntArray)
  lazy val IntArray_apply  = binary ("IntArray_apply" , IntArray, I32,      I32     )
  lazy val IntArray_update = ternary("IntArray_update", IntArray, I32, I32, Unit    )
  lazy val IntArray_length = unary  ("IntArray_length", IntArray,           I32     )

  lazy val LongArray        = nrtcls ("LongArray")
  lazy val LongArray_alloc  = unary  ("LongArray_alloc" , I32,                 LongArray)
  lazy val LongArray_apply  = binary ("LongArray_apply" , LongArray, I32,      I64      )
  lazy val LongArray_update = ternary("LongArray_update", LongArray, I32, I64, Unit     )
  lazy val LongArray_length = unary  ("LongArray_length", LongArray,           I32      )

  lazy val FloatArray        = nrtcls ("FloatArray")
  lazy val FloatArray_alloc  = unary  ("FloatArray_alloc" , I32,                  FloatArray)
  lazy val FloatArray_apply  = binary ("FloatArray_apply" , FloatArray, I32,      F32       )
  lazy val FloatArray_update = ternary("FloatArray_update", FloatArray, I32, F32, Unit      )
  lazy val FloatArray_length = unary  ("FloatArray_length", FloatArray,           I32       )

  lazy val DoubleArray        = nrtcls ("DoubleArray")
  lazy val DoubleArray_alloc  = unary  ("DoubleArray_alloc" , I32,                   DoubleArray)
  lazy val DoubleArray_apply  = binary ("DoubleArray_apply" , DoubleArray, I32,      F64        )
  lazy val DoubleArray_update = ternary("DoubleArray_update", DoubleArray, I32, F64, Unit       )
  lazy val DoubleArray_length = unary  ("DoubleArray_length", DoubleArray,           I32        )

  lazy val ObjectArray        = nrtcls ("ObjectArray")
  lazy val ObjectArray_alloc  = unary  ("ObjectArray_alloc" , I32,                      ObjectArray)
  lazy val ObjectArray_apply  = binary ("ObjectArray_apply" , ObjectArray, I32,         Object     )
  lazy val ObjectArray_update = ternary("ObjectArray_update", ObjectArray, I32, Object, Unit       )
  lazy val ObjectArray_length = unary  ("ObjectArray_length", ObjectArray,              I32        )

  lazy val Type         = struct("Type")
  lazy val Type_getId   = unary ("Type_getId",   Ptr(Type), I32   )
  lazy val Type_getName = unary ("Type_getName", Ptr(Type), String)

  lazy val Nothing_type     = value("Nothing_type", Type)
  lazy val Null_type        = value("Null_type"   , Type)
  lazy val Object_type      = value("Object_type" , Type)
  lazy val Monitor_type     = value("Monitor_type", Type)
  lazy val Type_type        = value("Type_type"   , Type)
  lazy val BoolArray_type   = value("BoolArray_type"  , Type)
  lazy val CharArray_type   = value("CharArray_type"  , Type)
  lazy val ByteArray_type   = value("ByteArray_type"  , Type)
  lazy val ShortArray_type  = value("ShortArray_type" , Type)
  lazy val IntArray_type    = value("IntArray_type"   , Type)
  lazy val LongArray_type   = value("LongArray_type"  , Type)
  lazy val FloatArray_type  = value("FloatArray_type" , Type)
  lazy val DoubleArray_type = value("DoubleArray_type", Type)
  lazy val ObjectArray_type = value("ObjectArray_type", Type)

  lazy val init   = binary ("init" , I32, Ptr(Ptr(I8)), ObjectArray)
  lazy val yield_ = nullary("yield",                    Unit       )

  lazy val excrec      = nir.Type.AnonStruct(Seq(Ptr(I8), I32))
  lazy val throw_      = unary  ("throw"      , Object,  Nothing)
  lazy val begin_catch = unary  ("begin_catch", Ptr(I8), Unit   )
  lazy val end_catch   = nullary("end_catch"  ,          Unit   )

  lazy val Array = Map[Char, Class](
    'Z' -> BoolArray  ,
    'C' -> CharArray  ,
    'B' -> ByteArray  ,
    'S' -> ShortArray ,
    'I' -> IntArray   ,
    'L' -> LongArray  ,
    'F' -> FloatArray ,
    'D' -> DoubleArray,
    'O' -> ObjectArray
  )

  lazy val Array_apply = Map[Char, Val.Global](
    'Z' -> BoolArray_apply  ,
    'C' -> CharArray_apply  ,
    'B' -> ByteArray_apply  ,
    'S' -> ShortArray_apply ,
    'I' -> IntArray_apply   ,
    'L' -> LongArray_apply  ,
    'F' -> FloatArray_apply ,
    'D' -> DoubleArray_apply,
    'O' -> ObjectArray_apply
  )

  lazy val Array_update = Map[Char, Val.Global](
    'Z' -> BoolArray_update  ,
    'C' -> CharArray_update  ,
    'B' -> ByteArray_update  ,
    'S' -> ShortArray_update ,
    'I' -> IntArray_update   ,
    'L' -> LongArray_update  ,
    'F' -> FloatArray_update ,
    'D' -> DoubleArray_update,
    'O' -> ObjectArray_update
  )

  lazy val Array_length = Map[Char, Val.Global](
    'Z' -> BoolArray_length  ,
    'C' -> CharArray_length  ,
    'B' -> ByteArray_length  ,
    'S' -> ShortArray_length ,
    'I' -> IntArray_length   ,
    'L' -> LongArray_length  ,
    'F' -> FloatArray_length ,
    'D' -> DoubleArray_length,
    'O' -> ObjectArray_length
  )

  lazy val Array_alloc = Map[Char, Val.Global](
    'Z' -> BoolArray_alloc  ,
    'C' -> CharArray_alloc  ,
    'B' -> ByteArray_alloc  ,
    'S' -> ShortArray_alloc ,
    'I' -> IntArray_alloc   ,
    'L' -> LongArray_alloc  ,
    'F' -> FloatArray_alloc ,
    'D' -> DoubleArray_alloc,
    'O' -> ObjectArray_alloc
  )

  lazy val types = Map[Type, Val.Global](
    Nothing     -> Nothing_type    ,
    Null        -> Null_type       ,
    Object      -> Object_type     ,
    Type        -> Type_type       ,
    BoolArray   -> BoolArray_type  ,
    CharArray   -> CharArray_type  ,
    ByteArray   -> ByteArray_type  ,
    ShortArray  -> ShortArray_type ,
    IntArray    -> IntArray_type   ,
    LongArray   -> LongArray_type  ,
    FloatArray  -> FloatArray_type ,
    DoubleArray -> DoubleArray_type,
    ObjectArray -> ObjectArray_type
  )

  lazy val layouts = Map[Global, Seq[Type]](
    AnyArray.name    -> Seq(Ptr(Type), I32)                            ,
    BoolArray.name   -> Seq(Ptr(Type), I32, nir.Type.Array(Bool, 0))   ,
    CharArray.name   -> Seq(Ptr(Type), I32, nir.Type.Array(I16, 0))    ,
    ByteArray.name   -> Seq(Ptr(Type), I32, nir.Type.Array(I8, 0))     ,
    ShortArray.name  -> Seq(Ptr(Type), I32, nir.Type.Array(I16, 0))    ,
    IntArray.name    -> Seq(Ptr(Type), I32, nir.Type.Array(I32, 0))    ,
    LongArray.name   -> Seq(Ptr(Type), I32, nir.Type.Array(I64, 0))    ,
    FloatArray.name  -> Seq(Ptr(Type), I32, nir.Type.Array(F32, 0))    ,
    DoubleArray.name -> Seq(Ptr(Type), I32, nir.Type.Array(F64, 0))    ,
    ObjectArray.name -> Seq(Ptr(Type), I32, nir.Type.Array(Ptr(I8), 0)),
    Type.name        -> Seq(Ptr(Type), I32, Ptr(I8))                   ,
    String.name      -> Seq(Ptr(Type), CharArray, I32, I32)
  )
}
