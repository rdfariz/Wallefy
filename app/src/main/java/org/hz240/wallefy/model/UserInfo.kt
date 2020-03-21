package org.hz240.wallefy.model

data class UserInfo(
    var username: String? = null,
    var displayName: String? = null,
    var email: String? = null,
    var status: String? = null,
    var photoUrl: String? = null
)