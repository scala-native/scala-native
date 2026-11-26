package java.nio.file.attribute

import java.util.{HashSet, Objects, Set}

import scala.annotation.varargs

final class AclEntry private (
    _type: AclEntryType,
    _principal: UserPrincipal,
    _permissions: Set[AclEntryPermission],
    _flags: Set[AclEntryFlag]
) {
  def `type`(): AclEntryType = _type

  def principal(): UserPrincipal = _principal

  def permissions(): Set[AclEntryPermission] = new HashSet(_permissions)

  def flags(): Set[AclEntryFlag] = new HashSet(_flags)

  override def equals(other: Any): Boolean = other match {
    case that: AclEntry =>
      _type == that.`type`() &&
        _principal.equals(that.principal()) &&
        _permissions.equals(that.permissions()) &&
        _flags.equals(that.flags())
    case _ => false
  }

  override def hashCode(): Int =
    Objects.hash(Array[AnyRef](_type, _principal, _permissions, _flags))

  override def toString(): String = {
    val sb = new java.lang.StringBuilder(_principal.getName())
    sb.append(':')
    def appendSorted(all: Array[_ <: AnyRef], set: Set[_]): Unit = {
      if (!set.isEmpty()) {
        var first = true
        for (e <- all if set.contains(e)) {
          if (!first) sb.append('/')
          sb.append(e.toString())
          first = false
        }
        sb.append(':')
      }
    }
    appendSorted(AclEntryPermission.values(), _permissions)
    appendSorted(AclEntryFlag.values(), _flags)
    sb.append(_type.name())
    sb.toString()
  }
}

object AclEntry {
  def newBuilder(): Builder =
    new Builder(null, null, new HashSet(), new HashSet())

  def newBuilder(entry: AclEntry): Builder =
    new Builder(
      entry.`type`(),
      entry.principal(),
      entry.permissions(),
      entry.flags()
    )

  final class Builder private[AclEntry] (
      private var _type: AclEntryType,
      private var _principal: UserPrincipal,
      private var _permissions: Set[AclEntryPermission],
      private var _flags: Set[AclEntryFlag]
  ) {
    def build(): AclEntry = {
      if (_type == null)
        throw new IllegalStateException("Missing type component")
      if (_principal == null)
        throw new IllegalStateException("Missing who component")
      new AclEntry(_type, _principal, _permissions, _flags)
    }

    def setType(`type`: AclEntryType): Builder = {
      _type = Objects.requireNonNull(`type`)
      this
    }

    def setPrincipal(who: UserPrincipal): Builder = {
      _principal = Objects.requireNonNull(who)
      this
    }

    def setPermissions(perms: Set[AclEntryPermission]): Builder = {
      _permissions = checkedCopy(perms, classOf[AclEntryPermission])
      this
    }

    @varargs
    def setPermissions(perms: AclEntryPermission*): Builder = {
      _permissions = checkedCopy(perms, classOf[AclEntryPermission])
      this
    }

    def setFlags(flags: Set[AclEntryFlag]): Builder = {
      _flags = checkedCopy(flags, classOf[AclEntryFlag])
      this
    }

    @varargs
    def setFlags(flags: AclEntryFlag*): Builder = {
      _flags = checkedCopy(flags, classOf[AclEntryFlag])
      this
    }
  }

  private def checkedCopy[E](elems: Set[E], cls: Class[E]): Set[E] = {
    val copy = new HashSet[E]()
    val it = elems.iterator()
    while (it.hasNext()) copy.add(checked(it.next(), cls))
    copy
  }

  private def checkedCopy[E](elems: Seq[E], cls: Class[E]): Set[E] = {
    val copy = new HashSet[E]()
    elems.foreach(e => copy.add(checked(e, cls)))
    copy
  }

  private def checked[E](e: Any, cls: Class[E]): E = {
    Objects.requireNonNull(e)
    if (!cls.isInstance(e.asInstanceOf[AnyRef]))
      throw new ClassCastException(e.getClass().getName())
    e.asInstanceOf[E]
  }
}
