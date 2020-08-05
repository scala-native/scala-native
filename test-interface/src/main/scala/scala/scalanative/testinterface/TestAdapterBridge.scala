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

import sbt.testing._
import scala.concurrent.Promise
import scala.scalanative.testinterface.common._
import scala.util.Try
import scala.util.control.NonFatal
import JVMEndpoints._

import scala.scalanative.testinterface.common.NativeEndpoints._
private[testinterface] object TestAdapterBridge {

  private[this] val mux = new RunMuxRPC(NativeRPC)

  def start(): Unit = {

    NativeRPC.attach(detectFrameworks)(detectFrameworksFun)
    NativeRPC.attach(createMasterRunner)(createRunnerFun(isMaster = true))
    NativeRPC.attach(createSlaveRunner)(createRunnerFun(isMaster = false))
  }

  private def detectFrameworksFun = { names: List[List[String]] =>
    FrameworkLoader.detectFrameworkNames(names).map { maybeName =>
      maybeName.map { name =>
        val framework = FrameworkLoader.loadFramework(name)
        new FrameworkInfo(name,
                          framework.name(),
                          framework.fingerprints().toList)
      }
    }
  }

  private def createRunnerFun(isMaster: Boolean) = { args: RunnerArgs =>
    val framework = FrameworkLoader.loadFramework(args.frameworkImpl)
    val loader    = new ScalaNativeClassLoader()

    val runID = args.runID
    println(s"create runner - master:$isMaster - ${framework.name()}")
    val runner = {
      if (isMaster) {
        framework.runner(args.args.toArray, args.remoteArgs.toArray, loader)
      } else {
        framework.slaveRunner(args.args.toArray,
                              args.remoteArgs.toArray,
                              loader,
                              mux.send(JVMEndpoints.msgSlave, runID))
      }
    }

    mux.attach(NativeEndpoints.tasks, runID)(tasksFun(runner))
    mux.attach(NativeEndpoints.execute, runID)(executeFun(runID, runner))
    mux.attach(NativeEndpoints.done, runID)(doneFun(runID, runner, isMaster))

    if (isMaster) {
      mux.attach(NativeEndpoints.msgMaster, runID)(msgMasterFun(runID, runner))
    } else {
      mux.attach(NativeEndpoints.msgSlave, runID)(runner.receiveMessage)
    }
  }

  private def detachRunnerCommands(runID: RunMux.RunID,
                                   isMaster: Boolean): Unit = {
    mux.detach(NativeEndpoints.tasks, runID)
    mux.detach(NativeEndpoints.execute, runID)
    mux.detach(NativeEndpoints.done, runID)

    if (isMaster)
      mux.detach(NativeEndpoints.msgMaster, runID)
    else
      mux.detach(NativeEndpoints.msgSlave, runID)
  }

  private def tasksFun(runner: Runner) = { taskDefs: List[TaskDef] =>
    val tasks = runner.tasks(taskDefs.toArray)
    val x     = tasks.map(TaskInfoBuilder.detachTask(_, runner)).toList
    x.map(_.taskDef.fullyQualifiedName()).foreach(println)
    x
  }

  private def executeFun(runID: RunMux.RunID, runner: Runner) = {
    req: ExecuteRequest =>
      val task = TaskInfoBuilder.attachTask(req.taskInfo, runner)
      println(s"executeFun $runID - ${task.taskDef().fullyQualifiedName()}")
      val eventHandler = new RemoteEventHandler(runID)

      val loggers = for {
        (withColor, i) <- req.loggerColorSupport.zipWithIndex
      } yield new RemoteLogger(runID, i, withColor)
//    val promise = Promise[List[TaskInfo]]()

//    def cont(tasks: Array[Task]) = {
//      Try(tasks.map(TaskInfoBuilder.detachTask(_, runner)).toList)
//      promise.complete(result)
//    }
//    try {
      val tasks = task.execute(eventHandler, loggers.toArray)
      tasks.map(TaskInfoBuilder.detachTask(_, runner)).toList
//     cont(tasks)
//    } catch {
//      case NonFatal(t) =>
//        promise.tryFailure(t)
//    }

//    promise.future
  }

  private def doneFun(runID: RunMux.RunID,
                      runner: Runner,
                      isMaster: Boolean) = { _: Unit =>
    try runner.done()
    finally detachRunnerCommands(runID, isMaster)
  }

  private def msgMasterFun(runID: RunMux.RunID, runner: Runner) = {
    msg: FrameworkMessage =>
      for (reply <- runner.receiveMessage(msg.msg)) {
        val fm = new FrameworkMessage(msg.slaveId, reply)
        mux.send(JVMEndpoints.msgMaster, runID)(fm)
      }
  }

  private class RemoteEventHandler(runID: RunMux.RunID) extends EventHandler {
    def handle(event: Event): Unit = mux.send(JVMEndpoints.event, runID)(event)
  }

  private class RemoteLogger(runID: RunMux.RunID,
                             index: Int,
                             val ansiCodesSupported: Boolean)
      extends Logger {
    private def l[T](x: T) = new LogElement(index, x)

    def error(msg: String): Unit  = mux.send(logError, runID)(l(msg))
    def warn(msg: String): Unit   = mux.send(logWarn, runID)(l(msg))
    def info(msg: String): Unit   = mux.send(logInfo, runID)(l(msg))
    def debug(msg: String): Unit  = mux.send(logDebug, runID)(l(msg))
    def trace(t: Throwable): Unit = mux.send(logTrace, runID)(l(t))
  }
}
