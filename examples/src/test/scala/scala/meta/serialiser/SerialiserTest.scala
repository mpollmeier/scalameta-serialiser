package scala.meta.serialiser

import org.scalatest._

object TestEntities {
  @entity case class SimpleCaseClass(i: Int, s: String)
  @entity case class WithTypeParam[N <: Number](n: Number)
  @entity case class WithBody(i: Int) { def banana: Int = i }
}

class SerialiserTest extends WordSpec with Matchers {
  import TestEntities._

  "simple case class" should {
    "serialise and deserialise" in {
      val testInstance = SimpleCaseClass(i = 42, s = "something")
      val keyValues = SimpleCaseClass.toMap(testInstance)
      SimpleCaseClass.fromMap(keyValues) shouldBe testInstance
    }
  }

  "case class with type param" should {
    "serialise and deserialise" in {
      val testInstance = WithTypeParam[Integer](n = 43)
      val keyValues = WithTypeParam.toMap(testInstance)
      WithTypeParam.fromMap(keyValues) shouldBe testInstance
    }
  }

  "case class with body" should {
    "still have the body as before" in {
      WithBody(100).banana shouldBe 100
    }
  }

}
