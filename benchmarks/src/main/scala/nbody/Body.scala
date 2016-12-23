/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package nbody

final class Body private () {
  private[this] var x: Double    = 0.0
  private[this] var y: Double    = 0.0
  private[this] var z: Double    = 0.0
  private[this] var vx: Double   = 0.0
  private[this] var vy: Double   = 0.0
  private[this] var vz: Double   = 0.0
  private[this] var mass: Double = 0.0

  def getX(): Double    = x
  def getY(): Double    = y
  def getZ(): Double    = z
  def getVX(): Double   = vx
  def getVY(): Double   = vy
  def getVZ(): Double   = vz
  def getMass(): Double = mass

  def setX(x: Double): Unit   = { this.x = x }
  def setY(y: Double): Unit   = { this.y = y }
  def setZ(z: Double): Unit   = { this.z = z }
  def setVX(vx: Double): Unit = { this.vx = vx }
  def setVY(vy: Double): Unit = { this.vy = vy }
  def setVZ(vz: Double): Unit = { this.vz = vz }

  def offsetMomentum(px: Double, py: Double, pz: Double): Unit = {
    vx = 0.0 - (px / Body.SOLAR_MASS)
    vy = 0.0 - (py / Body.SOLAR_MASS)
    vz = 0.0 - (pz / Body.SOLAR_MASS)
  }

  def this(x: Double,
           y: Double,
           z: Double,
           vx: Double,
           vy: Double,
           vz: Double,
           mass: Double) {
    this()
    this.x = x
    this.y = y
    this.z = z
    this.vx = vx * Body.DAYS_PER_YER
    this.vy = vy * Body.DAYS_PER_YER
    this.vz = vz * Body.DAYS_PER_YER
    this.mass = mass * Body.SOLAR_MASS
  }
}

object Body {
  private final val PI           = 3.141592653589793
  private final val SOLAR_MASS   = 4 * PI * PI
  private final val DAYS_PER_YER = 365.24

  def jupiter() =
    new Body(4.84143144246472090e+00, -1.16032004402742839e+00,
      -1.03622044471123109e-01, 1.66007664274403694e-03,
      7.69901118419740425e-03, -6.90460016972063023e-05,
      9.54791938424326609e-04)

  def saturn() =
    new Body(8.34336671824457987e+00, 4.12479856412430479e+00,
      -4.03523417114321381e-01, -2.76742510726862411e-03,
      4.99852801234917238e-03, 2.30417297573763929e-05,
      2.85885980666130812e-04)

  def uranus() =
    new Body(1.28943695621391310e+01, -1.51111514016986312e+01,
      -2.23307578892655734e-01, 2.96460137564761618e-03,
      2.37847173959480950e-03, -2.96589568540237556e-05,
      4.36624404335156298e-05)

  def neptune() =
    new Body(1.53796971148509165e+01, -2.59193146099879641e+01,
      1.79258772950371181e-01, 2.68067772490389322e-03,
      1.62824170038242295e-03, -9.51592254519715870e-05,
      5.15138902046611451e-05)

  def sun(): Body =
    new Body(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0)
}
