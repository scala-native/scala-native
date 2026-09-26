/* Ported to Scala Native from commons-rng:
 *   https://github.com/apache/commons-rng/blob/master/commons-rng-sampling/
 *     src/main/java/org/apache/commons/rng/sampling/distribution/
 *     ZigguratSampler.java
 *
 * Commit: 68a2208 Dated: 2026-04-14
 */

/* SN Development Notes
 *   0. Due to trying to closely, but not exactly, follow the common-rng
 *      original implementation, this file is better viewed at greater
 *      than 80 columns.
 *
 *   1. To allow later matching up with changes in commons-rng, the
 *      coding style, including block vs line comments, tends to follow
 *      commons-rng original rather than general javalib norm.
 *
 *   2. Converted Javadoc "/** */" comments to C style "/* */" comments
 *      to avoid CI doc build failures.
 *
 *      The commons-rng comments are essential to understanding the
 *      code.  In many places they contain inline markup which
 *      can break javalib doc Javadoc generation. The fix is to
 *      use C style comments and let the reader sort it out.
 *
 *   3. Original comments are mostly as-is to aid matching
 *      against Apache .java original. In some cases they
 *      make no sense for Scala Native. Some but probably not all
 *      such surds are noted. Treat commons-rng comments advisedly, as you
 *      would treat LLM answers.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.random

import java.{lang => jl}

/*
 * Modified ziggurat method for sampling from Gaussian and exponential distributions.
 *
 * <p>Uses the algorithm from:
 *
 * <blockquote>
 * McFarland, C.D. (2016)<br>
 * "A modified ziggurat algorithm for generating exponentially and normally distributed pseudorandom numbers".<br>
 * <i>Journal of Statistical Computation and Simulation</i> <b>86</b>, 1281-1294.
 * </blockquote>
 *
 * <p>Note: The algorithm is a modification of the
 * {@link ZigguratNormalizedGaussianSampler Marsaglia and Tsang "Ziggurat" method}.
 * The modification improves performance by:
 * <ol>
 * <li>Creating layers of the ziggurat entirely inside the probability density function (area B);
 * this allows the majority of samples to be obtained without checking if the value is in the
 * region of the ziggurat layer that requires a rejection test.</li>
 * <li>For samples not within the main ziggurat (area A) alias sampling is used to choose a
 * layer and rejection of points above the PDF is accelerated using precomputation of
 * triangle regions entirely below or above the curve.</li>
 * </ol>
 *
 * <pre>
 *           \
 * ----------+\
 *           | \
 *    B      |A \
 * -------------+\
 *              | \
 * </pre>
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextLong()}.
 *
 * @see <a href="https://www.tandfonline.com/doi/abs/10.1080/00949655.2015.1060234">
 * McFarland (2016) JSCS 86, 1281-1294</a>
 * @since 1.4
 */
private[random] object ZigguratSampler {
  /* Mask to extract the lowest 8-bits from an integer. */
  final val MASK_INT8 = 0xff

  /* Mask to create an unsigned long from a signed long.
   * This is the maximum value of a 64-bit long.
   */
  final val MAX_INT64 = jl.Long.MAX_VALUE

  /* SN:
   * McFarland's original C code
   *  https://github.com/cd-mcfarland/fast_prang/blob/master/shared.h
   * uses "pow(2, 63)". Use that variant. "pow()" probably has fast
   * special cases for exact powers of 2.  Spend a few runtime CPU cycles
   * in order to get a critical constant right. Have to leave room
   * for improvement for the next generation of Devi's to surpass the current.
   *
   * Leave as double to avoid redundant int to double conversions at use
   * sites. A CPU cycle saved is a CPU cycle earned.
   */

  /* 2^63. */
  final val TWO_POW_63 = jl.Math.pow(2.0, 63.0)

  /*
   * Compute the value of a point using linear interpolation of a data table of values
   * using the provided uniform deviate.
   * <pre>
   *  value = v[j] + u * (v[j-1] - v[j])
   * </pre>
   *
   * <p>This can be used to generate the (x,y) coordinates of a point in a rectangle
   * with the upper-left corner at {@code j} and lower-right corner at {@code j-1}:
   *
   * <pre>{@code
   *    X[j],Y[j]
   *        |\ |
   *        | \|
   *        |  \
   *        |  |\    Ziggurat overhang j (with hypotenuse not pdf(x))
   *        |  | \
   *        |  u2 \
   *        |      \
   *        |-->u1  \
   *        +-------- X[j-1],Y[j-1]
   *
   *   x = X[j] + u1 * (X[j-1] - X[j])
   *   y = Y[j] + u2 * (Y[j-1] - Y[j])
   * }</pre>
   *
   * @param v Ziggurat data table. Values assumed to be scaled by 2^-63.
   * @param j Index j. Value assumed to be above zero.
   * @param u Uniform deviate. Value assumed to be in {@code [0, 2^63)}.
   * @return value
   */
  @inline
  def interpolate(
      v: Array[scala.Double],
      j: scala.Int,
      u: scala.Long
  ): scala.Double = {
    // Note:
    // The reference code used two methods to interpolate X and Y separately.
    // The c language exploited declared pointers to X and Y and used a #define construct.
    // This computed X identically to this method but Y as:
    // y = Y[j-1] + (1-u2) * (Y[j] - Y[j-1])
    // Using a single method here clarifies the code. It avoids generating (1-u).
    // Tests show the alternative is 1 ULP different with approximately 3% frequency.
    // It has not been measured more than 1 ULP different.

    v(j) * TWO_POW_63 + u * (v(j - 1) - v(j))
  }
}

private[random] class ZigguratSampler(rng: RandomGenerator) {
  /* rng: Underlying source of randomness.
   * Scala Native RandomGenerator nextExponential() and nextGaussian()
   * always pass in an rng for the Uniform distribution.
   */

  /*
   * Generate a string to represent the sampler.
   *
   * @param type Sampler type (e.g. "exponential").
   * @return the string
   */
  def toString(`type`: String) =
    s"Modified ziggurat ${`type`} deviate [${rng.toString()}]"

  /*
   * Generates a positive {@code long} in {@code [0, 2^63)}.
   *
   * <p>In the c reference implementation RANDOM_INT63() obtains the current random value
   * and then advances the RNG. This implementation obtains a new value from the RNG.
   * Thus the java implementation must ensure a previous call to the RNG is cached
   * if RANDOM_INT63() is called without first advancing the RNG.
   *
   * @return the long
   */

  @inline
  def randomInt63(): scala.Long =
    rng.nextLong() >>> 1
}

// =========================================================================
// Implementation note:
//
// This has been adapted from the reference c implementation provided
// by C.D. McFarland:
//
// https://github.com/cd-mcfarland/fast_prng
//
// The adaption was based on the reference as of July-2021.
// The code uses similar naming conventions from the exponential.h and normal.h
// reference. Naming has been updated to be consistent in the exponential and normal
// samplers. Comments from the c source have been included.
// Branch frequencies have been measured and added as comments.
//
// Notable changes based on performance tests across JDKs and platforms:
// The generation of unsigned longs has been changed to use bit shifts to favour
// the significant bits of the long. The interpolation of X and Y uses a single method.
// Recursion in the exponential sampler has been avoided.
//
// Note: The c implementation uses a RNG where the current value can be obtained
// without advancing the generator. The entry point to the sample generation
// always has this value as a previously unused value. The RNG is advanced when new
// bits are required. This Java implementation will generate new values with calls
// to the RNG and cache the value if it is to be recycled.
//
// The script used to generate the tables has been modified to scale values by 2^63
// or 2^64 instead of 2^63 - 1 and 2^64 - 1. This allows a random 64-bit long to
// represent a uniform value in [0, 1) as the numerator of a fraction with a value of
// [0, 2^63) / 2^63 or [0, 2^64) / 2^64 respectively (the denominator is assumed).
// Scaling of the high precision float values in the script is exact before
// conversion to integers.
//
// Entries in the probability alias table are always compared to a long with the same
// lower 8-bits since these bits identify the index in the table.
// The entries in the IPMF tables have had the lower 8-bits set to zero. If these bits
// are >= 128 then 256 is added to the alias table to round the number. The alias table
// thus represents the numerator of a fraction with an unsigned magnitude of [0, 2^56 - 1)
// and denominator 2^56. The numerator is effectively left-shifted 8 bits and 2^63 is
// subtracted to store the value using a signed 64-bit long.
//
// Computation of these tables is dependent on the platform used to run the python script.
// The X and Y tables are identical to 1 ULP. The MAP is identical. The IPMF table is computed
// using rebalancing of the overhang probabilities to create the alias map. The table has
// been observed to exhibit differences in the last 7 bits of the 56 bits used (ignoring the
// final 8 bits) for the exponential and 11 bits for the normal. This corresponds to a
// probability of 2^-49 (1.78e-15), or 2^-45 (2.84e-14) respectively. The tables may be
// regenerated in future versions if the reference script receives updates to improve
// accuracy.
//
// Method Description
//
// The ziggurat is constructed using layers that fit exactly within the probability density
// function. Each layer has the same area. This area is chosen to be a fraction of the total
// area under the PDF with the denominator of the fraction a power of 2. These tables
// use 1/256 as the volume of each layer. The remaining part of the PDF that is not represented
// by the layers is the overhang. There is an overhang above each layer and a final tail.
// The following is a ziggurat with 3 layers:
//
//     Y3 |\
//        | \  j=3
//        |  \
//     Y2 |   \
//        |----\
//        |    |\
//        | i=2| \ j=2
//        |    |  \
//     Y1 |--------\
//        | i=1   | \ j=1
//        |       |  \
//     Y0 |-----------\
//        | i=0      | \ j=0 (tail)
//        +--------------
//        X3  |   |  X0
//            |   X1
//            X2
//
// There are N layers referenced using i in [0, N). The overhangs are referenced using
// j in [1, N]; j=0 is the tail. Note that N is < 256.
// Information about the ziggurat is pre-computed:
// X = The length of each layer (supplemented with zero for Xn)
// Y = PDF(X) for each layer (supplemented with PDF(x=0) for Yn)
//
// Sampling is performed as:
// - Pick index i in [0, 256).
// - If i is a layer then return a uniform deviate multiplied by the layer length
// - If i is not a layer then sample from the overhang or tail
//
// The overhangs and tail have different volumes. Sampling must pick a region j based the
// probability p(j) = vol(j) / sum (vol(j)). This is performed using alias sampling.
// (See Walker, AJ (1977) "An Efficient Method for Generating Discrete Random Variables with
// General Distributions" ACM Transactions on Mathematical Software 3 (3), 253-256.)
// This uses a table that has been constructed to evenly balance A categories with
// probabilities around the mean into B sections each allocated the 'mean'. For the 4
// regions in the ziggurat shown above balanced into 8 sections:
//
// 3
// 3
// 32
// 32
// 321
// 321   => 31133322
// 3210     01233322
//
// section  abcdefgh
//
// A section with an index below the number of categories represents the category j and
// optionally an alias. Sections with an index above the number
// of categories are entirely filled with the alias. The region is chosen
// by selecting a section and then checking if a uniform deviate is above the alias
// threshold. If so then the alias is used in place of the original index.
//
// Alias sampling uses a table size of 256. This allows fast computation of the index
// as a power of 2. The probability threshold is stored as the numerator of a fraction
// allowing direct comparison with a uniform long deviate.
//
// MAP = Alias map for j in [0, 256)
// IPMF = Alias probability threshold for j
//
// Note: The IPMF table is larger than the number of regions. Thus the final entries
// must represent a probability of zero so that the alias is always used.
//
// If the selected region j is the tail then sampling uses a sampling method appropriate
// for the PDF. If the selected region is an overhang then sampling generates a random
// coordinate inside the rectangle covering the overhang using random deviates u1 and u2:
//
//    X[j],Y[j]
//        |\-->u1
//        | \  |
//        |  \ |
//        |   \|    Overhang j (with hypotenuse not pdf(x))
//        |    \
//        |    |\
//        |    | \
//        |    u2 \
//        +-------- X[j-1],Y[j-1]
//
// The random point (x,y) has coordinates:
// x = X[j] + u1 * (X[j-1] - X[j])
// y = Y[j] + u2 * (Y[j-1] - Y[j])
//
// The expressions can be evaluated from the opposite direction using (1-u), e.g:
// y = Y[j-1] + (1-u2) * (Y[j] - Y[j-1])
// This allows the large value to subtract the small value before multiplying by u.
// This method is used in the reference c code. It uses an addition subtraction to create 1-u.
// Note that the tables X and Y have been scaled by 2^-63. This allows U to be a uniform
// long in [0, 2^63). Thus the value c in 'c + m * x' must be scaled up by 2^63.
//
// If point (x,y) is below pdf(x) then the sample is accepted.
// If u2 > u1 then the point is below the hypotenuse.
// If u1 > u2 then the point is above the hypotenuse.
// The distance above/below the hypotenuse is the difference u2 - u1: negative is above;
// positive is below.
//
// The pdf(x) may lie completely above or below the hypotenuse. If the region under the pdf
// is the inside then the curve is referred to as either convex (above) or concave (below).
// The exponential function is concave for all regions. The normal function is convex below
// x=1, and concave above x=1. x=1 is the point of inflection.
//
//        Concave                   Convex
//        |-                        |----
//        | -                       |    ---
//        |  -                      |       --
//        |   --                    |         --
//        |     --                  |           -
//        |       ---               |            -
//        |          ----           |             -
//
// Optimisations:
//
// Regions that are concave can detect a point (x,y) above the hypotenuse and reflect the
// point in the hypotenuse by swapping u1 and u2.
//
// Regions that are convex can detect a point (x,y) below the hypotenuse and immediately accept
// the sample.
//
// The maximum distance of pdf(x) from the hypotenuse can be precomputed. This can be done for
// each region or by taking the largest distance across all regions. This value can be
// compared to the distance between u1 and u2 and the point immediately accepted (concave)
// or rejected (convex) as it is known to be respectively inside or outside the pdf.
// This sampler uses a single value for the maximum distance of pdf(x) from the hypotenuse.
// For the normal distribution this is two values to separate the maximum for convex and
// concave regions.
// =========================================================================
/*
 * Modified ziggurat method for sampling from an exponential distribution.
 */
