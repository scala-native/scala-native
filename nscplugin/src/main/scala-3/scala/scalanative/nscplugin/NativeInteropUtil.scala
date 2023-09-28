package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Definitions

trait NativeInteropUtil { self: PluginPhase =>

  /** Returns the definitions in the current context. */
  protected def defn(using Context): Definitions = ctx.definitions

  /** Returns the Native IR definitions in the current context. */
  protected def defnNir(using Context): NirDefinitions = NirDefinitions.get

}
