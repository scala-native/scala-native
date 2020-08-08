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

sealed trait Endpoint {
  val opCode: RPCCore.OpCode
}

sealed trait MsgEndpoint extends Endpoint {
  type Msg

  implicit val msgSerializer: Serializer[Msg]
}

object MsgEndpoint {

  /** Helper type for readability */
  type EP[M] = MsgEndpoint { type Msg = M }

  def apply[M](opc: RPCCore.OpCode)(implicit ms: Serializer[M]): EP[M] = {
    require(!RPCCore.isReservedOpCode(opc), s"Reserved op code: $opc")

    new MsgEndpoint {
      type Msg = M
      val opCode: RPCCore.OpCode                  = opc
      implicit val msgSerializer: Serializer[Msg] = ms
    }
  }
}

sealed trait RPCEndpoint extends Endpoint {
  type Req
  type Resp

  implicit val reqSerializer: Serializer[Req]
  implicit val respSerializer: Serializer[Resp]
}

object RPCEndpoint {

  /** Helper type for readability */
  type EP[Rq, Rp] = RPCEndpoint { type Req = Rq; type Resp = Rp }

  def apply[Rq, Rp](opc: RPCCore.OpCode)(implicit rqs: Serializer[Rq],
                                         rps: Serializer[Rp]): EP[Rq, Rp] = {
    require(!RPCCore.isReservedOpCode(opc), s"Reserved op code: $opc")

    new RPCEndpoint {
      type Req  = Rq
      type Resp = Rp
      val opCode: RPCCore.OpCode                    = opc
      implicit val reqSerializer: Serializer[Req]   = rqs
      implicit val respSerializer: Serializer[Resp] = rps
    }
  }
}
