package com.example.beatrouteassignment.data.remote

import com.example.producthandling.StreamLibAPI
import com.example.producthandling.model.Product
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(
    private val api: StreamLibAPI
) : ProductRemoteDataSource {

    override suspend fun getAllProducts(): Collection<Product> = api.getAllProducts()

    override suspend fun getPriceTax(): Double = api.getPriceTax()
    override suspend fun getCompanyUpdatedPrices(): List<Pair<Int, Double>> =
        api.getCompanyUpdatedPrices()

    override suspend fun getCompanyUpdatedStocks(): List<Pair<Int, Int>> =
        api.getCompanyUpdatedStocks()

    override suspend fun getProductsToDelete(): List<Int> = api.getProductsToDelete()
    override suspend fun getNewProducts(): List<Product> = api.getNewProducts()
}