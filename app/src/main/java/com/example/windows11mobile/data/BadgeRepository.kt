package com.example.windows11mobile.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object BadgeRepository {
    private val _messageBadgeCount = MutableStateFlow(1) // Default 1 for unread message badge testing
    val messageBadgeCount: StateFlow<Int> = _messageBadgeCount.asStateFlow()

    fun incrementMessageBadge() {
        _messageBadgeCount.update { it + 1 }
    }

    fun decrementMessageBadge() {
        _messageBadgeCount.update { (it - 1).coerceAtLeast(0) }
    }

    fun setMessageBadge(count: Int) {
        _messageBadgeCount.value = count
    }

    fun clearMessageBadge() {
        _messageBadgeCount.value = 0
    }
}
