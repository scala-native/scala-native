package scala.scalanative.nscplugin

import dotty.tools._
import dotc._
import dotc.transform.{LazyVals, MoveStatics}
import dotc.ast.tpd._
import plugins._
import core.Flags._
import core.Contexts._
import core.Names._
import core.Symbols._
import core.StdNames._
import scala.annotation.{threadUnsafe => tu}

// This phase is responsible for rewriting calls to scala.runtime.LazyVals with
// its scala native specific counter-part. This is needed, because LazyVals are
// using JVM unsafe API and static class constructors which are not supported
// in Scala Native.
object AdaptLazyVals extends PluginPhase {
  val phaseName = "scalanative-adaptLazyVals"

  override val runsAfter = Set(LazyVals.name, MoveStatics.name)
  override val runsBefore = Set(GenNIR.name)

  def defn(using Context) = LazyValsDefns.get
  def defnNir(using Context) = NirDefinitions.defnNir

  private def isLazyFieldOffset(name: Name) =
    name.startsWith(nme.LAZY_FIELD_OFFSET.toString)

  // Map of field symbols for LazyVals offsets and literals
  // with the name of referenced bitmap fields within given TypeDef
  private val bitmapFieldNames = collection.mutable.Map.empty[Symbol, Literal]

  override def prepareForUnit(tree: Tree)(using Context): Context = {
    bitmapFieldNames.clear()
    super.prepareForUnit(tree)
  }

  // Collect informations about offset fields
  override def prepareForTypeDef(td: TypeDef)(using Context): Context = {
    val sym = td.symbol
    val hasLazyFields = sym.denot.info.fields
      .exists(f => isLazyFieldOffset(f.name))

    if (hasLazyFields) {
      val template @ Template(_, _, _, _) = td.rhs
      bitmapFieldNames ++= template.body.collect {
        case vd: ValDef if isLazyFieldOffset(vd.name) =>
          val Apply(_, List(cls: Literal, fieldname: Literal)) = vd.rhs
          vd.symbol -> fieldname
      }.toMap
    }

    ctx
  }

  override def transformDefDef(dd: DefDef)(using Context): Tree = {
    val hasLazyFields = dd.symbol.owner.denot.info.fields
      .exists(f => isLazyFieldOffset(f.name))

    // Remove LazyVals Offset fields assignments from static constructors,
    // as they're leading to reachability problems
    // Drop static constructor if empty after filtering
    if (hasLazyFields && dd.symbol.isStaticConstructor) {
      val DefDef(_, _, _, b @ Block(stats, expr)) = dd
      val newBlock = cpy.Block(b.asInstanceOf[Tree])(
        stats = b.stats
          .filter {
            case Assign(lhs, rhs) => !isLazyFieldOffset(lhs.symbol.name)
            case _                => true
          }
          .asInstanceOf[List[Tree]],
        expr = expr.asInstanceOf[Tree]
      )
      if (newBlock.stats.isEmpty) EmptyTree
      else cpy.DefDef(dd)(dd.name, dd.paramss, dd.tpt, newBlock)
    } else dd
  }

  // Replace all usages of all unsupported LazyVals methods with their
  // Scala Native specific implementation (taking Ptr instead of object + offset)
  override def transformApply(tree: Apply)(using Context): Tree = {
    // Create call to SN intrinsic methods returning pointer to bitmap field
    def classFieldPtr(target: Tree, fieldRef: Tree): Tree = {
      val fieldName = bitmapFieldNames(fieldRef.symbol)
      cpy.Apply(tree)(
        fun = ref(defnNir.Intrinsics_classFieldRawPtr),
        args = List(target, fieldName)
      )
    }

    val Apply(fun, args) = tree
    val sym = fun.symbol

    if bitmapFieldNames.isEmpty then tree // No LazyVals in TypeDef, fast path
    else if sym == defn.LazyVals_get then
      val List(target, fieldRef) = args
      cpy.Apply(tree)(
        fun = ref(defn.NativeLazyVals_get),
        args = List(classFieldPtr(target, fieldRef))
      )
    else if sym == defn.LazyVals_setFlag then
      val List(target, fieldRef, value, ord) = args
      cpy.Apply(tree)(
        fun = ref(defn.NativeLazyVals_setFlag),
        args = List(classFieldPtr(target, fieldRef), value, ord)
      )
    else if sym == defn.LazyVals_CAS then
      val List(target, fieldRef, expected, value, ord) = args
      cpy.Apply(tree)(
        fun = ref(defn.NativeLazyVals_CAS),
        args = List(classFieldPtr(target, fieldRef), expected, value, ord)
      )
    else if sym == defn.LazyVals_wait4Notification then
      val List(target, fieldRef, value, ord) = args
      cpy.Apply(tree)(
        fun = ref(defn.NativeLazyVals_wait4Notification),
        args = List(classFieldPtr(target, fieldRef), value, ord)
      )
    else tree
  }

  object LazyValsDefns {
    var lastCtx: Option[Context] = None
    var lastDefns: LazyValsDefns = _

    def get(using Context): LazyValsDefns = {
      if (!lastCtx.contains(ctx)) {
        lastDefns = LazyValsDefns()
        lastCtx = Some(ctx)
      }
      lastDefns
    }
  }
  class LazyValsDefns(using Context) {
    @tu lazy val NativeLazyValsModule = requiredModule(
      "scala.scalanative.runtime.LazyVals"
    )
    @tu lazy val NativeLazyVals_get = NativeLazyValsModule.requiredMethod("get")
    @tu lazy val NativeLazyVals_setFlag =
      NativeLazyValsModule.requiredMethod("setFlag")
    @tu lazy val NativeLazyVals_CAS = NativeLazyValsModule.requiredMethod("CAS")
    @tu lazy val NativeLazyVals_wait4Notification =
      NativeLazyValsModule.requiredMethod("wait4Notification")

    @tu lazy val LazyValsModule = requiredModule("scala.runtime.LazyVals")
    @tu lazy val LazyVals_get = LazyValsModule.requiredMethod("get")
    @tu lazy val LazyVals_setFlag = LazyValsModule.requiredMethod("setFlag")
    @tu lazy val LazyVals_CAS = LazyValsModule.requiredMethod("CAS")
    @tu lazy val LazyVals_wait4Notification =
      LazyValsModule.requiredMethod("wait4Notification")
  }

}
