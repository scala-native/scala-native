package native
package nir

import Type._

object Intr {
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

  def call(intr: Val.Global, args: Val*): Op = {
    val Val.Global(_, Ptr(ty)) = intr
    Op.Call(ty, intr, args)
  }

  lazy val object_          = cls   ("object")
  lazy val object_init      = unary ("object_init"    , object_,          unit      )
  lazy val object_equals    = binary("object_equals"  , object_, object_, Bool      )
  lazy val object_toString  = unary ("object_toString", object_,          string    )
  lazy val object_hashCode  = unary ("object_hashCode", object_,          I32       )
  lazy val object_getType   = unary ("object_getType" , object_,          Ptr(type_))

  lazy val monitor_enter     = unary  ("monitor_enter"    , object_,           unit  )
  lazy val monitor_exit      = unary  ("monitor_exit"     , object_,           unit  )
  lazy val monitor_notify    = unary  ("monitor_notify"   , object_,           unit  )
  lazy val monitor_notifyAll = unary  ("monitor_notifyAll", object_,           unit  )
  lazy val monitor_wait      = ternary("monitor_wait"     , object_, I64, I32, unit  )

  lazy val any_array        = cls    ("any_array")
  lazy val any_array_apply  = binary ("any_array_apply" , any_array, I32,          object_)
  lazy val any_array_update = ternary("any_array_update", any_array, I32, object_, unit   )
  lazy val any_array_length = unary  ("any_array_length", any_array,               I32    )

  lazy val unit_array        = cls    ("unit_array")
  lazy val unit_array_apply  = binary ("unit_array_apply" , unit_array, I32,       unit)
  lazy val unit_array_update = ternary("unit_array_update", unit_array, I32, unit, unit)
  lazy val unit_array_length = unary  ("unit_array_length", unit_array,            I32 )

  lazy val bool_array        = cls    ("bool_array")
  lazy val bool_array_apply  = binary ("bool_array_apply" , bool_array, I32,       Bool)
  lazy val bool_array_update = ternary("bool_array_update", bool_array, I32, Bool, unit)
  lazy val bool_array_length = unary  ("bool_array_length", bool_array,            I32 )

  lazy val char_array        = cls    ("char_array")
  lazy val char_array_apply  = binary ("char_array_apply" , char_array, I32,      I16 )
  lazy val char_array_update = ternary("char_array_update", char_array, I32, I16, unit)
  lazy val char_array_length = unary  ("char_array_length", char_array,           I32 )

  lazy val byte_array        = cls    ("byte_array")
  lazy val byte_array_apply  = binary ("byte_array_apply" , byte_array, I32,     I8  )
  lazy val byte_array_update = ternary("byte_array_update", byte_array, I32, I8, unit)
  lazy val byte_array_length = unary  ("byte_array_length", byte_array,          I32 )

  lazy val short_array        = cls    ("short_array")
  lazy val short_array_apply  = binary ("short_array_apply" , short_array, I32,      I16 )
  lazy val short_array_update = ternary("short_array_update", short_array, I32, I16, unit)
  lazy val short_array_length = unary  ("short_array_length", short_array,           I32 )

  lazy val int_array        = cls    ("int_array")
  lazy val int_array_apply  = binary ("int_array_apply" , int_array, I32,      I32 )
  lazy val int_array_update = ternary("int_array_update", int_array, I32, I32, unit)
  lazy val int_array_length = unary  ("int_array_length", int_array,           I32 )

  lazy val long_array        = cls    ("long_array")
  lazy val long_array_apply  = binary ("long_array_apply" , long_array, I32,      I64 )
  lazy val long_array_update = ternary("long_array_update", long_array, I32, I64, unit)
  lazy val long_array_length = unary  ("long_array_length", long_array,           I32 )

  lazy val float_array        = cls    ("float_array")
  lazy val float_array_apply  = binary ("float_array_apply" , float_array, I32,      F32 )
  lazy val float_array_update = ternary("float_array_update", float_array, I32, F32, unit)
  lazy val float_array_length = unary  ("float_array_length", float_array,           I32 )

