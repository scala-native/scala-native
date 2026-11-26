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
// (org.apache.harmony.luni.internal.net.www.protocol.jar.JarURLConnectionImpl)

package scala.scalanative.javalib.net.www.protocol.jar

import java.io.{
  BufferedInputStream, File, FileNotFoundException, FileOutputStream,
  FilterInputStream, IOException, InputStream
}
import java.net.{JarURLConnection, URL, URLConnection}
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.{JarEntry, JarFile}
import java.util.zip.ZipFile

import scala.scalanative.javalib.net.www.protocol.file.FileURLConnection

/** A connection to a local JAR file, or a temporary copy of a remote one. With
 *  `useCaches` (the default) connections to the same JAR file share one open
 *  `JarFile`; otherwise it is closed with the entry's input stream.
 */
final class JarURLConnectionImpl(u: URL) extends JarURLConnection(u) {

  private var jarFile: JarFile = null
  private var entry: JarEntry = null

  @throws[IOException]
  override def connect(): Unit = if (!connected) {
    jarFile = JarURLConnectionImpl.open(getJarFileURL(), getUseCaches())
    val name = getEntryName()
    if (name != null) {
      entry = jarFile.getJarEntry(name)
      if (entry == null) {
        if (!getUseCaches()) jarFile.close()
        throw new FileNotFoundException("JAR entry not found in jar file")
      }
    }
    connected = true
  }

  /** The `JarFile` for the JAR file itself, else the entry's content. */
  @throws[IOException]
  override def getContent(): AnyRef = {
    connect()
    if (entry == null) jarFile else super.getContent()
  }

  @throws[IOException]
  override def getJarFile(): JarFile = {
    connect()
    jarFile
  }

  @throws[IOException]
  override def getJarEntry(): JarEntry = {
    connect()
    entry
  }

  @throws[IOException]
  override def getInputStream(): InputStream = {
    connect()
    if (entry == null) throw new IOException("no entry name specified")
    val in = jarFile.getInputStream(entry)
    if (getUseCaches()) in
    else new JarURLConnectionImpl.ClosingInputStream(in, jarFile)
  }

  /** `x-java/jar` for the JAR file itself; for an entry, guessed from its name,
   *  then from its content.
   */
  override def getContentType(): String =
    if (getEntryName() == null) "x-java/jar"
    else
      try {
        connect()
        var t = URLConnection.guessContentTypeFromName(entry.getName())
        if (t == null && !entry.isDirectory()) {
          val in = new BufferedInputStream(jarFile.getInputStream(entry), 64)
          try t = URLConnection.guessContentTypeFromStream(in)
          finally in.close()
        }
        if (t == null) "content/unknown" else t
      } catch { case _: IOException => "content/unknown" }

  /** The size of the entry, or of the JAR file itself; -1 if unknown. */
  override def getContentLengthLong(): Long =
    try {
      connect()
      if (entry != null) entry.getSize()
      else new File(jarFile.getName()).length()
    } catch { case _: IOException => -1L }

  override def getContentLength(): Int = {
    val n = getContentLengthLong()
    if (n > Int.MaxValue) -1 else n.toInt
  }

  override def getHeaderField(name: String): String =
    try jarFileConnection().getHeaderField(name)
    catch { case _: IOException => null }

  override def getLastModified(): Long =
    try {
      connect()
      val t = if (entry == null) -1L else entry.getTime()
      if (t != -1L) t else jarFileConnection().getLastModified()
    } catch { case _: IOException => 0L }

  @throws[IOException]
  private def jarFileConnection(): URLConnection = {
    if (jarFileURLConnection == null)
      jarFileURLConnection = getJarFileURL().openConnection()
    jarFileURLConnection
  }
}

private object JarURLConnectionImpl {

  private val cache = new ConcurrentHashMap[String, JarFile]()

  @throws[IOException]
  private def open(url: URL, useCaches: Boolean): JarFile = {
    val key = url.toExternalForm()
    if (useCaches) {
      val cached = cache.get(key)
      if (cached != null) return cached
    }
    val jar =
      if (url.getProtocol() == "file" && FileURLConnection.isLocal(url)) {
        val file = FileURLConnection.toFile(url)
        if (file.getPath().indexOf('\u0000') >= 0)
          throw new FileNotFoundException("Invalid file path")
        new CachedJarFile(file, ZipFile.OPEN_READ, key)
      } else {
        val tmp = File.createTempFile("jar_cache", null)
        tmp.deleteOnExit()
        try {
          val in = url.openStream()
          try {
            // Written in place: replacing it could follow a planted symlink
            val out = new FileOutputStream(tmp)
            try in.transferTo(out)
            finally out.close()
          } finally in.close()
          new CachedJarFile(tmp, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE, key)
        } catch {
          case e: IOException =>
            tmp.delete()
            throw e
        }
      }
    if (useCaches) {
      val previous = cache.putIfAbsent(key, jar)
      if (previous != null) {
        jar.close()
        return previous
      }
    }
    jar
  }

  /** Leaves the cache when closed. */
  private final class CachedJarFile(file: File, mode: Int, key: String)
      extends JarFile(file, true, mode) {
    override def close(): Unit = {
      cache.remove(key, this)
      super.close()
    }
  }

  private final class ClosingInputStream(in: InputStream, jar: JarFile)
      extends FilterInputStream(in) {
    override def close(): Unit =
      try super.close()
      finally jar.close()
  }
}
