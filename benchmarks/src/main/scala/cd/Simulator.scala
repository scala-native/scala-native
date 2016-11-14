package cd

import som._

final class Simulator(numAircraft: Int) {
  val aircraft = new Vector[CallSign]();
  (0 until numAircraft).foreach { i =>
    aircraft.append(new CallSign(i))
  }

  def simulate(time: Double): Vector[Aircraft] = {
    val frame = new Vector[Aircraft]();
    (0 until aircraft.size() by 2).foreach { i =>
      frame.append(
        new Aircraft(aircraft.at(i),
                     new Vector3D(time, Math.cos(time) * 2 + i * 3, 10)))
      frame.append(
        new Aircraft(aircraft.at(i + 1),
                     new Vector3D(time, Math.sin(time) * 2 + i * 3, 10)))
    }
    frame
  }
}
