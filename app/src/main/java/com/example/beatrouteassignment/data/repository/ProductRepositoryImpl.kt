package com.example.beatrouteassignment.data.repository

import com.example.beatrouteassignment.data.remote.ProductRemoteDataSource
import com.example.beatrouteassignment.domain.model.ProductUpdate
import com.example.beatrouteassignment.domain.repository.ProductRepository
import com.example.producthandling.model.Product
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProductRepository {

    private val mutex = Mutex()

    override fun observeProductUpdates(): Flow<ProductUpdate> = callbackFlow {

        val currentProducts = mutableListOf<Product>()

        val baseProducts = remoteDataSource.getAllProducts()
        mutex.withLock { currentProducts.addAll(baseProducts) }

        trySend(ProductUpdate.Initial(currentProducts.toList()))

        val tax = remoteDataSource.getPriceTax()

        mutex.withLock {
            currentProducts.replaceAll { product ->
                val price = product.price ?: 0.0
                product.copy(price = price * (1 + tax / 100))
            }
        }
        trySend(ProductUpdate.PricesUpdated(currentProducts.toList()))

        launch(ioDispatcher) {
            runCatching { remoteDataSource.getProductsToDelete() }
                .onSuccess { idsToDelete ->
                    mutex.withLock { currentProducts.removeAll { it.id in idsToDelete } }
                    trySend(ProductUpdate.ProductsDeleted(currentProducts.toList()))
                }
        }

        launch(ioDispatcher) {
            runCatching { remoteDataSource.getNewProducts() }
                .onSuccess { newProducts ->
                    val taxedNewProducts = newProducts.map { product ->
                        val price = product.price ?: 0.0
                        product.copy(price = price * (1 + tax / 100))
                    }
                    mutex.withLock { currentProducts.addAll(taxedNewProducts) }
                    trySend(ProductUpdate.NewProductsAdded(currentProducts.toList()))
                }
        }

        launch(ioDispatcher) {
            runCatching { remoteDataSource.getCompanyUpdatedStocks() }
                .onSuccess { stockUpdates ->
                    val stockMap = stockUpdates.toMap()
                    mutex.withLock {
                        currentProducts.replaceAll { product ->
                            stockMap[product.id]?.let { newStock -> product.copy(stock = newStock) }
                                ?: product
                        }
                    }
                    trySend(ProductUpdate.StocksUpdated(currentProducts.toList()))
                }
        }

        launch(ioDispatcher) {
            runCatching { remoteDataSource.getCompanyUpdatedPrices() }
                .onSuccess { priceUpdates ->
                    val priceMap = priceUpdates.toMap()
                    mutex.withLock {
                        currentProducts.replaceAll { product ->
                            priceMap[product.id]?.let { newPrice -> product.copy(price = newPrice) }
                                ?: product
                        }
                    }
                    trySend(ProductUpdate.PricesUpdated(currentProducts.toList()))
                }
        }
        awaitClose()
    }
}