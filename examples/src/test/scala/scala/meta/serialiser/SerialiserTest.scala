package scala.meta.serialiser

import org.scalatest._

object TestEntities {
  @entity case class SimpleCaseClass(i: Int, s: String)
  @entity case class WithTypeParam[N <: Number](n: Number)
}

class SerialiserTest extends WordSpec with Matchers {
  import TestEntities._

  "simple case class" should {
    "serialise to Map" in {
      val testInstance = SimpleCaseClass(i = 42, s = "something")
      val keyValues = SimpleCaseClass.toMap(testInstance)
      keyValues should contain ("i" -> testInstance.i)
      keyValues should contain ("s" -> testInstance.s)
    }

    "deserialise from Map" in {
      val testInstance = SimpleCaseClass.fromMap(Map("i" -> 42, "s" -> "something"))
      testInstance.i shouldBe 42
      testInstance.s shouldBe "something"
    }
  }

  "case class with type param" should {
    "serialise to Map" in {
      val testInstance = WithTypeParam[Integer](n = 43)
      val keyValues = WithTypeParam.toMap(testInstance)
      keyValues should contain ("n" -> testInstance.n)
    }

    "deserialise from Map" in {
      val testInstance = WithTypeParam.fromMap(Map("n" -> 43))
      testInstance.n shouldBe 43
    }
  }

}
