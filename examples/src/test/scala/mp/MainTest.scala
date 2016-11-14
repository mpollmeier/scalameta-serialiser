package mp

import org.scalatest._

class MainTest extends WordSpec with Matchers {

  "Main" should {
    "have a banana" in {
      val initialBananaCount = SillyGlobalState.bananaCount
      MyMain.banana(Array.empty)
      SillyGlobalState.bananaCount shouldBe (initialBananaCount + 1)
    }
  }

}
