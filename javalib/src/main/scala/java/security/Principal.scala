package java.security

import javax.security.auth.Subject

trait Principal {
  def getName(): String
  def implies(subject: Subject): Boolean =
    subject != null && subject.getPrincipals().contains(this)
}
