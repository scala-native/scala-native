package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scalanative.nir.Shows.{showType => _, showGlobal => _, _}
import scalanative.util.{sh, Show}, Show.{Repeat => r}

trait NirNameEncoding { self: NirCodeGen =>
  import global.{Name => _, _}, definitions._
  import nirAddons.nirDefinitions._

  private lazy val StringClassName  = nir.Global.Type("java.lang.String")
  private lazy val StringModuleName = nir.Global.Val("java.lang.String")

  def genClassName(sym: Symbol): nir.Global = {
    val id = {
      val fullName = sym.fullName.toString
      if (fullName == "java.lang._String") "java.lang.String"
      else if (fullName == "java.lang._Object") "java.lang.Object"
      else fullName
    }
    val name = sym match {
      case ObjectClass                               => nir.Rt.Object.name
      case _ if sym.isModule                         => genClassName(sym.moduleClass)
      case _ if sym.isModuleClass || sym.isImplClass => nir.Global.Val(id)
      case _ if sym.isInterface                      => nir.Global.Type(id)
      case _                                         => nir.Global.Type(id)
    }
    name
  }

  def genFieldName(sym: Symbol) = {
    val owner = genClassName(sym.owner)
    val id0   = sym.name.decoded.toString
    val id =
      if (id0.charAt(id0.length() - 1) != ' ') id0
      else id0.substring(0, id0.length() - 1)

    owner + id
  }

  def genMethodName(sym: Symbol): nir.Global = {
    val owner   = sym.owner
    val ownerId = genClassName(sym.owner)
    val id = {
      val name = sym.name.decoded
      if (owner == NObjectClass) name.substring(1) // skip the _
      else name.toString
    }
    val tpe           = sym.tpe.widen
    val mangledParams = tpe.params.toSeq.map(mangledType)

    if (sym == String_+) {
      genMethodName(StringConcatMethod)
    } else if (isExternalModule(owner)) {
      ownerId + id
    } else if (sym.name == nme.CONSTRUCTOR) {
      ownerId + ("init" +: mangledParams).mkString("_")
    } else {
      ownerId + (id +: (mangledParams :+ mangledType(tpe.resultType)))
        .mkString("_")
    }
  }

  private def mangledType(sym: Symbol): String =
    mangledType(sym.info)

  private def mangledType(tpe: Type): String =
    mangledType(genType(tpe))

  private def mangledType(ty: nir.Type): String = {
    implicit lazy val showMangledType: Show[nir.Type] = Show {
      case nir.Type.None                => ""
      case nir.Type.Void                => "void"
      case nir.Type.Label               => "label"
      case nir.Type.Vararg              => "..."
      case nir.Type.Bool                => "bool"
      case nir.Type.I8                  => "i8"
      case nir.Type.I16                 => "i16"
      case nir.Type.I32                 => "i32"
      case nir.Type.I64                 => "i64"
      case nir.Type.F32                 => "f32"
      case nir.Type.F64                 => "f64"
      case nir.Type.Array(ty, n)        => sh"arr.$ty.$n"
      case nir.Type.Ptr(ty)             => sh"ptr.$ty"
      case nir.Type.Function(args, ret) => sh"fun.${r(args :+ ret, sep = ".")}"
      case nir.Type.Struct(name)        => sh"struct.$name"
      case nir.Type.AnonStruct(tys)     => sh"anon-struct.${r(tys, sep = ".")}"

      case nir.Type.Size             => "size"
      case nir.Type.Unit             => "unit"
      case nir.Type.Nothing          => "nothing"
      case nir.Type.Null             => "null"
      case nir.Type.Class(name)      => sh"class.$name"
      case nir.Type.ClassValue(name) => sh"class-value.$name"
      case nir.Type.Trait(name)      => sh"trait.$name"
      case nir.Type.Module(name)     => sh"module.$name"
    }

    implicit lazy val showMangledGlobal: Show[nir.Global] = Show { g =>
      val head +: tail = g.parts
      val parts        = head.replace("scala.scalanative.runtime", "nrt") +: tail
      sh"${r(parts, sep = "_")}"
    }

    sh"$ty".toString
  }
}
