// Ported from Scala.js commit: 060c3397 dated: 2021-02-09

package scala.tools.partest.scalanative

import sbt.testing.TaskDef

/** Represents one run of a suite of tests.
 */
case class Runner(args: Array[String],
                  remoteArgs: Array[String],
                  testClassLoader: ClassLoader)
    extends sbt.testing.Runner {

  /** Returns an array of tasks that when executed will run tests and suites determined by the
   *  passed <code>TaskDef</code>s.
   *
   *  <p>
   *  Each returned task, when executed, will run tests and suites determined by the
   *  test class name, fingerprints, "explicitly specified" field, and selectors of one of the passed <code>TaskDef</code>s.
   *  </p>
   *
   *  <p>
   *  This <code>tasks</code> method may be called with <code>TaskDef</code>s containing the same value for <code>testClassName</code> but
   *  different fingerprints. For example, if both a class and its companion object were test classes, the <code>tasks</code> method could be
   *  passed an array containing <code>TaskDef</code>s with the same name but with a different value for <code>fingerprint.isModule</code>.
   *  </p>
   *
   *  <p>
   *  A test framework may "reject" a requested task by returning no <code>Task</code> for that <code>TaskDef</code>.
   *  </p>
   *
   *  @param taskDefs the <code>TaskDef</code>s for requested tasks
   *  @return an array of <code>Task</code>s
   *  @throws IllegalStateException if invoked after <code>done</code> has been invoked.
   */
  def tasks(taskDefs: Array[TaskDef]): Array[sbt.testing.Task] =
    taskDefs map (PartestTask(_, args): sbt.testing.Task)

  /** Indicates the client is done with this <code>Runner</code> instance.
   *
   *  @return a possibly multi-line summary string, or the empty string if no summary is provided -- TODO
   */
  def done(): String = ""
}
