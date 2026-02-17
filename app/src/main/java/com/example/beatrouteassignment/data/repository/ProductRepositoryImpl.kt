package com.example.beatrouteassignment.data.repository

import com.example.beatrouteassignment.data.model.ProductPriceUpdate
import com.example.beatrouteassignment.data.model.ProductStockUpdate
import com.example.beatrouteassignment.data.remote.ProductRemoteDataSource
import com.example.beatrouteassignment.di.IoDispatcher
import com.example.beatrouteassignment.domain.repository.ProductRepository
import com.example.beatrouteassignment.util.Result
import com.example.producthandling.model.Product
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    override suspend fun getAllProducts(): Result<Collection<Product>> = withContext(ioDispatcher) {
        try {
            val products = remoteDataSource.getAllProducts()
            Result.Success(products)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getPriceTax(): Result<Double> = withContext(ioDispatcher) {
        try {
            val tax = remoteDataSource.getPriceTax()
            Result.Success(tax)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCompanyUpdatedPrices(): Result<List<ProductPriceUpdate>> =
        withContext(ioDispatcher) {
            try {
                val prices = remoteDataSource.getCompanyUpdatedPrices()
                    .map { (id, price) -> ProductPriceUpdate(productId = id, newPrice = price) }
                Result.Success(prices)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getCompanyUpdatedStocks(): Result<List<ProductStockUpdate>> =
        withContext(ioDispatcher) {
            try {
                val stocks = remoteDataSource.getCompanyUpdatedStocks().map { (id, stock) ->
                    ProductStockUpdate(productId = id, newStock = stock)
                }
                Result.Success(stocks)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getProductsToDelete(): Result<List<Int>> = withContext(ioDispatcher) {
        try {
            val ids = remoteDataSource.getProductsToDelete()
            Result.Success(ids)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getNewProducts(): Result<List<Product>> = withContext(ioDispatcher) {
        try {
            val newProducts = remoteDataSource.getNewProducts()
            Result.Success(newProducts)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