  lazy val double_array        = cls    ("double_array")
  lazy val double_array_apply  = binary ("double_array_apply" , double_array, I32,      F64 )
  lazy val double_array_update = ternary("double_array_update", double_array, I32, F64, unit)
  lazy val double_array_length = unary  ("double_array_length", double_array,           I32 )

  lazy val object_array        = cls    ("object_array")
  lazy val object_array_apply  = binary ("object_array_apply" , object_array, I32,          object_)
  lazy val object_array_update = ternary("object_array_update", object_array, I32, object_, unit   )
  lazy val object_array_length = unary  ("object_array_length", object_array,               I32    )

  lazy val type_        = struct("type")
  lazy val type_getId   = unary ("type_getId",   Ptr(type_), I32   )
  lazy val type_getName = unary ("type_getName", Ptr(type_), string)

  lazy val unit_type    = value ("unit_type"   , type_)
  lazy val nothing_type = value ("nothing_type", type_)
  lazy val null_type    = value ("null_type"   , type_)
  lazy val object_type  = value ("object_type" , type_)
  lazy val type_type    = value ("type_type"   , type_)
  lazy val bool_type    = value ("bool_type"   , type_)
  lazy val char_type    = value ("char_type"   , type_)
  lazy val byte_type    = value ("byte_type"   , type_)
  lazy val short_type   = value ("short_type"  , type_)
  lazy val int_type     = value ("int_type"    , type_)
  lazy val long_type    = value ("long_type"   , type_)
  lazy val float_type   = value ("float_type"  , type_)
  lazy val double_type  = value ("double_type" , type_)
  lazy val string_type  = value ("string_type" , type_)

  lazy val unit_array_type   = value("unit_array_type"  , type_)
  lazy val bool_array_type   = value("bool_array_type"  , type_)
  lazy val char_array_type   = value("char_array_type"  , type_)
  lazy val byte_array_type   = value("byte_array_type"  , type_)
  lazy val short_array_type  = value("short_array_type" , type_)
  lazy val int_array_type    = value("int_array_type"   , type_)
  lazy val long_array_type   = value("long_array_type"  , type_)
  lazy val float_array_type  = value("float_array_type" , type_)
  lazy val double_array_type = value("double_array_type", type_)
  lazy val object_array_type = value("object_array_type", type_)

  lazy val unit          = cls  ("unit")
  lazy val unit_unbox    = unary("unit_unbox",    unit, unit  )
  lazy val unit_box      = unary("unit_box",      unit, unit  )
  lazy val unit_toString = unary("unit_toString", unit, string)
  lazy val unit_hashCode = unary("unit_hashCode", unit, I32   )
  lazy val unit_value    = Val.Global(Global.intrinsic("unit_value"), Intr.unit)

  lazy val bool          = cls  ("bool")
  lazy val bool_box      = unary("bool_box"     , Bool, bool  )
  lazy val bool_unbox    = unary("bool_unbox"   , bool, Bool  )
  lazy val bool_toString = unary("bool_toString", Bool, string)
  lazy val bool_parse    = unary("bool_parse"   , Bool, string)
  lazy val bool_hashCode = unary("bool_hashCode", Bool, I32   )

  lazy val char          = cls  ("char")
  lazy val char_box      = unary("char_box"      , I16 , char  )
  lazy val char_unbox    = unary("char_unbox"    , char, I16   )
  lazy val char_toString = unary("char_toString" , I16 , string)
  lazy val char_hashCode = unary("char_hashCode" , I16 , I32   )

  lazy val byte           = cls   ("byte")
  lazy val byte_box       = unary ("byte_box"      , I8  ,      byte  )
  lazy val byte_unbox     = unary ("byte_unbox"    , byte,      I8    )
  lazy val byte_toString  = unary ("byte_toString" , I8  ,      string)
  lazy val byte_parse     = unary ("byte_parse"    , I8  ,      string)
  lazy val byte_parse_rdx = binary("byte_parse_rdx", I8  , I32, string)
  lazy val byte_hashCode  = unary ("byte_hashCode" , I8  ,      I32   )

