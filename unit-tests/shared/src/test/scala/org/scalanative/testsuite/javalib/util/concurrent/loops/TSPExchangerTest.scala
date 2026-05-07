/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 *  file: src/test/loops/TSPExchangerTest.java
 *  revision 1.19, dated: 2015-09-13
 */

/* Scala Native Notes for Travelling Salesperson Problem (TSP) ExchangerTest:
 *
 *   0. BEWARE! This code currently reveals a GC-crash bug as
 *      described in SN Issue #4871.  To run to completion, it
 *      should be built with mode=debug. Note well that the
 *      execution speed when built with mode=debug and a SN backend
 *      is 15 or more times slower than with a JVM backend.
 *
 *      This BUG manifests itself when DEFAULT_MAX_THREADS > NCPUS.
 *      To use mode=release-fast, say for execution timing studies,
 *      one can edit this file and reduce DEFAULT_MAX_THREADS to or below
 *      NCPUS. Of course, doing so limits the available parallelism.
 *      Nasty bug!
 *
 *      This code does not encounter this bug when run with a JVM
 *      background. That gives evidence that the port, by itself, is not
 *      deadlocking or in an infinite loop or two. By original JSR-166
 *      design it _is_ in a number of CPU intensive threads.
 *
 *   1. This test is intended as a manual test for Exploring the behavior
 *      of Exchanger.scala. Because the results require validation which
 *      is hard to automate and because it is CPU intensive at scale,
 *      it is not intended for regular Continuous Integration (CI) use.
 *
 *      It has been manually exercised using Scala 3.8.3 on macOS 26.4.
 *
 *      It is particularly useful for long duration runs and inserting
 *      debugging printf to ensure that invariants hold.  It can also
 *      be used with care to compare execution times between .java
 *      original, this code run on Java, and this code run on Scala Native.
 *
 *      Two other Exchanger related tests exist in CVS src/test/loops/.
 *      Porting them is left as an exercise for the reader.
 *
 *   2a. The direction of goodness for best case (B:) and worst case (W:)
 *       is towards zero: smaller is better.
 *
 *   2b. After the starting array fill,  the invariant  best_case <= worst_case
 *       should always hold in printed results.
 *
 *   3a. The output from this program compiled for Scala and run on JVM and
 *       Scala Native is approximately/visually the same (same order and
 *       direction of magnitude, and within a few decimal places)
 *
 *   3b. When build mode=release_fast and run with a JVM or SN backend
 *       the execution times approximately equal that of the .java
 *       original.
 *
 *   4. The 'n' in the banner "Replication: n" is indexed [0, n) in
 *      the JSR-166 .java code.
 *
 *      So do not be concerned wondering when examining the tail
 *      of the output, wondering what happened to the last iteration.
 *
 *      An index starting at 0 is how one knows that one is doing
 *      Computer Science.
 *
 *   5. System.out.printf() statements use explicit conversions to Objects
 *      so that Scala 2.12 CI will compile.
 */

/*
 * Written by Doug Lea and Bill Scherer with assistance from members
 * of JCP JSR-166 Expert Group and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.util._
import java.util.concurrent._
import java.util.concurrent.atomic._
import java.util.concurrent.locks._
import java.{lang => jl}

/*
 * A parallel Traveling Salesperson Problem (TSP) program based on a
 * genetic algorithm using an Exchanger.  A population of chromosomes is
 * distributed among "subpops".  Each chromosomes represents a tour,
 * and its fitness is the total tour length.
 *
 * A set of worker threads perform updates on subpops. The basic
 * update step is:
 * <ol>
 *   <li>Select a breeder b from the subpop
 *   <li>Create a strand of its tour with a random starting point and length
 *   <li>Offer the strand to the exchanger, receiving a strand from
 *       another subpop
 *   <li>Combine b and the received strand using crossing function to
 *       create new chromosome c.
 *   <li>Replace a chromosome in the subpop with c.
 * </ol>
 *
 * This continues for a given number of generations per subpop.
 * Because there are normally more subpops than threads, each worker
 * thread performs small (randomly sized) run of updates for one
 * subpop and then selects another. A run continues until there is at
 * most one remaining thread performing updates.
 *
 * See below for more details.
 */
