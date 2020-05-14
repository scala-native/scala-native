package scala.scalanative
package testinterface
package serialization

final class RemoteException(msg: String,
                            _toString: String,
                            cause: Throwable,
                            val originalClass: String)
    extends Exception(msg, cause) {
  override def toString: String = _toString
}
