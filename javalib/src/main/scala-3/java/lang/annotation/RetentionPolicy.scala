// Enums are not source-compatbile, make sure to sync this file with Scala 2 implementation

package java.lang.annotation

enum RetentionPolicy(name: String, ordinal: Int):
  case SOURCE extends RetentionPolicy("SOURCE", 0)
  case CLASS extends RetentionPolicy("CLASS", 1)
  case RUNTIME extends RetentionPolicy("RUNTIME", 2)
