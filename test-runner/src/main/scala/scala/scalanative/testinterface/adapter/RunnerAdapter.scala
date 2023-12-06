package scala.scalanative.testinterface.adapter

// Ported from Scala.js

import sbt.testing._
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import scala.scalanative.testinterface.adapter.TestAdapter.ManagedRunner
import scala.scalanative.testinterface.common.{
  FrameworkMessage,
  JVMEndpoints,
  NativeEndpoints,
  RunMuxRPC,
  RunnerArgs
}

private final class RunnerAdapter private (
    runnerArgs: RunnerArgs,
    controller: ManagedRunner,
    testAdapter: TestAdapter
) extends Runner {

  private val runID = runnerArgs.runID
  private val rpcGetter = () => getRunnerRPC()

  private val workers = TrieMap.empty[Long, ManagedRunner]

  // Route master messages to workers.
  controller.mux.attach(JVMEndpoints.msgController, runID) { msg =>
    workers(msg.workerId).mux.send(NativeEndpoints.msgWorker, runID)(msg.msg)
  }

  def args(): Array[String] = runnerArgs.args.toArray

  def remoteArgs(): Array[String] = runnerArgs.remoteArgs.toArray

  def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    getRunnerRPC()
      .call(NativeEndpoints.tasks, runID)(taskDefs.toList)
      .await()
      .map(new TaskAdapter(_, runID, rpcGetter))
      .toArray
  }

  def done(): String = synchronized {
    // .toList to make it strict.
    val workers = this.workers.values.filter(!_.com.isClosed).toList

    try {
      workers
        .map(_.mux.call(NativeEndpoints.done, runID)(()))
        .foreach(_.await())
      // RPC connection was closed, probaly due to native runner crash, skip sending fruitless command
      if (controller.com.isClosed) ""
      else controller.mux.call(NativeEndpoints.done, runID)(()).await()
    } finally {
      workers.foreach(_.mux.detach(JVMEndpoints.msgWorker, runID))
      controller.mux.detach(JVMEndpoints.msgController, runID)

      this.workers.clear()
      testAdapter.runDone(runID)
    }
  }

  private def getRunnerRPC(): RunMuxRPC = {
    val mRunner = testAdapter.getRunnerForThread()

    if (mRunner != controller && !workers.contains(mRunner.id)) {
      // Put the worker in the map so messages can be routed.
      workers.put(mRunner.id, mRunner)

      // Attach message endpoint.
      mRunner.mux.attach(JVMEndpoints.msgWorker, runID) { msg =>
        controller.mux.send(NativeEndpoints.msgController, runID)(
          new FrameworkMessage(mRunner.id, msg)
        )
      }

      // Start worker.
      mRunner.com.call(NativeEndpoints.createWorker)(runnerArgs).await()
    }

    mRunner.mux
  }
}

private[adapter] object RunnerAdapter {
  def apply(
      testAdapter: TestAdapter,
      frameworkImplName: String,
      args: Array[String],
      remoteArgs: Array[String]
  ): Runner = {
    val runID = testAdapter.runStarting()

    try {
      val runnerArgs =
        new RunnerArgs(runID, frameworkImplName, args.toList, remoteArgs.toList)
      val mRunner = testAdapter.getRunnerForThread()
      mRunner.com.call(NativeEndpoints.createController)(runnerArgs).await()

      new RunnerAdapter(runnerArgs, mRunner, testAdapter)
    } catch {
      case t: Throwable =>
        testAdapter.runDone(runID)
        throw t
    }
  }
}
