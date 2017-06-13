package scala.scalanative
package testinterface
package serialization

import sbt.testing.Fingerprint

final case class FrameworkInfo(name: String, fingerprints: Seq[Fingerprint])
