/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.scalanative.testinterface.common

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
