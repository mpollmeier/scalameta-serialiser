package mp

import org.scalatest._

class SemigroupTest extends WordSpec with Matchers {

  "SemiGroup" should {
    "append" in {
      val sg = new MySemigroup
      sg.append1(1, 2) shouldBe 3
    }
  }

}
