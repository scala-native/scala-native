package scala.scalanative
package nscplugin

import scala.tools.nsc.Global
import scalanative.util.unreachable

trait NirGenName[G <: Global with Singleton] {
  self: NirGenPhase[G] =>

  import global.{Name => _, _}, definitions._
  import nirAddons.nirDefinitions._
  import SimpleType.{fromSymbol, fromType}

  def genAnonName(owner: Symbol, anon: Symbol) =
    genName(owner).member(nir.Sig.Extern(anon.fullName.toString))

  def genName(sym: Symbol): nir.Global =
    if (sym.isType) {
      genTypeName(sym)
    } else if (sym.isMethod) {
      genMethodName(sym)
    } else if (sym.isField) {
      genFieldName(sym)
    } else {
      unreachable
    }

  def genTypeName(sym: Symbol): nir.Global.Top = {
    val id = {
      val fullName = sym.fullName
      if (fullName == "java.lang._String") "java.lang.String"
      else if (fullName == "java.lang._Object") "java.lang.Object"
      else if (fullName == "java.lang._Class") "java.lang.Class"
      else fullName
    }
    val name = sym match {
      case ObjectClass =>
        nir.Rt.Object.name.asInstanceOf[nir.Global.Top]
      case _ if sym.isModule =>
        genTypeName(sym.moduleClass)
      case _ =>
        val needsModuleClassSuffix =
          sym.isModuleClass && !isImplClass(sym)
        val idWithSuffix = if (needsModuleClassSuffix) id + "$" else id
        nir.Global.Top(idWithSuffix)
    }
    name
  }

  def genFieldName(sym: Symbol): nir.Global = {
    val owner = genTypeName(sym.owner)
    val id = nativeIdOf(sym)
    val scope = {
      /* Variables are internally private, but with public setter/getter.
       * Removing this check would cause problems with reachability
       */
      if (sym.isPrivate && !sym.isVariable) nir.Sig.Scope.Private(owner)
      else nir.Sig.Scope.Public
    }

    owner.member {
      if (sym.owner.isExternModule) {
        nir.Sig.Extern(id)
      } else {
        nir.Sig.Field(id, scope)
      }
    }
  }

  def genMethodName(sym: Symbol): nir.Global = {
    val owner = genTypeName(sym.owner)
    val id = nativeIdOf(sym)
    val tpe = sym.tpe.widen
    val scope =
      if (sym.isPrivate) nir.Sig.Scope.Private(owner)
      else nir.Sig.Scope.Public

    val paramTypes = tpe.params.toSeq.map(p => genType(p.info))

    if (sym == String_+) {
      genMethodName(StringConcatMethod)
    } else if (sym.owner.isExternModule) {
      if (sym.isSetter) {
        val id = nativeIdOf(sym.getter)
        owner.member(nir.Sig.Extern(id))
      } else {
        owner.member(nir.Sig.Extern(id))
      }
    } else if (sym.name == nme.CONSTRUCTOR) {
      owner.member(nir.Sig.Ctor(paramTypes))
    } else {
      val retType = genType(tpe.resultType)
      owner.member(nir.Sig.Method(id, paramTypes :+ retType, scope))
    }
  }

  def genFuncPtrExternForwarderName(ownerSym: Symbol): nir.Global = {
    val owner = genTypeName(ownerSym)
    owner.member(nir.Sig.Generated("$extern$forwarder"))
  }

  private def nativeIdOf(sym: Symbol): String = {
    sym.getAnnotation(NameClass).flatMap(_.stringArg(0)).getOrElse {
      val name = sym.javaSimpleName.toString()
      val id: String = if (sym.owner.isExternModule) {
        // Don't use encoded names for externs
        sym.decodedName.trim()
      } else if (sym.isField) {
        // Scala 2 fields can contain ' ' suffix
        name.trim()
      } else if (sym.isMethod) {
        val isScalaHashOrEquals = name.startsWith("__scala_")
        if (sym.owner == NObjectClass || isScalaHashOrEquals) {
          name.substring(2) // strip the __
        } else {
          name
        }
      } else {
        scalanative.util.unreachable
      }
      /*
       * Double quoted identifiers are not allowed in CLang.
       * We're replacing them with unicode to allow distinction between x / `x` and `"x"`.
       * It follows Scala JVM naming convention.
       */
      id.replace("\"", "$u0022")
    }
  }
}
