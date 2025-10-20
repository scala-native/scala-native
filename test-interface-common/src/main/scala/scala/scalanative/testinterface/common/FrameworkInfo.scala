package scala.scalanative.testinterface.common

// Ported from Scala.js

import sbt.testing.*

private[testinterface] final class FrameworkInfo(
    val implName: String,
    val displayName: String,
    val fingerprints: List[Fingerprint]
)

private[testinterface] object FrameworkInfo {
  implicit object FrameworkInfoSerializer extends Serializer[FrameworkInfo] {
    def serialize(x: FrameworkInfo, out: Serializer.SerializeState): Unit = {
      out.write(x.implName)
      out.write(x.displayName)
      out.write(x.fingerprints)
    }

    def deserialize(in: Serializer.DeserializeState): FrameworkInfo = {
      new FrameworkInfo(
        in.read[String](),
        in.read[String](),
        in.read[List[Fingerprint]]()
      )
    }
  }
}
