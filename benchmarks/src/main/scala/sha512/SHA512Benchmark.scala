/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js Benchmarks        **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013, Jonas Fonseca    **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \                               **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */

/*
 *  SHA-384/512 implementation
 *
 *  Copyright (C) 2006-2015, ARM Limited, All Rights Reserved
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may
 *  not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/* This is a Scala implementation of the SHA-512 hashing algorithm from:
 * https://github.com/ARMmbed/mbedtls/blob/bfafadb45daf8d2114e3109e2f9021fc72ee36bb/library/sha512.c
 *
 * Ported by Sébastien Doeraene
 *
 * This implementation MUST NOT be used for any cryptographic usage. I did not
 * make any effort to ensure its correctness wrt. the specification. It is only
 * intended as a benchmark.
 */

package sha512

/**
 * SHA-512 hashing.
 */
class SHA512Benchmark extends benchmarks.Benchmark[Boolean] {
  override def run(): Boolean = {
    disableBenchmark()
    // Doesn't link.
    // Test.selfTest(verbose = false)
    true
  }

  override def check(t: Boolean): Boolean =
    t
}

object Test {

  /*
   * FIPS-180-2 test vectors
   */
  val sha512TestBuf = Array(
    "abc",
    "abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmn" +
      "hijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu",
    "a" * 1000
  ).map(_.toArray.map(_.toByte))

