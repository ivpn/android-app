package net.ivpn.client.v2.map.dialogue.model

import net.ivpn.client.v2.map.dialogue.DialogueDrawer

data class DialogueData(
        var state: DialogueDrawer.DialogState = DialogueDrawer.DialogState.NONE,
        var x: Float = 0f,
        var y: Float = 0f,
        var locationData: LocationData = LocationData()
)