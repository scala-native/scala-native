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

  lazy val String          = cls   ("java.lang.String")
  lazy val RefArray        = cls   ("scala.scalanative.runtime.RefArray")
  lazy val BoxedUnit       = module("scala.scalanative.runtime.BoxedUnit")
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

  lazy val Type         = struct("Type")
  lazy val Type_getId   = unary ("Type_getId",   Ptr(Type), I32   )
  lazy val Type_getName = unary ("Type_getName", Ptr(Type), String)

  lazy val Nothing_type     = value("Nothing_type", Type)
  lazy val Null_type        = value("Null_type"   , Type)
  lazy val Object_type      = value("Object_type" , Type)
  lazy val Monitor_type     = value("Monitor_type", Type)
  lazy val Type_type        = value("Type_type"   , Type)

  lazy val init   = binary ("init" , I32, Ptr(Ptr(I8)), RefArray)
  lazy val yield_ = nullary("yield",                    Unit    )

  lazy val excrec      = nir.Type.AnonStruct(Seq(Ptr(I8), I32))
  lazy val throw_      = unary  ("throw"      , Object,  Nothing)
  lazy val begin_catch = unary  ("begin_catch", Ptr(I8), Unit   )
  lazy val end_catch   = nullary("end_catch"  ,          Unit   )

  lazy val types = Map[Type, Val.Global](
    Nothing -> Nothing_type,
    Null    -> Null_type   ,
    Object  -> Object_type ,
    Monitor -> Monitor_type,
    Type    -> Type_type
  )

  lazy val layouts = Map[Global, Seq[Type]](
    Type.name   -> Seq(Ptr(Type), I32, Ptr(I8))     ,
    String.name -> Seq(Ptr(Type), Ptr(I8), I32, I32)
  )
}
