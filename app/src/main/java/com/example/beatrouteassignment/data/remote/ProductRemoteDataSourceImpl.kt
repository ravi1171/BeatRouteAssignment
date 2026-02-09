package com.example.beatrouteassignment.data.remote

import com.example.producthandling.StreamLibProvider
import com.example.producthandling.model.Product
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRemoteDataSourceImpl @Inject constructor() : ProductRemoteDataSource {
    private val api = StreamLibProvider.instance

    override suspend fun getAllProducts(): Collection<Product> = api.getAllProducts().toList()
    override suspend fun getPriceTax(): Double = api.getPriceTax()
    override suspend fun getCompanyUpdatedPrices(): List<Pair<Int, Double>> =
        api.getCompanyUpdatedPrices()

    override suspend fun getCompanyUpdatedStocks(): List<Pair<Int, Int>> =
        api.getCompanyUpdatedStocks()

    override suspend fun getProductsToDelete(): List<Int> = api.getProductsToDelete()
    override suspend fun getNewProducts(): List<Product> = api.getNewProducts()
}