  val sha512TestSum = Array(
    /*
     * SHA-384 test vectors
     */
    Array(
      0xCB,
      0x00,
      0x75,
      0x3F,
      0x45,
      0xA3,
      0x5E,
      0x8B,
      0xB5,
      0xA0,
      0x3D,
      0x69,
      0x9A,
      0xC6,
      0x50,
      0x07,
      0x27,
      0x2C,
      0x32,
      0xAB,
      0x0E,
      0xDE,
      0xD1,
      0x63,
      0x1A,
      0x8B,
      0x60,
      0x5A,
      0x43,
      0xFF,
      0x5B,
      0xED,
      0x80,
      0x86,
      0x07,
      0x2B,
      0xA1,
      0xE7,
      0xCC,
      0x23,
      0x58,
      0xBA,
      0xEC,
      0xA1,
      0x34,
      0xC8,
      0x25,
      0xA7
    ),
    Array(
      0x09,
      0x33,
      0x0C,
      0x33,
      0xF7,
      0x11,
      0x47,
      0xE8,
      0x3D,
      0x19,
      0x2F,
      0xC7,
      0x82,
      0xCD,
      0x1B,
      0x47,
      0x53,
      0x11,
      0x1B,
      0x17,
      0x3B,
      0x3B,
      0x05,
      0xD2,
      0x2F,
      0xA0,
      0x80,
      0x86,
      0xE3,
      0xB0,
      0xF7,
      0x12,
      0xFC,
      0xC7,
      0xC7,
      0x1A,
      0x55,
      0x7E,
      0x2D,
      0xB9,
      0x66,
      0xC3,
      0xE9,
      0xFA,
      0x91,
      0x74,
      0x60,
      0x39
    ),
    Array(
      0x9D,
      0x0E,
      0x18,
      0x09,
      0x71,
      0x64,
      0x74,
      0xCB,
      0x08,
      0x6E,
      0x83,
      0x4E,
      0x31,
      0x0A,
      0x4A,
      0x1C,
      0xED,
      0x14,
      0x9E,
      0x9C,
      0x00,
      0xF2,
      0x48,
      0x52,
      0x79,
      0x72,
      0xCE,
      0xC5,
      0x70,
      0x4C,
      0x2A,
      0x5B,
      0x07,
      0xB8,
      0xB3,
      0xDC,
      0x38,
      0xEC,
      0xC4,
      0xEB,
      0xAE,
      0x97,
      0xDD,
      0xD8,
      0x7F,
      0x3D,
      0x89,
      0x85
    ),
    /*
     * SHA-512 test vectors
     */
    Array(
      0xDD,
      0xAF,
      0x35,
      0xA1,
      0x93,
      0x61,
      0x7A,
      0xBA,
      0xCC,
      0x41,
      0x73,
      0x49,
      0xAE,
      0x20,
      0x41,
      0x31,
      0x12,
      0xE6,
      0xFA,
      0x4E,
      0x89,
      0xA9,
      0x7E,
      0xA2,
      0x0A,
      0x9E,
      0xEE,
      0xE6,
      0x4B,
      0x55,
      0xD3,
      0x9A,
      0x21,
      0x92,
      0x99,
      0x2A,
      0x27,
      0x4F,
      0xC1,
      0xA8,
      0x36,
      0xBA,
      0x3C,
      0x23,
      0xA3,
      0xFE,
      0xEB,
      0xBD,
      0x45,
      0x4D,
      0x44,
      0x23,
      0x64,
      0x3C,
      0xE8,
      0x0E,
      0x2A,
      0x9A,
      0xC9,
      0x4F,
      0xA5,
      0x4C,
      0xA4,
      0x9F
    ),
    Array(
      0x8E,
      0x95,
      0x9B,
      0x75,
      0xDA,
      0xE3,
      0x13,
      0xDA,
      0x8C,
      0xF4,
      0xF7,
      0x28,
      0x14,
      0xFC,
      0x14,
      0x3F,
      0x8F,
      0x77,
      0x79,
      0xC6,
      0xEB,
      0x9F,
      0x7F,
      0xA1,
      0x72,
      0x99,
      0xAE,
      0xAD,
      0xB6,
      0x88,
      0x90,
      0x18,
      0x50,
      0x1D,
      0x28,
      0x9E,
      0x49,
      0x00,
      0xF7,
      0xE4,
      0x33,
      0x1B,
      0x99,
      0xDE,
      0xC4,
      0xB5,
      0x43,
      0x3A,
      0xC7,
      0xD3,
      0x29,
      0xEE,
      0xB6,
      0xDD,
      0x26,
      0x54,
      0x5E,
      0x96,
      0xE5,
      0x5B,
      0x87,
      0x4B,
      0xE9,
      0x09
    ),
    Array(
      0xE7,
      0x18,
      0x48,
      0x3D,
      0x0C,
      0xE7,
      0x69,
      0x64,
      0x4E,
      0x2E,
      0x42,
      0xC7,
      0xBC,
      0x15,
      0xB4,
      0x63,
      0x8E,
      0x1F,
      0x98,
      0xB1,
      0x3B,
      0x20,
      0x44,
      0x28,
      0x56,
      0x32,
      0xA8,
      0x03,
      0xAF,
      0xA9,
      0x73,
      0xEB,
      0xDE,
      0x0F,
      0xF2,
      0x44,
      0x87,
      0x7E,
      0xA6,
      0x0A,
      0x4C,
      0xB0,
      0x43,
      0x2C,
      0xE5,
      0x77,
      0xC3,
      0x1B,
      0xEB,
      0x00,
      0x9C,
      0x5C,
      0x2C,
      0x49,
      0xAA,
      0x2E,
      0x4E,
      0xAD,
      0xB2,
      0x17,
      0xAD,
      0x8C,
      0xC0,
      0x9B
    )
  ).map(_.map(_.toByte))

  /*
   * Checkup routine
   */
  def selfTest(verbose: Boolean): Boolean = {
    for (i <- 0 until sha512TestSum.length) {
      val j     = i % 3
      val is384 = i < 3

      if (verbose)
        print(s"  SHA-${if (is384) 384 else 512} test #${j + 1}: ")

      val ctx = new SHA512Context(is384)
      val buf = sha512TestBuf(j)

      if (j == 2) {
        for (x <- 0 until 1000)
          ctx.update(buf, buf.length)
      } else {
        ctx.update(buf, buf.length)
      }

      val sha512sum = new Array[Byte](if (!is384) 64 else 48)
      ctx.finish(sha512sum)

      if (!java.util.Arrays.equals(sha512sum, sha512TestSum(i))) {
        if (verbose)
          println("failed")
        return false
      }

      if (verbose)
        println("passed")
    }

    if (verbose)
      println()

    true
  }
}

