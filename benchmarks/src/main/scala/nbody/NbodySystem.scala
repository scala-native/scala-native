/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package nbody

class NBodySystem {
  private val bodies = createBodies()

  def createBodies() = {
    val bodies = Array(Body.sun(),
                       Body.jupiter(),
                       Body.saturn(),
                       Body.uranus(),
                       Body.neptune())

    var px = 0.0d
    var py = 0.0d
    var pz = 0.0d

    bodies.foreach { b =>
      px += b.getVX() * b.getMass()
      py += b.getVY() * b.getMass()
      pz += b.getVZ() * b.getMass()
    }

    bodies(0).offsetMomentum(px, py, pz)

    bodies
  }

  def advance(dt: Double): Unit = {
    (0 until bodies.length).foreach { i =>
      val iBody = bodies(i)

      ((i + 1) until bodies.length).foreach { j =>
        val jBody = bodies(j)
        val dx    = iBody.getX() - jBody.getX()
        val dy    = iBody.getY() - jBody.getY()
        val dz    = iBody.getZ() - jBody.getZ()

        val dSquared = dx * dx + dy * dy + dz * dz
        val distance = Math.sqrt(dSquared)
        val mag      = dt / (dSquared * distance)

        iBody.setVX(iBody.getVX() - (dx * jBody.getMass() * mag))
        iBody.setVY(iBody.getVY() - (dy * jBody.getMass() * mag))
        iBody.setVZ(iBody.getVZ() - (dz * jBody.getMass() * mag))

        jBody.setVX(jBody.getVX() + (dx * iBody.getMass() * mag))
        jBody.setVY(jBody.getVY() + (dy * iBody.getMass() * mag))
        jBody.setVZ(jBody.getVZ() + (dz * iBody.getMass() * mag))
      }
    }

    bodies.foreach { body =>
      body.setX(body.getX() + dt * body.getVX())
      body.setY(body.getY() + dt * body.getVY())
      body.setZ(body.getZ() + dt * body.getVZ())
    }
  }

  def energy(): Double = {
    var dx       = 0.0d
    var dy       = 0.0d
    var dz       = 0.0d
    var distance = 0.0d
    var e        = 0.0

    (0 until bodies.length).foreach { i =>
      val iBody = bodies(i)
      e += (0.5 * iBody.getMass()
        * (iBody.getVX() * iBody.getVX() +
          iBody.getVY() * iBody.getVY() +
          iBody.getVZ() * iBody.getVZ()))

      ((i + 1) until bodies.length).foreach { j =>
        val jBody = bodies(j)
        dx = iBody.getX() - jBody.getX()
        dy = iBody.getY() - jBody.getY()
        dz = iBody.getZ() - jBody.getZ()

        distance = Math.sqrt(dx * dx + dy * dy + dz * dz)
        e -= (iBody.getMass() * jBody.getMass()) / distance
      }
    }

    e
  }
}