object TSPExchangerTest {
  private final val NCPUS = Runtime.getRuntime().availableProcessors()

  /* Runs start with two threads, increasing by two through max */
  private final val DEFAULT_MAX_THREADS = Math.max(4, NCPUS + NCPUS / 2)

  /* The number of replication runs per thread value */
  private final val DEFAULT_REPLICATIONS = 3

  /* If true, print statistics in SNAPSHOT_RATE intervals */
  private var verbose = true
  private final val SNAPSHOT_RATE = 10 * 1000L // in milliseconds

  /*
   * The problem size. Each city is a random point. The goal is to
   * find a tour among them with smallest total Euclidean distance.
   */
  private final val DEFAULT_CITIES = 144

  // Tuning parameters.

  /*
   * The number of chromosomes per subpop. Must be a power of two.
   *
   * Smaller values lead to faster iterations but poorer quality
   * results
   */
  private final val DEFAULT_SUBPOP_SIZE = 32

  /*
   * The number of iterations per subpop. Convergence appears
   * to be roughly proportional to #cities-squared
   */
  private final val DEFAULT_GENERATIONS = DEFAULT_CITIES * DEFAULT_CITIES

  /*
   * The number of subpops. The total population is #subpops * subpopSize,
   * which should be roughly on the order of #cities-squared
   *
   * Smaller values lead to faster total runs but poorer quality
   * results
   */
  private final val DEFAULT_NSUBPOPS = DEFAULT_GENERATIONS / DEFAULT_SUBPOP_SIZE

  /*
   * The minimum length for a random chromosome strand.
   * Must be at least 1.
   */
  private final val MIN_STRAND_LENGTH = 3

  /*
   * The probability mask value for creating random strands,
   * that have lengths at least MIN_STRAND_LENGTH, and grow
   * with exponential decay 2^(-(1/(RANDOM_STRAND_MASK + 1)
   * Must be 1 less than a power of two.
   */
  private final val RANDOM_STRAND_MASK = 7

  /*
   * Probability control for selecting breeders.
   * Breeders are selected starting at the best-fitness chromosome,
   * with exponentially decaying probability
   * 1 / (subpopSize >>> BREEDER_DECAY).
   *
   * Larger values usually cause faster convergence but poorer
   * quality results
   */
  private final val BREEDER_DECAY = 1

  /*
   * Probability control for selecting dyers.
   * Dyers are selected starting at the worst-fitness chromosome,
   * with exponentially decaying probability
   * 1 / (subpopSize >>> DYER_DECAY)
   *
   * Larger values usually cause faster convergence but poorer
   * quality results
   */
  private final val DYER_DECAY = 1

  /*
   * The set of cities. Created once per program run, to
   * make it easier to compare solutions across different runs.
   */
  private var cities: CitySet = null

  def main(args: Array[String]): Unit = {
    var maxThreads = DEFAULT_MAX_THREADS
    var nCities = DEFAULT_CITIES
    var subpopSize = DEFAULT_SUBPOP_SIZE
    var nGen = nCities * nCities
    var nSubpops = nCities * nCities / subpopSize
    var nReps = DEFAULT_REPLICATIONS

    try {
      var argc = 0
      while (argc < args.length) {
        val option = args(argc)
        argc += 1

        if (option.equals("-c")) {
          nCities = Integer.parseInt(args(argc))
          nGen = nCities * nCities
          nSubpops = nCities * nCities / subpopSize
        } else if (option.equals("-p"))
          subpopSize = Integer.parseInt(args(argc))
        else if (option.equals("-g"))
          nGen = Integer.parseInt(args(argc))
        else if (option.equals("-n"))
          nSubpops = Integer.parseInt(args(argc))
        else if (option.equals("-q")) {
          verbose = false
          argc -= 1
        } else if (option.equals("-r"))
          nReps = Integer.parseInt(args(argc))
        else
          maxThreads = Integer.parseInt(option)
        argc += 1
      }
    } catch {
      case e: Exception =>
        reportUsageErrorAndDie()
    }

    System.out.print(s"\nTSPExchangerTest")
    System.out.print(" -c " + nCities)
    System.out.print(" -g " + nGen)
    System.out.print(" -p " + subpopSize)
    System.out.print(" -n " + nSubpops)
    System.out.print(" -r " + nReps)
    System.out.print(" max threads " + maxThreads)
    System.out.println()

    cities = new CitySet(nCities)

    if (false && NCPUS > 4) {
      val h = NCPUS / 2
      System.out.printf("Threads: %4d Warmup\n", jl.Integer.valueOf(h))
      oneRun(h, nSubpops, subpopSize, nGen)
      Thread.sleep(500)
    }

    val maxt =
      if (maxThreads < nSubpops) maxThreads
      else nSubpops

    for (j <- 0 until nReps) {
      for (i <- 2 to maxt by 2) {
        if (verbose)
          System.out.printf("\n")
        System.out.printf(
          "Threads: %4d Replication: %2d\n",
          jl.Integer.valueOf(i),
          jl.Integer.valueOf(j)
        )

        oneRun(i, nSubpops, subpopSize, nGen)
        Thread.sleep(500)
      }
    }
  }

