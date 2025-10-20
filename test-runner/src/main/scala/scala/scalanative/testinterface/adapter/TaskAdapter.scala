package scala.scalanative.testinterface.adapter

// Ported from Scala.js

import scala.scalanative.testinterface.common.*
import sbt.testing.*

private[adapter] final class TaskAdapter(
    taskInfo: TaskInfo,
    runID: RunMux.RunID,
    runnerGetter: () => RunMuxRPC
) extends Task {

  def taskDef: TaskDef = taskInfo.taskDef
  def tags: Array[String] = taskInfo.tags.toArray

  def execute(handler: EventHandler, loggers: Array[Logger]): Array[Task] = {
    val runner = runnerGetter()

    def log[T](level: Logger => (T => Unit))(log: LogElement[T]) =
      level(loggers(log.index))(log.x)

    runner.attach(JVMEndpoints.event, runID)(handler.handle)
    runner.attach(JVMEndpoints.logError, runID)(log(_.error))
    runner.attach(JVMEndpoints.logWarn, runID)(log(_.warn))
    runner.attach(JVMEndpoints.logInfo, runID)(log(_.info))
    runner.attach(JVMEndpoints.logDebug, runID)(log(_.debug))
    runner.attach(JVMEndpoints.logTrace, runID)(log(_.trace))

    try {
      val colorSupport = loggers.map(_.ansiCodesSupported).toList
      val req = new ExecuteRequest(taskInfo, colorSupport)

      runner
        .call(NativeEndpoints.execute, runID)(req)
        .await()
        .map(new TaskAdapter(_, runID, runnerGetter))
        .toArray
    } finally {
      runner.detach(JVMEndpoints.event, runID)
      runner.detach(JVMEndpoints.logError, runID)
      runner.detach(JVMEndpoints.logWarn, runID)
      runner.detach(JVMEndpoints.logInfo, runID)
      runner.detach(JVMEndpoints.logDebug, runID)
      runner.detach(JVMEndpoints.logTrace, runID)
    }
  }
}
