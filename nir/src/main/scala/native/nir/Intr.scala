package native
package nir

import Type._

object Intr {
  private def value(id: String, ty: Type) =
    Val.Global(Global.intrinsic(id), Type.Ptr(ty))
  private def intrinsic(id: String, args: Seq[Type], ret: Type) =
    Val.Global(Global.intrinsic(id.split("_"): _*), Type.Ptr(Type.Function(args, ret)))
  private def nullary(id: String, to: Type) =
    intrinsic(id, Seq(), to)
  private def unary(id: String, from: Type, to: Type) =
    intrinsic(id, Seq(from), to)
  private def binary(id: String, from1: Type, from2: Type, to: Type) =
    intrinsic(id, Seq(from1, from2), to)
  private def ternary(id: String, from1: Type, from2: Type, from3: Type, to: Type) =
    intrinsic(id, Seq(from1, from2, from3), to)
  private def cls(id: String) =
    Type.Class(Global.intrinsic(id))
  private def struct(id: String) =
    Type.Struct(Global.intrinsic(id))

  def call(intr: Val.Global, args: Val*): Op = {
    val Val.Global(_, Type.Ptr(ty)) = intr
    Op.Call(ty, intr, args)
  }

  lazy val object_          = cls   ("object")
  lazy val object_init      = unary ("object_init"    , object_,          Unit   )
  lazy val object_equals    = binary("object_equals"  , object_, object_, Bool   )
  lazy val object_toString  = unary ("object_toString", object_,          string )
  lazy val object_hashCode  = unary ("object_hashCode", object_,          I32    )

  lazy val array_bool   = struct("array_bool")
  lazy val array_char   = struct("array_char")
  lazy val array_byte   = struct("array_byte")
  lazy val array_short  = struct("array_short")
  lazy val array_int    = struct("array_int")
  lazy val array_long   = struct("array_long")
  lazy val array_float  = struct("array_float")
  lazy val array_double = struct("array_double")
  lazy val array_object = struct("array_object")

  lazy val monitor           = cls    ("monitor")
  lazy val monitor_enter     = unary  ("monitor_enter"    , object_,           Unit  )
  lazy val monitor_exit      = unary  ("monitor_exit"     , object_,           Unit  )
  lazy val monitor_notify    = unary  ("monitor_notify"   , object_,           Unit  )
  lazy val monitor_notifyAll = unary  ("monitor_notifyAll", object_,           Unit  )
  lazy val monitor_wait      = ternary("monitor_wait"     , object_, I64, I32, Unit  )

  lazy val type_     = struct("type")
  lazy val type_name = unary ("type_name"      , Type.Ptr(type_), string         )
  lazy val type_size = unary ("type_size"      , Type.Ptr(type_), Size           )
  lazy val type_of   = unary ("type_of"        , object_        , Type.Ptr(type_))

  lazy val type_of_null    = value ("type_of_null"   , type_)
  lazy val type_of_object  = value ("type_of_object" , type_)
  lazy val type_of_monitor = value ("type_of_monitor", type_)
  lazy val type_of_type    = value ("type_of_type"   , type_)
  lazy val type_of_bool    = value ("type_of_bool"   , type_)
  lazy val type_of_char    = value ("type_of_char"   , type_)
  lazy val type_of_byte    = value ("type_of_byte"   , type_)
  lazy val type_of_short   = value ("type_of_short"  , type_)
  lazy val type_of_int     = value ("type_of_int"    , type_)
  lazy val type_of_long    = value ("type_of_long"   , type_)
  lazy val type_of_float   = value ("type_of_float"  , type_)
  lazy val type_of_double  = value ("type_of_double" , type_)
  lazy val type_of_string  = value ("type_of_string" , type_)

  lazy val type_of_array_bool   = value("type_of_array_bool"  , type_)
  lazy val type_of_array_char   = value("type_of_array_char"  , type_)
  lazy val type_of_array_byte   = value("type_of_array_byte"  , type_)
  lazy val type_of_array_short  = value("type_of_array_short" , type_)
  lazy val type_of_array_int    = value("type_of_array_int"   , type_)
  lazy val type_of_array_long   = value("type_of_array_long"  , type_)
  lazy val type_of_array_float  = value("type_of_array_float" , type_)
  lazy val type_of_array_double = value("type_of_array_double", type_)
  lazy val type_of_array_object = value("type_of_array_object", type_)

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

  lazy val alloc  = binary ("alloc", Type.Ptr(type_), Size, object_)
  lazy val init   = binary ("init" , Type.I32, Type.Ptr(Type.Ptr(Type.I8)), ArrayClass(string))
  lazy val yield_ = nullary("yield", Unit)
  lazy val throw_ = unary  ("throw", object_, Type.Nothing)

