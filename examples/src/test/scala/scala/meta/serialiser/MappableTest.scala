package scala.meta.serialiser

import org.scalatest._

object TestEntities {
  @mappable case class SimpleCaseClass(i: Int, s: String)
  @mappable case class WithTypeParam[N <: Number](n: Number)
  @mappable case class WithBody(i: Int) { def banana: Int = i }

  object WithCompanion { def existingFun(): Int = 42 }
  @mappable case class WithCompanion (i: Int, s: String)
  @mappable case class WithDefaultValue(i: Int = 13, s: String)
}

class MappableTest extends WordSpec with Matchers {
  import TestEntities._

  "simple case class" should {
    "serialise and deserialise" in {
      val testInstance = SimpleCaseClass(i = 42, s = "something")
      val toMap = implicitly[ToMap[SimpleCaseClass]]
      val fromMap = implicitly[FromMap[SimpleCaseClass]]

      val keyValues = toMap(testInstance)
      fromMap(keyValues) shouldBe Some(testInstance)
    }
  }

  "case class with type param" should {
    "serialise and deserialise" in {
      val testInstance = WithTypeParam[Integer](n = 43)
      val toMap = implicitly[ToMap[WithTypeParam[Integer]]]
      val fromMap = implicitly[FromMap[WithTypeParam[Integer]]]

      val keyValues = toMap(testInstance)
      fromMap(keyValues) shouldBe Some(testInstance)
    }
  }

  "case class with body" should {
    "still have the body as before" in {
      WithBody(100).banana shouldBe 100
    }
  }

  "case class with companion" should {
    "serialise and deserialise" in {
      val testInstance = WithCompanion(i = 42, s = "something")
      val toMap = implicitly[ToMap[WithCompanion]]
      val fromMap = implicitly[FromMap[WithCompanion]]

      val keyValues = toMap(testInstance)
      fromMap(keyValues) shouldBe Some(testInstance)
    }

    "keep existing functionality in companion" in {
      WithCompanion.existingFun shouldBe 42
    }
  }

  "case class with default" should {
    "serialise and deserialise" in {
      val testInstance = WithDefaultValue(s = "something")
      val toMap = implicitly[ToMap[WithDefaultValue]]
      val fromMap = implicitly[FromMap[WithDefaultValue]]

      val keyValue = toMap(testInstance)
      fromMap(keyValue) shouldBe Some(testInstance)
    }

    "store correct defaultValueMap" in {
      WithDefaultValue.defaultValueMap shouldBe (Map[String, Any]("i" -> 13))
    }

    "keep default value in fromMap" in {
      val testInstance = WithDefaultValue(s = "something") // with default i = 13
      val fromMap = implicitly[FromMap[WithDefaultValue]]

      val keyValue = Map[String, Any]("s" -> "something")
      fromMap(keyValue) shouldBe Some(testInstance)
    }
  }

  "fromMap" should {
    "return None if provided with invalid data" in {
      val invalidKeyValues = Map("in" -> "valid")

      Seq(
        SimpleCaseClass.FromMap,
        WithTypeParam.FromMap[Integer],
        WithBody.FromMap,
        WithCompanion.FromMap) foreach { fromMap: FromMap[_] =>
          fromMap(invalidKeyValues) shouldBe None
      }
    }
  }

}
