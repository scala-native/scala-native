package native
package nir

import Type._

object Nrt {
  private def value(id: String, ty: Type) =
    Val.Global(Global.intrinsic(id), Ptr(ty))
  private def intrinsic(id: String, args: Seq[Type], ret: Type) =
    Val.Global(Global.intrinsic(id.split("_"): _*), Ptr(Function(args, ret)))
  private def nullary(id: String, to: Type) =
    intrinsic(id, Seq(), to)
  private def unary(id: String, from: Type, to: Type) =
    intrinsic(id, Seq(from), to)
  private def binary(id: String, from1: Type, from2: Type, to: Type) =
    intrinsic(id, Seq(from1, from2), to)
  private def ternary(id: String, from1: Type, from2: Type, from3: Type, to: Type) =
    intrinsic(id, Seq(from1, from2, from3), to)
  private def cls(id: String) =
    Class(Global.intrinsic(id))
  private def struct(id: String) =
    Struct(Global.intrinsic(id))

  def call(Intr: Val.Global, args: Val*): Op = {
    val Val.Global(_, Ptr(ty)) = Intr
    Op.Call(ty, Intr, args)
  }

  lazy val Object          = cls   ("Object")
  lazy val Object_init     = unary ("Object_init"    , Object,         Unit      )
  lazy val Object_equals   = binary("Object_equals"  , Object, Object, Bool      )
  lazy val Object_toString = unary ("Object_toString", Object,         String    )
  lazy val Object_hashCode = unary ("Object_hashCode", Object,         I32       )
  lazy val Object_getType  = unary ("Object_getType" , Object,         Ptr(Type))

  lazy val Monitor_enter     = unary  ("Monitor_enter"    , Object,           Unit  )
  lazy val Monitor_exit      = unary  ("Monitor_exit"     , Object,           Unit  )
  lazy val Monitor_notify    = unary  ("Monitor_notify"   , Object,           Unit  )
  lazy val Monitor_notifyAll = unary  ("Monitor_notifyAll", Object,           Unit  )
  lazy val Monitor_wait      = ternary("Monitor_wait"     , Object, I64, I32, Unit  )

  lazy val AnyArray        = cls    ("AnyArray")
  lazy val AnyArray_apply  = binary ("AnyArray_apply" , AnyArray, I32,         Object)
  lazy val AnyArray_update = ternary("AnyArray_update", AnyArray, I32, Object, Unit   )
  lazy val AnyArray_length = unary  ("AnyArray_length", AnyArray,              I32    )

  lazy val UnitArray        = cls    ("UnitArray")
  lazy val UnitArray_apply  = binary ("UnitArray_apply" , UnitArray, I32,       Unit)
  lazy val UnitArray_update = ternary("UnitArray_update", UnitArray, I32, Unit, Unit)
  lazy val UnitArray_length = unary  ("UnitArray_length", UnitArray,            I32 )

  lazy val BoolArray        = cls    ("BoolArray")
  lazy val BoolArray_apply  = binary ("BoolArray_apply" , BoolArray, I32,       Bool)
  lazy val BoolArray_update = ternary("BoolArray_update", BoolArray, I32, Bool, Unit)
  lazy val BoolArray_length = unary  ("BoolArray_length", BoolArray,            I32 )

  lazy val CharArray        = cls    ("CharArray")
  lazy val CharArray_apply  = binary ("CharArray_apply" , CharArray, I32,      I16 )
  lazy val CharArray_update = ternary("CharArray_update", CharArray, I32, I16, Unit)
  lazy val CharArray_length = unary  ("CharArray_length", CharArray,           I32 )

  lazy val ByteArray        = cls    ("ByteArray")
  lazy val ByteArray_apply  = binary ("ByteArray_apply" , ByteArray, I32,     I8  )
  lazy val ByteArray_update = ternary("ByteArray_update", ByteArray, I32, I8, Unit)
  lazy val ByteArray_length = unary  ("ByteArray_length", ByteArray,          I32 )

  lazy val ShortArray        = cls    ("ShortArray")
  lazy val ShortArray_apply  = binary ("ShortArray_apply" , ShortArray, I32,      I16 )
  lazy val ShortArray_update = ternary("ShortArray_update", ShortArray, I32, I16, Unit)
  lazy val ShortArray_length = unary  ("ShortArray_length", ShortArray,           I32 )

