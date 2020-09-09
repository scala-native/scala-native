package sbt.testing

import scala.scalanative.reflect.annotation.EnableReflectiveInstantiation

/** Interface implemented by test frameworks. */
@EnableReflectiveInstantiation
trait Framework {

  /** A human-friendly name of the test framework that this object represents.
   */
  def name(): String

  /** An array of <a href="Fingerprint.html"><code>Fingerprint</code></a>s
   *  that specify how to identify test classes during discovery.
   */
  def fingerprints(): Array[Fingerprint]

  /** Initiates a run.
   *
   *  If a client invokes this method before a previously initiated run has
   *  completed, the test framework may throw
   *  <code>IllegalStateException</code> to indicate it cannot perform the two
   *  runs concurrently.
   *
   *  @param args the test-framework-specific arguments for the new run
   *  @param remoteArgs the test-framework-specific remote arguments for the run in a forked JVM
   *  @param testClassLoader a class loader to use when loading test classes during the run
   *
   *  @return a <code>Runner</code> representing the newly started run.
   *  @throws java.lang.IllegalStateException if the test framework is unable to
   *      initiate a run because it is already performing a previously initiated
   *      run that has not yet completed.
   */
  def runner(args: Array[String],
             remoteArgs: Array[String],
             testClassLoader: ClassLoader): Runner

  /** Scala Native specific: Creates a worker for a given run.
   *  Ported from Scala.js
   *  The worker may send a message to the controller runner by calling `send`.
   *
   *  Important: this method name cannot be changed.
   *  It must keep the name `slaveRunner` for compatibility with Scala.js.
   *  This is a public to-be-overridden API that testing frameworks must implement.
   *  Currently testing frameworks can have a unique implementation of Framework for JVM, JS and Native.
   *  If we change that method name, they will need a separate source file for Native.
   *
   *  Similarly, Scala.js cannot change the name of that method in its API either,
   *  since that would be a binary breaking change that would break all published testing frameworks, which is not acceptable.
   *
   *  Introducing another method and deprecating this one as a forwarder is not an option,
   *  because testing frameworks must implement that method, not call it.
   */
  def slaveRunner(args: Array[String],
                  remoteArgs: Array[String],
                  testClassLoader: ClassLoader,
                  send: String => Unit): Runner
}
