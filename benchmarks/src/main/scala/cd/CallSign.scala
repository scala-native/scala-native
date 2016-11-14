package cd

final class CallSign(val value: Int) extends Comparable[CallSign] {
  override def compareTo(other: CallSign) =
    if (value == other.value) 0
    else if (value < other.value) -1
    else 1
}
