package scala.scalanative.build

import scala.scalanative.nir._

private[scalanative] trait ErrorReporter {
  def virtualMethodDispatchError(op: Op.Method, position: Position): Unit
}

private[scalanative] object ErrorReporter {

  /** Executes given code using provied ErrorReporter, throws BuildException if
   *  any error are reported
   */
  def boundary[T](logger: Logger, config: NativeConfig)(label: String)(
      block: ErrorReporter => T
  ): T = {
    val reporter = new Default(logger, config)
    try block(reporter)
    finally {
      if (reporter.errorsFound > 0) {
        throw new BuildException(
          s"Found ${reporter.errorsFound} in $label, aborting the build"
        )
      }
    }
  }

  private class Default(logger: Logger, config: NativeConfig)
      extends ErrorReporter {
    override def virtualMethodDispatchError(
        op: Op.Method,
        position: Position
    ): Unit = {
      val msg =
        s"Not found any target for virtual method dispatch of ${op.sig.unmangled} for object of type ${op.obj.ty.show}. " +
          "Unresolved method target would lead to undefined beviour at runtime, and might be coused by binary incompatibilites of project dependencies. " +
          s"The unresolved method was used in ${position.show}."
      if (config.linkStubs)
        logger.warn(
          msg + " Disable linking stubs in NativeConfig to make it fatal error."
        )
      else logError(msg)
    }

    var errorsFound: Int = 0
    private def logError(msg: String) = {
      logger.error(msg)
      errorsFound += 1
    }

  }
}
