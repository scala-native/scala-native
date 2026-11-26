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

// Ported from Apache Harmony (org.apache.harmony.luni.internal.net.www.MimeTable)
package scala.scalanative.javalib.net.www

import java.net.FileNameMap
import java.util.HashMap

object MimeTable extends FileNameMap {

  private val types = new HashMap[String, String]()

  private def add(tpe: String, exts: String*): Unit =
    exts.foreach(types.put(_, tpe))

  add("text/plain", "txt", "text", "java", "c", "h", "cc", "c++", "pl", "el")
  add("text/html", "htm", "html")
  add("text/css", "css")
  add("text/javascript", "js")
  add("text/csv", "csv")
  add("text/markdown", "md")
  add("text/x-setext", "etx")
  add("text/tab-separated-values", "tsv")
  add("application/xml", "xml")
  add("application/json", "json")
  add("application/rtf", "rtf")
  add("application/x-tex", "tex")
  add("application/x-shar", "sh", "shar")
  add("image/gif", "gif")
  add("image/jpeg", "jpg", "jpeg", "jpe", "jfif")
  add("image/png", "png")
  add("image/bmp", "bmp")
  add("image/svg+xml", "svg")
  add("image/tiff", "tif", "tiff")
  add("image/webp", "webp")
  add("image/x-xbitmap", "xbm", "xpm")
  add("image/x-portable-bitmap", "pbm")
  add("image/x-portable-graymap", "pgm")
  add("image/x-portable-pixmap", "ppm")
  add("image/x-portable-anymap", "pnm")
  add("image/x-cmu-rast", "ras")
  add("image/x-xwindowdump", "xwd")
  add("image/ief", "ief")
  add("audio/basic", "au", "snd")
  add("audio/x-wav", "wav")
  add("audio/x-aiff", "aif", "aiff", "aifc")
  add("audio/mpeg", "mp3")
  add("video/mp4", "mp4")
  add("video/mpeg", "mpg", "mpeg", "mpe")
  add("video/quicktime", "mov", "qt")
  add("video/x-sgi-movie", "movie")
  add("application/x-troff-msvideo", "avi")
  add("application/pdf", "pdf")
  add("application/postscript", "ps", "ai", "eps")
  add("application/zip", "zip")
  add("application/java-archive", "jar")
  add("application/gzip", "gz")
  add("application/x-tar", "tar")
  add("application/bz2", "bz2")
  add("application/x-7z-compressed", "7z")
  add("application/vnd.rar", "rar")
  add("application/octet-stream", "exe", "bin", "hqx")
  add("application/msword", "doc")
  add(
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "docx"
  )
  add("application/vnd.ms-excel", "xls")
  add(
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "xlsx"
  )
  add("application/vnd.ms-powerpoint", "ppt")
  add(
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "pptx"
  )
  add("application/vnd.oasis.opendocument.text", "odt")
  add("application/x-dvi", "dvi")
  add("application/x-latex", "latex")
  add("application/x-texinfo", "texi", "texinfo")
  add("application/x-hdf", "hdf")
  add("application/x-netcdf", "nc")
  add("application/x-sv4cpio", "sv4cpio")
  add("application/x-sv4crc", "sv4crc")
  add("application/x-cpio", "cpio")
  add("application/x-gtar", "gtar")
  add("application/oda", "oda")
  add("application/x-wais-source", "src")
  add("application/x-ustar", "ustar")
  add("application/x-troff-man", "man")
  add("application/x-troff-me", "me")
  add("application/x-troff-ms", "ms")
  add("application/x-troff", "t", "tr", "roff")
  add("application/wasm", "wasm")

  /** The type of the extension after the last `.` of the last path segment
   *  (ignoring case and a `#` reference), or `null`.
   */
  override def getContentTypeFor(fileName: String): String = {
    var name = fileName
    val hash = name.lastIndexOf('#')
    if (hash >= 0) name = name.substring(0, hash)
    val dot = name.lastIndexOf('.')
    if (dot < 0 || dot < name.lastIndexOf('/')) null
    else types.get(name.substring(dot + 1).toLowerCase())
  }
}
