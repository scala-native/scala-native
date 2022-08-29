package java.security

/** Fake implementation of `SecureRandom` that is not actually secure at all.
 *
 *  It directly delegates to `java.util.Random`.
 */
class SecureRandom extends java.util.Random
