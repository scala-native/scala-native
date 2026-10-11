package scala.scalanative.nscplugin

import dotty.tools.dotc.core.Contexts._

import CompilerCompat.ScalaPrimitives

abstract class NirPrimitivesBase(using ctx: Context)
    extends ScalaPrimitives(ctx)
