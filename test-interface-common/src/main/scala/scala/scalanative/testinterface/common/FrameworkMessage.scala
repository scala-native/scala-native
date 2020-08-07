package scala.scalanative.testinterface.common

// Ported from Scala.js

private[testinterface] final class FrameworkMessage(val workerId: Long,
                                                    val msg: String)

private[testinterface] object FrameworkMessage {
  implicit object FrameworkMessageSerializer
      extends Serializer[FrameworkMessage] {
    def serialize(x: FrameworkMessage, out: Serializer.SerializeState): Unit = {
      out.write(x.workerId)
      out.write(x.msg)
    }

    def deserialize(in: Serializer.DeserializeState): FrameworkMessage = {
      new FrameworkMessage(in.read[Long](), in.read[String]())
    }
  }
}
