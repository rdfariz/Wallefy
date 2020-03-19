package org.hz240.wallefy.model

data class UserInfo(
    var userList: ArrayList<Info> = arrayListOf()
)

data class Info(
    var username: String = "",
    var status: String = "",
    var photoUrl: String = ""
)