object SHA512Context {

  def sha512(input: Array[Byte], is384: Boolean = false): Array[Byte] = {
    val ctx    = new SHA512Context(is384)
    val output = new Array[Byte](if (!is384) 64 else 48)
    ctx.update(input, input.length)
    ctx.finish(output)
    output
  }

  /*
   * 64-bit integer manipulation macros (big endian)
   */
  @inline
  def getUInt64BE(b: Array[Byte], i: Int): Long = {
    ((b(i) & 0xffL) << 56) |
      ((b(i + 1) & 0xffL) << 48) |
      ((b(i + 2) & 0xffL) << 40) |
      ((b(i + 3) & 0xffL) << 32) |
      ((b(i + 4) & 0xffL) << 24) |
      ((b(i + 5) & 0xffL) << 16) |
      ((b(i + 6) & 0xffL) << 8) |
      (b(i + 7) & 0xffL)
  }

  @inline
  def putUInt64BE(b: Array[Byte], i: Int, n: Long): Unit = {
    b(i) = (n >> 56).toByte
    b(i + 1) = (n >> 48).toByte
    b(i + 2) = (n >> 40).toByte
    b(i + 3) = (n >> 32).toByte
    b(i + 4) = (n >> 24).toByte
    b(i + 5) = (n >> 16).toByte
    b(i + 6) = (n >> 8).toByte
    b(i + 7) = n.toByte
  }

  /*
   * Round constants
   */
  val K = Array[Long](
    0x428A2F98D728AE22L,
    0x7137449123EF65CDL,
    0xB5C0FBCFEC4D3B2FL,
    0xE9B5DBA58189DBBCL,
    0x3956C25BF348B538L,
    0x59F111F1B605D019L,
    0x923F82A4AF194F9BL,
    0xAB1C5ED5DA6D8118L,
    0xD807AA98A3030242L,
    0x12835B0145706FBEL,
    0x243185BE4EE4B28CL,
    0x550C7DC3D5FFB4E2L,
    0x72BE5D74F27B896FL,
    0x80DEB1FE3B1696B1L,
    0x9BDC06A725C71235L,
    0xC19BF174CF692694L,
    0xE49B69C19EF14AD2L,
    0xEFBE4786384F25E3L,
    0x0FC19DC68B8CD5B5L,
    0x240CA1CC77AC9C65L,
    0x2DE92C6F592B0275L,
    0x4A7484AA6EA6E483L,
    0x5CB0A9DCBD41FBD4L,
    0x76F988DA831153B5L,
    0x983E5152EE66DFABL,
    0xA831C66D2DB43210L,
    0xB00327C898FB213FL,
    0xBF597FC7BEEF0EE4L,
    0xC6E00BF33DA88FC2L,
    0xD5A79147930AA725L,
    0x06CA6351E003826FL,
    0x142929670A0E6E70L,
    0x27B70A8546D22FFCL,
    0x2E1B21385C26C926L,
    0x4D2C6DFC5AC42AEDL,
    0x53380D139D95B3DFL,
    0x650A73548BAF63DEL,
    0x766A0ABB3C77B2A8L,
    0x81C2C92E47EDAEE6L,
    0x92722C851482353BL,
    0xA2BFE8A14CF10364L,
    0xA81A664BBC423001L,
    0xC24B8B70D0F89791L,
    0xC76C51A30654BE30L,
    0xD192E819D6EF5218L,
    0xD69906245565A910L,
    0xF40E35855771202AL,
    0x106AA07032BBD1B8L,
    0x19A4C116B8D2D0C8L,
    0x1E376C085141AB53L,
    0x2748774CDF8EEB99L,
    0x34B0BCB5E19B48A8L,
    0x391C0CB3C5C95A63L,
    0x4ED8AA4AE3418ACBL,
    0x5B9CCA4F7763E373L,
    0x682E6FF3D6B2B8A3L,
    0x748F82EE5DEFB2FCL,
    0x78A5636F43172F60L,
    0x84C87814A1F0AB72L,
    0x8CC702081A6439ECL,
    0x90BEFFFA23631E28L,
    0xA4506CEBDE82BDE9L,
    0xBEF9A3F7B2C67915L,
    0xC67178F2E372532BL,
    0xCA273ECEEA26619CL,
    0xD186B8C721C0C207L,
    0xEADA7DD6CDE0EB1EL,
    0xF57D4F7FEE6ED178L,
    0x06F067AA72176FBAL,
    0x0A637DC5A2C898A6L,
    0x113F9804BEF90DAEL,
    0x1B710B35131C471BL,
    0x28DB77F523047D84L,
    0x32CAAB7B40C72493L,
    0x3C9EBE0A15C9BEBCL,
    0x431D67C49C100D4CL,
    0x4CC5D4BECB3E42B6L,
    0x597F299CFC657E2AL,
    0x5FCB6FAB3AD6FAECL,
    0x6C44198C4A475817L
  )

