package com.mindInk.app.data.settings

import com.mindInk.app.domain.model.AccountSettings

data class AccountSettingsDto(
    val name: String = "",
    val github: String = "",
    val instagram: String = ""
) {
    fun toModel() = AccountSettings(
        name = name,
        github = github,
        instagram = instagram
    )
}
