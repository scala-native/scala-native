package java.text

import java.util.Objects

class FieldPosition(private val attribute: Format.Field,
                    private val fieldID: Int) {
  def this(attribute: Format.Field) = this(attribute, -1)

  def this(field: Int) = this(null, field)

  def getFieldAttribute(): Format.Field = attribute

  def getField(): Int = fieldID

  private[this] var beginIndex: Int = _
  private[this] var endIndex: Int   = _

  def getBeginIndex(): Int = beginIndex

  def getEndIndex(): Int = endIndex

  def setBeginIndex(bi: Int): Unit = beginIndex = bi

  def setEndIndex(ei: Int): Unit = endIndex = ei

  override def equals(obj: Any): Boolean =
    obj match {
      case that: FieldPosition =>
        this.getFieldAttribute() == that.getFieldAttribute() &&
          this.getField() == that.getField() &&
          this.getBeginIndex() == that.getBeginIndex() &&
          this.getEndIndex() == that.getEndIndex()
      case _ => false
    }

  override def hashCode(): Int =
    Objects.hash(
      Array(
        this.getFieldAttribute(),
        this.getField().asInstanceOf[Object],
        this.getBeginIndex().asInstanceOf[Object],
        this.getEndIndex().asInstanceOf[Object]
      ))

  override def toString(): String =
    s"java.text.FieldPosition[field=${getField()},attribute=${getFieldAttribute()},beginIndex=${getBeginIndex()},endIndex=${getEndIndex()}]"
}
