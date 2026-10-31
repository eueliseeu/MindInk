package com.mindInk.app.data.settings

import com.mindInk.app.domain.model.AccountSettings

data class AccountSettingsDto(
    val name: String = "",
    val github: String = "",
    val linkedin: String = ""
) {
    fun toModel() = AccountSettings(
        name = name,
        github = github,
        linkedin = linkedin
    )
}