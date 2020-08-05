package scala.scalanative.testinterface.common

// Ported from Scala.JS

import sbt.testing._

final class FrameworkInfo(val implName: String,
                          val displayName: String,
                          val fingerprints: List[Fingerprint])

object FrameworkInfo {
  implicit object FrameworkInfoSerializer extends Serializer[FrameworkInfo] {
    def serialize(x: FrameworkInfo, out: Serializer.SerializeState): Unit = {
      out.write(x.implName)
      out.write(x.displayName)
      out.write(x.fingerprints)
    }

    def deserialize(in: Serializer.DeserializeState): FrameworkInfo = {
      new FrameworkInfo(in.read[String](),
                        in.read[String](),
                        in.read[List[Fingerprint]]())
    }
  }
}
