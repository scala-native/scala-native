package java.util

object RandomSuite extends tests.Suite {

  /** Helper class to access next */
  class HackRandom(seed: Long) extends Random(seed) {
    override def next(bits: Int): Int = super.next(bits)
  }

  test("seed 10") {
    val random = new HackRandom(10)

    assert(random.next(10) == 747)
    assert(random.next(1) == 0)
    assert(random.next(6) == 16)
    assert(random.next(20) == 432970)
    assert(random.next(32) == 254270492)
  }

  test("seed -5") {
    val random = new HackRandom(-5)

    assert(random.next(10) == 275)
    assert(random.next(1) == 0)
    assert(random.next(6) == 21)
    assert(random.next(20) == 360349)
    assert(random.next(32) == 1635930704)
  }

  test("seed max long") {
    val random = new HackRandom(Long.MaxValue)

    assert(random.next(10) == 275)
    assert(random.next(1) == 0)
    assert(random.next(6) == 0)
    assert(random.next(20) == 574655)
    assert(random.next(32) == -1451336087)
  }

  test("seed max int") {
    val random = new HackRandom(Int.MinValue)

    assert(random.next(10) == 388)
    assert(random.next(1) == 0)
    assert(random.next(6) == 25)
    assert(random.next(20) == 352095)
    assert(random.next(32) == -2140124682)
  }

  test("seed reset") {
    val random = new HackRandom(11)
    assert(random.next(10) == 747)
    assert(random.next(1) == 1)
    assert(random.next(6) == 27)

    random.setSeed(11)
    assert(random.next(10) == 747)
    assert(random.next(1) == 1)
    assert(random.next(6) == 27)
  }

  test("reset nextGaussian") {
    val random = new Random(-1)
    assert(random.nextGaussian() == 1.7853314409882288)
    random.setSeed(-1)
    assert(random.nextGaussian() == 1.7853314409882288)
  }

  test("nextDouble") {
    val random = new Random(-45)
    assert(random.nextDouble() == 0.27288421395636253)
    assert(random.nextDouble() == 0.5523165360074201)
    assert(random.nextDouble() == 0.5689979434708298)
    assert(random.nextDouble() == 0.9961166166874871)
    assert(random.nextDouble() == 0.5368984665202684)
    assert(random.nextDouble() == 0.19849067496547423)
    assert(random.nextDouble() == 0.6021019223595357)
    assert(random.nextDouble() == 0.06132131151816378)
    assert(random.nextDouble() == 0.7303867762743866)
    assert(random.nextDouble() == 0.7426529384056163)
  }

  test("nextBoolean") {
    val random = new Random(4782934)
    assert(random.nextBoolean() == false)
    assert(random.nextBoolean() == true)
    assert(random.nextBoolean() == true)
    assert(random.nextBoolean() == false)
    assert(random.nextBoolean() == false)
    assert(random.nextBoolean() == false)
    assert(random.nextBoolean() == true)
    assert(random.nextBoolean() == false)
  }

  test("nextInt") {
    val random = new Random(-84638)
    assert(random.nextInt() == -1217585344)
    assert(random.nextInt() == 1665699216)
    assert(random.nextInt() == 382013296)
    assert(random.nextInt() == 1604432482)
    assert(random.nextInt() == -1689010196)
    assert(random.nextInt() == 1743354032)
    assert(random.nextInt() == 454046816)
    assert(random.nextInt() == 922172344)
    assert(random.nextInt() == -1890515287)
    assert(random.nextInt() == 1397525728)
  }

  test("nextIntN") {
    val random = new Random(7)
    assert(random.nextInt(76543) == 32736)
    assert {
      try {
        random.nextInt(0)
        false
      } catch {
        case _: Throwable => true
      }
    }
    assert(random.nextInt(45) == 29)
    assert(random.nextInt(945) == 60)
    assert(random.nextInt(35694839) == 20678044)
    assert(random.nextInt(35699) == 23932)
    assert(random.nextInt(3699) == 2278)
    assert(random.nextInt(10) == 8)
  }

