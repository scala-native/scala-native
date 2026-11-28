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
import java.util.{List, Map}

abstract class CookieHandler {

  @throws[IOException]
  def get(
      uri: URI,
      requestHeaders: Map[String, List[String]]
  ): Map[String, List[String]]

  @throws[IOException]
  def put(uri: URI, responseHeaders: Map[String, List[String]]): Unit
}

object CookieHandler {

  @volatile private var defaultHandler: CookieHandler = null

  def getDefault(): CookieHandler = defaultHandler

  /** `null` removes the default handler. */
  def setDefault(cHandler: CookieHandler): Unit = defaultHandler = cHandler
}
