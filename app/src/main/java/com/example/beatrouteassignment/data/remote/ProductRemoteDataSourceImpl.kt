package com.example.beatrouteassignment.data.remote

import com.example.beatrouteassignment.di.IoDispatcher
import com.example.producthandling.StreamLibAPI
import com.example.producthandling.model.Product
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(
    private val api: StreamLibAPI,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProductRemoteDataSource {

    override suspend fun getAllProducts(): List<Product> = withContext(ioDispatcher) {
        api.getAllProducts().toList()
    }

    override suspend fun getPriceTax(): Double = api.getPriceTax()
    override suspend fun getCompanyUpdatedPrices(): List<Pair<Int, Double>> =
        api.getCompanyUpdatedPrices()

    override suspend fun getCompanyUpdatedStocks(): List<Pair<Int, Int>> =
        api.getCompanyUpdatedStocks()

    override suspend fun getProductsToDelete(): List<Int> = api.getProductsToDelete()
    override suspend fun getNewProducts(): List<Product> = api.getNewProducts()
}