  private def reportUsageErrorAndDie(): Unit = {
    System.out.print("usage: TSPExchangerTest")
    System.out.print(" [-c #cities]")
    System.out.print(" [-p #subpopSize]")
    System.out.print(" [-g #generations]")
    System.out.print(" [-n #subpops]")
    System.out.print(" [-r #replications]")
    System.out.print(" [-q <quiet>]")
    System.out.print(" #threads]")
    System.out.println()
    System.exit(0)
  }

  /*
   * Performs one run with the given parameters.  Each run completes
   * when there are fewer than 2 active threads.  When there is
   * only one remaining thread, it will have no one to exchange
   * with, so it is terminated (via interrupt).
   */

  private def oneRun(
      nThreads: Int,
      nSubpops: Int,
      subpopSize: Int,
      nGen: Int
  ): Unit = {
    val p = new Population(nThreads, nSubpops, subpopSize, nGen)
    var mon: ProgressMonitor = null
    if (verbose) {
      p.printSnapshot(0)
      mon = new ProgressMonitor(p)
      mon.start()
    }
    val startTime = System.nanoTime()
    p.start()
    p.awaitDone()
    val stopTime = System.nanoTime()
    if (mon != null)
      mon.interrupt()
    p.shutdown()
    //        Thread.sleep(100); // JSR-166 original commenting out sleep()

    val elapsed = stopTime - startTime
    val secs = elapsed / 1000000000.0
    p.printSnapshot(secs)
  }

  /*
   * A Population creates the subpops, subpops, and threads for a run
   * and has control methods to start, stop, and report progress.
   */
  private final class Population(
      nThreads: Int,
      nSubpops: Int,
      val subpopSize: Int,
      nGen: Int
  ) {
    final val threads = new Array[Worker](nThreads)
    final val subpops = new Array[Subpop](nSubpops)
    final val exchanger = new Exchanger[Strand]
    final val done = new CountDownLatch(nThreads - 1)

    for (i <- 0 until nSubpops)
      subpops(i) = new Subpop(this)

    val maxExchanges = nGen * nSubpops / nThreads
    for (i <- 0 until nThreads)
      threads(i) = new Worker(this, maxExchanges)

    def start(): Unit = {
      for (i <- 0 until nThreads)
        threads(i).start()
    }

    /* Stop the tasks */
    def shutdown(): Unit = {
      for (i <- 0 until threads.length)
        threads(i).interrupt()
    }

    def threadDone(): Unit =
      done.countDown()

    /* Wait for tasks to complete */
    def awaitDone(): Unit =
      done.await()

    def totalExchanges(): Int = {
      var xs = 0
      for (i <- 0 until threads.length)
        xs += threads(i).exchanges
      xs
    }

    /*
     * Prints statistics, including best and worst tour lengths
     * for points scaled in [0,1), scaled by the square root of
     * number of points. This simplifies checking results.  The
     * expected optimal TSP for random points is believed to be
     * around 0.76 * sqrt(N). For papers discussing this, see
     * http://www.densis.fee.unicamp.br/~moscato/TSPBIB_home.html
     */
    def printSnapshot(secs: Double): Unit = {
      val xs = totalExchanges()
      val rate =
        if (xs == 0) 0L
        else ((secs * 1000000000.0) / xs).toLong

      var bestc = subpops(0).chromosomes(0)
      var worstc = bestc

      for (k <- 0 until subpops.length) {
        val cs = subpops(k).chromosomes
        if (cs(0).fitness < bestc.fitness)
          bestc = cs(0)
        // SN: val 'w' is in .java original but apparently unused.
        // val w = cs(cs.length - 1).fitness;
        if (cs(cs.length - 1).fitness > worstc.fitness)
          worstc = cs(cs.length - 1)
      }

      val sqrtn = Math.sqrt(cities.length)
      val best = bestc.unitTourLength() / sqrtn
      val worst = worstc.unitTourLength() / sqrtn

      // jl.mumble explicit conversions are required by Scala 2.12 CI
      java.lang.System.out.printf(
        "N:%4d T:%8.3f B:%6.3f W:%6.3f X:%9d R:%7d\n",
        jl.Integer.valueOf(nThreads),
        jl.Double.valueOf(secs),
        jl.Double.valueOf(best),
        jl.Double.valueOf(worst),
        jl.Integer.valueOf(xs),
        jl.Long.valueOf(rate)
      )
      // JSR-166 orignal comments
      //            exchanger.printStats();
      //            System.out.print(" s: " + exchanger.aveSpins());
      //            System.out.print(" p: " + exchanger.aveParks());

    }
  }

