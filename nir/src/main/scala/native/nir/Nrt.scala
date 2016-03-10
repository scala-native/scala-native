package native
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

  lazy val Character = cls("java.lang.Character")
  lazy val Boolean   = cls("java.lang.Boolean")
  lazy val Byte      = cls("java.lang.Byte")
  lazy val Short     = cls("java.lang.Short")
  lazy val Integer   = cls("java.lang.Int")
  lazy val Long      = cls("java.lang.Long")
  lazy val Float     = cls("java.lang.Float")
  lazy val Double    = cls("java.lang.Double")

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

  lazy val Monitor           = nrtcls ("Monitor")
  lazy val Monitor_enter     = unary  ("Monitor_enter"    , Object,           Unit  )
  lazy val Monitor_exit      = unary  ("Monitor_exit"     , Object,           Unit  )
  lazy val Monitor_notify    = unary  ("Monitor_notify"   , Object,           Unit  )
  lazy val Monitor_notifyAll = unary  ("Monitor_notifyAll", Object,           Unit  )
  lazy val Monitor_wait      = ternary("Monitor_wait"     , Object, I64, I32, Unit  )

  lazy val String         = nrtcls("String")
  lazy val String_charAt  = binary("String_charAt",  String,  I32,      I16   )
  lazy val String_concat  = binary("String_concat",  String,  String,   String)
  lazy val String_length  = unary ("String_length",  String,            I32   )

  lazy val AnyArray        = nrtcls ("AnyArray")
  lazy val AnyArray_apply  = binary ("AnyArray_apply" , AnyArray, I32,         Object)
  lazy val AnyArray_update = ternary("AnyArray_update", AnyArray, I32, Object, Unit  )
  lazy val AnyArray_length = unary  ("AnyArray_length", AnyArray,              I32   )

  lazy val UnitArray        = nrtcls ("UnitArray")
  lazy val UnitArray_alloc  = unary  ("UnitArray_alloc" , I32,                  UnitArray)
  lazy val UnitArray_apply  = binary ("UnitArray_apply" , UnitArray, I32,       Unit     )
  lazy val UnitArray_update = ternary("UnitArray_update", UnitArray, I32, Unit, Unit     )
  lazy val UnitArray_length = unary  ("UnitArray_length", UnitArray,            I32      )

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
  lazy val String_type      = value("String_type" , Type)
  lazy val UnitArray_type   = value("UnitArray_type"  , Type)
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

  lazy val Array = Map[Type, Class](
    Unit      -> UnitArray  ,
    Bool      -> BoolArray  ,
    Character -> CharArray  ,
    Byte      -> ByteArray  ,
    Short     -> ShortArray ,
    Integer   -> IntArray 	,
    Long      -> LongArray  ,
    Float     -> FloatArray ,
    Double    -> DoubleArray,
    Object    -> ObjectArray
  )

  lazy val Array_apply = Map[Type, Val.Global](
    Unit      -> UnitArray_apply  ,
    Bool      -> BoolArray_apply  ,
    Character -> CharArray_apply  ,
    Byte      -> ByteArray_apply  ,
    Short     -> ShortArray_apply ,
    Integer   -> IntArray_apply   ,
    Long      -> LongArray_apply  ,
    Float     -> FloatArray_apply ,
    Double    -> DoubleArray_apply,
    Object    -> ObjectArray_apply
  )

  lazy val Array_update = Map[Type, Val.Global](
    Unit      -> UnitArray_update  ,
    Bool      -> BoolArray_update  ,
    Character -> CharArray_update  ,
    Byte      -> ByteArray_update  ,
    Short     -> ShortArray_update ,
    Integer   -> IntArray_update   ,
    Long      -> LongArray_update  ,
    Float     -> FloatArray_update ,
    Double    -> DoubleArray_update,
    Object    -> ObjectArray_update
  )

  lazy val Array_length = Map[Type, Val.Global](
    Unit      -> UnitArray_length  ,
    Bool      -> BoolArray_length  ,
    Character -> CharArray_length  ,
    Byte      -> ByteArray_length  ,
    Short     -> ShortArray_length ,
    Integer   -> IntArray_length   ,
    Long      -> LongArray_length  ,
    Float     -> FloatArray_length ,
    Double    -> DoubleArray_length,
    Object    -> ObjectArray_length
  )

  lazy val Array_alloc = Map[Type, Val.Global](
    Unit      -> UnitArray_alloc  ,
    Bool      -> BoolArray_alloc  ,
    Character -> CharArray_alloc  ,
    Byte      -> ByteArray_alloc  ,
    Short     -> ShortArray_alloc ,
    Integer   -> IntArray_alloc   ,
    Long      -> LongArray_alloc  ,
    Float     -> FloatArray_alloc ,
    Double    -> DoubleArray_alloc,
    Object    -> ObjectArray_alloc
  )

  lazy val types = Map[Type, Val.Global](
    Nothing     -> Nothing_type    ,
    Null        -> Null_type       ,
    Object      -> Object_type     ,
    Type        -> Type_type       ,
    String      -> String_type     ,
    UnitArray   -> UnitArray_type  ,
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
    UnitArray.name   -> Seq(Ptr(Type), I32)                            ,
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
    String.name      -> Seq(Ptr(Type), I32, Ptr(I8))
  )
}
