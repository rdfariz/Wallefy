package org.hz240.wallefy.model

data class CommunityInfo(
    val idCommunity: String? = null,
    val displayName: String? = null,
    val admin: ArrayList<HashMap<String, Any?>>? = null,
    val members: ArrayList<HashMap<String, Any?>>? = null,
    val saldo: Int? = null
)