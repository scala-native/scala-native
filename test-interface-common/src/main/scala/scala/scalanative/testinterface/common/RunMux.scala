package scala.scalanative.testinterface.common

// Ported from Scala.JS

final class RunMux[+T](val runId: RunMux.RunID, val value: T)

object RunMux {
  type RunID = Int

  implicit def runMuxSerializer[T: Serializer]: Serializer[RunMux[T]] = {
    new Serializer[RunMux[T]] {
      def serialize(x: RunMux[T], out: Serializer.SerializeState): Unit = {
        out.write(x.runId)
        out.write(x.value)
      }

      def deserialize(in: Serializer.DeserializeState): RunMux[T] = {
        new RunMux(in.read[Int](), in.read[T]())
      }
    }
  }
}
