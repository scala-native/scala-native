package scala.scalanative.runtime

/** Method-invocation conversions for the non-exact VarHandle access path.
 *  Signature checks precede argument conversion and field mutation.
 */
private[runtime] object VarHandleConversions {
  private val types = List[(Class[_], Class[_])](
    classOf[Boolean] -> classOf[java.lang.Boolean],
    classOf[Byte] -> classOf[java.lang.Byte],
    classOf[Short] -> classOf[java.lang.Short],
    classOf[Char] -> classOf[java.lang.Character],
    classOf[Int] -> classOf[java.lang.Integer],
    classOf[Long] -> classOf[java.lang.Long],
    classOf[Float] -> classOf[java.lang.Float],
    classOf[Double] -> classOf[java.lang.Double]
  )
  private val primitives = types.map(_._1)
  private val wrappers = types.map(_._2)
  private val numeric = List[Class[_]](
    classOf[Byte],
    classOf[Short],
    classOf[Int],
    classOf[Long],
    classOf[Float],
    classOf[Double]
  )

  private def wrapper(tpe: Class[_]): Class[_] = wrappers(
    primitives.indexOf(tpe)
  )
  private def widens(source: Class[_], target: Class[_]): Boolean = {
    if (source == target) true
    else if (source == classOf[Char])
      numeric.indexOf(target) >= numeric.indexOf(classOf[Int])
    else {
      val from = numeric.indexOf(source)
      val to = numeric.indexOf(target)
      from >= 0 && to > from
    }
  }

  def canConvert(source: Class[_], target: Class[_]): Boolean = {
    if (source == target) return true
    if (source.isPrimitive) {
      if (target.isPrimitive) widens(source, target)
      else target.isAssignableFrom(wrapper(source))
    } else if (target.isPrimitive) {
      val exactWrapper = wrappers.indexOf(source)
      if (exactWrapper >= 0) widens(primitives(exactWrapper), target)
      else
        primitives.indices.exists(i =>
          source.isAssignableFrom(wrappers(i)) && widens(primitives(i), target)
        )
    } else true
  }

  def convert(value: AnyRef, source: Class[_], target: Class[_]): AnyRef = {
    // Exact static reference types are already checked by the call site;
    // exact primitive operands arrive boxed by the compiler protocol.
    if (source == target) return value

    if (!target.isPrimitive) {
      // Class.cast currently erases its type parameter on Native, so perform
      // the dynamic check explicitly rather than relying on its generic cast.
      if (value != null && !target.isInstance(value))
        throw new ClassCastException(
          s"cannot cast ${value.getClass.getName} to ${target.getName}"
        )
      return value
    }

    if (value == null) throw new NullPointerException("null VarHandle operand")

    val actual = wrappers.indexOf(value.getClass)
    if (actual < 0 || !widens(primitives(actual), target))
      throw new ClassCastException(
        s"cannot unbox ${value.getClass.getName} as ${target.getName}"
      )

    if (primitives(actual) == target) return value
    val number: java.lang.Number = value match {
      case character: java.lang.Character =>
        java.lang.Integer.valueOf(character.charValue().toInt)
      case number: java.lang.Number => number
    }
    if (target == classOf[Short]) java.lang.Short.valueOf(number.shortValue())
    else if (target == classOf[Int])
      java.lang.Integer.valueOf(number.intValue())
    else if (target == classOf[Long]) java.lang.Long.valueOf(number.longValue())
    else if (target == classOf[Float])
      java.lang.Float.valueOf(number.floatValue())
    else java.lang.Double.valueOf(number.doubleValue())
  }
}
