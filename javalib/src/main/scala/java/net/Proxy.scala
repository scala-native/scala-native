/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Ported from Apache Harmony

package java.net

class Proxy private (tpe: Proxy.Type, sa: SocketAddress, direct: Boolean) {

  /** @throws IllegalArgumentException
   *    if `type` is `DIRECT` or `sa` is `null`
   */
  def this(`type`: Proxy.Type, sa: SocketAddress) = {
    this(`type`, sa, false)
    if (`type` == Proxy.Type.DIRECT || sa == null)
      throw new IllegalArgumentException(
        "type " + `type` + " is not compatible with address " + sa
      )
  }

  def `type`(): Proxy.Type = tpe

  def address(): SocketAddress = sa

  override def toString(): String =
    if (tpe == Proxy.Type.DIRECT) "DIRECT"
    else String.valueOf(tpe) + " @ " + sa

  override final def equals(obj: Any): Boolean = obj match {
    case p: Proxy =>
      (p eq this) || (p.`type`() == tpe && p.address() == sa)
    case _ => false
  }

  override final def hashCode(): Int =
    (if (tpe == null) 0 else tpe.hashCode()) +
      (if (sa == null) 0 else sa.hashCode())
}

object Proxy {

  final class Type private (name: String, ordinal: Int)
      extends _Enum[Type](name, ordinal)

  object Type {
    final val DIRECT = new Type("DIRECT", 0)
    final val HTTP = new Type("HTTP", 1)
    final val SOCKS = new Type("SOCKS", 2)

    private val cachedValues = Array(DIRECT, HTTP, SOCKS)

    def values(): Array[Type] = cachedValues.clone()

    def valueOf(name: String): Type =
      cachedValues.find(_.name() == name).getOrElse {
        throw new IllegalArgumentException(
          "No enum constant java.net.Proxy.Type." + name
        )
      }
  }

  final val NO_PROXY: Proxy = new Proxy(Type.DIRECT, null, true)
}