  val padding = Array[Byte](
    0x80.toByte,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0
  )
}

class SHA512Context(val is384: Boolean) {
  import SHA512Context._

  val total  = new Array[Long](2)   // number of bytes processed
  val state  = new Array[Long](8)   // intermediate digest state
  val buffer = new Array[Byte](128) // data block being processed

  init()

  def init(): Unit = {
    total(0) = 0
    total(1) = 0

    if (!is384) {
      // SHA-512
      state(0) = 0x6A09E667F3BCC908L
      state(1) = 0xBB67AE8584CAA73BL
      state(2) = 0x3C6EF372FE94F82BL
      state(3) = 0xA54FF53A5F1D36F1L
      state(4) = 0x510E527FADE682D1L
      state(5) = 0x9B05688C2B3E6C1FL
      state(6) = 0x1F83D9ABFB41BD6BL
      state(7) = 0x5BE0CD19137E2179L
    } else {
      // SHA-384
      state(0) = 0xCBBB9D5DC1059ED8L
      state(1) = 0x629A292A367CD507L
      state(2) = 0x9159015A3070DD17L
      state(3) = 0x152FECD8F70E5939L
      state(4) = 0x67332667FFC00B31L
      state(5) = 0x8EB44A8768581511L
      state(6) = 0xDB0C2E0D64F98FA7L
      state(7) = 0x47B5481DBEFA4FA4L
    }
  }

