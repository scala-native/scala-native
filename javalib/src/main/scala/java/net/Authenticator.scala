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

abstract class Authenticator {

  private var host: String = null
  private var addr: InetAddress = null
  private var port: Int = 0
  private var protocol: String = null
  private var prompt: String = null
  private var scheme: String = null
  private var url: URL = null
  private var rt: Authenticator.RequestorType = null

  /** Called when credentials are needed; the default returns `null`. */
  protected def getPasswordAuthentication(): PasswordAuthentication = null

  protected final def getRequestingHost(): String = host

  protected final def getRequestingSite(): InetAddress = addr

  protected final def getRequestingPort(): Int = port

  protected final def getRequestingProtocol(): String = protocol

  protected final def getRequestingPrompt(): String = prompt

  protected final def getRequestingScheme(): String = scheme

  protected def getRequestingURL(): URL = url

  protected def getRequestorType(): Authenticator.RequestorType = rt

  def requestPasswordAuthenticationInstance(
      host: String,
      addr: InetAddress,
      port: Int,
      protocol: String,
      prompt: String,
      scheme: String,
      url: URL,
      reqType: Authenticator.RequestorType
  ): PasswordAuthentication = synchronized {
    this.host = host
    this.addr = addr
    this.port = port
    this.protocol = protocol
    this.prompt = prompt
    this.scheme = scheme
    this.url = url
    this.rt = reqType
    getPasswordAuthentication()
  }
}

object Authenticator {

  final class RequestorType private (name: String, ordinal: Int)
      extends _Enum[RequestorType](name, ordinal)

  object RequestorType {
    final val PROXY = new RequestorType("PROXY", 0)
    final val SERVER = new RequestorType("SERVER", 1)

    private val cachedValues = Array(PROXY, SERVER)

    def values(): Array[RequestorType] = cachedValues.clone()

    def valueOf(name: String): RequestorType = {
      var i = 0
      while (i < cachedValues.length) {
        if (cachedValues(i).name() == name) return cachedValues(i)
        i += 1
      }
      throw new IllegalArgumentException(
        "No enum constant java.net.Authenticator.RequestorType." + name
      )
    }
  }

  @volatile private var theAuthenticator: Authenticator = null

  /** `null` removes the default authenticator. */
  def setDefault(a: Authenticator): Unit = theAuthenticator = a

  def getDefault(): Authenticator = theAuthenticator

  def requestPasswordAuthentication(
      addr: InetAddress,
      port: Int,
      protocol: String,
      prompt: String,
      scheme: String
  ): PasswordAuthentication =
    requestPasswordAuthentication(
      theAuthenticator,
      null,
      addr,
      port,
      protocol,
      prompt,
      scheme,
      null,
      RequestorType.SERVER
    )

  def requestPasswordAuthentication(
      host: String,
      addr: InetAddress,
      port: Int,
      protocol: String,
      prompt: String,
      scheme: String
  ): PasswordAuthentication =
    requestPasswordAuthentication(
      theAuthenticator,
      host,
      addr,
      port,
      protocol,
      prompt,
      scheme,
      null,
      RequestorType.SERVER
    )

  def requestPasswordAuthentication(
      host: String,
      addr: InetAddress,
      port: Int,
      protocol: String,
      prompt: String,
      scheme: String,
      url: URL,
      reqType: RequestorType
  ): PasswordAuthentication =
    requestPasswordAuthentication(
      theAuthenticator,
      host,
      addr,
      port,
      protocol,
      prompt,
      scheme,
      url,
      reqType
    )

  /** Asks `authenticator`, or the default one if it is `null`. */
  def requestPasswordAuthentication(
      authenticator: Authenticator,
      host: String,
      addr: InetAddress,
      port: Int,
      protocol: String,
      prompt: String,
      scheme: String,
      url: URL,
      reqType: RequestorType
  ): PasswordAuthentication = {
    val a = if (authenticator != null) authenticator else theAuthenticator
    if (a == null) null
    else
      a.requestPasswordAuthenticationInstance(
        host,
        addr,
        port,
        protocol,
        prompt,
        scheme,
        url,
        reqType
      )
  }
}