  /*
   * Worker threads perform updates on subpops.
   */
  private final class Worker(pop: Population, maxExchanges: Int)
      extends Thread {

    var exchanges = 0
    final val rng = new RNG()

    /*
     * Repeatedly, find a subpop that is not being updated by
     * another thread, and run a random number of updates on it.
     */
    override def run(): Unit = {
      try {
        val len = pop.subpops.length
        var pos = (rng.next() & 0x7fffffff) % len
        while (exchanges < maxExchanges) {
          val s = pop.subpops(pos)
          val busy = s.busy
          if (!busy.get() && busy.compareAndSet(false, true)) {
            exchanges += s.runUpdates()
            busy.set(false)
            pos = (rng.next() & 0x7fffffff) % len
          } else if ({ pos += 1; pos } >= len)
            pos = 0
        }
        pop.threadDone()
      } catch {
        case e: InterruptedException =>
      }
    }
  }

  /*
   * A Subpop maintains a set of chromosomes.
   */
  private final class Subpop(pop: Population) {
    /* pop: The parent population */

    /* Reservation bit for worker threads */
    final val busy = new AtomicBoolean(false)

    /* The common exchanger, same for all subpops */
    final val exchanger = pop.exchanger

    private val length = cities.length

    /* The current strand being exchanged */
    var strand = new Strand(length)

    /* Bitset used in cross */
    final val inTour = new Array[Int]((length >>> 5) + 1)

    final val rng = new RNG()

    final val subpopSize = pop.subpopSize

    /* The chromosomes, kept in sorted order */
    final val chromosomes = new Array[Chromosome](subpopSize)

    for (j <- 0 until subpopSize)
      chromosomes(j) = new Chromosome(length, rng)

    Arrays.sort(chromosomes.asInstanceOf[Array[Object]])

    /*
     * Run a random number of updates.  The number of updates is
     * at least 1 and no more than subpopSize.  This
     * controls the granularity of multiplexing subpop updates on
     * to threads. It is small enough to balance out updates
     * across tasks, but large enough to avoid having runs
     * dominated by subpop selection. It is randomized to avoid
     * long runs where pairs of subpops exchange only with each
     * other.  It is hardwired because small variations of it
     * don't matter much.
     *
     * @param g the first generation to run
     */

    def runUpdates(): Int = {
      val n = 1 + (rng.next() & ((subpopSize << 1) - 1))

      for (i <- 0 until n)
        update()

      n
    }

    /*
     * Chooses a breeder, exchanges strand with another subpop, and
     * crosses them to create new chromosome to replace a chosen
     * dyer.
     */
    def update(): Unit = {
      val b = chooseBreeder()
      val d = chooseDyer(b)
      val breeder = chromosomes(b)
      val child = chromosomes(d)

      chooseStrand(breeder)
      strand = exchanger.exchange(strand)
      cross(breeder, child)
      fixOrder(child, d)
    }

