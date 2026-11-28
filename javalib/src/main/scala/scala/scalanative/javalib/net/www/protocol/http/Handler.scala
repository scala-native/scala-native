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
// (org.apache.harmony.luni.internal.net.www.protocol.http.Handler)

package scala.scalanative.javalib.net.www.protocol.http

import java.io.IOException
import java.net.{Proxy, URL, URLConnection, URLStreamHandler}

final class Handler extends URLStreamHandler {

  override def getDefaultPort(): Int = 80

  @throws[IOException]
  override def openConnection(u: URL): URLConnection =
    new HttpURLConnectionImpl(u, null)

  @throws[IOException]
  override def openConnection(u: URL, p: Proxy): URLConnection = {
    if (p == null) throw new IllegalArgumentException("proxy can not be null")
    new HttpURLConnectionImpl(u, p)
  }
}