  lazy val short           = cls   ("short")
  lazy val short_box       = unary ("short_box"      , I16  ,      short )
  lazy val short_unbox     = unary ("short_unbox"    , short,      I16   )
  lazy val short_toString  = unary ("short_toString" , I16  ,      string)
  lazy val short_parse     = unary ("short_parse"    , I16  ,      string)
  lazy val short_parse_rdx = binary("short_parse_rdx", I16  , I32, string)
  lazy val short_hashCode  = unary ("short_hashCode" , I16  ,      I32   )

  lazy val int                      = cls   ("int")
  lazy val int_box                  = unary ("int_box"                 , I32,      int   )
  lazy val int_unbox                = unary ("int_unbox"               , int,      I32   )
  lazy val int_toString             = unary ("int_toString"            , I32,      string)
  lazy val int_toUnsignedString     = unary ("int_toUnsignedString"    , I32,      string)
  lazy val int_toString_rdx         = binary("int_toString_rdx"        , I32, I32, string)
  lazy val int_toUnsignedString_rdx = binary("int_toUnsignedString_rdx", I32, I32, string)
  lazy val int_parse                = unary ("int_parse"               , I32,      string)
  lazy val int_parse_rdx            = binary("int_parse_rdx"           , I32, I32, string)
  lazy val int_parseUnsigned        = unary ("int_parseUnsigned"       , I32,      string)
  lazy val int_parseUnsigned_rdx    = binary("int_parseUnsigned_rdx"   , I32, I32, string)
  lazy val int_hashCode             = unary ("int_hashCode"            , I32,      I32   )

  lazy val long                      = cls   ("long")
  lazy val long_box                  = unary ("long_box"                 , I64,      int   )
  lazy val long_unbox                = unary ("long_unbox"               , int,      I64   )
  lazy val long_toString             = unary ("long_toString"            , I64,      string)
  lazy val long_toUnsignedString     = unary ("long_toUnsignedString"    , I64,      string)
  lazy val long_toString_rdx         = binary("long_toString_rdx"        , I64, I32, string)
  lazy val long_toUnsignedString_rdx = binary("long_toUnsignedString_rdx", I64, I32, string)
  lazy val long_parse                = unary ("long_parse"               , I64,      string)
  lazy val long_parse_rdx            = binary("long_parse_rdx"           , I64, I32, string)
  lazy val long_parseUnsigned        = unary ("long_parseUnsigned"       , I64,      string)
  lazy val long_parseUnsigned_rdx    = binary("long_parseUnsigned_rdx"   , I64, I32, string)
  lazy val long_hashCode             = unary ("long_hashCode"            , I64,      I32   )

  lazy val float          = cls  ("float")
  lazy val float_box      = unary("float_box"     , F32  , float )
  lazy val float_unbox    = unary("float_unbox"   , float, F32   )
  lazy val float_toString = unary("float_toString", F32  , string)
  lazy val float_parse    = unary("float_parse"   , F32  , string)
  lazy val float_hashCode = unary("float_hashCode", F32  , I32   )

  lazy val double          = cls  ("double")
  lazy val double_box      = unary("double_box"     , F64   , double)
  lazy val double_unbox    = unary("double_unbox"   , double, F64   )
  lazy val double_toString = unary("double_toString", F64   , string)
  lazy val double_parse    = unary("double_parse"   , F64   , string)
  lazy val double_hashCode = unary("double_hashCode", F64   , I32   )

  lazy val string         = cls   ("string")
  lazy val string_charAt  = binary("string_charAt",  string,  I32,      I16   )
  lazy val string_concat  = binary("string_concat",  string,  string,   string)
  lazy val string_length  = unary ("string_length",  string,            I32   )

