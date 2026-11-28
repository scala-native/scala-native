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

// Ported from Apache Harmony, without the permission checks.

package java.net

import java.io.IOException
import java.util.{Collections, List}

import scala.scalanative.javalib.net.DefaultProxySelector

abstract class ProxySelector {

  def select(uri: URI): List[Proxy]

  def connectFailed(uri: URI, sa: SocketAddress, ioe: IOException): Unit
}

object ProxySelector {

  @volatile private var defaultSelector: ProxySelector =
    new DefaultProxySelector

  /** The initial selector follows the proxy system properties. */
  def getDefault(): ProxySelector = defaultSelector

  /** `null` removes the default selector. */
  def setDefault(ps: ProxySelector): Unit = defaultSelector = ps

  /** Selects an HTTP proxy at `proxyAddress` for `http` and `https` URIs,
   *  `DIRECT` for other URIs or when `proxyAddress` is `null`.
   */
  def of(proxyAddress: InetSocketAddress): ProxySelector =
    new StaticProxySelector(proxyAddress)

  private final class StaticProxySelector(address: InetSocketAddress)
      extends ProxySelector {

    private val proxies = Collections.singletonList(
      if (address == null) Proxy.NO_PROXY
      else new Proxy(Proxy.Type.HTTP, address)
    )

    override def select(uri: URI): List[Proxy] = {
      if (uri == null) throw new IllegalArgumentException("URI can't be null")
      val scheme = uri.getScheme()
      if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
        proxies
      else DefaultProxySelector.Direct
    }

    override def connectFailed(
        uri: URI,
        sa: SocketAddress,
        ioe: IOException
    ): Unit =
      if (uri == null || sa == null || ioe == null)
        throw new IllegalArgumentException("Arguments can't be null.")
  }
}
