package org.scalanative.testsuite.utils

import org.junit.Assume._

object ContinuousIntegrationEnvironment {
  // Some Tests require conditions which may not hold in
  // developer environments or if CI moves off GitHub_Actions.

  def isGitHubCI(): Boolean =
    (System.getenv("GITHUB_ACTIONS") != null) && (System.getenv("CI") != null)

  def skipIfNotKnownCIEnv(): Unit = {
    // Scala Native Windows CI envirionment is not yet defined.
    val isWindows = scalanative.runtime.Platform.isWindows()

    assumeTrue("Not executing as known GitHub CI, skipping...",
               (isGitHubCI() && (!isWindows)))
  }

  // A User Principal name known to exist.
  def existentUserName(): String = "root"

  // A User Principal name known to not exist.
  def nonExistentUserName(): String = "gobbledygook"

  // A Group Principal name known to exist.
  def existentGroupName(): String = "root"

  // A Group Principal name known to not exist.
  def nonExistentGroupName(): String = "gobbledygook"
}
