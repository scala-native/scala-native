package cd

import java.lang.Boolean.{TRUE, FALSE}
import som._

final class CollisionDetector {
  import CollisionDetector._

  private val state = new RedBlackTree[CallSign, Vector3D]()

  def handleNewFrame(frame: Vector[Aircraft]): Vector[Collision] = {
    val motions = new Vector[Motion]()
    val seen    = new RedBlackTree[CallSign, Boolean]()

    frame.forEach { aircraft =>
      var oldPosition = state.put(aircraft.callsign, aircraft.position)
      var newPosition = aircraft.position
      seen.put(aircraft.callsign, true)

      if (oldPosition == null) {
        // Treat newly introduced aircraft as if they were stationary.
        oldPosition = newPosition
      }

      motions.append(new Motion(aircraft.callsign, oldPosition, newPosition))
    }

    // Remove aircraft that are no longer present.
    val toRemove = new Vector[CallSign]();
    state.forEach { e =>
      if (!seen.get(e.key)) {
        toRemove.append(e.key);
      }
    }

    toRemove.forEach { e =>
      state.remove(e)
    }

    val allReduced = reduceCollisionSet(motions);
    val collisions = new Vector[Collision]();
    allReduced.forEach { reduced =>
      (0 until reduced.size()).foreach { i =>
        val motion1 = reduced.at(i)
        ((i + 1) until reduced.size()).foreach { j =>
          val motion2   = reduced.at(j)
          val collision = motion1.findIntersection(motion2)
          if (collision != null) {
            collisions.append(
              new Collision(motion1.callsign, motion2.callsign, collision))
          }
        }
      }
    }

    collisions
  }

}

object CollisionDetector {
  val horizontal = new Vector2D(Constants.GOOD_VOXEL_SIZE, 0.0);
  val vertical   = new Vector2D(0.0, Constants.GOOD_VOXEL_SIZE);

  def isInVoxel(voxel: Vector2D, motion: Motion): Boolean = {
    if (voxel.x > Constants.MAX_X ||
        voxel.x < Constants.MIN_X ||
        voxel.y > Constants.MAX_Y ||
        voxel.y < Constants.MIN_Y) {
      return false;
    }

    val init = motion.posOne
    val fin  = motion.posTwo

    val v_s = Constants.GOOD_VOXEL_SIZE;
    val r   = Constants.PROXIMITY_RADIUS / 2.0;

    val v_x = voxel.x
    val x0  = init.x
    val xv  = fin.x - init.x

    val v_y = voxel.y
    val y0  = init.y
    val yv  = fin.y - init.y

    var low_x  = 0.0d
    var high_x = 0.0d
    low_x = (v_x - r - x0) / xv
    high_x = (v_x + v_s + r - x0) / xv

    if (xv < 0.0) {
      val tmp = low_x
      low_x = high_x
      high_x = tmp
    }

    var low_y  = 0.0d
    var high_y = 0.0d
    low_y = (v_y - r - y0) / yv
    high_y = (v_y + v_s + r - y0) / yv

    if (yv < 0.0) {
      val tmp = low_y
      low_y = high_y
      high_y = tmp
    }

    (((xv == 0.0 && v_x <= x0 + r && x0 - r <= v_x + v_s) /* no motion in x */ ||
    (low_x <= 1.0 && 1.0 <= high_x) || (low_x <= 0.0 && 0.0 <= high_x) ||
    (0.0 <= low_x && high_x <= 1.0)) &&
    ((yv == 0.0 && v_y <= y0 + r && y0 - r <= v_y + v_s) /* no motion in y */ ||
    ((low_y <= 1.0 && 1.0 <= high_y) || (low_y <= 0.0 && 0.0 <= high_y) ||
    (0.0 <= low_y && high_y <= 1.0))) &&
    (xv == 0.0 || yv == 0.0 || /* no motion in x or y or both */
    (low_y <= high_x && high_x <= high_y) ||
    (low_y <= low_x && low_x <= high_y) ||
    (low_x <= low_y && high_y <= high_x)))
  }

  def putIntoMap(voxelMap: RedBlackTree[Vector2D, Vector[Motion]],
                 voxel: Vector2D,
                 motion: Motion): Unit = {
    var array = voxelMap.get(voxel)
    if (array == null) {
      array = new Vector()
      voxelMap.put(voxel, array)
    }
    array.append(motion)
  }

  def recurse(voxelMap: RedBlackTree[Vector2D, Vector[Motion]],
              seen: RedBlackTree[Vector2D, Boolean],
              nextVoxel: Vector2D,
              motion: Motion): Unit = {
    if (!isInVoxel(nextVoxel, motion)) {
      return
    }

    if (seen.put(nextVoxel, true) == TRUE) {
      return
    }

    putIntoMap(voxelMap, nextVoxel, motion)

    recurse(voxelMap, seen, nextVoxel.minus(horizontal), motion)
    recurse(voxelMap, seen, nextVoxel.plus(horizontal), motion)
    recurse(voxelMap, seen, nextVoxel.minus(vertical), motion)
    recurse(voxelMap, seen, nextVoxel.plus(vertical), motion)
    recurse(voxelMap,
            seen,
            nextVoxel.minus(horizontal).minus(vertical),
            motion)
    recurse(voxelMap, seen, nextVoxel.minus(horizontal).plus(vertical), motion)
    recurse(voxelMap, seen, nextVoxel.plus(horizontal).minus(vertical), motion)
    recurse(voxelMap, seen, nextVoxel.plus(horizontal).plus(vertical), motion)
  }

  def reduceCollisionSet(motions: Vector[Motion]): Vector[Vector[Motion]] = {
    val voxelMap = new RedBlackTree[Vector2D, Vector[Motion]]()
    motions.forEach { motion =>
      drawMotionOnVoxelMap(voxelMap, motion)
    }

    val result = new Vector[Vector[Motion]]();
    voxelMap.forEach { e =>
      if (e.value.size() > 1) {
        result.append(e.value)
      }
    }
    result
  }

  def voxelHash(position: Vector3D): Vector2D = {
    val xDiv = (position.x / Constants.GOOD_VOXEL_SIZE).toInt
    val yDiv = (position.y / Constants.GOOD_VOXEL_SIZE).toInt

    var x = Constants.GOOD_VOXEL_SIZE * xDiv;
    var y = Constants.GOOD_VOXEL_SIZE * yDiv;

    if (position.x < 0) {
      x -= Constants.GOOD_VOXEL_SIZE;
    }
    if (position.y < 0) {
      y -= Constants.GOOD_VOXEL_SIZE;
    }

    new Vector2D(x, y)
  }

  def drawMotionOnVoxelMap(voxelMap: RedBlackTree[Vector2D, Vector[Motion]],
                           motion: Motion): Unit = {
    val seen = new RedBlackTree[Vector2D, Boolean]()
    recurse(voxelMap, seen, voxelHash(motion.posOne), motion)
  }
}
