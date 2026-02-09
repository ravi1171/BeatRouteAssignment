package com.example.beatrouteassignment.data.remote

import com.example.producthandling.model.Product

interface ProductRemoteDataSource {
    suspend fun getAllProducts(): Collection<Product>

    suspend fun getPriceTax(): Double

    suspend fun getCompanyUpdatedPrices(): ArrayList<Pair<Int, Double>>

    suspend fun getCompanyUpdatedStocks(): ArrayList<Pair<Int, Int>>

    suspend fun getProductsToDelete(): List<Int>

    suspend fun getNewProducts(): ArrayList<Product>
}