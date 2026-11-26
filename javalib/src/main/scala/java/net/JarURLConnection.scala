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

import java.io.IOException
import java.security.cert.Certificate
import java.util.jar.{Attributes, JarEntry, JarFile, Manifest}

import scala.scalanative.javalib.net.www.protocol.file.FileURLConnection

/** A connection to a JAR file or an entry in it: `jar:<url>!/[entry]`. */
abstract class JarURLConnection protected (url: URL)
    extends URLConnection(url) {

  protected var jarFileURLConnection: URLConnection = null

  private val (jarFileURL, entryName) = {
    val file = url.getFile()
    val sep = file.indexOf("!/")
    if (sep < 0)
      throw new MalformedURLException("no !/ found in url spec:" + file)
    if (file.regionMatches(true, 0, "jar:", 0, 4))
      throw new MalformedURLException("Nested JAR URLs are not supported")
    val entry = file.substring(sep + 2)
    (
      new URL(file.substring(0, sep)),
      if (entry.isEmpty) null else FileURLConnection.decode(entry)
    )
  }

  def getJarFileURL(): URL = jarFileURL

  /** The entry name, or `null` for the JAR file itself. */
  def getEntryName(): String = entryName

  @throws[IOException]
  def getJarFile(): JarFile

  /** A copy of the manifest of the JAR file, or `null`. */
  @throws[IOException]
  def getManifest(): Manifest = {
    val m = getJarFile().getManifest()
    if (m == null) null
    else {
      val copy = new Manifest(m)
      val it = copy.getEntries().entrySet().iterator()
      while (it.hasNext()) {
        val e = it.next()
        e.setValue(e.getValue().clone().asInstanceOf[Attributes])
      }
      copy
    }
  }

  /** The entry, or `null` for the JAR file itself. */
  @throws[IOException]
  def getJarEntry(): JarEntry =
    if (entryName == null) null else getJarFile().getJarEntry(entryName)

  @throws[IOException]
  def getAttributes(): Attributes = {
    val e = getJarEntry()
    if (e == null) null else e.getAttributes()
  }

  @throws[IOException]
  def getMainAttributes(): Attributes = {
    val m = getManifest()
    if (m == null) null else m.getMainAttributes()
  }

  /** Always `null`: signed JAR files are not verified. Does not link yet, as
   *  `java.security.cert.Certificate` is missing.
   */
  @throws[IOException]
  def getCertificates(): Array[Certificate] = {
    getJarEntry()
    null
  }
}