  test("nextInt2Pow") {
    val random = new Random(-56938)

    assert(random.nextInt(32) == 8)
    assert(random.nextInt(8) == 3)
    assert(random.nextInt(128) == 3)
    assert(random.nextInt(4096) == 1950)
    assert(random.nextInt(8192) == 3706)
    assert(random.nextInt(8192) == 4308)
    assert(random.nextInt(8192) == 3235)
    assert(random.nextInt(8192) == 7077)
    assert(random.nextInt(8192) == 2392)
    assert(random.nextInt(32) == 31)
  }

  test("nextLong") {
    val random = new Random(205620432625028L)
    assert(random.nextLong() == 3710537363280377478L)
    assert(random.nextLong() == 4121778334981170700L)
    assert(random.nextLong() == 289540773990891960L)
    assert(random.nextLong() == 307008980197674441L)
    assert(random.nextLong() == 7527069864796025013L)
    assert(random.nextLong() == -4563192874520002144L)
    assert(random.nextLong() == 7619507045427546529L)
    assert(random.nextLong() == -7888117030898487184L)
    assert(random.nextLong() == -3499168703537933266L)
    assert(random.nextLong() == -1998975913933474L)
  }

  test("nextFloat") {
    val random = new Random(-3920005825473L)

    def closeTo(num: Float, exp: Double): Boolean =
      ((num < (exp + 0.0000001)) && (num > (exp - 0.0000001)))

    assert(closeTo(random.nextFloat(), 0.059591234))
    assert(closeTo(random.nextFloat(), 0.7007871))
    assert(closeTo(random.nextFloat(), 0.39173192))
    assert(closeTo(random.nextFloat(), 0.0647918))
    assert(closeTo(random.nextFloat(), 0.9029677))
    assert(closeTo(random.nextFloat(), 0.18226051))
    assert(closeTo(random.nextFloat(), 0.94444054))
    assert(closeTo(random.nextFloat(), 0.008844078))
    assert(closeTo(random.nextFloat(), 0.08891684))
    assert(closeTo(random.nextFloat(), 0.06482434))
  }

  test("nextBytes") {
    val random = new Random(7399572013373333L)

    def test(exps: Array[Int]) = {
      val exp = exps.map(_.toByte)
      val buf = new Array[Byte](exp.length)
      random.nextBytes(buf)
      var i   = 0
      var res = true
      assert {
        while (i < buf.size && res == true) {
          res = (buf(i) == exp(i))
          i += 1
        }
        res
      }
    }

    test(Array[Int](62, 89, 68, -91, 10, 0, 85))
    test(
      Array[Int](-89, -76, 88, 121, -25, 47, 58, -8, 78, 20, -77, 84, -3, -33,
        58, -9, 11, 57, -118, 40, -74, -86, 78, 123, 58))
    test(Array[Int](-77, 112, -116))
    test(Array[Int]())
    test(Array[Int](-84, -96, 108))
    test(Array[Int](57, -106, 42, -100, -47, -84, 67, -48, 45))
  }

  test("nextGaussian") {
    val random = new Random(2446004)
    assert(random.nextGaussian() == -0.5043346938630431)
    assert(random.nextGaussian() == -0.3250983270156675)
    assert(random.nextGaussian() == -0.23799457294994966)
    assert(random.nextGaussian() == 0.4164610631507695)
    assert(random.nextGaussian() == 0.22086348814760687)
    assert(random.nextGaussian() == -0.706833209972521)
    assert(random.nextGaussian() == 0.6730758289772553)
    assert(random.nextGaussian() == 0.2797393696191283)
    assert(random.nextGaussian() == -0.2979099632667685)
    assert(random.nextGaussian() == 0.37443415981434314)
    assert(random.nextGaussian() == 0.9584801742918951)
    assert(random.nextGaussian() == 1.1762179112229345)
    assert(random.nextGaussian() == 0.8736960092848826)
    assert(random.nextGaussian() == 0.12301554931271008)
    assert(random.nextGaussian() == -0.6052081187207353)
    assert(random.nextGaussian() == -0.2015925608755316)
    assert(random.nextGaussian() == -1.0071216119742104)
    assert(random.nextGaussian() == 0.6734222041441913)
    assert(random.nextGaussian() == 0.3990565555091522)
    assert(random.nextGaussian() == 2.0051627385915154)
  }

  test("default seed") {
    // added for #849
    val random1 = new Random()
    val random2 = new Random()
    assert(random1.hashCode != random2.hashCode)
    assert(random1.nextInt != random2.nextInt)
  }
}
