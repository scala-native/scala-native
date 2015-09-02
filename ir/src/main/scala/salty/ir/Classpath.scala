package salty.ir

abstract class Classpath {
  def resolve: Seq[(Name, String)]
}
