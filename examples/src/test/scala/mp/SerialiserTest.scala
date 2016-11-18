package mp

import org.scalatest._

class SerialiserTest extends WordSpec with Matchers {

  "Serialiser" should {
    "serialise to Map" in {
      val myEntity = MyEntity(i = 42, s = "something")

      val keyValues = myEntity.toMap
      keyValues should contain ("i" -> myEntity.i)
      keyValues should contain ("s" -> myEntity.s)
    }

    // TODO: "deserialise from Map" in {}
  }

}
