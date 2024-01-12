package scala.scalanative.testinterface

// Ported from Scala.js

import sbt.testing._
import scala.scalanative.testinterface.common.JVMEndpoints._
import scala.scalanative.testinterface.common.NativeEndpoints._
import scala.scalanative.testinterface.common._

private[testinterface] class TestAdapterBridge(rpcClient: NativeRPC) {

  private val mux = new RunMuxRPC(rpcClient)

  def start(): Unit = {
    rpcClient.attach(detectFrameworks)(detectFrameworksFun)
    rpcClient.attach(createController)(createRunnerFun(isController = true))
    rpcClient.attach(createWorker)(createRunnerFun(isController = false))
  }

  private def detectFrameworksFun = { (names: List[List[String]]) =>
    FrameworkLoader.detectFrameworkNames(names).map { maybeName =>
      maybeName.map { name =>
        val framework = FrameworkLoader.loadFramework(name)
        new FrameworkInfo(
          name,
          framework.name(),
          framework.fingerprints().toList
        )
      }
    }
  }

  private def createRunnerFun(isController: Boolean) = { (args: RunnerArgs) =>
    val framework = FrameworkLoader.loadFramework(args.frameworkImpl)
    val loader = new ScalaNativeClassLoader()

    val runID = args.runID
    val runner = if (isController) {
      framework.runner(args.args.toArray, args.remoteArgs.toArray, loader)
    } else {
      framework.slaveRunner(
        args.args.toArray,
        args.remoteArgs.toArray,
        loader,
        mux.send(JVMEndpoints.msgWorker, runID)
      )
    }

    mux.attach(NativeEndpoints.tasks, runID)(tasksFun(runner))
    mux.attach(NativeEndpoints.execute, runID)(executeFun(runID, runner))
    mux.attach(NativeEndpoints.done, runID)(
      doneFun(runID, runner, isController)
    )

    if (isController) {
      mux.attach(NativeEndpoints.msgController, runID)(
        msgMasterFun(runID, runner)
      )
    } else {
      mux.attach(NativeEndpoints.msgWorker, runID)(runner.receiveMessage)
    }
  }

  private def detachRunnerCommands(
      runID: RunMux.RunID,
      isController: Boolean
  ): Unit = {
    mux.detach(NativeEndpoints.tasks, runID)
    mux.detach(NativeEndpoints.execute, runID)
    mux.detach(NativeEndpoints.done, runID)

    if (isController)
      mux.detach(NativeEndpoints.msgController, runID)
    else
      mux.detach(NativeEndpoints.msgWorker, runID)
  }

  private def tasksFun(runner: Runner) = { (taskDefs: List[TaskDef]) =>
    val tasks = runner.tasks(taskDefs.toArray)
    tasks.map(TaskInfoBuilder.detachTask(_, runner)).toList
  }

  private def executeFun(runID: RunMux.RunID, runner: Runner) = {
    (req: ExecuteRequest) =>
      val task = TaskInfoBuilder.attachTask(req.taskInfo, runner)
      val eventHandler = new RemoteEventHandler(runID)

      val loggers = for {
        (withColor, i) <- req.loggerColorSupport.zipWithIndex
      } yield new RemoteLogger(runID, i, withColor)

      val tasks = task.execute(eventHandler, loggers.toArray)
      tasks.map(TaskInfoBuilder.detachTask(_, runner)).toList
  }

  private def doneFun(
      runID: RunMux.RunID,
      runner: Runner,
      isController: Boolean
  ) = { (_: Unit) =>
    try runner.done()
    finally detachRunnerCommands(runID, isController)
  }

  private def msgMasterFun(runID: RunMux.RunID, runner: Runner) = {
    (msg: FrameworkMessage) =>
      for (reply <- runner.receiveMessage(msg.msg)) {
        val fm = new FrameworkMessage(msg.workerId, reply)
        mux.send(JVMEndpoints.msgController, runID)(fm)
      }
  }

  private class RemoteEventHandler(runID: RunMux.RunID) extends EventHandler {
    def handle(event: Event): Unit = mux.send(JVMEndpoints.event, runID)(event)
  }

  private class RemoteLogger(
      runID: RunMux.RunID,
      index: Int,
      _ansiCodesSupported: Boolean
  ) extends Logger {
    def ansiCodesSupported(): Boolean = _ansiCodesSupported

    private def l[T](x: T) = new LogElement(index, x)

    def error(msg: String): Unit = mux.send(logError, runID)(l(msg))
    def warn(msg: String): Unit = mux.send(logWarn, runID)(l(msg))
    def info(msg: String): Unit = mux.send(logInfo, runID)(l(msg))
    def debug(msg: String): Unit = mux.send(logDebug, runID)(l(msg))
    def trace(t: Throwable): Unit = mux.send(logTrace, runID)(l(t))
  }
}
