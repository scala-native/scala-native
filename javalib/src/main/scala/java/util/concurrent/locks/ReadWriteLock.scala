package java.util.concurrent
package locks

trait ReadWriteLock {

  def readLock(): Lock

  def writeLock(): Lock

}