  lazy val IntArray        = cls    ("IntArray")
  lazy val IntArray_apply  = binary ("IntArray_apply" , IntArray, I32,      I32 )
  lazy val IntArray_update = ternary("IntArray_update", IntArray, I32, I32, Unit)
  lazy val IntArray_length = unary  ("IntArray_length", IntArray,           I32 )

  lazy val LongArray        = cls    ("LongArray")
  lazy val LongArray_apply  = binary ("LongArray_apply" , LongArray, I32,      I64 )
  lazy val LongArray_update = ternary("LongArray_update", LongArray, I32, I64, Unit)
  lazy val LongArray_length = unary  ("LongArray_length", LongArray,           I32 )

  lazy val FloatArray        = cls    ("FloatArray")
  lazy val FloatArray_apply  = binary ("FloatArray_apply" , FloatArray, I32,      F32 )
  lazy val FloatArray_update = ternary("FloatArray_update", FloatArray, I32, F32, Unit)
  lazy val FloatArray_length = unary  ("FloatArray_length", FloatArray,           I32 )

  lazy val DoubleArray        = cls    ("DoubleArray")
  lazy val DoubleArray_apply  = binary ("DoubleArray_apply" , DoubleArray, I32,      F64 )
  lazy val DoubleArray_update = ternary("DoubleArray_update", DoubleArray, I32, F64, Unit)
  lazy val DoubleArray_length = unary  ("DoubleArray_length", DoubleArray,           I32 )

  lazy val ObjectArray        = cls    ("ObjectArray")
  lazy val ObjectArray_apply  = binary ("ObjectArray_apply" , ObjectArray, I32,         Object)
  lazy val ObjectArray_update = ternary("ObjectArray_update", ObjectArray, I32, Object, Unit   )
  lazy val ObjectArray_length = unary  ("ObjectArray_length", ObjectArray,              I32    )

  lazy val Type         = struct("Type")
  lazy val Type_getId   = unary ("Type_getId",   Ptr(Type), I32   )
  lazy val Type_getName = unary ("Type_getName", Ptr(Type), String)

  lazy val Unit_type    = value ("Unit_type"   , Type)
  lazy val Nothing_type = value ("Nothing_type", Type)
  lazy val Null_type    = value ("Null_type"   , Type)
  lazy val Object_type  = value ("Object_type" , Type)
  lazy val Type_type    = value ("Type_type"   , Type)
  lazy val Bool_type    = value ("Bool_type"   , Type)
  lazy val Char_type    = value ("Char_type"   , Type)
  lazy val Byte_type    = value ("Byte_type"   , Type)
  lazy val Short_type   = value ("Short_type"  , Type)
  lazy val Int_type     = value ("Int_type"    , Type)
  lazy val Long_type    = value ("Long_type"   , Type)
  lazy val Float_type   = value ("Float_type"  , Type)
  lazy val Double_type  = value ("Double_type" , Type)
  lazy val String_type  = value ("String_type" , Type)

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

  lazy val Unit          = cls  ("Unit")
  lazy val Unit_unbox    = unary("Unit_unbox",    Unit, Unit  )
  lazy val Unit_box      = unary("Unit_box",      Unit, Unit  )
  lazy val Unit_toString = unary("Unit_toString", Unit, String)
  lazy val Unit_hashCode = unary("Unit_hashCode", Unit, I32   )
  lazy val Unit_value    = Val.Global(Global.intrinsic("Unit_value"), Nrt.Unit)

  lazy val Bool          = cls  ("Bool")
  lazy val Bool_box      = unary("Bool_box"     , Bool, Bool  )
  lazy val Bool_unbox    = unary("Bool_unbox"   , Bool, Bool  )
  lazy val Bool_toString = unary("Bool_toString", Bool, String)
  lazy val Bool_parse    = unary("Bool_parse"   , Bool, String)
  lazy val Bool_hashCode = unary("Bool_hashCode", Bool, I32   )

  lazy val Char          = cls  ("Char")
  lazy val Char_box      = unary("Char_box"      , I16 , Char  )
  lazy val Char_unbox    = unary("Char_unbox"    , Char, I16   )
  lazy val Char_toString = unary("Char_toString" , I16 , String)
  lazy val Char_hashCode = unary("Char_hashCode" , I16 , I32   )

