package mp

@Main object MyMain {
  val i = 42
  SillyGlobalState.bananaCount += 1
}

object SillyGlobalState {
  var bananaCount = 0
}
