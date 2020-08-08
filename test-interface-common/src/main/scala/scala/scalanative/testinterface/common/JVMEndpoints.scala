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

import sbt.testing.Event

object JVMEndpoints {
  val msgSlave: MsgEndpoint.EP[RunMux[String]] = MsgEndpoint[RunMux[String]](2)

  val msgMaster: MsgEndpoint.EP[RunMux[FrameworkMessage]] =
    MsgEndpoint[RunMux[FrameworkMessage]](3)

  val event: MsgEndpoint.EP[RunMux[Event]] = MsgEndpoint[RunMux[Event]](4)

  val logError: MsgEndpoint.EP[RunMux[LogElement[String]]] =
    MsgEndpoint[RunMux[LogElement[String]]](5)

  val logWarn: MsgEndpoint.EP[RunMux[LogElement[String]]] =
    MsgEndpoint[RunMux[LogElement[String]]](6)

  val logInfo: MsgEndpoint.EP[RunMux[LogElement[String]]] =
    MsgEndpoint[RunMux[LogElement[String]]](7)

  val logDebug: MsgEndpoint.EP[RunMux[LogElement[String]]] =
    MsgEndpoint[RunMux[LogElement[String]]](8)

  val logTrace: MsgEndpoint.EP[RunMux[LogElement[Throwable]]] =
    MsgEndpoint[RunMux[LogElement[Throwable]]](9)
}