  lazy val alloc              = binary("alloc"             , Ptr(type_), Size, object_     )
  lazy val alloc_unit_array   = unary ("alloc_unit_array"  , I32,              unit_array  )
  lazy val alloc_bool_array   = unary ("alloc_bool_array"  , I32,              bool_array  )
  lazy val alloc_char_array   = unary ("alloc_char_array"  , I32,              char_array  )
  lazy val alloc_byte_array   = unary ("alloc_byte_array"  , I32,              byte_array  )
  lazy val alloc_short_array  = unary ("alloc_short_array" , I32,              short_array )
  lazy val alloc_int_array    = unary ("alloc_int_array"   , I32,              int_array   )
  lazy val alloc_long_array   = unary ("alloc_long_array"  , I32,              long_array  )
  lazy val alloc_float_array  = unary ("alloc_float_array" , I32,              float_array )
  lazy val alloc_double_array = unary ("alloc_double_array", I32,              double_array)
  lazy val alloc_object_array = unary ("alloc_object_array", I32,              object_array)

  lazy val init   = binary ("init" , I32, Ptr(Ptr(I8)), object_array)
  lazy val yield_ = nullary("yield",                    unit        )

  lazy val excrec      = Type.AnonStruct(Seq(Type.Ptr(I8), I32))
  lazy val throw_      = unary  ("throw"      , object_, Nothing)
  lazy val begin_catch = unary  ("begin_catch", object_, unit   )
  lazy val end_catch   = nullary("end_catch"  ,          unit   )

  lazy val array = Map[Type, Class](
    unit    -> unit_array  ,
    bool    -> bool_array  ,
    char    -> char_array  ,
    byte    -> byte_array  ,
    short   -> short_array ,
    int     -> int_array 	 ,
    long    -> long_array  ,
    float   -> float_array ,
    double  -> double_array,
    object_ -> object_array
  )

  lazy val array_apply = Map[Type, Val.Global](
    unit    -> unit_array_apply  ,
    bool    -> bool_array_apply  ,
    char    -> char_array_apply  ,
    byte    -> byte_array_apply  ,
    short   -> short_array_apply ,
    int     -> int_array_apply   ,
    long    -> long_array_apply  ,
    float   -> float_array_apply ,
    double  -> double_array_apply,
    object_ -> object_array_apply
  )

  lazy val array_update = Map[Type, Val.Global](
    unit    -> unit_array_update  ,
    bool    -> bool_array_update  ,
    char    -> char_array_update  ,
    byte    -> byte_array_update  ,
    short   -> short_array_update ,
    int     -> int_array_update   ,
    long    -> long_array_update  ,
    float   -> float_array_update ,
    double  -> double_array_update,
    object_ -> object_array_update
  )

  lazy val array_length = Map[Type, Val.Global](
    unit    -> unit_array_length  ,
    bool    -> bool_array_length  ,
    char    -> char_array_length  ,
    byte    -> byte_array_length  ,
    short   -> short_array_length ,
    int     -> int_array_length   ,
    long    -> long_array_length  ,
    float   -> float_array_length ,
    double  -> double_array_length,
    object_ -> object_array_length
  )

  lazy val alloc_array = Map[Type, Val.Global](
    unit    -> alloc_unit_array  ,
    bool    -> alloc_bool_array  ,
    char    -> alloc_char_array  ,
    byte    -> alloc_byte_array  ,
    short   -> alloc_short_array ,
    int     -> alloc_int_array   ,
    long    -> alloc_long_array  ,
    float   -> alloc_float_array ,
    double  -> alloc_double_array,
    object_ -> alloc_object_array
  )

  lazy val box = Map[Type, Val.Global](
    unit   -> unit_unbox,
    bool   -> bool_box  ,
    char   -> char_box  ,
    byte   -> byte_box  ,
    short  -> short_box ,
    int    -> int_box   ,
    long   -> long_box  ,
    float  -> float_box ,
    double -> double_box
  )

  lazy val unbox = Map[Type, Val.Global](
    unit   -> unit_unbox  ,
    bool   -> bool_unbox  ,
    char   -> char_unbox  ,
    byte   -> byte_unbox  ,
    short  -> short_unbox ,
    int    -> int_unbox   ,
    long   -> long_unbox  ,
    float  -> float_unbox ,
    double -> double_unbox
  )

