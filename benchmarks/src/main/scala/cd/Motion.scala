package cd

class Motion(val callsign: CallSign,
             val posOne: Vector3D,
             val posTwo: Vector3D) {
  def delta(): Vector3D =
    posTwo.minus(this.posOne);

  def findIntersection(other: Motion): Vector3D = {
    var init1  = this.posOne
    var init2  = other.posOne
    var vec1   = delta()
    var vec2   = other.delta()
    val radius = Constants.PROXIMITY_RADIUS

    // this test is not geometrical 3-d intersection test, it takes the fact that the aircraft move
    // into account ; so it is more like a 4d test
    // (it assumes that both of the aircraft have a constant speed over the tested interval)

    // we thus have two points, each of them moving on its line segment at constant speed ; we are looking
    // for times when the distance between these two points is smaller than r

    // vec1 is vector of aircraft 1
    // vec2 is vector of aircraft 2

    // a = (V2 - V1)^T * (V2 - V1)
    var a = vec2.minus(vec1).squaredMagnitude()

    if (a != 0.0) {
      // we are first looking for instances of time when the planes are exactly r from each other
      // at least one plane is moving ; if the planes are moving in parallel, they do not have constant speed

      // if the planes are moving in parallel, then
      //   if the faster starts behind the slower, we can have 2, 1, or 0 solutions
      //   if the faster plane starts in front of the slower, we can have 0 or 1 solutions

      // if the planes are not moving in parallel, then

      // point P1 = I1 + vV1
      // point P2 = I2 + vV2
      //   - looking for v, such that dist(P1,P2) = || P1 - P2 || = r

      // it follows that || P1 - P2 || = sqrt( < P1-P2, P1-P2 > )
      //   0 = -r^2 + < P1 - P2, P1 - P2 >
      //  from properties of dot product
      //   0 = -r^2 + <I1-I2,I1-I2> + v * 2<I1-I2, V1-V2> + v^2 *<V1-V2,V1-V2>
      //   so we calculate a, b, c - and solve the quadratic equation
      //   0 = c + bv + av^2

      // b = 2 * <I1-I2, V1-V2>
      var b = 2.0 * init1.minus(init2).dot(vec1.minus(vec2))

      // c = -r^2 + (I2 - I1)^T * (I2 - I1)
      var c = -radius * radius + init2.minus(init1).squaredMagnitude()

      var discr = b * b - 4.0 * a * c
      if (discr < 0.0) {
        return null
      }

      var v1 = (-b - Math.sqrt(discr)) / (2.0 * a)
      var v2 = (-b + Math.sqrt(discr)) / (2.0 * a)

      if (v1 <= v2 && ((v1 <= 1.0 && 1.0 <= v2) ||
          (v1 <= 0.0 && 0.0 <= v2) ||
          (0.0 <= v1 && v2 <= 1.0))) {
        // Pick a good "time" at which to report the collision.
        var v = 0.0d
        if (v1 <= 0.0) {
          // The collision started before this frame. Report it at the start of the frame.
          v = 0.0
        } else {
          // The collision started during this frame. Report it at that moment.
          v = v1
        }

        var result1 = init1.plus(vec1.times(v))
        var result2 = init2.plus(vec2.times(v))

        var result = result1.plus(result2).times(0.5)
        if (result.x >= Constants.MIN_X &&
            result.x <= Constants.MAX_X &&
            result.y >= Constants.MIN_Y &&
            result.y <= Constants.MAX_Y &&
            result.z >= Constants.MIN_Z &&
            result.z <= Constants.MAX_Z) {
          return result;
        }
      }

      return null
    }

    // the planes have the same speeds and are moving in parallel (or they are not moving at all)
    // they  thus have the same distance all the time ; we calculate it from the initial point

    // dist = || i2 - i1 || = sqrt(  ( i2 - i1 )^T * ( i2 - i1 ) )
    var dist = init2.minus(init1).magnitude()
    if (dist <= radius) {
      return init1.plus(init2).times(0.5)
    }

    return null;
  }
}
