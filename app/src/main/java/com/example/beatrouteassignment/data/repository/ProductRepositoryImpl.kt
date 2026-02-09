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
        println(
            "Repository: Fetched all products | size=${products.size}, sample=${
                products.take(5).map { it.name }
            }"
        )
        products
    }

    override suspend fun getPriceTax(): Double = withContext(ioDispatcher) {
        val tax = remoteDataSource.getPriceTax()
        println("Repository: Fetched tax=$tax%")
        tax
    }

    override suspend fun getCompanyUpdatedPrices(): List<Pair<Int, Double>> =
        withContext(ioDispatcher) {
            val prices = remoteDataSource.getCompanyUpdatedPrices()
            println(
                "Repository: Fetched updated prices | size=${prices.size}, sample=${
                    prices.take(
                        5
                    )
                }"
            )
            prices
    }

    override suspend fun getCompanyUpdatedStocks(): List<Pair<Int, Int>> =
        withContext(ioDispatcher) {
            val stocks = remoteDataSource.getCompanyUpdatedStocks()
            println(
                "Repository: Fetched updated stocks | size=${stocks.size}, sample=${
                    stocks.take(
                        5
                    )
                }"
            )
            stocks
    }

    override suspend fun getProductsToDelete(): List<Int> = withContext(ioDispatcher) {
        val ids = remoteDataSource.getProductsToDelete()
        println("Repository: Fetched products to delete | size=${ids.size}, sample=${ids.take(5)}")
        ids
    }

    override suspend fun getNewProducts(): List<Product> = withContext(ioDispatcher) {
        val newProducts = remoteDataSource.getNewProducts()
        println(
            "Repository: Fetched new products | size=${newProducts.size}, sample=${
                newProducts.take(
                    5
                ).map { it.name }
            }"
        )
        newProducts
    }
}