  lazy val Byte           = cls   ("Byte")
  lazy val Byte_box       = unary ("Byte_box"      , I8  ,      Byte  )
  lazy val Byte_unbox     = unary ("Byte_unbox"    , Byte,      I8    )
  lazy val Byte_toString  = unary ("Byte_toString" , I8  ,      String)
  lazy val Byte_parse     = unary ("Byte_parse"    , I8  ,      String)
  lazy val Byte_parse_rdx = binary("Byte_parse_rdx", I8  , I32, String)
  lazy val Byte_hashCode  = unary ("Byte_hashCode" , I8  ,      I32   )

  lazy val Short           = cls   ("Short")
  lazy val Short_box       = unary ("Short_box"      , I16  ,      Short )
  lazy val Short_unbox     = unary ("Short_unbox"    , Short,      I16   )
  lazy val Short_toString  = unary ("Short_toString" , I16  ,      String)
  lazy val Short_parse     = unary ("Short_parse"    , I16  ,      String)
  lazy val Short_parse_rdx = binary("Short_parse_rdx", I16  , I32, String)
  lazy val Short_hashCode  = unary ("Short_hashCode" , I16  ,      I32   )

  lazy val Int                      = cls   ("Int")
  lazy val Int_box                  = unary ("Int_box"                 , I32,      Int   )
  lazy val Int_unbox                = unary ("Int_unbox"               , Int,      I32   )
  lazy val Int_toString             = unary ("Int_toString"            , I32,      String)
  lazy val Int_toUnsignedString     = unary ("Int_toUnsignedString"    , I32,      String)
  lazy val Int_toString_rdx         = binary("Int_toString_rdx"        , I32, I32, String)
  lazy val Int_toUnsignedString_rdx = binary("Int_toUnsignedString_rdx", I32, I32, String)
  lazy val Int_parse                = unary ("Int_parse"               , I32,      String)
  lazy val Int_parse_rdx            = binary("Int_parse_rdx"           , I32, I32, String)
  lazy val Int_parseUnsigned        = unary ("Int_parseUnsigned"       , I32,      String)
  lazy val Int_parseUnsigned_rdx    = binary("Int_parseUnsigned_rdx"   , I32, I32, String)
  lazy val Int_hashCode             = unary ("Int_hashCode"            , I32,      I32   )

  lazy val Long                      = cls   ("Long")
  lazy val Long_box                  = unary ("Long_box"                 , I64,      Int   )
  lazy val Long_unbox                = unary ("Long_unbox"               , Int,      I64   )
  lazy val Long_toString             = unary ("Long_toString"            , I64,      String)
  lazy val Long_toUnsignedString     = unary ("Long_toUnsignedString"    , I64,      String)
  lazy val Long_toString_rdx         = binary("Long_toString_rdx"        , I64, I32, String)
  lazy val Long_toUnsignedString_rdx = binary("Long_toUnsignedString_rdx", I64, I32, String)
  lazy val Long_parse                = unary ("Long_parse"               , I64,      String)
  lazy val Long_parse_rdx            = binary("Long_parse_rdx"           , I64, I32, String)
  lazy val Long_parseUnsigned        = unary ("Long_parseUnsigned"       , I64,      String)
  lazy val Long_parseUnsigned_rdx    = binary("Long_parseUnsigned_rdx"   , I64, I32, String)
  lazy val Long_hashCode             = unary ("Long_hashCode"            , I64,      I32   )

  lazy val Float          = cls  ("Float")
  lazy val Float_box      = unary("Float_box"     , F32  , Float )
  lazy val Float_unbox    = unary("Float_unbox"   , Float, F32   )
  lazy val Float_toString = unary("Float_toString", F32  , String)
  lazy val Float_parse    = unary("Float_parse"   , F32  , String)
  lazy val Float_hashCode = unary("Float_hashCode", F32  , I32   )

  lazy val Double          = cls  ("Double")
  lazy val Double_box      = unary("Double_box"     , F64   , Double)
  lazy val Double_unbox    = unary("Double_unbox"   , Double, F64   )
  lazy val Double_toString = unary("Double_toString", F64   , String)
  lazy val Double_parse    = unary("Double_parse"   , F64   , String)
  lazy val Double_hashCode = unary("Double_hashCode", F64   , I32   )

