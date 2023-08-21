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

  def genLocalName(sym: Symbol): String = sym.javaSimpleName.toString

  def genTypeName(sym: Symbol): nir.Global.Top = {
    val id = {
      val fullName = sym.fullName
      MappedNames.getOrElse(fullName, fullName)
    }
    val name = sym match {
      case ObjectClass =>
        nir.Rt.Object.name.asInstanceOf[nir.Global.Top]
      case _ if sym.isModule =>
        genTypeName(sym.moduleClass)
      case _ =>
        val needsModuleClassSuffix = sym.isModuleClass && !sym.isJavaDefined
        val idWithSuffix = if (needsModuleClassSuffix) id + "$" else id
        nir.Global.Top(idWithSuffix)
    }
    name
  }

  def genModuleName(sym: Symbol): nir.Global.Top = {
    if (sym.isModule) genTypeName(sym)
    else {
      val module = sym.moduleClass
      if (module.exists) genTypeName(module)
      else {
        val name @ nir.Global.Top(className) = genTypeName(sym)
        if (className.endsWith("$")) name
        else nir.Global.Top(className + "$")
      }
    }
  }

  def genFieldName(sym: Symbol): nir.Global = {
    val owner =
      if (sym.isStaticMember) genModuleName(sym.owner)
      else genTypeName(sym.owner)
    val id = nativeIdOf(sym)
    val scope = {
      /* Variables are internally private, but with public setter/getter.
       * Removing this check would cause problems with reachability
       */
      if (sym.isPrivate && !sym.isVariable) nir.Sig.Scope.Private(owner)
      else nir.Sig.Scope.Public
    }

    owner.member {
      if (sym.owner.isExternType) {
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
      if (sym.isStaticMember) {
        if (sym.isPrivate) nir.Sig.Scope.PrivateStatic(owner)
        else nir.Sig.Scope.PublicStatic
      } else if (sym.isPrivate)
        nir.Sig.Scope.Private(owner)
      else nir.Sig.Scope.Public

    val paramTypes = tpe.params.toSeq.map(p => genType(p.info))

    def isExtern = sym.owner.isExternType

    if (sym == String_+)
      genMethodName(StringConcatMethod)
    else if (isExtern)
      owner.member(genExternSigImpl(sym, id))
    else if (sym.name == nme.CONSTRUCTOR)
      owner.member(nir.Sig.Ctor(paramTypes))
    else {
      val retType = genType(tpe.resultType)
      owner.member(nir.Sig.Method(id, paramTypes :+ retType, scope))
    }
  }

  def genExternSig(sym: Symbol): nir.Sig.Extern =
    genExternSigImpl(sym, nativeIdOf(sym))

  private def genExternSigImpl(sym: Symbol, id: String) =
    if (sym.isSetter) {
      val id = nativeIdOf(sym.getter)
      nir.Sig.Extern(id)
    } else nir.Sig.Extern(id)

  def genStaticMemberName(
      sym: Symbol,
      explicitOwner: Symbol
  ): nir.Global = {
    // Use explicit owner in case if forwarder target was defined in the trait/interface
    // or was abstract. `sym.owner` would always point to original owner, even if it also defined
    // in the super class. This is important, becouse (on the JVM) static methods are resolved at
    // compile time and do never use dynamic method dispatch, however it is possible to shadow
    // static method in the parent class by defining static method with the same name in the child.
    val typeName = genTypeName(
      Option(explicitOwner)
        .fold[Symbol](NoSymbol) {
          _.filter(_.isSubClass(sym.owner))
        }
        .orElse(sym.owner)
    )
    val owner = nir.Global.Top(typeName.id.stripSuffix("$"))
    val id = nativeIdOf(sym)
    val scope =
      if (sym.isPrivate) nir.Sig.Scope.PrivateStatic(owner)
      else nir.Sig.Scope.PublicStatic

    val tpe = sym.tpe.widen
    val paramTypes = tpe.params.toSeq.map(p => genType(p.info))
    val retType = genType(fromType(sym.info.resultType))

    val name = sym.name
    val sig = nir.Sig.Method(id, paramTypes :+ retType, scope)
    owner.member(sig)
  }

  def genFuncPtrExternForwarderName(ownerSym: Symbol): nir.Global = {
    val owner = genTypeName(ownerSym)
    owner.member(nir.Sig.Generated("$extern$forwarder"))
  }

  private def nativeIdOf(sym: Symbol): String = {
    sym.getAnnotation(NameClass).flatMap(_.stringArg(0)).getOrElse {
      val name = sym.javaSimpleName.toString()
      val id: String = if (sym.owner.isExternType) {
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

  private val MappedNames = Map(
    "java.lang._Class" -> "java.lang.Class",
    "java.lang._Enum" -> "java.lang.Enum",
    "java.lang._Object" -> "java.lang.Object",
    "java.lang._String" -> "java.lang.String",
    "scala.Nothing" -> "scala.runtime.Nothing$",
    "scala.Null" -> "scala.runtime.Null$"
  ).flatMap {
    case classEntry @ (nativeName, javaName) =>
      classEntry ::
        (nativeName + "$", javaName + "$") ::
        Nil
  }
}