  lazy val toString_ = Map[Type, Val.Global](
    object_ -> object_toString,
    unit    -> unit_toString  ,
    bool    -> bool_toString  ,
    char    -> char_toString  ,
    byte    -> byte_toString  ,
    short   -> short_toString ,
    int     -> int_toString   ,
    long    -> long_toString  ,
    float   -> float_toString ,
    double  -> double_toString
  )

  lazy val toUnsignedString = Map[Type, Val.Global](
    int  -> int_toUnsignedString ,
    long -> long_toUnsignedString
  )

  lazy val toString_rdx = Map[Type, Val.Global](
    int  -> int_toString_rdx ,
    long -> long_toString_rdx
  )

  lazy val toUnsignedString_rdx = Map[Type, Val.Global](
    int  -> int_toUnsignedString_rdx ,
    long -> long_toUnsignedString_rdx
  )

  lazy val parse = Map[Type, Val.Global](
    bool   -> bool_parse  ,
    byte   -> byte_parse  ,
    short  -> short_parse ,
    int    -> int_parse   ,
    long   -> long_parse  ,
    float  -> float_parse ,
    double -> double_parse
  )

  lazy val parseUnsigned = Map[Type, Val.Global](
    int  -> int_parseUnsigned ,
    long -> long_parseUnsigned
  )

  lazy val parse_rdx = Map[Type, Val.Global](
    byte  -> byte_parse_rdx ,
    short -> short_parse_rdx,
    int   -> int_parse_rdx  ,
    long  -> long_parse_rdx
  )

  lazy val parseUnsigned_rdx = Map[Type, Val.Global](
    int  -> int_parseUnsigned_rdx,
    long -> long_parseUnsigned_rdx
  )

  lazy val hashCode_ = Map[Type, Val.Global](
    unit   -> unit_hashCode  ,
    bool   -> bool_hashCode  ,
    char   -> char_hashCode  ,
    byte   -> byte_hashCode  ,
    short  -> short_hashCode ,
    int    -> int_hashCode   ,
    long   -> long_hashCode  ,
    float  -> float_hashCode ,
    double -> double_hashCode
  )

  lazy val intrinsic_type = Map[Type, Val.Global](
    Nothing  -> nothing_type,
    Null     -> null_type   ,
    object_  -> object_type ,
    type_    -> type_type   ,
    unit     -> unit_type   ,
    bool     -> bool_type   ,
    char     -> char_type   ,
    byte     -> byte_type   ,
    short    -> short_type  ,
    int      -> int_type    ,
    long     -> long_type   ,
    float    -> float_type  ,
    double   -> double_type
  )

  lazy val array_type = Map[Type, Val.Global](
    unit    -> unit_array_type  ,
    bool    -> bool_array_type  ,
    char    -> char_array_type  ,
    byte    -> byte_array_type  ,
    short   -> short_array_type ,
    int     -> int_array_type   ,
    long    -> long_array_type  ,
    float   -> float_array_type ,
    double  -> double_array_type,
    object_ -> object_array_type
  )

  lazy val layout = Map[Global, Seq[Type]](
    any_array.name    -> Seq(Ptr(type_), I32)                   ,
    unit_array.name   -> Seq(Ptr(type_), I32)                   ,
    bool_array.name   -> Seq(Ptr(type_), I32, Array(Bool, 0))   ,
    char_array.name   -> Seq(Ptr(type_), I32, Array(I16, 0))    ,
    byte_array.name   -> Seq(Ptr(type_), I32, Array(I8, 0))     ,
    short_array.name  -> Seq(Ptr(type_), I32, Array(I16, 0))    ,
    int_array.name    -> Seq(Ptr(type_), I32, Array(I32, 0))    ,
    long_array.name   -> Seq(Ptr(type_), I32, Array(I64, 0))    ,
    float_array.name  -> Seq(Ptr(type_), I32, Array(F32, 0))    ,
    double_array.name -> Seq(Ptr(type_), I32, Array(F64, 0))    ,
    object_array.name -> Seq(Ptr(type_), I32, Array(Ptr(I8), 0)),
    type_.name        -> Seq(Ptr(type_), I32, Ptr(I8))          ,
    string.name       -> Seq(Ptr(type_), I32, Ptr(I8))
  )
}
