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

package scala.scalanative.javalib.net.www.protocol

import _root_.java.io.IOException
import _root_.java.net.{
  Proxy, URL, URLConnection, URLStreamHandler, UnknownServiceException
}

object BuiltinHandlers {

  /** The built-in handler of a lower-case protocol, or `null`. */
  def forProtocol(protocol: String): URLStreamHandler = protocol match {
    case "file"   => new file.Handler
    case "http"   => new ParseOnlyHandler("http", 80)
    case "https"  => new ParseOnlyHandler("https", 443)
    case "ftp"    => new ParseOnlyHandler("ftp", 21)
    case "jar"    => new ParseOnlyHandler("jar", -1)
    case "mailto" => new ParseOnlyHandler("mailto", -1)
    case _        => null
  }
}

/** Parses URLs of a protocol whose connections are not supported. */
final class ParseOnlyHandler(protocol: String, defaultPort: Int)
    extends URLStreamHandler {

  override def getDefaultPort(): Int = defaultPort

  @throws[IOException]
  override def openConnection(u: URL): URLConnection =
    throw new UnknownServiceException("protocol not supported: " + protocol)

  @throws[IOException]
  override def openConnection(u: URL, p: Proxy): URLConnection =
    openConnection(u)
}
