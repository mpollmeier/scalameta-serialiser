package mp

import org.scalatest._

class SerialiserTest extends WordSpec with Matchers {

  "serialiser" should {
    "blub" in {
      MyMain.banana(Array.empty)
    }
  }

}

@Main object MyMain {
  println("here i am")
}
