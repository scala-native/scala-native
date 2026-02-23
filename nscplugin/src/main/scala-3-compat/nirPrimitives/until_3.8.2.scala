package scala.scalanative.nscplugin

import dotty.tools.backend.jvm.DottyPrimitives
import dotty.tools.dotc.ast.tpd.Apply
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Types.Type

abstract class NirPrimitivesCompat(using ctx: Context) extends DottyPrimitives(ctx):
  self: NirPrimitives =>
  override final def getPrimitive(app: Apply, tpe: Type)(using Context): Int =
    nirPrimitives.getOrElse(app.fun.symbol, super.getPrimitive(app, tpe))
