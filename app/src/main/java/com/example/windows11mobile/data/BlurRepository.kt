package com.example.windows11mobile.data

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlurRepository private constructor(context: Context) {
    private val _blurAmount = MutableStateFlow(20.dp)
    val blurAmount: StateFlow<Dp> = _blurAmount.asStateFlow()

    fun setBlur(amount: Dp) {
        _blurAmount.value = amount
    }

    companion object {
        @Volatile
        private var INSTANCE: BlurRepository? = null

        fun getInstance(context: Context): BlurRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BlurRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