  val array = Map[Type, Type](
    bool    -> array_bool  ,
    char    -> array_char  ,
    byte    -> array_byte  ,
    short   -> array_short ,
    int     -> array_int 	 ,
    long    -> array_long  ,
    float   -> array_float ,
    double  -> array_double,
    object_ -> array_object
  )

  val box = Map[Type, Val.Global](
    bool   -> bool_box  ,
    char   -> char_box  ,
    byte   -> byte_box  ,
    short  -> short_box ,
    int    -> int_box   ,
    long   -> long_box  ,
    float  -> float_box ,
    double -> double_box
  )

  val unbox = Map[Type, Val.Global](
    bool   -> bool_unbox  ,
    char   -> char_unbox  ,
    byte   -> byte_unbox  ,
    short  -> short_unbox ,
    int    -> int_unbox   ,
    long   -> long_unbox  ,
    float  -> float_unbox ,
    double -> double_unbox
  )

  val toString_ = Map[Type, Val.Global](
    object_ -> object_toString,
    bool    -> bool_toString  ,
    char    -> char_toString  ,
    byte    -> byte_toString  ,
    short   -> short_toString ,
    int     -> int_toString   ,
    long    -> long_toString  ,
    float   -> float_toString ,
    double  -> double_toString
  )

  val toUnsignedString = Map[Type, Val.Global](
    int  -> int_toUnsignedString ,
    long -> long_toUnsignedString
  )

  val toString_rdx = Map[Type, Val.Global](
    int  -> int_toString_rdx ,
    long -> long_toString_rdx
  )

  val toUnsignedString_rdx = Map[Type, Val.Global](
    int  -> int_toUnsignedString_rdx ,
    long -> long_toUnsignedString_rdx
  )

  val parse = Map[Type, Val.Global](
    bool   -> bool_parse  ,
    byte   -> byte_parse  ,
    short  -> short_parse ,
    int    -> int_parse   ,
    long   -> long_parse  ,
    float  -> float_parse ,
    double -> double_parse
  )

  val parseUnsigned = Map[Type, Val.Global](
    int  -> int_parseUnsigned ,
    long -> long_parseUnsigned
  )

  val parse_rdx = Map[Type, Val.Global](
    byte  -> byte_parse_rdx ,
    short -> short_parse_rdx,
    int   -> int_parse_rdx  ,
    long  -> long_parse_rdx
  )

  val parseUnsigned_rdx = Map[Type, Val.Global](
    int  -> int_parseUnsigned_rdx,
    long -> long_parseUnsigned_rdx
  )

  val hashCode_ = Map[Type, Val.Global](
    bool   -> bool_hashCode  ,
    char   -> char_hashCode  ,
    byte   -> byte_hashCode  ,
    short  -> short_hashCode ,
    int    -> int_hashCode   ,
    long   -> long_hashCode  ,
    float  -> float_hashCode ,
    double -> double_hashCode
  )

  val type_of_intrinsic = Map[Type, Val.Global](
    Type.Null -> type_of_null   ,
    object_   -> type_of_object ,
    monitor   -> type_of_monitor,
    type_     -> type_of_type   ,
    bool      -> type_of_bool   ,
    char      -> type_of_char   ,
    byte      -> type_of_byte   ,
    short     -> type_of_short  ,
    int       -> type_of_int    ,
    long      -> type_of_long   ,
    float     -> type_of_float  ,
    double    -> type_of_double
  )

  lazy val type_of_array = Map[Type, Val.Global](
    bool    -> type_of_array_bool  ,
    char    -> type_of_array_char  ,
    byte    -> type_of_array_byte  ,
    short   -> type_of_array_short ,
    int     -> type_of_array_int   ,
    long    -> type_of_array_long  ,
    float   -> type_of_array_float ,
    double  -> type_of_array_double,
    object_ -> type_of_array_object
  )

  val structs = Map[Global, Seq[Type]](
    array_bool.name   -> Seq(Ptr(type_), I32, Array(Bool, 0))   ,
    array_char.name   -> Seq(Ptr(type_), I32, Array(I16, 0))    ,
    array_byte.name   -> Seq(Ptr(type_), I32, Array(I8, 0))     ,
    array_short.name  -> Seq(Ptr(type_), I32, Array(I16, 0))    ,
    array_int.name    -> Seq(Ptr(type_), I32, Array(I32, 0))    ,
    array_long.name   -> Seq(Ptr(type_), I32, Array(I64, 0))    ,
    array_float.name  -> Seq(Ptr(type_), I32, Array(F32, 0))    ,
    array_double.name -> Seq(Ptr(type_), I32, Array(F64, 0))    ,
    array_object.name -> Seq(Ptr(type_), I32, Array(Ptr(I8), 0)),
    type_.name        -> Seq(Ptr(type_))               ,
    string.name       -> Seq(Ptr(type_), I32, Ptr(Type.I8))
  )
}