private[random] object ExponentialZiggurat {
  // Ziggurat volumes:
  // Inside the layers              = 98.4375%  (252/256)
  // Fraction outside the layers:
  // concave overhangs              = 96.6972%
  // tail                           =  3.3028%  (x > 7.56...)

  /* The number of layers in the ziggurat. Maximum i value for early exit. */

  final val I_MAX = 252

  /* Maximum deviation of concave pdf(x) below the hypotenuse value for early exit.
   * Equal to approximately 0.0926 scaled by 2^63. */
  final val E_MAX = 853965788476313647L

  /* Beginning of tail. Equal to X[0] * 2^63. */
  final val X_0 = 7.569274694148063

// format: off

  /* The alias map. An integer in [0, 255] stored as a byte to save space.
   * Contains the alias j for each index. j=0 is the tail; j in [1, N] is the
   * overhang for each layer.
   */

  final val MAP = Array[scala.Byte](
      /* [  0] */    0.toByte,   0.toByte,   1.toByte, 235.toByte,   3.toByte,   4.toByte,   5.toByte,   0.toByte,
      /* [  8] */    0.toByte,   0.toByte,   0.toByte,   0.toByte,   0.toByte,   0.toByte,   0.toByte,   0.toByte,
      /* [ 16] */    0.toByte,   0.toByte,   1.toByte,   1.toByte,   1.toByte,   1.toByte,   2.toByte,   2.toByte,
      /* [ 24] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [ 32] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [ 40] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [ 48] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [ 56] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [ 64] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [ 72] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [ 80] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [ 88] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [ 96] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [104] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [112] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [120] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [128] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [136] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [144] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [152] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [160] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [168] */  252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
      /* [176] */  252.toByte, 251.toByte, 251.toByte, 251.toByte, 251.toByte, 251.toByte, 251.toByte, 251.toByte,
      /* [184] */  251.toByte, 251.toByte, 251.toByte, 251.toByte, 251.toByte, 251.toByte, 250.toByte, 250.toByte,
      /* [192] */  250.toByte, 250.toByte, 250.toByte, 250.toByte, 250.toByte, 249.toByte, 249.toByte, 249.toByte,
      /* [200] */  249.toByte, 249.toByte, 249.toByte, 248.toByte, 248.toByte, 248.toByte, 248.toByte, 247.toByte,
      /* [208] */  247.toByte, 247.toByte, 247.toByte, 246.toByte, 246.toByte, 246.toByte, 245.toByte, 245.toByte,
      /* [216] */  244.toByte, 244.toByte, 243.toByte, 243.toByte, 242.toByte, 241.toByte, 241.toByte, 240.toByte,
      /* [224] */  239.toByte, 237.toByte,   3.toByte,   3.toByte,   4.toByte,   4.toByte,   6.toByte,   0.toByte,
      /* [232] */    0.toByte,   0.toByte,   0.toByte, 236.toByte, 237.toByte, 238.toByte, 239.toByte, 240.toByte,
      /* [240] */  241.toByte, 242.toByte, 243.toByte, 244.toByte, 245.toByte, 246.toByte, 247.toByte, 248.toByte,
      /* [248] */  249.toByte, 250.toByte, 251.toByte, 252.toByte, 2.toByte, 0.toByte, 0.toByte, 0.toByte
    )


  /* The alias inverse PMF. This is the probability threshold to use the alias for j in-place of j.
   * This has been scaled by 2^64 and offset by -2^63. It represents the numerator of a fraction
   * with denominator 2^64 and can be compared directly to a uniform long deviate.
   * The value probability 0.0 is Long.MIN_VALUE and is used when {@code j > I_MAX}.
   */
  final val IPMF = Array[scala.Long](
      /* [  0] */  9223372036854774016L,  1623796909450834944L,  2664290944894291200L,  7387971354164060928L,
      /* [  4] */  6515064486552723200L,  8840508362680718848L,  6099647593382936320L,  7673130333659513856L,
      /* [  8] */  6220332867583438080L,  5045979640552813824L,  4075305837223955456L,  3258413672162525440L,
      /* [ 12] */  2560664887087762432L,  1957224924672899584L,  1429800935350577408L,   964606309710808320L,
      /* [ 16] */   551043923599587072L,   180827629096890368L,  -152619738120023552L,  -454588624410291456L,
      /* [ 20] */  -729385126147774976L,  -980551509819447040L, -1211029700667463936L, -1423284293868548352L,
      /* [ 24] */ -1619396356369050368L, -1801135830956211712L, -1970018048575618048L, -2127348289059705344L,
      /* [ 28] */ -2274257249303686400L, -2411729520096655360L, -2540626634159181056L, -2661705860113406464L,
      /* [ 32] */ -2775635634532450560L, -2883008316030465280L, -2984350790383654912L, -3080133339198116352L,
      /* [ 36] */ -3170777096303091200L, -3256660348483819008L, -3338123885075136256L, -3415475560473299200L,
      /* [ 40] */ -3488994201966428160L, -3558932970354473216L, -3625522261068041216L, -3688972217741989376L,
      /* [ 44] */ -3749474917563782656L, -3807206277531056128L, -3862327722496843520L, -3914987649156779776L,
      /* [ 48] */ -3965322714631865344L, -4013458973776895488L, -4059512885612783360L, -4103592206186241024L,
      /* [ 52] */ -4145796782586128128L, -4186219260694347008L, -4224945717447275264L, -4262056226866285568L,
      /* [ 56] */ -4297625367836519680L, -4331722680528537344L, -4364413077437472512L, -4395757214229401600L,
      /* [ 60] */ -4425811824915135744L, -4454630025296932608L, -4482261588141290496L, -4508753193105288192L,
      /* [ 64] */ -4534148654077808896L, -4558489126279958272L, -4581813295192216576L, -4604157549138257664L,
      /* [ 68] */ -4625556137145255168L, -4646041313519104512L, -4665643470413305856L, -4684391259530326528L,
      /* [ 72] */ -4702311703971761664L, -4719430301145103360L, -4735771117539946240L, -4751356876102087168L,
      /* [ 76] */ -4766209036859133952L, -4780347871386013440L, -4793792531638892032L, -4806561113635132672L,
      /* [ 80] */ -4818670716409306624L, -4830137496634465536L, -4840976719260837888L, -4851202804490348800L,
      /* [ 84] */ -4860829371376460032L, -4869869278311657472L, -4878334660640771072L, -4886236965617427200L,
      /* [ 88] */ -4893586984900802560L, -4900394884772702720L, -4906670234238885376L, -4912422031164496896L,
      /* [ 92] */ -4917658726580119808L, -4922388247283532288L, -4926618016851066624L, -4930354975163335168L,
      /* [ 96] */ -4933605596540651264L, -4936375906575303936L, -4938671497741366016L, -4940497543854575616L,
      /* [100] */ -4941858813449629440L, -4942759682136114944L, -4943204143989086720L, -4943195822025528064L,
      /* [104] */ -4942737977813206528L, -4941833520255033344L, -4940485013586738944L, -4938694684624359424L,
      /* [108] */ -4936464429291795968L, -4933795818458825728L, -4930690103114057984L, -4927148218896864000L,
      /* [112] */ -4923170790008275968L, -4918758132519213568L, -4913910257091645696L, -4908626871126539264L,
      /* [116] */ -4902907380349533952L, -4896750889844272896L, -4890156204540531200L, -4883121829162554368L,
      /* [120] */ -4875645967641781248L, -4867726521994927104L, -4859361090668103424L, -4850546966345113600L,
      /* [124] */ -4841281133215539200L, -4831560263698491904L, -4821380714613447424L, -4810738522790066176L,
      /* [128] */ -4799629400105481984L, -4788048727936307200L, -4775991551010514944L, -4763452570642114304L,
      /* [132] */ -4750426137329494528L, -4736906242696389120L, -4722886510751377664L, -4708360188440089088L,
      /* [136] */ -4693320135461421056L, -4677758813316108032L, -4661668273553489152L, -4645040145179241472L,
      /* [140] */ -4627865621182772224L, -4610135444140930048L, -4591839890849345536L, -4572968755929961472L,
      /* [144] */ -4553511334358205696L, -4533456402849101568L, -4512792200036279040L, -4491506405372580864L,
      /* [148] */ -4469586116675402496L, -4447017826233107968L, -4423787395382284800L, -4399880027458416384L,
      /* [152] */ -4375280239014115072L, -4349971829190472192L, -4323937847117721856L, -4297160557210933504L,
      /* [156] */ -4269621402214949888L, -4241300963840749312L, -4212178920821861632L, -4182234004204451584L,
      /* [160] */ -4151443949668877312L, -4119785446662287616L, -4087234084103201536L, -4053764292396156928L,
      /* [164] */ -4019349281473081856L, -3983960974549692672L, -3947569937258423296L, -3910145301787345664L,
      /* [168] */ -3871654685619032064L, -3832064104425388800L, -3791337878631544832L, -3749438533114327552L,
      /* [172] */ -3706326689447984384L, -3661960950051848192L, -3616297773528534784L, -3569291340409189376L,
      /* [176] */ -3520893408440946176L, -3471053156460654336L, -3419717015797782528L, -3366828488034805504L,
      /* [180] */ -3312327947826460416L, -3256152429334010368L, -3198235394669719040L, -3138506482563172864L,
      /* [184] */ -3076891235255162880L, -3013310801389730816L, -2947681612411374848L, -2879915029671670784L,
      /* [188] */ -2809916959107513856L, -2737587429961866240L, -2662820133571325696L, -2585501917733380096L,
      /* [192] */ -2505512231579385344L, -2422722515205211648L, -2336995527534088448L, -2248184604988727552L,
      /* [196] */ -2156132842510765056L, -2060672187261025536L, -1961622433929371904L, -1858790108950105600L,
      /* [200] */ -1751967229002895616L, -1640929916937142784L, -1525436855617582592L, -1405227557075253248L,
      /* [204] */ -1280020420662650112L, -1149510549536596224L, -1013367289578704896L,  -871231448632104192L,
      /* [208] */  -722712146453667840L,  -567383236774436096L,  -404779231966938368L,  -234390647591545856L,
      /* [212] */   -55658667960119296L,   132030985907841280L,   329355128892811776L,   537061298001085184L,
      /* [216] */   755977262693564160L,   987022116608033280L,  1231219266829431296L,  1489711711346518528L,
      /* [220] */  1763780090187553792L,  2054864117341795072L,  2364588157623768832L,  2694791916990503168L,
      /* [224] */  3047567482883476224L,  3425304305830816256L,  3830744187097297920L,  4267048975685830400L,
      /* [228] */  4737884547990017280L,  5247525842198998272L,  5800989391535355392L,  6404202162993295360L,
      /* [232] */  7064218894258540544L,  7789505049452331520L,  8590309807749444864L,  7643763810684489984L,
      /* [236] */  8891950541491446016L,  5457384281016206080L,  9083704440929284096L,  7976211653914433280L,
      /* [240] */  8178631350487117568L,  2821287825726744832L,  6322989683301709568L,  4309503753387611392L,
      /* [244] */  4685170734960170496L,  8404845967535199744L,  7330522972447554048L,  1960945799076992000L,
      /* [248] */  4742910674644899072L,  -751799822533509888L,  7023456603741959936L,  3843116882594676224L,
      /* [252] */  3927231442413903104L, -9223372036854775808L, -9223372036854775808L, -9223372036854775808L
    )

  /*
   * The precomputed ziggurat lengths, denoted X_i in the main text.
   * <ul>
   * <li>X_i = length of ziggurat layer i.</li>
   * <li>X_j is the upper-left X coordinate of overhang j (starting from 1).</li>
   * <li>X_(j-1) is the lower-right X coordinate of overhang j.</li>
   * </ul>
   * <p>Values have been scaled by 2^-63.
   * Contains {@code I_MAX + 1} entries as the final value is 0.
   */
  final val X = Array[scala.Double](
            /* [  0] */ 8.2066240675348816e-19, 7.3973732351607284e-19, 6.9133313377915293e-19, 6.5647358820964533e-19,
            /* [  4] */ 6.2912539959818508e-19, 6.0657224129604964e-19, 5.8735276103737269e-19, 5.7058850528536941e-19,
            /* [  8] */  5.557094569162239e-19, 5.4232438903743953e-19, 5.3015297696508776e-19, 5.1898739257708062e-19,
            /* [ 12] */  5.086692261799833e-19, 4.9907492938796469e-19, 4.9010625894449536e-19, 4.8168379010649187e-19,
            /* [ 16] */ 4.7374238653644714e-19, 4.6622795807196824e-19, 4.5909509017784048e-19, 4.5230527790658154e-19,
            /* [ 20] */  4.458255881635396e-19, 4.3962763126368381e-19,  4.336867596710647e-19, 4.2798143618469714e-19,
            /* [ 24] */ 4.2249273027064889e-19,  4.172039125346411e-19, 4.1210012522465616e-19, 4.0716811225869233e-19,
            /* [ 28] */ 4.0239599631006903e-19, 3.9777309342877357e-19, 3.9328975785334499e-19, 3.8893725129310323e-19,
            /* [ 32] */ 3.8470763218720385e-19, 3.8059366138180143e-19,  3.765887213854473e-19, 3.7268674692030177e-19,
            /* [ 36] */ 3.6888216492248162e-19, 3.6516984248800068e-19, 3.6154504153287473e-19, 3.5800337915318032e-19,
            /* [ 40] */ 3.5454079284533432e-19, 3.5115350988784242e-19, 3.4783802030030962e-19, 3.4459105288907336e-19,
            /* [ 44] */ 3.4140955396563316e-19, 3.3829066838741162e-19, 3.3523172262289001e-19, 3.3223020958685874e-19,
            /* [ 48] */ 3.2928377502804472e-19, 3.2639020528202049e-19, 3.2354741622810815e-19, 3.2075344331080789e-19,
            /* [ 52] */ 3.1800643250478609e-19, 3.1530463211820845e-19, 3.1264638534265134e-19, 3.1003012346934211e-19,
            /* [ 56] */ 3.0745435970137301e-19, 3.0491768350005559e-19, 3.0241875541094565e-19,  2.999563023214455e-19,
            /* [ 60] */ 2.9752911310742592e-19, 2.9513603463113224e-19, 2.9277596805684267e-19, 2.9044786545442563e-19,
            /* [ 64] */ 2.8815072666416712e-19, 2.8588359639906928e-19, 2.8364556156331615e-19, 2.8143574876779799e-19,
            /* [ 68] */ 2.7925332202553125e-19, 2.7709748061152879e-19, 2.7496745707320232e-19, 2.7286251537873397e-19,
            /* [ 72] */ 2.7078194919206054e-19,  2.687250802641905e-19, 2.6669125693153442e-19, 2.6467985271278891e-19,
            /* [ 76] */ 2.6269026499668434e-19, 2.6072191381359757e-19, 2.5877424068465143e-19, 2.5684670754248168e-19,
            /* [ 80] */ 2.5493879571835479e-19, 2.5305000499077481e-19,  2.511798526911271e-19, 2.4932787286227806e-19,
            /* [ 84] */  2.474936154663866e-19, 2.4567664563848669e-19, 2.4387654298267842e-19, 2.4209290090801527e-19,
            /* [ 88] */ 2.4032532600140538e-19, 2.3857343743505147e-19, 2.3683686640614648e-19, 2.3511525560671253e-19,
            /* [ 92] */ 2.3340825872163284e-19, 2.3171553995306794e-19, 2.3003677356958333e-19, 2.2837164347843482e-19,
            /* [ 96] */ 2.2671984281957174e-19, 2.2508107358001938e-19, 2.2345504622739592e-19, 2.2184147936140775e-19,
            /* [100] */ 2.2024009938224424e-19, 2.1865064017486842e-19, 2.1707284280826716e-19, 2.1550645524878675e-19,
            /* [104] */ 2.1395123208673778e-19,  2.124069342755064e-19, 2.1087332888245875e-19, 2.0935018885097035e-19,
            /* [108] */ 2.0783729277295508e-19, 2.0633442467130712e-19, 2.0484137379170616e-19, 2.0335793440326865e-19,
            /* [112] */  2.018839056075609e-19, 2.0041909115551697e-19, 1.9896329927183254e-19,  1.975163424864309e-19,
            /* [116] */ 1.9607803747261946e-19, 1.9464820489157862e-19, 1.9322666924284314e-19, 1.9181325872045647e-19,
            /* [120] */ 1.9040780507449479e-19, 1.8901014347767504e-19, 1.8762011239677479e-19, 1.8623755346860768e-19,
            /* [124] */ 1.8486231138030984e-19, 1.8349423375370566e-19, 1.8213317103353295e-19, 1.8077897637931708e-19,
            /* [128] */ 1.7943150556069476e-19, 1.7809061685599652e-19, 1.7675617095390567e-19, 1.7542803085801941e-19,
            /* [132] */ 1.7410606179414531e-19,  1.727901311201724e-19, 1.7148010823836362e-19, 1.7017586450992059e-19,
            /* [136] */ 1.6887727317167824e-19, 1.6758420925479093e-19, 1.6629654950527621e-19, 1.6501417230628659e-19,
            /* [140] */ 1.6373695760198277e-19,  1.624647868228856e-19, 1.6119754281258616e-19, 1.5993510975569615e-19,
            /* [144] */ 1.5867737310692309e-19, 1.5742421952115544e-19, 1.5617553678444595e-19, 1.5493121374578016e-19,
            /* [148] */ 1.5369114024951992e-19, 1.5245520706841019e-19, 1.5122330583703858e-19, 1.4999532898563561e-19,
            /* [152] */ 1.4877116967410352e-19, 1.4755072172615974e-19, 1.4633387956347966e-19, 1.4512053813972103e-19,
            /* [156] */ 1.4391059287430991e-19, 1.4270393958586506e-19, 1.4150047442513381e-19, 1.4030009380730888e-19,
            /* [160] */ 1.3910269434359025e-19, 1.3790817277185197e-19, 1.3671642588626657e-19, 1.3552735046573446e-19,
            /* [164] */ 1.3434084320095729e-19, 1.3315680061998685e-19, 1.3197511901207148e-19, 1.3079569434961214e-19,
            /* [168] */ 1.2961842220802957e-19, 1.2844319768333099e-19, 1.2726991530715219e-19, 1.2609846895903523e-19,
            /* [172] */ 1.2492875177568625e-19,  1.237606560569394e-19, 1.2259407316813331e-19, 1.2142889343858445e-19,
            /* [176] */ 1.2026500605581765e-19, 1.1910229895518744e-19, 1.1794065870449425e-19, 1.1677997038316715e-19,
            /* [180] */ 1.1562011745554883e-19, 1.1446098163777869e-19, 1.1330244275772562e-19, 1.1214437860737343e-19,
            /* [184] */  1.109866647870073e-19, 1.0982917454048923e-19, 1.0867177858084351e-19, 1.0751434490529747e-19,
            /* [188] */ 1.0635673859884002e-19, 1.0519882162526621e-19, 1.0404045260457141e-19, 1.0288148657544097e-19,
            /* [192] */ 1.0172177474144965e-19, 1.0056116419943559e-19, 9.9399497648346677e-20, 9.8236613076667446e-20,
            /* [196] */ 9.7072343426320094e-20, 9.5906516230690634e-20, 9.4738953224154196e-20, 9.3569469920159036e-20,
            /* [200] */ 9.2397875154569468e-20, 9.1223970590556472e-20, 9.0047550180852874e-20, 8.8868399582647627e-20,
            /* [204] */  8.768629551976745e-20, 8.6501005086071005e-20, 8.5312284983141187e-20, 8.4119880684385214e-20,
            /* [208] */  8.292352551651342e-20, 8.1722939648034506e-20, 8.0517828972839211e-20, 7.9307883875099226e-20,
            /* [212] */ 7.8092777859524425e-20, 7.6872166028429042e-20, 7.5645683383965122e-20, 7.4412942930179128e-20,
            /* [216] */ 7.3173533545093332e-20, 7.1927017587631075e-20, 7.0672928197666785e-20, 6.9410766239500362e-20,
            /* [220] */ 6.8139996829256425e-20, 6.6860045374610234e-20, 6.5570293040210081e-20, 6.4270071533368528e-20,
            /* [224] */ 6.2958657080923559e-20, 6.1635263438143136e-20,   6.02990337321517e-20, 5.8949030892850181e-20,
            /* [228] */  5.758422635988593e-20, 5.6203486669597397e-20, 5.4805557413499315e-20, 5.3389043909003295e-20,
            /* [232] */ 5.1952387717989917e-20, 5.0493837866338355e-20, 4.9011415222629489e-20, 4.7502867933366117e-20,
            /* [236] */ 4.5965615001265455e-20, 4.4396673897997565e-20, 4.2792566302148588e-20, 4.1149193273430015e-20,
            /* [240] */ 3.9461666762606287e-20, 3.7724077131401685e-20,  3.592916408620436e-20, 3.4067836691100565e-20,
            /* [244] */ 3.2128447641564046e-20, 3.0095646916399994e-20, 2.7948469455598328e-20, 2.5656913048718645e-20,
            /* [248] */ 2.3175209756803909e-20, 2.0426695228251291e-20, 1.7261770330213488e-20, 1.3281889259442579e-20,
            /* [252] */                      0,
    )

  /*
   * The precomputed ziggurat heights, denoted Y_i in the main text.
   * <ul>
   * <li>Y_i = height of ziggurat layer i.</li>
   * <li>Y_j is the upper-left Y coordinate of overhang j (starting from 1).</li>
   * <li>Y_(j-1) is the lower-right Y coordinate of overhang j.</li>
   * </ul>
   * <p>Values have been scaled by 2^-63.
   * Contains {@code I_MAX + 1} entries as the final value is pdf(x=0).
   */
  final val Y = Array[scala.Double](
      /* [  0] */  5.595205495112736e-23, 1.1802509982703313e-22, 1.8444423386735829e-22, 2.5439030466698309e-22,
      /* [  4] */ 3.2737694311509334e-22, 4.0307732132706715e-22, 4.8125478319495115e-22, 5.6172914896583308e-22,
      /* [  8] */ 6.4435820540443526e-22, 7.2902662343463681e-22, 8.1563888456321941e-22, 9.0411453683482223e-22,
      /* [ 12] */ 9.9438488486399206e-22, 1.0863906045969114e-21, 1.1800799775461269e-21, 1.2754075534831208e-21,
      /* [ 16] */  1.372333117637729e-21, 1.4708208794375214e-21, 1.5708388257440445e-21, 1.6723581984374566e-21,
      /* [ 20] */ 1.7753530675030514e-21, 1.8797999785104595e-21, 1.9856776587832504e-21, 2.0929667704053244e-21,
      /* [ 24] */  2.201649700995824e-21, 2.3117103852306179e-21, 2.4231341516125464e-21, 2.5359075901420891e-21,
      /* [ 28] */ 2.6500184374170538e-21, 2.7654554763660391e-21, 2.8822084483468604e-21, 3.0002679757547711e-21,
      /* [ 32] */ 3.1196254936130377e-21, 3.2402731888801749e-21, 3.3622039464187092e-21, 3.4854113007409036e-21,
      /* [ 36] */ 3.6098893927859475e-21, 3.7356329310971768e-21, 3.8626371568620053e-21, 3.9908978123552837e-21,
      /* [ 40] */ 4.1204111123918948e-21, 4.2511737184488913e-21, 4.3831827151633737e-21, 4.5164355889510656e-21,
      /* [ 44] */ 4.6509302085234806e-21, 4.7866648071096003e-21, 4.9236379662119969e-21, 5.0618486007478993e-21,
      /* [ 48] */ 5.2012959454434732e-21, 5.3419795423648946e-21, 5.4838992294830959e-21, 5.6270551301806347e-21,
      /* [ 52] */ 5.7714476436191935e-21, 5.9170774358950678e-21, 6.0639454319177027e-21, 6.2120528079531677e-21,
      /* [ 56] */ 6.3614009847804375e-21, 6.5119916214136427e-21, 6.6638266093481696e-21, 6.8169080672926277e-21,
      /* [ 60] */ 6.9712383363524377e-21, 7.1268199756340822e-21, 7.2836557582420336e-21, 7.4417486676430174e-21,
      /* [ 64] */ 7.6011018943746355e-21, 7.7617188330775411e-21, 7.9236030798322572e-21, 8.0867584297834842e-21,
      /* [ 68] */ 8.2511888750363333e-21, 8.4168986028103258e-21, 8.5838919938383098e-21, 8.7521736209986459e-21,
      /* [ 72] */ 8.9217482481700712e-21, 9.0926208292996504e-21, 9.2647965076751277e-21, 9.4382806153938292e-21,
      /* [ 76] */ 9.6130786730210328e-21, 9.7891963894314161e-21,  9.966639661827884e-21, 1.0145414575932636e-20,
      /* [ 80] */ 1.0325527406345955e-20, 1.0506984617068672e-20, 1.0689792862184811e-20, 1.0873958986701341e-20,
      /* [ 84] */   1.10594900275424e-20, 1.1246393214695825e-20, 1.1434675972510121e-20, 1.1624345921140471e-20,
      /* [ 88] */ 1.1815410878142659e-20, 1.2007878860214202e-20, 1.2201758085082226e-20,  1.239705697353804e-20,
      /* [ 92] */ 1.2593784151618565e-20, 1.2791948452935152e-20,   1.29915589211506e-20, 1.3192624812605428e-20,
      /* [ 96] */ 1.3395155599094805e-20, 1.3599160970797774e-20, 1.3804650839360727e-20, 1.4011635341137284e-20,
      /* [100] */ 1.4220124840587164e-20, 1.4430129933836705e-20, 1.4641661452404201e-20,  1.485473046709328e-20,
      /* [104] */ 1.5069348292058084e-20, 1.5285526489044053e-20, 1.5503276871808626e-20, 1.5722611510726402e-20,
      /* [108] */ 1.5943542737583543e-20, 1.6166083150566702e-20, 1.6390245619451956e-20, 1.6616043290999594e-20,
      /* [112] */ 1.6843489594561079e-20, 1.7072598247904713e-20, 1.7303383263267072e-20, 1.7535858953637607e-20,
      /* [116] */ 1.7770039939284241e-20, 1.8005941154528286e-20, 1.8243577854777398e-20, 1.8482965623825808e-20,
      /* [120] */ 1.8724120381431627e-20, 1.8967058391181452e-20, 1.9211796268653192e-20, 1.9458350989888484e-20,
      /* [124] */ 1.9706739900186868e-20, 1.9956980723234356e-20, 2.0209091570579904e-20, 2.0463090951473895e-20,
      /* [128] */ 2.0718997783083593e-20,  2.097683140110135e-20,  2.123661157076213e-20, 2.1498358498287976e-20,
      /* [132] */ 2.1762092842777868e-20, 2.2027835728562592e-20, 2.2295608758045219e-20, 2.2565434025049041e-20,
      /* [136] */ 2.2837334128696004e-20,  2.311133218784001e-20, 2.3387451856080863e-20, 2.3665717337386111e-20,
      /* [140] */  2.394615340234961e-20,  2.422878540511741e-20, 2.4513639301013211e-20, 2.4800741664897764e-20,
      /* [144] */ 2.5090119710298442e-20, 2.5381801309347597e-20,   2.56758150135705e-20, 2.5972190075566336e-20,
      /* [148] */ 2.6270956471628253e-20, 2.6572144925351523e-20, 2.6875786932281841e-20, 2.7181914785659148e-20,
      /* [152] */ 2.7490561603315974e-20, 2.7801761355793055e-20, 2.8115548895739172e-20, 2.8431959988666534e-20,
      /* [156] */ 2.8751031345137833e-20, 2.9072800654466307e-20, 2.9397306620015486e-20, 2.9724588996191657e-20,
      /* [160] */ 3.0054688627228112e-20, 3.0387647487867642e-20, 3.0723508726057078e-20, 3.1062316707775905e-20,
      /* [164] */ 3.1404117064129991e-20, 3.1748956740850969e-20, 3.2096884050352357e-20, 3.2447948726504914e-20,
      /* [168] */ 3.2802201982306013e-20, 3.3159696570631373e-20,  3.352048684827223e-20, 3.3884628843476888e-20,
      /* [172] */ 3.4252180327233346e-20, 3.4623200888548644e-20, 3.4997752014001677e-20,  3.537589717186906e-20,
      /* [176] */ 3.5757701901149035e-20, 3.6143233905835799e-20,   3.65325631548274e-20, 3.6925761987883572e-20,
      /* [180] */ 3.7322905228086981e-20, 3.7724070301302117e-20, 3.8129337363171041e-20, 3.8538789434235234e-20,
      /* [184] */ 3.8952512543827862e-20, 3.9370595883442399e-20, 3.9793131970351439e-20, 4.0220216822325769e-20,
      /* [188] */ 4.0651950144388133e-20, 4.1088435528630944e-20, 4.1529780668232712e-20, 4.1976097586926582e-20,
      /* [192] */ 4.2427502885307452e-20, 4.2884118005513604e-20, 4.3346069515987453e-20, 4.3813489418210257e-20,
      /* [196] */ 4.4286515477520838e-20, 4.4765291580372353e-20, 4.5249968120658306e-20, 4.5740702418054417e-20,
      /* [200] */ 4.6237659171683015e-20, 4.6741010952818368e-20, 4.7250938740823415e-20, 4.7767632507051219e-20,
      /* [204] */ 4.8291291852069895e-20, 4.8822126702292804e-20, 4.9360358072933852e-20, 4.9906218905182021e-20,
      /* [208] */ 5.0459954986625539e-20, 5.1021825965285324e-20, 5.1592106469178258e-20, 5.2171087345169234e-20,
      /* [212] */ 5.2759077033045284e-20, 5.3356403093325858e-20, 5.3963413910399511e-20, 5.4580480596259246e-20,
      /* [216] */ 5.5207999124535584e-20,  5.584639272987383e-20,  5.649611461419377e-20, 5.7157651009290713e-20,
      /* [220] */ 5.7831524654956632e-20, 5.8518298763794323e-20, 5.9218581558791713e-20,   5.99330314883387e-20,
      /* [224] */ 6.0662363246796887e-20,    6.1407354758435e-20, 6.2168855320499763e-20, 6.2947795150103727e-20,
      /* [228] */ 6.3745196643214394e-20, 6.4562187737537985e-20, 6.5400017881889097e-20, 6.6260077263309343e-20,
      /* [232] */  6.714392014514662e-20, 6.8053293447301698e-20,    6.8990172088133e-20, 6.9956803158564498e-20,
      /* [236] */  7.095576179487843e-20,  7.199002278894508e-20, 7.3063053739105458e-20, 7.4178938266266881e-20,
      /* [240] */ 7.5342542134173124e-20, 7.6559742171142969e-20,  7.783774986341285e-20, 7.9185582674029512e-20,
      /* [244] */   8.06147755373533e-20, 8.2140502769818073e-20, 8.3783445978280519e-20, 8.5573129249678161e-20,
      /* [248] */   8.75544596695901e-20, 8.9802388057706877e-20, 9.2462471421151086e-20, 9.5919641344951721e-20,
      /* [252] */ 1.0842021724855044e-19
    )

// format: on

  /*
   * Create a new exponential sampler with {@code mean = 1}.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @return the sampler
   */

  def of(rng: RandomGenerator): ExponentialZiggurat =
    new ExponentialZiggurat(rng)

  /*
   * Create a new exponential sampler with the specified {@code mean}.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param mean Mean.
   * @return the sampler
   * @throws IllegalArgumentException if the mean is not strictly
   *         positive ({@code mean <= 0})
   */
  def of(rng: RandomGenerator, mean: scala.Double): ExponentialZiggurat =
    new ExponentialZigguratMean(
      rng,
      InternalUtils.requireStrictlyPositive(mean, "mean")
    )
}

