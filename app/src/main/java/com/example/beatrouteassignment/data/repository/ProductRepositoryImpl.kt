package com.example.beatrouteassignment.data.repository

import com.example.beatrouteassignment.data.remote.ProductRemoteDataSource
import com.example.beatrouteassignment.di.IoDispatcher
import com.example.beatrouteassignment.domain.repository.ProductRepository
import com.example.producthandling.model.Product
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    override suspend fun getAllProducts(): List<Product> = withContext(ioDispatcher) {
        val products = remoteDataSource.getAllProducts().toList()
        products
    }

    override suspend fun getPriceTax(): Double = withContext(ioDispatcher) {
        val tax = remoteDataSource.getPriceTax()
        tax
    }

    override suspend fun getCompanyUpdatedPrices(): List<Pair<Int, Double>> =
        withContext(ioDispatcher) {
            val prices = remoteDataSource.getCompanyUpdatedPrices()
            prices
    }

    override suspend fun getCompanyUpdatedStocks(): List<Pair<Int, Int>> =
        withContext(ioDispatcher) {
            val stocks = remoteDataSource.getCompanyUpdatedStocks()
            stocks
    }

    override suspend fun getProductsToDelete(): List<Int> = withContext(ioDispatcher) {
        val ids = remoteDataSource.getProductsToDelete()
        ids
    }

    override suspend fun getNewProducts(): List<Product> = withContext(ioDispatcher) {
        val newProducts = remoteDataSource.getNewProducts()
        newProducts
    }
}
