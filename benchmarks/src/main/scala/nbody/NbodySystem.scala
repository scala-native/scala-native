/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package nbody

class NBodySystem {
  private[this] val bodies: Array[Body] = createBodies()

  def createBodies(): Array[Body] = {
    val bodies = Array(Body.sun(),
                       Body.jupiter(),
                       Body.saturn(),
                       Body.uranus(),
                       Body.neptune())

    var px: Double = 0.0
    var py: Double = 0.0
    var pz: Double = 0.0

    bodies.foreach { b =>
      px += b.getVX() * b.getMass()
      py += b.getVY() * b.getMass()
      pz += b.getVZ() * b.getMass()
    }

    bodies(0).offsetMomentum(px, py, pz)

    bodies
  }

  def advance(dt: Double): Unit = {
    var i = 0
    while (i < bodies.length) {
      var iBody = bodies(i)

      var j = i + 1
      while (j < bodies.length) {
        var jBody = bodies(j)

        val dx: Double = iBody.getX() - jBody.getX()
        val dy: Double = iBody.getY() - jBody.getY()
        val dz: Double = iBody.getZ() - jBody.getZ()

        val dSquared: Double = dx * dx + dy * dy + dz * dz
        val distance: Double = Math.sqrt(dSquared)
        val mag: Double      = dt / (dSquared * distance)

        iBody.setVX(iBody.getVX() - (dx * jBody.getMass() * mag))
        iBody.setVY(iBody.getVY() - (dy * jBody.getMass() * mag))
        iBody.setVZ(iBody.getVZ() - (dz * jBody.getMass() * mag))

        jBody.setVX(jBody.getVX() + (dx * iBody.getMass() * mag))
        jBody.setVY(jBody.getVY() + (dy * iBody.getMass() * mag))
        jBody.setVZ(jBody.getVZ() + (dz * iBody.getMass() * mag))

        j += 1
      }

      i += 1
    }

    bodies.foreach { body =>
      body.setX(body.getX() + dt * body.getVX())
      body.setY(body.getY() + dt * body.getVY())
      body.setZ(body.getZ() + dt * body.getVZ())
    }
  }

  def energy(): Double = {
    var dx: Double       = 0.0
    var dy: Double       = 0.0
    var dz: Double       = 0.0
    var distance: Double = 0.0
    var e: Double        = 0.0

    var i = 0
    while (i < bodies.length) {
      val iBody = bodies(i)
      e += (0.5 * iBody.getMass()
        * (iBody.getVX() * iBody.getVX() +
          iBody.getVY() * iBody.getVY() +
          iBody.getVZ() * iBody.getVZ()))

      var j = i + 1
      while (j < bodies.length) {
        val jBody = bodies(j)
        dx = iBody.getX() - jBody.getX()
        dy = iBody.getY() - jBody.getY()
        dz = iBody.getZ() - jBody.getZ()

        distance = Math.sqrt(dx * dx + dy * dy + dz * dz)
        e -= (iBody.getMass() * jBody.getMass()) / distance

        j += 1
      }

      i += 1
    }

    e
  }
}
