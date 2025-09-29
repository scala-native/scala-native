package scala.scalanative
package buildinfo

import scala.sys.process.*

import java.time.*

case object ScalaNativeBuildInfo {

  val version: String = {
    if (System.getenv("GITHUB_REF_TYPE") == "tag")
      System.getenv("GITHUB_REF_NAME").stripPrefix("v")
    else {
      val shaGH = System.getenv("GITHUB_SHA")
      val argvDesc = Seq("git", "describe", "--tags", "--long")
      val tagDesc = Option(shaGH).fold(argvDesc)(argvDesc :+ _).!!.trim

      // tagDesc is like v0.4.0-19691231-NIGHTLY-10-gabcdef0123
      val shaIdx = tagDesc.lastIndexOf('-') // will need to skip the 'g'
      val cntIdx = tagDesc.lastIndexOf('-', shaIdx - 1)
      val tagOff = if (tagDesc(0) == 'v') 1 else 0

      // use BACKPORT_RELEASE to force a release build using prior tag
      if (System.getenv("BACKPORT_RELEASE") == "true" ||
          cntIdx + 2 == shaIdx && tagDesc(cntIdx + 1) == '0') // cnt is zero
        tagDesc.substring(tagOff, cntIdx)
      else {
        val sb = new java.lang.StringBuilder()
        sb.append(tagDesc, tagOff, cntIdx) // 1.2.3 from tag
        if (shaGH == null) // for local builds, use simple 1.2.3-next-SNAPSHOT
          sb.append("-next")
        else {
          val argvTime = Seq("git", "log", "-1", "--format=%ct", shaGH)
          format.DateTimeFormatter // now 1.2.3-19691231
            .ofPattern("-yyyyMMdd")
            .withZone(ZoneOffset.UTC)
            .formatTo(Instant.ofEpochSecond(argvTime.!!.trim.toLong), sb)
          // finally, 1.2.3-19691231+10-sha
          sb.append('+').append(tagDesc, cntIdx + 1, shaIdx)
          sb.append('-').append(tagDesc, shaIdx + 2, tagDesc.length)
        }
        sb.append("-SNAPSHOT")
        sb.toString
      }
    }
  }

}
