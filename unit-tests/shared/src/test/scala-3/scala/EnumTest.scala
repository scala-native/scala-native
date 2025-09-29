package scala

import org.junit.Assert._
import org.junit.Test

private enum Command extends Enum[Command] { case Sit, Stay }
private enum Animal(val sound: String):
  case Dog extends Animal("woof")
  case Cat extends Animal("miau")
end Animal

class EnumTest:
  @Test def enumsFieldsCanBeAccessed(): Unit = {
    assertEquals(0, Animal.Dog.ordinal)
    assertEquals("Dog", Animal.Dog.toString)

    assertEquals(1, Animal.Cat.ordinal)
    assertEquals("Cat", Animal.Cat.toString)

    assertEquals(0, Command.Sit.ordinal)
    assertEquals(1, Command.Stay.ordinal)

    assertEquals("Sit", Command.Sit.name)
    assertEquals("Stay", Command.Stay.name)
  }

  @Test def enumsCanBeSummonedByName(): Unit = {
    assertEquals(Animal.Cat, Animal.valueOf("Cat"))
    assertEquals(Command.Stay, Command.valueOf("Stay"))
  }

  @Test def enumsCanBeSummonedByOrdinal(): Unit = {
    assertEquals(Animal.Dog, Animal.fromOrdinal(0))
    assertEquals(Animal.Cat, Animal.fromOrdinal(1))
    assertEquals(Command.Sit, Command.fromOrdinal(0))
    assertEquals(Command.Stay, Command.fromOrdinal(1))
  }

  @Test def valuesContainsAllCases(): Unit = {
    assertTrue(Animal.values.diff(Seq(Animal.Dog, Animal.Cat)).isEmpty)
    assertTrue(Command.values.diff(Seq(Command.Sit, Command.Stay)).isEmpty)
  }