  lazy val String         = cls   ("String")
  lazy val String_CharAt  = binary("String_CharAt",  String,  I32,      I16   )
  lazy val String_concat  = binary("String_concat",  String,  String,   String)
  lazy val String_length  = unary ("String_length",  String,            I32   )

  lazy val alloc             = binary("alloc"            , Ptr(Type), Size, Object     )
  lazy val alloc_UnitArray   = unary ("alloc_UnitArray"  , I32,             UnitArray  )
  lazy val alloc_BoolArray   = unary ("alloc_BoolArray"  , I32,             BoolArray  )
  lazy val alloc_CharArray   = unary ("alloc_CharArray"  , I32,             CharArray  )
  lazy val alloc_ByteArray   = unary ("alloc_ByteArray"  , I32,             ByteArray  )
  lazy val alloc_ShortArray  = unary ("alloc_ShortArray" , I32,             ShortArray )
  lazy val alloc_IntArray    = unary ("alloc_IntArray"   , I32,             IntArray   )
  lazy val alloc_LongArray   = unary ("alloc_LongArray"  , I32,             LongArray  )
  lazy val alloc_FloatArray  = unary ("alloc_FloatArray" , I32,             FloatArray )
  lazy val alloc_DoubleArray = unary ("alloc_DoubleArray", I32,             DoubleArray)
  lazy val alloc_ObjectArray = unary ("alloc_ObjectArray", I32,             ObjectArray)

  lazy val init   = binary ("init" , I32, Ptr(Ptr(I8)), ObjectArray)
  lazy val yield_ = nullary("yield",                    Unit        )

  lazy val excrec      = nir.Type.AnonStruct(Seq(Ptr(I8), I32))
  lazy val throw_      = unary  ("throw"      , Object, Nothing)
  lazy val begin_catch = unary  ("begin_catch", Object, Unit   )
  lazy val end_catch   = nullary("end_catch"  ,         Unit   )

  lazy val Array = Map[Type, Class](
    Unit    -> UnitArray  ,
    Bool    -> BoolArray  ,
    Char    -> CharArray  ,
    Byte    -> ByteArray  ,
    Short   -> ShortArray ,
    Int     -> IntArray 	 ,
    Long    -> LongArray  ,
    Float   -> FloatArray ,
    Double  -> DoubleArray,
    Object -> ObjectArray
  )

  lazy val Array_apply = Map[Type, Val.Global](
    Unit    -> UnitArray_apply  ,
    Bool    -> BoolArray_apply  ,
    Char    -> CharArray_apply  ,
    Byte    -> ByteArray_apply  ,
    Short   -> ShortArray_apply ,
    Int     -> IntArray_apply   ,
    Long    -> LongArray_apply  ,
    Float   -> FloatArray_apply ,
    Double  -> DoubleArray_apply,
    Object -> ObjectArray_apply
  )

  lazy val Array_update = Map[Type, Val.Global](
    Unit    -> UnitArray_update  ,
    Bool    -> BoolArray_update  ,
    Char    -> CharArray_update  ,
    Byte    -> ByteArray_update  ,
    Short   -> ShortArray_update ,
    Int     -> IntArray_update   ,
    Long    -> LongArray_update  ,
    Float   -> FloatArray_update ,
    Double  -> DoubleArray_update,
    Object -> ObjectArray_update
  )

  lazy val Array_length = Map[Type, Val.Global](
    Unit    -> UnitArray_length  ,
    Bool    -> BoolArray_length  ,
    Char    -> CharArray_length  ,
    Byte    -> ByteArray_length  ,
    Short   -> ShortArray_length ,
    Int     -> IntArray_length   ,
    Long    -> LongArray_length  ,
    Float   -> FloatArray_length ,
    Double  -> DoubleArray_length,
    Object  -> ObjectArray_length
  )

  lazy val allocArray = Map[Type, Val.Global](
    Unit    -> alloc_UnitArray  ,
    Bool    -> alloc_BoolArray  ,
    Char    -> alloc_CharArray  ,
    Byte    -> alloc_ByteArray  ,
    Short   -> alloc_ShortArray ,
    Int     -> alloc_IntArray   ,
    Long    -> alloc_LongArray  ,
    Float   -> alloc_FloatArray ,
    Double  -> alloc_DoubleArray,
    Object -> alloc_ObjectArray
  )