/*
 * Specialisation which multiplies the standard exponential result by a
 * specified mean.
 */
private final class ExponentialZigguratMean(
    rng: RandomGenerator,
    mean: scala.Double
) extends ExponentialZiggurat(rng) {

  override def sample() =
    super.sample() * mean
}

/*
 * Modified ziggurat method for sampling from an exponential distribution.
 *
 * @param rng Generator of uniformly distributed random numbers.
 */
private[random] class ExponentialZiggurat(rng: RandomGenerator)
    extends ZigguratSampler(rng) {
  import ZigguratSampler._
  import ExponentialZiggurat._

  override def toString() =
    toString("exponential")

  def sample(): Double = {
    // SN: this comment is probably not revelant to Scala Native.
    //
    // Ideally this method byte code size should be below -XX:MaxInlineSize
    // (which defaults to 35 bytes). This compiles to 35 bytes.

    val x = rng.nextLong()
    // Float multiplication squashes these last 8 bits, so they can be used to sample i
    val i = x.toInt & MASK_INT8

    if (i < I_MAX) {
      // Early exit.
      // Expected frequency = 0.984375
      // Drop the sign bit to multiply by [0, 2^63).
      X(i) * (x >>> 1)
    } else {
      // Expected frequency = 0.015625

      // Tail frequency     = 0.000516062 (recursion)
      // Overhang frequency = 0.0151089

      // Recycle x as the upper 56 bits have not been used.
      edgeSample(x)
    }
  }

  /*
   * Create the sample from the edge of the ziggurat.
   *
   * <p>This method has been extracted to fit the main sample method within 35 bytes (the
   * default size for a JVM to inline a method).
   *
   * @param xx Initial random deviate
   * @return a sample
   */

  private def edgeSample(xx: scala.Long): scala.Double = {
    var j = selectRegion()

    if (j != 0) {
      // Expected overhang frequency = 0.966972
      sampleOverhang(j, xx)
    } else {
      // Expected tail frequency = 0.033028 (recursion)

      // xx must be discarded as the lower bits have already been used to generate i

      // If the tail then exploit the memoryless property of the exponential distribution.
      // Perform a new sample and add it to the start of the tail.
      // This loop sums tail values until a sample can be returned from the exponential.
      // The sum is added to the final sample on return.
      var x0 = X_0

      while (true) {
        // Duplicate of the sample() method
        val x = rng.nextLong()
        val i = x.toInt & 0xff

        if (i < I_MAX) {
          // Early exit.
          return x0 + X(i) * (x >>> 1)
        }
        // Edge of the ziggurat
        j = selectRegion()
        if (j != 0)
          return x0 + sampleOverhang(j, x)

        // Add another tail sample
        x0 += X_0
      }
      -0.0 // Should never get here, keep compiler happy.
    }
  }

  /*
   * Select the overhang region or the tail using alias sampling.
   *
   * @return the region
   */
  private def selectRegion(): Int = {
    val x = rng.nextLong()
    // j in [0, 256)
    val j = x.toInt & MASK_INT8
    // map to j in [0, N] with N the number of layers of the ziggurat
    if (x >= IPMF(j)) MAP(j) & MASK_INT8
    else j
  }

  /*
   * Sample from overhang region {@code j}.
   *
   * @param j Index j (must be {@code > 0})
   * @param xx Initial random deviate
   * @return the sample
   */
  private def sampleOverhang(j: scala.Int, xx: scala.Long): scala.Double = {
    // Recycle the initial random deviate.
    // Shift right to make an unsigned long.
    var u1 = xx >>> 1

    while (true) {
      // Sample from the triangle:
      //    X[j],Y[j]
      //        |\-->u1
      //        | \  |
      //        |  \ |
      //        |   \|    Overhang j (with hypotenuse not pdf(x))
      //        |    \
      //        |    |\
      //        |    | \
      //        |    u2 \
      //        +-------- X[j-1],Y[j-1]
      // Create a second uniform deviate (as u1 is recycled).
      val u = randomInt63()

      // If u2 < u1 then reflect in the hypotenuse by swapping u1 and u2.
      // Use conditional ternary to avoid a 50/50 branch statement to swap the pair.
      val u2 =
        if (u1 < u) u
        else u1

      u1 =
        if (u1 < u) u1
        else u

      val x = interpolate(X, j, u1)

      if (u2 - u1 >= E_MAX) {
        // Early Exit: x < y - epsilon
        return x
      }

      // Note: Frequencies have been empirically measured per call into expOverhang:
      // Early Exit = 0.823328
      // Accept Y   = 0.161930
      // Reject Y   = 0.0147417 (recursion)

      if (interpolate(Y, j, u2) <= Math.exp(-x)) {
        return x
      }

      // Generate another variate for the next iteration
      u1 = randomInt63()
    }

    -0.0 // Should never get here, keep compiler happy.
  }
}

