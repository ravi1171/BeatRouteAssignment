package com.example.beatrouteassignment.domain.repository

import com.example.beatrouteassignment.data.model.ProductPriceUpdate
import com.example.beatrouteassignment.data.model.ProductStockUpdate
import com.example.beatrouteassignment.util.Result
import com.example.producthandling.model.Product

interface ProductRepository {

    suspend fun getAllProducts(): Result<Collection<Product>>

    suspend fun getPriceTax(): Result<Double>

    suspend fun getCompanyUpdatedPrices(): Result<List<ProductPriceUpdate>>

    suspend fun getCompanyUpdatedStocks(): Result<List<ProductStockUpdate>>

    suspend fun getProductsToDelete(): Result<List<Int>>

    suspend fun getNewProducts(): Result<List<Product>>
}