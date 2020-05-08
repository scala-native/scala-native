package scala.scalanative
package nir
package parser

import fastparse._
import NoWhitespace._
import Base._

object Op extends Base[nir.Op] {

  //import Base.IgnoreWhitespace._

  def Call[_: P] =
    P(
      "call[" ~ Type.parser ~ "]" ~ Val.parser ~ "(" ~ Val.parser
        .rep(sep = ",") ~ ")").map {
      case (ty, f, args) => nir.Op.Call(ty, f, args)
    }
  def Load[_: P] =
    P("load[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, ptr) =>
        nir.Op.Load(ty, ptr)
    })
  def Store[_: P] =
    P("store[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser map {
      case (ty, ptr, value) =>
        nir.Op.Store(ty, ptr, value)
    })
  def Elem[_: P] =
    P(
      "elem[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser
        .rep(sep = ",") map {
        case (ty, ptr, indices) => nir.Op.Elem(ty, ptr, indices)
      })
  def Extract[_: P] =
    P("extract" ~ Val.parser ~ "," ~ int.rep(sep = ",") map {
      case (aggr, indices) => nir.Op.Extract(aggr, indices)
    })
  def Insert[_: P] =
    P("insert" ~ Val.parser ~ "," ~ Val.parser ~ "," ~ int.rep(sep = ",") map {
      case (aggr, value, indices) => nir.Op.Insert(aggr, value, indices)
    })
  def Stackalloc[_: P] =
    P("stackalloc[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, n) => nir.Op.Stackalloc(ty, n)
    })
  def Bin[_: P] =
    P(nir.parser.Bin.parser ~ "[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser map {
      case (name, ty, l, r) => nir.Op.Bin(name, ty, l, r)
    })
  def Comp[_: P] =
    P(nir.parser.Comp.parser ~ "[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser map {
      case (name, ty, l, r) => nir.Op.Comp(name, ty, l, r)
    })
  def Conv[_: P] =
    P(nir.parser.Conv.parser ~ "[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (name, ty, v) => nir.Op.Conv(name, ty, v)
    })
  def Classalloc[_: P] =
    P("classalloc" ~ Global.parser map (nir.Op.Classalloc(_)))
  def Fieldload[_: P] =
    P("fieldload" ~ "[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Global.parser map {
      case (ty, value, name) => nir.Op.Fieldload(ty, value, name)
    })
  def Fieldstore[_: P] =
    P("fieldstore" ~ "[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Global.parser ~ "," ~ Val.parser map {
      case (ty, obj, name, value) => nir.Op.Fieldstore(ty, obj, name, value)
    })
  def Method[_: P] =
    P("method" ~ Val.parser ~ "," ~ Base.stringLit map {
      case (v, sig) => nir.Op.Method(v, Unmangle.unmangleSig(sig))
    })
  def Dynmethod[_: P] =
    P("dynmethod" ~ Val.parser ~ "," ~ Base.stringLit map {
      case (v, sig) => nir.Op.Dynmethod(v, Unmangle.unmangleSig(sig))
    })
  def Module[_: P] = P("module" ~ Global.parser).map {
    case name => nir.Op.Module(name)
  }
  def As[_: P] =
    P("as[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, value) => nir.Op.As(ty, value)
    })
  def Is[_: P] =
    P("is[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, value) => nir.Op.Is(ty, value)
    })
  def Copy[_: P]   = P("copy" ~ Val.parser map (nir.Op.Copy(_)))
  def Sizeof[_: P] = P("sizeof[" ~ Type.parser ~ "]" map (nir.Op.Sizeof(_)))
  def Box[_: P] =
    P("box[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, obj) => nir.Op.Box(ty, obj)
    })
  def Unbox[_: P] =
    P("unbox[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, obj) => nir.Op.Unbox(ty, obj)
    })
  def Var[_: P] =
    P("var[" ~ Type.parser ~ "]" map {
      case ty => nir.Op.Var(ty)
    })
  def Varload[_: P] =
    P("varload" ~ Val.parser map {
      case slot => nir.Op.Varload(slot)
    })
  def Varstore[_: P] =
    P("varstore" ~ Val.parser ~ "," ~ Val.parser map {
      case (slot, value) => nir.Op.Varstore(slot, value)
    })
  def Arrayalloc[_: P] =
    P("arrayalloc[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, init) => nir.Op.Arrayalloc(ty, init)
    })
  def Arrayload[_: P] =
    P("arrayload[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser map {
      case (ty, arr, idx) => nir.Op.Arrayload(ty, arr, idx)
    })
  def Arraystore[_: P] =
    P("arraystore[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser ~ "," ~ Val.parser map {
      case (ty, arr, idx, value) => nir.Op.Arraystore(ty, arr, idx, value)
    })
  def Arraylength[_: P] =
    P("arraylength" ~ Val.parser map {
      case arr => nir.Op.Arraylength(arr)
    })
  override def parser[_: P]: P[nir.Op] =
    Call | Load | Store | Elem | Extract | Insert | Stackalloc | Bin | Comp | Conv | Classalloc | Fieldload | Fieldstore | Method | Dynmethod | Module | As | Is | Copy | Sizeof | Box | Unbox | Var | Varload | Varstore | Arrayalloc | Arrayload | Arraystore | Arraylength
}