  def process(data: Array[Byte], start: Int): Unit = {
    import java.lang.Long.{rotateRight => rotr}

    @inline def shr(x: Long, distance: Int): Long = x >>> distance

    @inline def S0(x: Long): Long = rotr(x, 1) ^ rotr(x, 8) ^ shr(x, 7)
    @inline def S1(x: Long): Long = rotr(x, 19) ^ rotr(x, 61) ^ shr(x, 6)

    @inline def S2(x: Long): Long = rotr(x, 28) ^ rotr(x, 34) ^ rotr(x, 39)
    @inline def S3(x: Long): Long = rotr(x, 14) ^ rotr(x, 18) ^ rotr(x, 41)

    @inline def F0(x: Long, y: Long, z: Long): Long = ((x & y) | (z & (x | y)))
    @inline def F1(x: Long, y: Long, z: Long): Long = (z ^ (x & (y ^ z)))

    // Returns (newD, newH) - the C implementation updates them in-place
    @inline
    def P(a: Long,
          b: Long,
          c: Long,
          d: Long,
          e: Long,
          f: Long,
          g: Long,
          h: Long,
          x: Long,
          K: Long): (Long, Long) = {
      val temp1 = h + S3(e) + F1(e, f, g) + K + x
      val temp2 = S2(a) + F0(a, b, c)
      (d + temp1, temp1 + temp2)
    }

    val W = new Array[Long](80)

    for (i <- 0 until 16)
      W(i) = getUInt64BE(data, start + (i << 3))

    for (i <- 16 until 80)
      W(i) = S1(W(i - 2)) + W(i - 7) + S0(W(i - 15)) + W(i - 16)

    var A = state(0)
    var B = state(1)
    var C = state(2)
    var D = state(3)
    var E = state(4)
    var F = state(5)
    var G = state(6)
    var H = state(7)

    var i = 0
    do {
      {
        // scoping the newABC variables
        val (newD, newH) = P(A, B, C, D, E, F, G, H, W(i), K(i)); D = newD;
        H = newH; i += 1
        val (newC, newG) = P(H, A, B, C, D, E, F, G, W(i), K(i)); C = newC;
        G = newG; i += 1
        val (newB, newF) = P(G, H, A, B, C, D, E, F, W(i), K(i)); B = newB;
        F = newF; i += 1
        val (newA, newE) = P(F, G, H, A, B, C, D, E, W(i), K(i)); A = newA;
        E = newE; i += 1
      }

      {
        // scoping the newABC variables
        val (newH, newD) = P(E, F, G, H, A, B, C, D, W(i), K(i)); H = newH;
        D = newD; i += 1
        val (newG, newC) = P(D, E, F, G, H, A, B, C, W(i), K(i)); G = newG;
        C = newC; i += 1
        val (newF, newB) = P(C, D, E, F, G, H, A, B, W(i), K(i)); F = newF;
        B = newB; i += 1
        val (newE, newA) = P(B, C, D, E, F, G, H, A, W(i), K(i)); E = newE;
        A = newA; i += 1
      }
    } while (i < 80)

    state(0) += A
    state(1) += B
    state(2) += C
    state(3) += D
    state(4) += E
    state(5) += F
    state(6) += G
    state(7) += H
  }

  /*
   * SHA-512 process buffer
   */
  def update(input: Array[Byte], ilen0: Int): Unit = {
    if (ilen0 == 0)
      return

    var ilen = ilen0
    var left = total(0).toInt & 0x7f
    val fill = 128 - left

    total(0) += ilen.toLong
    if (total(0) < (ilen.toLong))
      total(1) += 1L

    var inputIndex = 0

    if (left != 0 && ilen >= fill) {
      System.arraycopy(input, inputIndex, buffer, left, fill)
      process(buffer, 0)
      inputIndex += fill
      ilen -= fill
      left = 0
    }

    while (ilen >= 128) {
      process(input, inputIndex)
      inputIndex += 128
      ilen -= 128
    }

    if (ilen > 0)
      System.arraycopy(input, inputIndex, buffer, left, ilen)
  }

  /*
   * SHA-512 final digest
   */
  def finish(output: Array[Byte]): Unit = {
    val high = (total(0) >>> 61) | (total(1) << 3)
    val low  = total(0) << 3

    val msglen = new Array[Byte](16)
    putUInt64BE(msglen, 0, high)
    putUInt64BE(msglen, 8, low)

    val last = total(0).toInt & 0x7f
    val padn = if (last < 112) 112 - last else 240 - last

    update(padding, padn)
    update(msglen, 16)

    putUInt64BE(output, 0, state(0))
    putUInt64BE(output, 8, state(1))
    putUInt64BE(output, 16, state(2))
    putUInt64BE(output, 24, state(3))
    putUInt64BE(output, 32, state(4))
    putUInt64BE(output, 40, state(5))

    if (!is384) {
      putUInt64BE(output, 48, state(6))
      putUInt64BE(output, 56, state(7))
    }
  }

}
