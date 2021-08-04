package scala.scalanative.testinterface.signalhandling

import scala.scalanative.unsafe._

@extern
private[testinterface] object SignalConfig {
  @name("scalanative_set_default_handlers")
  def setDefaultHandlers(): Unit = extern
}
