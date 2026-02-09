package com.example.beatrouteassignment.domain.repository

import com.example.beatrouteassignment.domain.model.ProductUpdate
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    fun observeProductUpdates(): Flow<ProductUpdate>
}