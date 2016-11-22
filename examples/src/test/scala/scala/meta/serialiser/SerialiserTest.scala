package scala.meta.serialiser

import org.scalatest._

object TestEntities {
  @entity case class MyEntity(i: Int, s: String)
}

class SerialiserTest extends WordSpec with Matchers {
  import TestEntities._

  "Serialiser" should {
    "serialise to Map" in {
      val testInstance = MyEntity(i = 42, s = "something")

      val keyValues = MyEntity.toMap(testInstance)
      keyValues should contain ("i" -> testInstance.i)
      keyValues should contain ("s" -> testInstance.s)
    }

    "deserialise from Map" in {
      val myEntity = MyEntity.fromMap(
        Map(
          "i" -> 42,
          "s" -> "something"
        )
      )

      myEntity.i shouldBe 42
      myEntity.s shouldBe "something"
    }
  }

}
