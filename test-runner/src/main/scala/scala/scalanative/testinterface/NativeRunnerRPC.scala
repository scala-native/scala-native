package scala.scalanative.testinterface

// Ported from Scala.js

import java.io.File
import java.net.ServerSocket
import scala.concurrent.ExecutionContext
import scala.scalanative.build.Logger
import scala.scalanative.testinterface.common.*

/** RPC Core for use with native rpc. */
private[testinterface] final class NativeRunnerRPC(
    executableFile: File,
    envVars: Map[String, String],
    args: Seq[String],
    logger: Logger
)(implicit ec: ExecutionContext)
    extends RPCCore() {

  private val serverSocket: ServerSocket = new ServerSocket(
    /* port = */ 0,
    /* backlog = */ 1
  )
  val processRunner = new ProcessRunner(
    executableFile,
    envVars,
    args,
    logger,
    serverSocket.getLocalPort
  )
  val runner = new ComRunner(processRunner, serverSocket, logger, handleMessage)

  /* Once the com closes, ensure all still pending calls are failing.
   * Note: We do not need to give a grace time here, since the reply
   * dispatch happens synchronously in `handleMessage`.
   * In other words, at this point we'll only see pending calls that
   * would require another call to `handleMessage` in order to complete
   * successfully. But this is not going to happen since the com run is
   * completed (and it is an explicit guarantee that `handleMessage` is not
   * called anymore after that).
   */
  runner.future.onComplete { t =>
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
