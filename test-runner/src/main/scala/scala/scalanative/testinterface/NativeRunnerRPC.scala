/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.scalanative.testinterface

import java.io.File
import scala.concurrent.ExecutionContext
import scala.scalanative.build.Logger
import scala.scalanative.testinterface.common._

/** RPC Core for use with native rpc. */
private[testinterface] final class NativeRunnerRPC(
    binaryFile: File,
    envVars: Map[String, String],
    args: Seq[String],
    logger: Logger
)(implicit ec: ExecutionContext)
    extends RPCCore() {
  val runner = new ComRunner(binaryFile, envVars, args, logger, handleMessage)

  /* Once the com closes, ensure all still pending calls are failing.
   * This can be necessary, if the JSEnv terminates unexpectedly.
   * Note: We do not need to give a grace time here, since the reply
   * dispatch happens synchronously in `handleMessage`.
   * In other words, at this point we'll only see pending calls that
   * would require another call to `handleMessage` in order to complete
   * successfully. But this is not going to happen since the com run is
   * completed (and it is an explicit guarantee that `handleMessage` is not
   * called anymore after that).
   */
  runner.future.onComplete { t =>
    logger.info("ComRunner closed")
    close(NativeRunnerRPC.RunTerminatedException(t.failed.toOption))
  }

  override protected def send(msg: String): Unit = runner.send(msg)

  override def close(cause: Throwable): Unit = {
    /* Close the RPC layer and fail all pending calls.
     * This needs to happen first so we do not race completion of the run
     * itself (to retain the cause given here).
     */
    super.close(cause)

    // Now terminate the run itself.
    runner.close()
  }
}

private[testinterface] object NativeRunnerRPC {
  final case class RunTerminatedException(c: Option[Throwable])
      extends Exception(null, c.orNull)
}
