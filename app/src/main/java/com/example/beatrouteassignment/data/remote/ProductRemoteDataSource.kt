package com.example.beatrouteassignment.data.remote

import com.example.producthandling.model.Product

interface ProductRemoteDataSource {
    suspend fun getAllProducts(): Collection<Product>
    suspend fun getPriceTax(): Double
    suspend fun getCompanyUpdatedPrices(): List<Pair<Int, Double>>
    suspend fun getCompanyUpdatedStocks(): List<Pair<Int, Int>>
    suspend fun getProductsToDelete(): List<Int>
    suspend fun getNewProducts(): List<Product>
}