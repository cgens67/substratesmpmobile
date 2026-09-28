  fun startPrivateCall(callChannelId: String, callChannelName: String, isVideo: Boolean) {
    val myTag = userState.value.gamertag
    if (myTag.isBlank()) return
    voiceManager.joinVoiceChannel(callChannelId, callChannelName, myTag, isVideo = isVideo)
  }

  fun inviteToCall(targetGamertag: String, callChannelId: String, callChannelName: String, isVideo: Boolean) {
    val myTag = userState.value.gamertag.trim()
    if (myTag.isBlank() || targetGamertag.isBlank()) return

    val dmId = "dm_" + listOf(myTag.lowercase(), targetGamertag.lowercase()).sorted().joinToString("_")
    sendMessage(
      content = "📞 Invited you to a private call ($callChannelName)",
      coordinates = null,
      channelId = dmId
    )
  }
