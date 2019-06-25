package scala.scalanative
package nir
package parser

import fastparse.all._
import Base._

object Op extends Base[nir.Op] {

  import Base.IgnoreWhitespace._

  val Call =
    P(
      "call[" ~ Type.parser ~ "]" ~ Val.parser ~ "(" ~ Val.parser
        .rep(sep = ",") ~ ")").map {
      case (ty, f, args) => nir.Op.Call(ty, f, args)
    }
  val Load =
    P("load[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, ptr) =>
        nir.Op.Load(ty, ptr)
    })
  val Store =
    P("store[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser map {
      case (ty, ptr, value) =>
        nir.Op.Store(ty, ptr, value)
    })
  val Elem =
    P(
      "elem[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser
        .rep(sep = ",") map {
        case (ty, ptr, indices) => nir.Op.Elem(ty, ptr, indices)
      })
  val Extract =
    P("extract" ~ Val.parser ~ "," ~ int.rep(sep = ",") map {
      case (aggr, indices) => nir.Op.Extract(aggr, indices)
    })
  val Insert =
    P("insert" ~ Val.parser ~ "," ~ Val.parser ~ "," ~ int.rep(sep = ",") map {
      case (aggr, value, indices) => nir.Op.Insert(aggr, value, indices)
    })
  val Stackalloc =
    P("stackalloc[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, n) => nir.Op.Stackalloc(ty, n)
    })
  val Bin =
    P(nir.parser.Bin.parser ~ "[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser map {
      case (name, ty, l, r) => nir.Op.Bin(name, ty, l, r)
    })
  val Comp =
    P(nir.parser.Comp.parser ~ "[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser map {
      case (name, ty, l, r) => nir.Op.Comp(name, ty, l, r)
    })
  val Conv =
    P(nir.parser.Conv.parser ~ "[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (name, ty, v) => nir.Op.Conv(name, ty, v)
    })
  val Classalloc = P("classalloc" ~ Global.parser map (nir.Op.Classalloc(_)))
  val Fieldload =
    P("fieldload" ~ "[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Global.parser map {
      case (ty, value, name) => nir.Op.Fieldload(ty, value, name)
    })
  val Fieldstore =
    P("fieldstore" ~ "[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Global.parser ~ "," ~ Val.parser map {
      case (ty, obj, name, value) => nir.Op.Fieldstore(ty, obj, name, value)
    })
  val Method =
    P("method" ~ Val.parser ~ "," ~ Base.stringLit map {
      case (v, sig) => nir.Op.Method(v, Unmangle.unmangleSig(sig))
    })
  val Dynmethod =
    P("dynmethod" ~ Val.parser ~ "," ~ Base.stringLit map {
      case (v, sig) => nir.Op.Dynmethod(v, Unmangle.unmangleSig(sig))
    })
  val Module = P("module" ~ Global.parser).map {
    case name => nir.Op.Module(name)
  }
  val As =
    P("as[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, value) => nir.Op.As(ty, value)
    })
  val Is =
    P("is[" ~ Type.parser ~ "]" ~ Val.parser map {
      case (ty, value) => nir.Op.Is(ty, value)
    })
  val Copy   = P("copy" ~ Val.parser map (nir.Op.Copy(_)))
  val Sizeof = P("sizeof[" ~ Type.parser ~ "]" map (nir.Op.Sizeof(_)))
  val Box = P("box[" ~ Type.parser ~ "]" ~ Val.parser map {
    case (ty, obj) => nir.Op.Box(ty, obj)
  })
  val Unbox = P("unbox[" ~ Type.parser ~ "]" ~ Val.parser map {
    case (ty, obj) => nir.Op.Unbox(ty, obj)
  })
  val Var = P("var[" ~ Type.parser ~ "]" map {
    case ty => nir.Op.Var(ty)
  })
  val Varload = P("varload" ~ Val.parser map {
    case slot => nir.Op.Varload(slot)
  })
  val Varstore = P("varstore" ~ Val.parser ~ "," ~ Val.parser map {
    case (slot, value) => nir.Op.Varstore(slot, value)
  })
  val Arrayalloc = P("arrayalloc[" ~ Type.parser ~ "]" ~ Val.parser map {
    case (ty, init) => nir.Op.Arrayalloc(ty, init)
  })
  val Arrayload = P(
    "arrayload[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser map {
      case (ty, arr, idx) => nir.Op.Arrayload(ty, arr, idx)
    })
  val Arraystore = P(
    "arraystore[" ~ Type.parser ~ "]" ~ Val.parser ~ "," ~ Val.parser ~ "," ~ Val.parser map {
      case (ty, arr, idx, value) => nir.Op.Arraystore(ty, arr, idx, value)
    })
  val Arraylength = P("arraylength" ~ Val.parser map {
    case arr => nir.Op.Arraylength(arr)
  })
  override val parser: P[nir.Op] =
    Call | Load | Store | Elem | Extract | Insert | Stackalloc | Bin | Comp | Conv | Classalloc | Fieldload | Fieldstore | Method | Dynmethod | Module | As | Is | Copy | Sizeof | Box | Unbox | Var | Varload | Varstore | Arrayalloc | Arrayload | Arraystore | Arraylength
}
