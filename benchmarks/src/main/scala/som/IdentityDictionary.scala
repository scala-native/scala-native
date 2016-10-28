package som;

import som.Dictionary._

class IdentityDictionary[K <: CustomHash, V <: AnyRef](
    size: Int = INITIAL_CAPACITY)
    extends Dictionary[K, V](size) {

  class IdEntry[K, V](hash: Int, key: K, value: V, next: Entry[K, V])
      extends Entry[K, V](hash, key, value, next) {

    override def match_(hash: Int, key: K): Boolean =
      this.hash == hash && this.key == key;
  }

  override def newEntry(key: K, value: V, hash: Int): Entry[K, V] =
    new IdEntry(hash, key, value, null)
}