    /*
     * Chooses a breeder, with exponentially decreasing probability
     * starting at best.
     * @return index of selected breeder
     */
    def chooseBreeder(): Int = {
      val mask = (subpopSize >>> BREEDER_DECAY) - 1
      var b = 0
      while ((rng.next() & mask) != mask) {
        if ({ b += 1; b } >= subpopSize)
          b = 0
      }

      b
    }

    /*
     * Chooses a chromosome that will be replaced, with
     * exponentially decreasing probability starting at
     * worst, ignoring the excluded index.
     * @param exclude index to ignore; use -1 to not exclude any
     * @return index of selected dyer
     */
    def chooseDyer(exclude: Int): Int = {
      val mask = (subpopSize >>> DYER_DECAY) - 1
      var d = subpopSize - 1
      while (d == exclude || (rng.next() & mask) != mask) {
        if ({ d -= 1; d } < 0)
          d = subpopSize - 1
      }

      d
    }

    /*
     * Select a random strand of b's.
     * @param breeder the breeder
     */

    def chooseStrand(breeder: Chromosome): Unit = {
      val bs = breeder.alleles
      val length = bs.length
      var strandLength = MIN_STRAND_LENGTH
      while (strandLength < length &&
          (rng.next() & RANDOM_STRAND_MASK) != RANDOM_STRAND_MASK)
        strandLength += 1

      strand.strandLength = strandLength
      val ss = strand.alleles
      var k = (rng.next() & 0x7fffffff) % length

      for (i <- 0 until strandLength) {
        ss(i) = bs(k)
        if ({ k += 1; k } >= length) k = 0
      }
    }

    /*
     * Copies current strand to start of c's, and then appends all
     * remaining b's that aren't in the strand.
     * @param breeder the breeder
     * @param child the child
     */

    def cross(breeder: Chromosome, child: Chromosome): Unit = {

      for (k <- 0 until inTour.length) // clear bitset
        inTour(k) = 0

      // Copy current strand to c
      val cs = child.alleles
      val ssize = strand.strandLength
      val ss = strand.alleles
      var i = 0

      for (idx <- 0 until ssize) {
        val x = ss(idx)
        cs(idx) = x
        inTour(x >>> 5) |= 1 << (x & 31) // record in bit set
        i += 1
      }

      // Find index of matching origin in b
      val first = cs(0)
      var j = 0
      val bs = breeder.alleles

      while (bs(j) != first)
        j += 1

      // Append remaining b's that aren't already in tour
      while (i < cs.length) {
        if ({ j += 1; j } >= bs.length) j = 0
        val x = bs(j)
        if ((inTour(x >>> 5) & (1 << (x & 31))) == 0) {
          cs(i) = x
          i += 1
        }
      }
    }

    /*
     * Fixes the sort order of a changed Chromosome c at position k.
     * @param c the chromosome
     * @param k the index
     */
    def fixOrder(c: Chromosome, k: Int): Unit = {
      val cs = chromosomes
      val oldFitness = c.fitness

      c.recalcFitness()

      val newFitness = c.fitness

      if (newFitness < oldFitness) {
        var j = k
        var p = j - 1
        while (p >= 0 && cs(p).fitness > newFitness) {
          cs(j) = cs(p)
          j = p
          p -= 1
        }
        cs(j) = c
      } else if (newFitness > oldFitness) {
        var j = k
        var n = j + 1
        while (n < cs.length && cs(n).fitness < newFitness) {
          cs(j) = cs(n)
          j = n
          n += 1
        }
        cs(j) = c
      }
    }
  }

