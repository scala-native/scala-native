package scala.scalanative.testinterface.adapter

// Ported from Scala.JS

import sbt.testing._
import scala.collection.concurrent.TrieMap
import scala.scalanative.testinterface.adapter.TestAdapter.ManagedRunner
import scala.scalanative.testinterface.common.{
  FrameworkMessage,
  JVMEndpoints,
  NativeEndpoints,
  RunMuxRPC,
  RunnerArgs
}

private final class RunnerAdapter private (runnerArgs: RunnerArgs,
                                           master: ManagedRunner,
                                           testAdapter: TestAdapter)
    extends Runner {

  private val runID     = runnerArgs.runID
  private val rpcGetter = () => getRunnerRPC()

  private val slaves = TrieMap.empty[Long, ManagedRunner]

  // Route master messages to slaves.
  master.mux.attach(JVMEndpoints.msgMaster, runID) { msg =>
    slaves(msg.slaveId).mux.send(NativeEndpoints.msgSlave, runID)(msg.msg)
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
    val slaves = this.slaves.values.toList // .toList to make it strict.

    try {
      slaves.map(_.mux.call(NativeEndpoints.done, runID)(())).foreach(_.await())
      master.mux.call(NativeEndpoints.done, runID)(()).await()
    } finally {
      slaves.foreach(_.mux.detach(JVMEndpoints.msgSlave, runID))
      master.mux.detach(JVMEndpoints.msgMaster, runID)

      this.slaves.clear()
      testAdapter.runDone(runID)
    }
  }

  private def getRunnerRPC(): RunMuxRPC = {
    val mRunner = testAdapter.getRunnerForThread()

    if (mRunner != master && !slaves.contains(mRunner.id)) {
      // Put the slave in the map so messages can be routed.
      slaves.put(mRunner.id, mRunner)

      // Attach message endpoint.
      mRunner.mux.attach(JVMEndpoints.msgSlave, runID) { msg =>
        master.mux.send(NativeEndpoints.msgMaster, runID)(
          new FrameworkMessage(mRunner.id, msg))
      }

      // Start slave.
      mRunner.com.call(NativeEndpoints.createSlaveRunner)(runnerArgs).await()
    }

    mRunner.mux
  }
}

private[adapter] object RunnerAdapter {
  def apply(testAdapter: TestAdapter,
            frameworkImplName: String,
            args: Array[String],
            remoteArgs: Array[String]): Runner = {
    val runID = testAdapter.runStarting()

    try {
      val runnerArgs =
        new RunnerArgs(runID, frameworkImplName, args.toList, remoteArgs.toList)
      val mRunner = testAdapter.getRunnerForThread()
      mRunner.com.call(NativeEndpoints.createMasterRunner)(runnerArgs).await()

      new RunnerAdapter(runnerArgs, mRunner, testAdapter)
    } catch {
      case t: Throwable =>
        testAdapter.runDone(runID)
        throw t
    }
  }
}