private[random] object GaussianZiggurat {
  // Ziggurat volumes:
  // Inside the layers              = 98.8281%  (253/256)
  // Fraction outside the layers:
  // convex overhangs               = 76.1941%  (x < 1)
  // inflection overhang            =  0.1358%  (x ~ 1)
  // concave overhangs              = 21.3072%  (x > 1)
  // tail                           =  2.3629%  (x > 3.63...)

  /* The number of layers in the ziggurat. Maximum i value for early exit. */
  final val I_MAX = 253

  /* The point where the Gaussian switches from convex to concave.
   * This is the largest value of X[j] below 1.
   */
  final val J_INFLECTION = 204

  /* Maximum epsilon distance of convex pdf(x) above the hypotenuse value for early rejection.
   * Equal to approximately 0.2460 scaled by 2^63. This is negated on purpose as the
   * distance for a point (x,y) above the hypotenuse is negative:
   * {@code (|d| < max) == (d >= -max)}.
   */

  final val CONVEX_E_MAX = -2269182951627976004L

  /* Maximum distance of concave pdf(x) below the hypotenuse value
   * for early exit.
   * Equal to approximately 0.08244 scaled by 2^63.
   */

  final val CONCAVE_E_MAX = 760463704284035184L

  /* Beginning of tail. Equal to X[0] * 2^63. */
  final val X_0 = 3.6360066255009455861

  /* 1/X_0. Used for tail sampling. */
  final val ONE_OVER_X_0 = 1.0 / X_0

// format: off

  /* The alias map. An integer in [0, 255] stored as a byte to save space.
   * Contains the alias j for each index. j=0 is the tail; j in [1, N] is the overhang
   * for each layer.
   */
  final val MAP = Array[scala.Byte](
    /* [  0] */   0.toByte,   0.toByte, 239.toByte,   2.toByte,   0.toByte,   0.toByte,   0.toByte,   0.toByte,
    /* [  8] */   0.toByte,   0.toByte,   0.toByte,   0.toByte,   1.toByte,   1.toByte,   1.toByte, 253.toByte,
    /* [ 16] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [ 24] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [ 32] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [ 40] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [ 48] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [ 56] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [ 64] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [ 72] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [ 80] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [ 88] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [ 96] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [104] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [112] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [120] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [128] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [136] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [144] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [152] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [160] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [168] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [176] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [184] */ 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte, 253.toByte,
    /* [192] */ 253.toByte, 253.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte, 252.toByte,
    /* [200] */ 252.toByte, 252.toByte, 252.toByte, 252.toByte, 251.toByte, 251.toByte, 251.toByte, 251.toByte,
    /* [208] */ 251.toByte, 251.toByte, 251.toByte, 250.toByte, 250.toByte, 250.toByte, 250.toByte, 250.toByte,
    /* [216] */ 249.toByte, 249.toByte, 249.toByte, 248.toByte, 248.toByte, 248.toByte, 247.toByte, 247.toByte,
    /* [224] */ 247.toByte, 246.toByte, 246.toByte, 245.toByte, 244.toByte, 244.toByte, 243.toByte, 242.toByte,
    /* [232] */ 240.toByte,   2.toByte,   2.toByte,   3.toByte,   3.toByte,   0.toByte,   0.toByte, 240.toByte,
    /* [240] */ 241.toByte, 242.toByte, 243.toByte, 244.toByte, 245.toByte, 246.toByte, 247.toByte, 248.toByte,
    /* [248] */ 249.toByte, 250.toByte, 251.toByte, 252.toByte, 253.toByte,   1.toByte,   0.toByte,   0.toByte
  )

  /* The alias inverse PMF. This is the probability threshold to use the alias for j in-place of j.
   * This has been scaled by 2^64 and offset by -2^63. It represents the numerator of a fraction
   * with denominator 2^64 and can be compared directly to a uniform long deviate.
   * The value probability 0.0 is Long.MIN_VALUE and is used when {@code j > I_MAX}.
   */
  final val IPMF = Array[scala.Long](
      /* [  0] */  9223372036854775296L,  1100243796534090752L,  7866600928998383104L,  6788754710675124736L,
      /* [  4] */  9022865200181688320L,  6522434035205502208L,  4723064097360024576L,  3360495653216416000L,
      /* [  8] */  2289663232373870848L,  1423968905551920384L,   708364817827798016L,   106102487305601280L,
      /* [ 12] */  -408333464665794560L,  -853239722779025152L, -1242095211825521408L, -1585059631105762048L,
      /* [ 16] */ -1889943050287169024L, -2162852901990669824L, -2408637386594511104L, -2631196530262954496L,
      /* [ 20] */ -2833704942520925696L, -3018774289025787392L, -3188573753472222208L, -3344920681707410944L,
      /* [ 24] */ -3489349705062150656L, -3623166100042179584L, -3747487436868335360L, -3863276422712173824L,
      /* [ 28] */ -3971367044063130880L, -4072485557029824000L, -4167267476830916608L, -4256271432240159744L,
      /* [ 32] */ -4339990541927306752L, -4418861817133802240L, -4493273980372377088L, -4563574004462246656L,
      /* [ 36] */ -4630072609770453760L, -4693048910430964992L, -4752754358862894848L, -4809416110052769536L,
      /* [ 40] */ -4863239903586985984L, -4914412541515875840L, -4963104028439161088L, -5009469424769119232L,
      /* [ 44] */ -5053650458856559360L, -5095776932695077632L, -5135967952544929024L, -5174333008451230720L,
      /* [ 48] */ -5210972924952654336L, -5245980700100460288L, -5279442247516297472L, -5311437055462369280L,
      /* [ 52] */ -5342038772315650560L, -5371315728843297024L, -5399331404632512768L, -5426144845448965120L,
      /* [ 56] */ -5451811038519422464L, -5476381248265593088L, -5499903320558339072L, -5522421955752311296L,
      /* [ 60] */ -5543978956085263616L, -5564613449659060480L, -5584362093436146432L, -5603259257517428736L,
      /* [ 64] */ -5621337193070986240L, -5638626184974132224L, -5655154691220933888L, -5670949470294763008L,
      /* [ 68] */ -5686035697601807872L, -5700437072199152384L, -5714175914219812352L, -5727273255295220992L,
      /* [ 72] */ -5739748920271997440L, -5751621603810412032L, -5762908939773946112L, -5773627565915007744L,
      /* [ 76] */ -5783793183152377600L, -5793420610475628544L, -5802523835894661376L, -5811116062947570176L,
      /* [ 80] */ -5819209754516120832L, -5826816672854571776L, -5833947916825278208L, -5840613956570608128L,
      /* [ 84] */ -5846824665591763456L, -5852589350491075328L, -5857916778480726528L, -5862815203334800384L,
      /* [ 88] */ -5867292388935742464L, -5871355631762284032L, -5875011781262890752L, -5878267259039093760L,
      /* [ 92] */ -5881128076579883520L, -5883599852028851456L, -5885687825288565248L, -5887396872144963840L,
      /* [ 96] */ -5888731517955042304L, -5889695949247728384L, -5890294025706689792L, -5890529289910829568L,
      /* [100] */ -5890404977675987456L, -5889924026487208448L, -5889089083913555968L, -5887902514965209344L,
      /* [104] */ -5886366408898372096L, -5884482585690639872L, -5882252601321090304L, -5879677752995027712L,
      /* [108] */ -5876759083794175232L, -5873497386318840832L, -5869893206505510144L, -5865946846617024256L,
      /* [112] */ -5861658367354159104L, -5857027590486131456L, -5852054100063428352L, -5846737243971504640L,
      /* [116] */ -5841076134082373632L, -5835069647234580480L, -5828716424754549248L, -5822014871949021952L,
      /* [120] */ -5814963157357531648L, -5807559211080072192L, -5799800723447229952L, -5791685142338073344L,
      /* [124] */ -5783209670985158912L, -5774371264582489344L, -5765166627072226560L, -5755592207057667840L,
      /* [128] */ -5745644193442049280L, -5735318510777133824L, -5724610813433666560L, -5713516480340333056L,
      /* [132] */ -5702030608556698112L, -5690148005851018752L, -5677863184109371904L, -5665170350903313408L,
      /* [136] */ -5652063400924580608L, -5638535907000141312L, -5624581109999480320L, -5610191908627599872L,
      /* [140] */ -5595360848093632768L, -5580080108034218752L, -5564341489875549952L, -5548136403221394688L,
      /* [144] */ -5531455851545399296L, -5514290416593586944L, -5496630242226406656L, -5478465016761742848L,
      /* [148] */ -5459783954986665216L, -5440575777891777024L, -5420828692432397824L, -5400530368638773504L,
      /* [152] */ -5379667916699401728L, -5358227861294116864L, -5336196115274292224L, -5313557951078385920L,
      /* [156] */ -5290297970633451520L, -5266400072915222272L, -5241847420214015744L, -5216622401043726592L,
      /* [160] */ -5190706591719534080L, -5164080714589203200L, -5136724594099067136L, -5108617109269313024L,
      /* [164] */ -5079736143458214912L, -5050058530461741312L, -5019559997031891968L, -4988215100963582976L,
      /* [168] */ -4955997165645491968L, -4922878208652041728L, -4888828866780320000L, -4853818314258475776L,
      /* [172] */ -4817814175855180032L, -4780782432601701888L, -4742687321746719232L, -4703491227581444608L,
      /* [176] */ -4663154564978699264L, -4621635653358766336L, -4578890580370785792L, -4534873055659683584L,
      /* [180] */ -4489534251700611840L, -4442822631898829568L, -4394683764809104128L, -4345060121983362560L,
      /* [184] */ -4293890858708922880L, -4241111576153830144L, -4186654061692619008L, -4130446006804747776L,
      /* [188] */ -4072410698657718784L, -4012466683838401024L, -3950527400305017856L, -3886500774061896704L,
      /* [192] */ -3820288777467837184L, -3751786943594897664L, -3680883832433527808L, -3607460442623922176L,
      /* [196] */ -3531389562483324160L, -3452535052891361792L, -3370751053395887872L, -3285881101633968128L,
      /* [200] */ -3197757155301365504L, -3106198503156485376L, -3011010550911937280L, -2911983463883580928L,
      /* [204] */ -2808890647470271744L, -2701487041141149952L, -2589507199690603520L, -2472663129329160192L,
      /* [208] */ -2350641842139870464L, -2223102583770035200L, -2089673683684728576L, -1949948966090106880L,
      /* [212] */ -1803483646855993856L, -1649789631480328192L, -1488330106139747584L, -1318513295725618176L,
      /* [216] */ -1139685236927327232L,  -951121376596854784L,  -752016768184775936L,  -541474585642866432L,
      /* [220] */  -318492605725778432L,   -81947227249193216L,   169425512612864512L,   437052607232193536L,
      /* [224] */   722551297568809984L,  1027761939299714304L,  1354787941622770432L,  1706044619203941632L,
      /* [228] */  2084319374409574144L,  2492846399593711360L,  2935400169348532480L,  3416413484613111552L,
      /* [232] */  3941127949860576256L,  4515787798793437952L,  5147892401439714304L,  5846529325380406016L,
      /* [236] */  6622819682216655360L,  7490522659874166016L,  8466869998277892096L,  8216968526387345408L,
      /* [240] */  4550693915488934656L,  7628019504138977280L,  6605080500908005888L,  7121156327650272512L,
      /* [244] */  2484871780331574272L,  7179104797032803328L,  7066086283830045440L,  1516500120817362944L,
      /* [248] */   216305945438803456L,  6295963418525324544L,  2889316805630113280L, -2712587580533804032L,
      /* [252] */  6562498853538167040L,  7975754821147501312L, -9223372036854775808L, -9223372036854775808L
    )
  
  /*
   * The precomputed ziggurat lengths, denoted X_i in the main text.
   * <ul>
   * <li>X_i = length of ziggurat layer i.</li>
   * <li>X_j is the upper-left X coordinate of overhang j (starting from 1).</li>
   * <li>X_(j-1) is the lower-right X coordinate of overhang j.</li>
   * </ul>
   * <p>Values have been scaled by 2^-63.
   * Contains {@code I_MAX + 1} entries as the final value is 0.
   */
  final val X = Array[scala.Double](
      /* [  0] */ 3.9421662825398133e-19, 3.7204945004119012e-19, 3.5827024480628678e-19, 3.4807476236540249e-19,
      /* [  4] */ 3.3990177171882136e-19, 3.3303778360340139e-19,  3.270943881761755e-19,   3.21835771324951e-19,
      /* [  8] */ 3.1710758541840432e-19, 3.1280307407034065e-19, 3.0884520655804019e-19, 3.0517650624107352e-19,
      /* [ 12] */   3.01752902925846e-19,  2.985398344070532e-19, 2.9550967462801797e-19, 2.9263997988491663e-19,
      /* [ 16] */ 2.8991225869977476e-19, 2.8731108780226291e-19, 2.8482346327101335e-19, 2.8243831535194389e-19,
      /* [ 20] */ 2.8014613964727031e-19, 2.7793871261807797e-19, 2.7580886921411212e-19, 2.7375032698308758e-19,
      /* [ 24] */ 2.7175754543391047e-19, 2.6982561247538484e-19, 2.6795015188771505e-19, 2.6612724730440033e-19,
      /* [ 28] */ 2.6435337927976633e-19, 2.6262537282028438e-19, 2.6094035335224142e-19, 2.5929570954331002e-19,
      /* [ 32] */ 2.5768906173214726e-19, 2.5611823497719608e-19, 2.5458123593393361e-19, 2.5307623292372459e-19,
      /* [ 36] */   2.51601538677984e-19, 2.5015559533646191e-19, 2.4873696135403158e-19, 2.4734430003079206e-19,
      /* [ 40] */ 2.4597636942892726e-19,  2.446320134791245e-19, 2.4331015411139206e-19, 2.4200978427132955e-19,
      /* [ 44] */ 2.4072996170445879e-19, 2.3946980340903347e-19, 2.3822848067252674e-19, 2.3700521461931801e-19,
      /* [ 48] */  2.357992722074133e-19, 2.3460996262069972e-19, 2.3343663401054455e-19,  2.322786705467384e-19,
      /* [ 52] */ 2.3113548974303765e-19, 2.3000654002704238e-19, 2.2889129852797606e-19, 2.2778926905921897e-19,
      /* [ 56] */ 2.2669998027527321e-19, 2.2562298398527416e-19,  2.245578536072726e-19, 2.2350418274933911e-19,
      /* [ 60] */ 2.2246158390513294e-19, 2.2142968725296249e-19, 2.2040813954857555e-19, 2.1939660310297601e-19,
      /* [ 64] */ 2.1839475483749618e-19, 2.1740228540916853e-19, 2.1641889840016519e-19, 2.1544430956570613e-19,
      /* [ 68] */ 2.1447824613540345e-19, 2.1352044616350571e-19, 2.1257065792395107e-19, 2.1162863934653125e-19,
      /* [ 72] */ 2.1069415749082026e-19, 2.0976698805483467e-19, 2.0884691491567363e-19, 2.0793372969963634e-19,
      /* [ 76] */ 2.0702723137954107e-19, 2.0612722589717129e-19, 2.0523352580895635e-19, 2.0434594995315797e-19,
      /* [ 80] */ 2.0346432313698148e-19, 2.0258847584216418e-19, 2.0171824394771313e-19, 2.0085346846857531e-19,
      /* [ 84] */ 1.9999399530912015e-19, 1.9913967503040585e-19, 1.9829036263028144e-19, 1.9744591733545175e-19,
      /* [ 88] */ 1.9660620240469857e-19, 1.9577108494251485e-19, 1.9494043572246307e-19, 1.9411412901962161e-19,
      /* [ 92] */ 1.9329204245152935e-19, 1.9247405682708168e-19, 1.9166005600287074e-19, 1.9084992674649826e-19,
      /* [ 96] */  1.900435586064234e-19, 1.8924084378793725e-19, 1.8844167703488436e-19, 1.8764595551677749e-19,
      /* [100] */  1.868535787209745e-19, 1.8606444834960934e-19, 1.8527846822098793e-19, 1.8449554417517928e-19,
      /* [104] */ 1.8371558398354868e-19, 1.8293849726199566e-19, 1.8216419538767393e-19, 1.8139259141898448e-19,
      /* [108] */ 1.8062360001864453e-19, 1.7985713737964743e-19, 1.7909312115393845e-19,   1.78331470383642e-19,
      /* [112] */ 1.7757210543468428e-19, 1.7681494793266395e-19,  1.760599207008314e-19, 1.7530694770004409e-19,
      /* [116] */ 1.7455595397057217e-19, 1.7380686557563475e-19, 1.7305960954655264e-19, 1.7231411382940904e-19,
      /* [120] */ 1.7157030723311378e-19, 1.7082811937877138e-19, 1.7008748065025788e-19, 1.6934832214591352e-19,
      /* [124] */ 1.6861057563126349e-19, 1.6787417349268046e-19, 1.6713904869190636e-19, 1.6640513472135291e-19,
      /* [128] */ 1.6567236556010242e-19, 1.6494067563053266e-19, 1.6420999975549115e-19, 1.6348027311594532e-19,
      /* [132] */ 1.6275143120903661e-19, 1.6202340980646725e-19, 1.6129614491314931e-19, 1.6056957272604589e-19,
      /* [136] */ 1.5984362959313479e-19, 1.5911825197242491e-19, 1.5839337639095554e-19,   1.57668939403708e-19,
      /* [140] */ 1.5694487755235889e-19, 1.5622112732380261e-19,  1.554976251083707e-19, 1.5477430715767271e-19,
      /* [144] */  1.540511095419833e-19, 1.5332796810709688e-19, 1.5260481843056974e-19, 1.5188159577726683e-19,
      /* [148] */ 1.5115823505412761e-19, 1.5043467076406199e-19, 1.4971083695888395e-19, 1.4898666719118714e-19,
      /* [152] */ 1.4826209446506113e-19, 1.4753705118554365e-19,  1.468114691066983e-19, 1.4608527927820112e-19,
      /* [156] */ 1.4535841199031451e-19, 1.4463079671711862e-19, 1.4390236205786415e-19, 1.4317303567630177e-19,
      /* [160] */ 1.4244274423783481e-19, 1.4171141334433217e-19, 1.4097896746642792e-19, 1.4024532987312287e-19,
      /* [164] */ 1.3951042255849034e-19, 1.3877416616527576e-19, 1.3803647990516385e-19, 1.3729728147547174e-19,
      /* [168] */ 1.3655648697200824e-19, 1.3581401079782068e-19, 1.3506976556752901e-19, 1.3432366200692418e-19,
      /* [172] */ 1.3357560884748263e-19, 1.3282551271542047e-19, 1.3207327801488087e-19, 1.3131880680481524e-19,
      /* [176] */ 1.3056199866908076e-19, 1.2980275057923788e-19, 1.2904095674948608e-19, 1.2827650848312727e-19,
      /* [180] */ 1.2750929400989213e-19, 1.2673919831340482e-19, 1.2596610294799512e-19, 1.2518988584399374e-19,
      /* [184] */ 1.2441042110056523e-19, 1.2362757876504165e-19, 1.2284122459762072e-19, 1.2205121982017852e-19,
      /* [188] */ 1.2125742084782245e-19, 1.2045967900166973e-19,  1.196578402011802e-19, 1.1885174463419555e-19,
      /* [192] */ 1.1804122640264091e-19, 1.1722611314162064e-19, 1.1640622560939109e-19, 1.1558137724540874e-19,
      /* [196] */ 1.1475137369333185e-19, 1.1391601228549047e-19, 1.1307508148492592e-19, 1.1222836028063025e-19,
      /* [200] */ 1.1137561753107903e-19, 1.1051661125053526e-19, 1.0965108783189755e-19, 1.0877878119905372e-19,
      /* [204] */ 1.0789941188076655e-19,  1.070126859970364e-19, 1.0611829414763286e-19, 1.0521591019102928e-19,
      /* [208] */ 1.0430518990027552e-19, 1.0338576948035472e-19, 1.0245726392923699e-19,  1.015192652220931e-19,
      /* [212] */ 1.0057134029488235e-19, 9.9613028799672809e-20, 9.8643840599459914e-20, 9.7663252964755816e-20,
      /* [216] */ 9.6670707427623454e-20,  9.566560624086667e-20, 9.4647308380433213e-20, 9.3615125017323508e-20,
      /* [220] */ 9.2568314370887282e-20, 9.1506075837638774e-20, 9.0427543267725716e-20,  8.933177723376368e-20,
      /* [224] */ 8.8217756102327883e-20, 8.7084365674892319e-20, 8.5930387109612162e-20, 8.4754482764244349e-20,
      /* [228] */ 8.3555179508462343e-20, 8.2330848933585364e-20, 8.1079683729129853e-20, 7.9799669284133864e-20,
      /* [232] */ 7.8488549286072745e-20, 7.7143783700934692e-20, 7.5762496979467566e-20, 7.4341413578485329e-20,
      /* [236] */ 7.2876776807378431e-20, 7.1364245443525374e-20, 6.9798760240761066e-20, 6.8174368944799054e-20,
      /* [240] */ 6.6483992986198539e-20, 6.4719110345162767e-20, 6.2869314813103699e-20, 6.0921687548281263e-20,
      /* [244] */ 5.8859873575576818e-20, 5.6662675116090981e-20, 5.4301813630894571e-20,  5.173817174449422e-20,
      /* [248] */ 4.8915031722398545e-20, 4.5744741890755301e-20, 4.2078802568583416e-20, 3.7625986722404761e-20,
      /* [252] */ 3.1628589805881879e-20,                    0.0
   )

  /*
   * The precomputed ziggurat heights, denoted Y_i in the main text.
   * <ul>
   * <li>Y_i = height of ziggurat layer i.</li>
   * <li>Y_j is the upper-left Y coordinate of overhang j (starting from 1).</li>
   * <li>Y_(j-1) is the lower-right Y coordinate of overhang j.</li>
   * </ul>
   * <p>Values have been scaled by 2^-63.
   * Contains {@code I_MAX + 1} entries as the final value is pdf(x=0).
   */
  final val Y = Array[scala.Double](
      /* [  0] */ 1.4598410796619063e-22, 3.0066613427942797e-22, 4.6129728815103466e-22, 6.2663350049234362e-22,
      /* [  4] */ 7.9594524761881544e-22, 9.6874655021705039e-22, 1.1446877002379439e-21, 1.3235036304379167e-21,
      /* [  8] */ 1.5049857692053131e-21, 1.6889653000719298e-21, 1.8753025382711626e-21, 2.0638798423695191e-21,
      /* [ 12] */ 2.2545966913644708e-21, 2.4473661518801799e-21, 2.6421122727763533e-21, 2.8387681187879908e-21,
      /* [ 16] */ 3.0372742567457284e-21, 3.2375775699986589e-21,  3.439630315794878e-21, 3.6433893657997798e-21,
      /* [ 20] */ 3.8488155868912312e-21, 4.0558733309492775e-21,  4.264530010428359e-21, 4.4747557422305067e-21,
      /* [ 24] */ 4.6865230465355582e-21, 4.8998065902775257e-21, 5.1145829672105489e-21, 5.3308305082046173e-21,
      /* [ 28] */ 5.5485291167031758e-21, 5.7676601252690476e-21, 5.9882061699178461e-21, 6.2101510795442221e-21,
      /* [ 32] */ 6.4334797782257209e-21, 6.6581781985713897e-21, 6.8842332045893181e-21, 7.1116325227957095e-21,
      /* [ 36] */ 7.3403646804903092e-21, 7.5704189502886418e-21, 7.8017853001379744e-21, 8.0344543481570017e-21,
      /* [ 40] */ 8.2684173217333118e-21, 8.5036660203915022e-21, 8.7401927820109521e-21, 8.9779904520281901e-21,
      /* [ 44] */ 9.2170523553061439e-21,  9.457372270392882e-21,  9.698944405926943e-21, 9.9417633789758424e-21,
      /* [ 48] */ 1.0185824195119818e-20,  1.043112223011477e-20, 1.0677653212987396e-20, 1.0925413210432004e-20,
      /* [ 52] */ 1.1174398612392891e-20, 1.1424606118728715e-20, 1.1676032726866302e-20, 1.1928675720361027e-20,
      /* [ 56] */ 1.2182532658289373e-20, 1.2437601365406785e-20, 1.2693879923010674e-20, 1.2951366660454145e-20,
      /* [ 60] */ 1.3210060147261461e-20, 1.3469959185800733e-20, 1.3731062804473644e-20, 1.3993370251385596e-20,
      /* [ 64] */ 1.4256880988463136e-20, 1.4521594685988369e-20, 1.4787511217522902e-20,  1.505463065519617e-20,
      /* [ 68] */ 1.5322953265335218e-20, 1.5592479504415048e-20, 1.5863210015310328e-20, 1.6135145623830982e-20,
      /* [ 72] */ 1.6408287335525592e-20, 1.6682636332737932e-20, 1.6958193971903124e-20, 1.7234961781071113e-20,
      /* [ 76] */ 1.7512941457646084e-20, 1.7792134866331487e-20,  1.807254403727107e-20, 1.8354171164377277e-20,
      /* [ 80] */ 1.8637018603838945e-20, 1.8921088872801004e-20, 1.9206384648209468e-20, 1.9492908765815636e-20,
      /* [ 84] */ 1.9780664219333857e-20, 2.0069654159747839e-20, 2.0359881894760859e-20, 2.0651350888385696e-20,
      /* [ 88] */ 2.0944064760670539e-20, 2.1238027287557466e-20, 2.1533242400870487e-20, 2.1829714188430474e-20,
      /* [ 92] */ 2.2127446894294597e-20,  2.242644491911827e-20, 2.2726712820637798e-20, 2.3028255314272276e-20,
      /* [ 96] */ 2.3331077273843558e-20, 2.3635183732413286e-20, 2.3940579883236352e-20, 2.4247271080830277e-20,
      /* [100] */  2.455526284216033e-20, 2.4864560847940368e-20, 2.5175170944049622e-20, 2.5487099143065929e-20,
      /* [104] */ 2.5800351625915997e-20, 2.6114934743643687e-20, 2.6430855019297323e-20, 2.6748119149937411e-20,
      /* [108] */ 2.7066734008766247e-20, 2.7386706647381193e-20, 2.7708044298153558e-20, 2.8030754376735269e-20,
      /* [112] */ 2.8354844484695747e-20, 2.8680322412291631e-20, 2.9007196141372126e-20, 2.9335473848423219e-20,
      /* [116] */ 2.9665163907753988e-20, 2.9996274894828624e-20, 3.0328815589748056e-20, 3.0662794980885287e-20,
      /* [120] */  3.099822226867876e-20, 3.1335106869588609e-20, 3.1673458420220558e-20, 3.2013286781622988e-20,
      /* [124] */ 3.2354602043762612e-20, 3.2697414530184806e-20,  3.304173480286495e-20, 3.3387573667257349e-20,
      /* [128] */ 3.3734942177548938e-20, 3.4083851642125208e-20, 3.4434313629256243e-20, 3.4786339973011376e-20,
      /* [132] */ 3.5139942779411164e-20, 3.5495134432826171e-20,  3.585192760263246e-20, 3.6210335250134172e-20,
      /* [136] */ 3.6570370635764384e-20, 3.6932047326575882e-20, 3.7295379204034252e-20, 3.7660380472126401e-20,
      /* [140] */ 3.8027065665798284e-20, 3.8395449659736649e-20, 3.8765547677510167e-20, 3.9137375301086406e-20,
      /* [144] */ 3.9510948480742172e-20,  3.988628354538543e-20, 4.0263397213308566e-20, 4.0642306603393541e-20,
      /* [148] */ 4.1023029246790967e-20, 4.1405583099096438e-20, 4.1789986553048817e-20, 4.2176258451776819e-20,
      /* [152] */ 4.2564418102621759e-20, 4.2954485291566197e-20, 4.3346480298300118e-20, 4.3740423911958146e-20,
      /* [156] */ 4.4136337447563716e-20, 4.4534242763218286e-20, 4.4934162278076256e-20, 4.5336118991149025e-20,
      /* [160] */ 4.5740136500984466e-20, 4.6146239026271279e-20, 4.6554451427421133e-20, 4.6964799229185088e-20,
      /* [164] */ 4.7377308644364938e-20, 4.7792006598684169e-20, 4.8208920756888113e-20, 4.8628079550147814e-20,
      /* [168] */ 4.9049512204847653e-20, 4.9473248772842596e-20, 4.9899320163277674e-20, 5.0327758176068971e-20,
      /* [172] */ 5.0758595537153414e-20, 5.1191865935622696e-20, 5.1627604062866059e-20, 5.2065845653856416e-20,
      /* [176] */ 5.2506627530725194e-20, 5.2949987648783448e-20, 5.3395965145159426e-20, 5.3844600390237576e-20,
      /* [180] */ 5.4295935042099358e-20, 5.4750012104183868e-20, 5.5206875986405073e-20, 5.5666572569983821e-20,
      /* [184] */ 5.6129149276275792e-20, 5.6594655139902476e-20, 5.7063140886520563e-20, 5.7534659015596918e-20,
      /* [188] */ 5.8009263888591218e-20, 5.8487011822987583e-20, 5.8967961192659803e-20, 5.9452172535103471e-20,
      /* [192] */ 5.9939708666122605e-20, 6.0430634802618929e-20, 6.0925018694200531e-20,  6.142293076440286e-20,
      /* [196] */ 6.1924444262401531e-20, 6.2429635426193939e-20, 6.2938583658336214e-20, 6.3451371715447563e-20,
      /* [200] */ 6.3968085912834963e-20, 6.4488816345752736e-20, 6.5013657128995346e-20, 6.5542706656731714e-20,
      /* [204] */ 6.6076067884730717e-20, 6.6613848637404196e-20,  6.715616194241298e-20,  6.770312639595058e-20,
      /* [208] */ 6.8254866562246408e-20, 6.8811513411327825e-20, 6.9373204799659681e-20, 6.9940085998959109e-20,
      /* [212] */ 7.0512310279279503e-20, 7.1090039553397167e-20, 7.1673445090644796e-20, 7.2262708309655784e-20,
      /* [216] */ 7.2858021661057338e-20,   7.34595896130358e-20, 7.4067629754967553e-20, 7.4682374037052817e-20,
      /* [220] */ 7.5304070167226666e-20, 7.5932983190698547e-20, 7.6569397282483754e-20, 7.7213617789487678e-20,
      /* [224] */ 7.7865973566417016e-20, 7.8526819659456755e-20,  7.919654040385056e-20, 7.9875553017037968e-20,
      /* [228] */  8.056431178890163e-20, 8.1263312996426176e-20, 8.1973100703706304e-20, 8.2694273652634034e-20,
      /* [232] */ 8.3427493508836792e-20, 8.4173494807453416e-20, 8.4933097052832066e-20, 8.5707219578230905e-20,
      /* [236] */ 8.6496899985930695e-20, 8.7303317295655327e-20, 8.8127821378859504e-20, 8.8971970928196666e-20,
      /* [240] */ 8.9837583239314064e-20, 9.0726800697869543e-20, 9.1642181484063544e-20, 9.2586826406702765e-20,
      /* [244] */ 9.3564561480278864e-20, 9.4580210012636175e-20, 9.5640015550850358e-20,  9.675233477050313e-20,
      /* [248] */ 9.7928851697808831e-20, 9.9186905857531331e-20, 1.0055456271343397e-19, 1.0208407377305566e-19,
      /* [252] */ 1.0390360993240711e-19, 1.0842021724855044e-19
    )

// format: on

  /*
   * Create a new normalised Gaussian sampler.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @return the sampler
   */
  def of(rng: RandomGenerator): GaussianZiggurat =
    new GaussianZiggurat(rng)
}

