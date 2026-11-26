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

package scala.scalanative.javalib.net

/** HTTP header dates as epoch milliseconds (UTC), without java.text or time
 *  zone data.
 */
object HttpDates {

  private val shortDays = Array("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
  private val longDays = Array(
    "monday",
    "tuesday",
    "wednesday",
    "thursday",
    "friday",
    "saturday",
    "sunday"
  )
  private val months = Array(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec"
  )
  private val fullMonths = Array(
    "january",
    "february",
    "march",
    "april",
    "may",
    "june",
    "july",
    "august",
    "september",
    "october",
    "november",
    "december"
  )

  /** RFC 1123 in GMT: `Thu, 02 Jan 2020 02:04:05 GMT`. */
  def format(millis: Long): String = {
    val days = Math.floorDiv(millis, 86400000L)
    val ms = Math.floorMod(millis, 86400000L)
    val (y, m, d) = civilFromDays(days)
    def two(n: Long) = if (n < 10) "0" + n else n.toString
    shortDays(Math.floorMod(days + 3, 7L).toInt) + ", " + two(d) + " " +
      months(m - 1) + " " + y + " " + two(ms / 3600000) + ":" +
      two(ms / 60000 % 60) + ":" + two(ms / 1000 % 60) + " GMT"
  }

  private val zones = Array(
    "gmt" -> 0,
    "ut" -> 0,
    "utc" -> 0,
    "est" -> -300,
    "edt" -> -240,
    "cst" -> -360,
    "cdt" -> -300,
    "mst" -> -420,
    "mdt" -> -360,
    "pst" -> -480,
    "pdt" -> -420
  )

  /** Parses a date leniently, following the rules of the `Date.parse` Javadoc:
   *  numbers are classified by the separator after them (`:` hour/minute, `/`
   *  month/day, otherwise minute, second, day or year), words are AM/PM, day
   *  and month names (3 letters suffice) and the US zone abbreviations,
   *  `+hhmm`/`-hh` after the year are offsets, text in parentheses is ignored.
   *  A two-digit year is within 80 years before and 19 after now. Without a
   *  zone the date is read as GMT. `Long.MinValue` if it does not parse.
   */
  def parseLenient(s: String): Long = {
    val Unset = Int.MinValue
    var year, month, day, hour, minute, second, offset = Unset
    var pm = -1 // -1: no AM/PM, 0: AM, 1: PM
    var i = 0
    val n = s.length
    var prev = ' '
    while (i < n) {
      val c = s.charAt(i)
      if (c <= ' ' || c == ',') { prev = c; i += 1 }
      else if (c == '(') {
        var depth = 1
        i += 1
        while (i < n && depth > 0) {
          if (s.charAt(i) == '(') depth += 1
          else if (s.charAt(i) == ')') depth -= 1
          i += 1
        }
        prev = ' '
      } else if (c == '+' || c == '-' || c == '/' || c == ':') {
        prev = c
        i += 1
      } else if (c >= '0' && c <= '9') {
        val start = i
        var v = 0L
        while (i < n && s.charAt(i) >= '0' && s.charAt(i) <= '9') {
          v = v * 10 + (s.charAt(i) - '0')
          if (v > Int.MaxValue) return Long.MinValue
          i += 1
        }
        val num = v.toInt
        val next = if (i < n) s.charAt(i) else ' '
        if ((prev == '+' || prev == '-') && year != Unset) {
          if (next == ':') return Long.MinValue
          val minutes = if (num < 24) num * 60 else num / 100 * 60 + num % 100
          offset = if (prev == '-') -minutes else minutes
        } else if (next == ':') {
          if (hour == Unset) hour = num
          else if (minute == Unset) minute = num
          else return Long.MinValue
        } else if (next == '/') {
          if (month == Unset) month = num - 1
          else if (day == Unset) day = num
          else return Long.MinValue
        } else if (num >= 70 && year == Unset && i - start > 0 &&
            (next <= ' ' || next == ',' || next == '/' || i >= n)) {
          year = num
        } else if (hour != Unset && minute == Unset) minute = num
        else if (minute != Unset && second == Unset) second = num
        else if (day == Unset) day = num
        else if (year == Unset) year = num
        else return Long.MinValue
        prev = ' '
      } else if (Character.isLetter(c)) {
        val start = i
        while (i < n && Character.isLetter(s.charAt(i))) i += 1
        val w = s.substring(start, i).toLowerCase()
        val zone = zones.find(_._1 == w)
        if (w == "am") pm = 0
        else if (w == "pm") pm = 1
        else if (zone.isDefined) offset = zone.get._2
        else if (w.length >= 3 && longDays.exists(_.startsWith(w))) ()
        else {
          val m = fullMonths.indexWhere(_.startsWith(w))
          if (m >= 0 && w.length >= 3 && month == Unset) month = m
          else return Long.MinValue
        }
        prev = ' '
      } else return Long.MinValue
    }
    if (year == Unset || month == Unset || day == Unset) return Long.MinValue
    if (year < 100) {
      val now =
        civilFromDays(Math.floorDiv(System.currentTimeMillis(), 86400000L))._1
      val century = (now - 80) / 100 * 100
      year += century.toInt
      if (year < now - 80) year += 100
    }
    var h = if (hour == Unset) 0 else hour
    if (pm == 1 && h < 12) h += 12
    else if (pm == 0 && h == 12) h = 0
    val mi = if (minute == Unset) 0 else minute
    val se = if (second == Unset) 0 else second
    if (month < 0 || month > 11 || day < 1 || day > 31 || h > 23 || mi > 59 ||
        se > 59)
      return Long.MinValue
    val local = (daysFromCivil(year, month + 1, 1) + day - 1) * 86400000L +
      ((h * 60L + mi) * 60L + se) * 1000L
    local - (if (offset == Unset) 0 else offset) * 60000L
  }

  /** (year, month 1-12, day) of a day count since 1970-01-01, proleptic
   *  Gregorian.
   */
  def civilFromDays(z0: Long): (Long, Int, Int) = {
    val z = z0 + 719468
    val era = Math.floorDiv(z, 146097L)
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = (doy - (153 * mp + 2) / 5 + 1).toInt
    val m = (if (mp < 10) mp + 3 else mp - 9).toInt
    (yoe + era * 400 + (if (m <= 2) 1 else 0), m, d)
  }

  /** Days since 1970-01-01 of a proleptic Gregorian date (month 1-12). */
  def daysFromCivil(year: Long, month: Int, day: Int): Long = {
    val y = if (month <= 2) year - 1 else year
    val era = Math.floorDiv(y, 400L)
    val yoe = y - era * 400
    val mp = (month + 9) % 12
    val doy = (153 * mp + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    era * 146097 + doe - 719468
  }
}
