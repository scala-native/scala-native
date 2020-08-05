package tests

import sbt.testing.{Runner, Task, TaskDef}

class NativeRunner(override val args: Array[String],
                   override val remoteArgs: Array[String])
    extends Runner {
  override def tasks(taskDefs: Array[TaskDef]): Array[Task] =
    taskDefs.map(taskDef => new NativeTask(taskDef))

  override def done(): String = ""

  /** Scala.js specific: Invoked on the master <code>Runner</code>, if a slave
   * sends a message (through the channel provided by the client).
   *
   * The master may send a message back to the sending slave by returning the
   * message in a Some.
   *
   * Invoked on a slave <code>Runner</code>, if the master responds to a
   * message (sent by the slave via the supplied closure in
   * <code>slaveRunner</code>). The return value of the call is ignored in
   * this case.
   */
  override def receiveMessage(msg: String): Option[String] = None

  /** Scala.js specific: Serialize a task created by <code>tasks</code> or
   * returned from <code>execute</code>.
   *
   * The resulting string will be passed to the <code>deserializeTask</code>
   * method of another runner. After calling this method, the passed task is
   * invalid and should dissociate from this runner.
   */
  override def serializeTask(task: Task,
                             serializer: TaskDef => String): String =
    serializer(task.taskDef())

  /** Scala.js specific: Deserialize a task that has been serialized by
   * <code>serializeTask</code> of another or this <code>Runner</code>.
   *
   * The resulting task must associate with this runner.
   */
  def deserializeTask(task: String, deserializer: String => TaskDef): Task =
    new NativeTask(deserializer(task))

}
