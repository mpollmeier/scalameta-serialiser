package mp

@Main object MyMain {
  SillyGlobalState.bananaCount += 1
}

object SillyGlobalState {
  var bananaCount = 0
}
