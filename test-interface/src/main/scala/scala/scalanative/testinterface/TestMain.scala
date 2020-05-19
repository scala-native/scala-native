package scala.scalanative
package testinterface

import java.io.{
  ByteArrayInputStream,
  ByteArrayOutputStream,
  DataInputStream,
  DataOutputStream
}
import java.net.Socket

import sbt.testing.{Event => SbtEvent, _}

import scala.annotation.tailrec
import scala.scalanative.testinterface.serialization._

object TestMain {

  /* The supported testing frameworks. */
  private lazy val frameworks: Seq[Framework] = Seq(
    FrameworkLoader.loadFramework("tests.NativeFramework")
  )

  private val usage: String = {
    """usage: test-main <server_port>
      |
      |arguments:
      |  server_port - the sbt test server port to use (required)
      |
      |*** Warning - dragons ahead ***
      |
      |This binary is meant to be executed by the Scala Native
      |testing framework and not standalone.
      |
      |Execute at your own risk!
      |""".stripMargin
  }

  /** Main method of the test runner. */
  def main(args: Array[String]): Unit = {
    val serverPort = args.headOption match {
      case None =>
        System.err.println(usage)
        throw new IllegalArgumentException("missing arguments")
      case Some(port) => port.toInt
    }
    val clientSocket = new Socket("127.0.0.1", serverPort)

    testRunner(Array.empty, null, clientSocket)
  }

  /** Test runner loop.
   *
   * @param tasks         The tasks known to the runner (executed and waiting)
   * @param runner        The actual underlying `Runner`
   * @param clientSocket  The client socket from which we receive and reply to commands
   */
  @tailrec
  private def testRunner(tasks: Array[Task],
                         runner: Runner,
                         clientSocket: Socket): Unit = {
    val stream = new DataInputStream(clientSocket.getInputStream)
    receive(stream) match {
      case Command.NewRunner(id, args, remoteArgs) =>
        val runner =
          frameworks(id).runner(args.toArray, remoteArgs.toArray, null)
        testRunner(tasks, runner, clientSocket)

      case Command.SendInfo(id, None) =>
        val fps  = frameworks(id).fingerprints()
        val name = frameworks(id).name()
        val info = Command.SendInfo(id, Some(FrameworkInfo(name, fps.toSeq)))
        send(clientSocket)(info)
        testRunner(tasks, runner, clientSocket)

      case Command.Tasks(newTasks) =>
        val ts = runner.tasks(newTasks.toArray)
        val taskInfos = TaskInfos(ts.zipWithIndex.toSeq.map {
          case (t, id) => task2TaskInfo(id, t)
        })
        send(clientSocket)(taskInfos)
        testRunner(tasks ++ ts, runner, clientSocket)

      case Command.Execute(taskID, colors) =>
        val handler = new RemoteEventHandler(clientSocket)
        val loggers =
          colors.map(new RemoteLogger(clientSocket, 0, _): Logger).toArray

        // Execute the task, possibly generating new tasks to execute...
        val newTasks = tasks
          .lift(taskID)
          .map(_.execute(handler, loggers))
          .getOrElse(Array.empty)
        val origSize = tasks.length

        // Convert the tasks to `TaskInfo` before sending to sbt. Keep task numbers correct.
        val taskInfos = newTasks.zipWithIndex.map {
          case (t, id) => task2TaskInfo(id + origSize, t)
        }
        send(clientSocket)(TaskInfos(taskInfos))
        testRunner(tasks ++ newTasks, runner, clientSocket)

      case Command.RunnerDone(_) =>
        val r       = runner.done()
        val message = Command.RunnerDone(r)
        send(clientSocket)(message)

      case other =>
        println(s"Unexpected message: $other")
    }
  }

  private def task2TaskInfo(id: Int, task: Task) =
    TaskInfo(id, task.taskDef, task.tags)

  /** Receives a message from `client`. */
  private def receive[T](dis: DataInputStream): Message = {
    val msglen = dis.readInt()
    val msgbuf = new Array[Byte](msglen)
    if (msglen > 0) {
      dis.readFully(msgbuf)
    }
    val deserializer = new SerializedInputStream(
      new ByteArrayInputStream(msgbuf))
    deserializer.readMessage()
  }

  /** Sends message `v` to `client`. */
  private def send[T](client: Socket)(msg: Message): Unit = {
    val bos = new ByteArrayOutputStream()
    SerializedOutputStream(new DataOutputStream(bos))(_.writeMessage(msg))
    val data = bos.toByteArray

    val out = client.getOutputStream
    out.write(data)
    out.flush()
  }

  private class RemoteEventHandler(client: Socket) extends EventHandler {
    override def handle(event: SbtEvent): Unit = {
      val ev = Event(event.fullyQualifiedName(),
                     event.fingerprint(),
                     event.selector(),
                     event.status(),
                     event.throwable(),
                     event.duration())
      send(client)(ev)
    }
  }

  private class RemoteLogger(client: Socket,
                             index: Int,
                             val ansiCodesSupported: Boolean)
      extends Logger {
    private def log(level: Log.Level,
                    msg: String,
                    twb: Option[Throwable]): Unit = {
      send(client)(Log(index, msg, twb, level))
    }

    override def error(msg: String): Unit  = log(Log.Level.Error, msg, None)
    override def warn(msg: String): Unit   = log(Log.Level.Warn, msg, None)
    override def info(msg: String): Unit   = log(Log.Level.Info, msg, None)
    override def debug(msg: String): Unit  = log(Log.Level.Debug, msg, None)
    override def trace(t: Throwable): Unit = log(Log.Level.Trace, "", Some(t))
  }
}
