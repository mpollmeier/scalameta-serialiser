package mp

import org.scalatest._

class MainTest extends WordSpec with Matchers {

  "Main" should {
    "have a banana" in {
      MyMain.banana(Array.empty)
    }
  }

}

@Main object MyMain {
  println("here i am")
}
