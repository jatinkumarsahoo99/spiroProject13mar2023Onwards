package com.safey.lungmonitoring.ui.profile

data class Avatar
    (var id: Int = 0,
    var resourceId: Int = 0,
    var imageURL: String? = null,
    var isSelected: Boolean = false,
    var avatarColor: String? = null)