  /*
   * A Chromosome is a candidate TSP tour.
   */
  private final class Chromosome(length: Int, random: RNG)
      extends Comparable[Chromosome] {

    /* Index of cities in tour order */
    val alleles = new Array[Int](length)

    /* Total tour length */
    var fitness = 0

    /*
     * Initializes to random tour.
     */
    for (i <- 0 until length)
      alleles(i) = i

    for (i <- (length - 1) until 0 by -1) {
      val idx = (random.next() & 0x7fffffff) % alleles.length
      val tmp = alleles(i)
      alleles(i) = alleles(idx)
      alleles(idx) = tmp
    }

    recalcFitness()

    def compareTo(x: Chromosome): Int = { // to enable sorting
      val xf = x.fitness
      val f = fitness
      if (f == xf) 0
      else if (f < xf) -1
      else 1
    }

    def recalcFitness(): Unit = {
      val a = alleles
      val len = a.length
      var p = a(0)
      var f: Long = cities.distanceBetween(a(len - 1), p) // Avoid Int overflow

      for (i <- 1 until len) {
        val n = a(i)
        // SN: be stricter than JSR-166 about detecting overflow.
        f = Math.addExact(f, cities.distanceBetween(p, n))
        p = n
      }

      fitness = (f / len).toInt
    }

    /*
     * Returns tour length for points scaled in [0, 1).
     */
    def unitTourLength(): Double = {
      val a = alleles
      val len = a.length
      var p = a(0)
      var f = cities.unitDistanceBetween(a(len - 1), p)

      for (i <- 1 until len) {
        val n = a(i)
        f += cities.unitDistanceBetween(p, n)
        p = n
      }

      f
    }

    /*
     * Checks that this tour visits each city.
     */
    def validate(): Unit = {
      val len = alleles.length
      val used = new Array[Boolean](len)

      for (i <- 0 until len)
        used(alleles(i)) = true

      for (i <- 0 until len)
        if (!used(i))
          throw new Error("Bad tour")
    }
  }

  /*
   * A Strand is a random sub-sequence of a Chromosome.  Each subpop
   * creates only one strand, and then trades it with others,
   * refilling it on each iteration.
   */
  private final class Strand(length: Int) {
    final val alleles = new Array[Int](length)
    var strandLength = 0
  }

  /*
   * A collection of (x,y) points that represent cities.
   */
  private final class CitySet(n: Int) {
    final val length = n
    final val xPts = new Array[Int](n)
    final val yPts = new Array[Int](n)
    final val distances = Array.ofDim[Int](n, n)

    val random = new RNG()

    for (i <- 0 until n) {
      xPts(i) = (random.next() & 0x7fffffff)
      yPts(i) = (random.next() & 0x7fffffff)
    }

    for (i <- 0 until n) {
      for (j <- 0 until n) {
        val dx = xPts(i).toDouble - xPts(j).toDouble
        val dy = yPts(i).toDouble - yPts(j).toDouble
        val dd = Math.hypot(dx, dy) / 2.0
        val ld = Math.round(dd)
        distances(i)(j) =
          if (ld >= Integer.MAX_VALUE) Integer.MAX_VALUE
          else ld.toInt
      }
    }

    /*
     * Returns the cached distance between a pair of cities.
     */
    def distanceBetween(i: Int, j: Int): Int =
      distances(i)(j)

    // Scale ints to doubles in [0,1)
    private final val PSCALE = 0x80000000L.toDouble

    /*
     * Returns distance for points scaled in [0,1). This simplifies
     * checking results.  The expected optimal TSP for random
     * points is believed to be around 0.76 * sqrt(N). For papers
     * discussing this, see
     * http://www.densis.fee.unicamp.br/~moscato/TSPBIB_home.html
     */
    def unitDistanceBetween(i: Int, j: Int): Double = {
      val dx = (xPts(i).toDouble - xPts(j).toDouble) / PSCALE
      val dy = (yPts(i).toDouble - yPts(j).toDouble) / PSCALE
      Math.hypot(dx, dy)
    }
  }

  /*
   * Cheap XorShift random number generator
   */
  private final class RNG(private var seed: Int) {
    /* Seed generator for XorShift RNGs */
    def this() = this((new Random()).nextInt | 1)

    def next(): Int = {
      var x = seed
      x ^= x << 6
      x ^= x >>> 21
      x ^= x << 7
      seed = x
      x
    }
  }

  private final class ProgressMonitor(pop: Population) extends Thread {
    override def run(): Unit = {
      var time = 0.0
      try {
        while (!Thread.interrupted()) {
          Thread.sleep(SNAPSHOT_RATE)
          time += SNAPSHOT_RATE
          pop.printSnapshot(time / 1000.0)
        }
      } catch {
        case e: InterruptedException => {}
      }
    }
  }
}