/*
 * Modified ziggurat method for sampling from a Gaussian distribution with
 * mean 0 and standard deviation 1.
 *
 * <p>Note: The algorithm is a modification of the
 * {@link ZigguratNormalizedGaussianSampler Marsaglia and Tsang "Ziggurat" method}.
 * The modification improves performance of the rejection method used to generate
 * samples at the edge of the ziggurat.
 */

private[random] class GaussianZiggurat(rng: RandomGenerator)
    extends ZigguratSampler(rng) {
  import ZigguratSampler._
  import GaussianZiggurat._

  /* Exponential sampler used for the long tail. */
  final val exponentialRng = ExponentialZiggurat.of(rng)

  override def toString() =
    toString("normalized Gaussian")

  def sample(): Double = {
    // SN: this comment is probably not revelant to Scala Native.
    //
    // Ideally this method byte code size should be below -XX:MaxInlineSize
    // (which defaults to 35 bytes). This compiles to 33 bytes.

    val xx = rng.nextLong()

    // Float multiplication squashes these last 8 bits, so they can be used to sample i
    val i = xx.toInt & MASK_INT8

    if (i < I_MAX) {
      // Early exit.
      // Expected frequency = 0.988281
      X(i) * xx
    } else {
      edgeSample(xx)
    }
  }

  /*
   * Create the sample from the edge of the ziggurat.
   *
   * <p>This method has been extracted to fit the main sample method within 35 bytes (the
   * default size for a JVM to inline a method).
   *
   * @param xx Initial random deviate
   * @return a sample
   */
  private def edgeSample(xx: scala.Long): scala.Double = {
    // Expected frequency = 0.0117188

    // Drop the sign bit to create u:
    var u1 = xx & MAX_INT64

    // Extract the sign bit for use later
    // Use 2 - 1 or 0 - 1
    val signBit = ((xx >>> 62) & 0x2) - 1.0

    val j = selectRegion()

    // Four kinds of overhangs:
    //  j = 0                :  Sample from tail
    //  0 < j < J_INFLECTION :  Overhang is concave; only sample from Lower-Left triangle
    //  j = J_INFLECTION     :  Must sample from entire overhang rectangle
    //  j > J_INFLECTION     :  Overhangs are convex; implicitly accept point in Lower-Left triangle
    //
    // Conditional statements are arranged such that the more likely outcomes are first.
    var x = 0.0
    var breakSeen = false

    if (j > J_INFLECTION) {
      // Convex overhang
      // Expected frequency: 0.00892899
      // Observed loop repeat frequency: 0.389804
      while (!breakSeen) {
        x = interpolate(X, j, u1)
        // u2 = u1 + (u2 - u1) = u1 + uDistance
        val uDistance = randomInt63() - u1
        if (uDistance >= 0) {
          // Lower-left triangle
          breakSeen = true
        }

        if (uDistance >= CONVEX_E_MAX &&
            // Within maximum distance of f(x) from the triangle hypotenuse.
            // Frequency (per upper-right triangle): 0.431497
            // Reject frequency: 0.489630
            interpolate(Y, j, u1 + uDistance) < Math.exp(-0.5 * x * x)) {
          breakSeen = true
        }

        // uDistance < E_MAX (upper-right triangle) or rejected as above the curve
        u1 = randomInt63()
      }
    } else if (j < J_INFLECTION) {
      if (j == 0) {
        // Tail
        // Expected frequency: 0.000276902
        // Note: Although less frequent than the next branch, j == 0 is a subset of
        // j < J_INFLECTION and must be first.
        // Observed loop repeat frequency: 0.0634786
        while ({
          x = ONE_OVER_X_0 * exponentialRng.sample()
          (exponentialRng.sample() < 0.5 * x * x)
        }) ()

        x += X_0
      } else {
        // Concave overhang
        // Expected frequency: 0.00249694
        // Observed loop repeat frequency: 0.0123784
        while (!breakSeen) {
          // Create a second uniform deviate (as u1 is recycled).
          val u = randomInt63()
          // If u2 < u1 then reflect in the hypotenuse by swapping u1 and u2.
          // Use conditional ternary to avoid a 50/50 branch statement to swap the pair.
          val u2 =
            if (u1 < u) u
            else u1

          u1 =
            if (u1 < u) u1
            else u

          x = interpolate(X, j, u1)
          if (u2 - u1 > CONCAVE_E_MAX ||
              interpolate(Y, j, u2) < Math.exp(-0.5 * x * x)) {
            breakSeen = true
          }

          u1 = randomInt63()
        }
      }
    } else {
      // Inflection point
      // Expected frequency: 0.000015914
      // Observed loop repeat frequency: 0.500213
      while (!breakSeen) {
        x = interpolate(X, j, u1)
        if (interpolate(Y, j, randomInt63()) < Math.exp(-0.5 * x * x)) {
          breakSeen = true
        }
        u1 = randomInt63()
      }
    }

    signBit * x
  }

  /*
   * Select the overhang region or the tail using alias sampling.
   *
   * @return the region
   */
  private def selectRegion(): Int = {
    val x = rng.nextLong()
    // j in [0, 256)
    val j = x.toInt & MASK_INT8

    // map to j in [0, N] with N the number of layers of the ziggurat
    if (x >= IPMF(j)) MAP(j) & MASK_INT8
    else j
  }
}

// from Apache rng InternalUtils.java
object InternalUtils {

  def requireStrictlyPositive(x: scala.Double, name: String): scala.Double = {
    // Logic inversion detects NaN
    if (!(x > 0))
      throw new IllegalArgumentException(s"$name is not strictly positive: $x")

    x
  }
}
