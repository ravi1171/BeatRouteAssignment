package com.example.beatrouteassignment.domain.repository

import com.example.producthandling.model.Product

interface ProductRepository {

    suspend fun getAllProducts(): List<Product>

    suspend fun getPriceTax(): Double

    suspend fun getCompanyUpdatedPrices(): List<Pair<Int, Double>>

    suspend fun getCompanyUpdatedStocks(): List<Pair<Int, Int>>

    suspend fun getProductsToDelete(): List<Int>

    suspend fun getNewProducts(): List<Product>
}