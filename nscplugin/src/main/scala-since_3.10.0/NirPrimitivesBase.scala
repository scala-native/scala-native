package scala.scalanative.nscplugin

import dotty.tools.dotc.core.Contexts.Context

import CompilerCompat.ScalaPrimitives

abstract class NirPrimitivesBase(using ctx: Context)
    extends ScalaPrimitives(using ctx)
