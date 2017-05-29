package scala.scalanative
package testinterface
package serialization

import sbt.testing.Fingerprint

import Serializer.{serialize => s, deserialize => d, _}

final case class FrameworkInfo(name: String, fingerprints: Seq[Fingerprint])

object FrameworkInfo {
  implicit val FrameworkInfoSerializable: Serializable[FrameworkInfo] =
    new Serializable[FrameworkInfo] {
      override def name: String = "FrameworkInfo"
      override def serialize(v: FrameworkInfo): Iterator[String] =
        s(v.name) ++ s(v.fingerprints)
      override def deserialize(in: Iterator[String]): FrameworkInfo =
        FrameworkInfo(d[String](in), d[Seq[Fingerprint]](in))
    }
}
