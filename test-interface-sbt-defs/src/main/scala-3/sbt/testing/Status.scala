package sbt.testing

/** Represents the status of running a test.
 *
 *  Test frameworks can decide which of these to use and what they mean, but in
 *  general, the intended meanings are:
 *
 *    - Success - a test succeeded
 *    - Error - an "error" occurred during a test
 *    - Failure - a "failure" during a test
 *    - Skipped - a test was skipped for any reason
 *    - Ignored - a test was ignored, <em>i.e.</em>, temporarily disabled with
 *      the intention of fixing it later
 *    - Canceled - a test was canceled, <em>i.e.</em>, not able to be completed
 *      because of some unmet pre-condition, such as a database being offline
 *      that the test requires
 *    - Pending - a test was declared as pending, <em>i.e.</em>, with test code
 *      and/or production code as yet unimplemented
 *
 *  The difference between errors and failures, if any, is determined by the
 *  test frameworks. JUnit and specs2 differentiate between errors and failures.
 *  ScalaTest reports everything (both assertion failures and unexpected errors)
 *  as failures. JUnit and ScalaTest support ignored tests. ScalaTest and specs2
 *  support a notion of pending tests. ScalaTest differentiates between ignored
 *  and canceled tests, whereas specs2 only supports skipped tests, which are
 *  implemented like ScalaTest's canceled tests. TestNG uses "skipped" to report
 *  tests that were not executed because of failures in dependencies, which is
 *  also similar to canceled tests in ScalaTest.
 */
enum Status:
  /** Indicates a test succeeded. */
  case Success extends Status

  /** Indicates an "error" occurred. */
  case Error extends Status

  /** Indicates a "failure" occurred. */
  case Failure extends Status

  /** Indicates a test was skipped. */
  case Skipped extends Status

  /** Indicates a test was ignored. */
  case Ignored extends Status

  /** Indicates a test was canceled. */
  case Canceled extends Status

  /** Indicates a test was declared as pending. */
  case Pending extends Status