  lazy val box = Map[Type, Val.Global](
    Unit   -> Unit_unbox,
    Bool   -> Bool_box  ,
    Char   -> Char_box  ,
    Byte   -> Byte_box  ,
    Short  -> Short_box ,
    Int    -> Int_box   ,
    Long   -> Long_box  ,
    Float  -> Float_box ,
    Double -> Double_box
  )

  lazy val unbox = Map[Type, Val.Global](
    Unit   -> Unit_unbox  ,
    Bool   -> Bool_unbox  ,
    Char   -> Char_unbox  ,
    Byte   -> Byte_unbox  ,
    Short  -> Short_unbox ,
    Int    -> Int_unbox   ,
    Long   -> Long_unbox  ,
    Float  -> Float_unbox ,
    Double -> Double_unbox
  )

  lazy val toString_ = Map[Type, Val.Global](
    Object  -> Object_toString,
    Unit    -> Unit_toString  ,
    Bool    -> Bool_toString  ,
    Char    -> Char_toString  ,
    Byte    -> Byte_toString  ,
    Short   -> Short_toString ,
    Int     -> Int_toString   ,
    Long    -> Long_toString  ,
    Float   -> Float_toString ,
    Double  -> Double_toString
  )

  lazy val toUnsignedString = Map[Type, Val.Global](
    Int  -> Int_toUnsignedString ,
    Long -> Long_toUnsignedString
  )

  lazy val toString_rdx = Map[Type, Val.Global](
    Int  -> Int_toString_rdx ,
    Long -> Long_toString_rdx
  )

  lazy val toUnsignedString_rdx = Map[Type, Val.Global](
    Int  -> Int_toUnsignedString_rdx ,
    Long -> Long_toUnsignedString_rdx
  )

  lazy val parse = Map[Type, Val.Global](
    Bool   -> Bool_parse  ,
    Byte   -> Byte_parse  ,
    Short  -> Short_parse ,
    Int    -> Int_parse   ,
    Long   -> Long_parse  ,
    Float  -> Float_parse ,
    Double -> Double_parse
  )

  lazy val parseUnsigned = Map[Type, Val.Global](
    Int  -> Int_parseUnsigned ,
    Long -> Long_parseUnsigned
  )

  lazy val parse_rdx = Map[Type, Val.Global](
    Byte  -> Byte_parse_rdx ,
    Short -> Short_parse_rdx,
    Int   -> Int_parse_rdx  ,
    Long  -> Long_parse_rdx
  )

  lazy val parseUnsigned_rdx = Map[Type, Val.Global](
    Int  -> Int_parseUnsigned_rdx,
    Long -> Long_parseUnsigned_rdx
  )

  lazy val hashCode_ = Map[Type, Val.Global](
    Unit   -> Unit_hashCode  ,
    Bool   -> Bool_hashCode  ,
    Char   -> Char_hashCode  ,
    Byte   -> Byte_hashCode  ,
    Short  -> Short_hashCode ,
    Int    -> Int_hashCode   ,
    Long   -> Long_hashCode  ,
    Float  -> Float_hashCode ,
    Double -> Double_hashCode
  )

  lazy val types = Map[Type, Val.Global](
    Nothing  -> Nothing_type,
    Null     -> Null_type   ,
    Object   -> Object_type ,
    Type     -> Type_type   ,
    Unit     -> Unit_type   ,
    Bool     -> Bool_type   ,
    Char     -> Char_type   ,
    Byte     -> Byte_type   ,
    Short    -> Short_type  ,
    Int      -> Int_type    ,
    Long     -> Long_type   ,
    Float    -> Float_type  ,
    Double   -> Double_type
  )

  lazy val arrayTypes = Map[Type, Val.Global](
    Unit    -> UnitArray_type  ,
    Bool    -> BoolArray_type  ,
    Char    -> CharArray_type  ,
    Byte    -> ByteArray_type  ,
    Short   -> ShortArray_type ,
    Int     -> IntArray_type   ,
    Long    -> LongArray_type  ,
    Float   -> FloatArray_type ,
    Double  -> DoubleArray_type,
    Object  -> ObjectArray_type
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
