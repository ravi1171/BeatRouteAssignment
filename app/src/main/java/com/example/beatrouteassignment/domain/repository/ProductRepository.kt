package com.example.beatrouteassignment.domain.repository

import com.example.beatrouteassignment.domain.model.ProductUpdate
import kotlinx.coroutines.flow.StateFlow

interface ProductRepository {
    val productUpdates: StateFlow<ProductUpdate>
    fun observeProductUpdates(): StateFlow<ProductUpdate>
}
