package com.example.beatrouteassignment.domain.usecase

import android.util.Log
import com.example.beatrouteassignment.di.IoDispatcher
import com.example.beatrouteassignment.domain.model.ProductUpdate
import com.example.beatrouteassignment.domain.repository.ProductRepository
import com.example.producthandling.model.Product
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject


class ProductUpdatesUseCase @Inject constructor(
    private val repository: ProductRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val _productUpdates =
        MutableStateFlow<ProductUpdate>(ProductUpdate.Initial(emptyList()))

    val productUpdates: StateFlow<ProductUpdate> = _productUpdates.asStateFlow()


    suspend fun fetchProducts() = supervisorScope {

        val baseList = try {
            repository.getAllProducts()
        } catch (e: Exception) {
            Log.e("UseCase", "Base API failed", e)
            return@supervisorScope
        }

        val productMap = baseList.associateBy { it.id }.toMutableMap()

        _productUpdates.value = ProductUpdate.Initial(productMap.values.toList())

        val mutex = Mutex()


        launch(ioDispatcher) {
            try {
                val tax = repository.getPriceTax()

                mutex.withLock {

                    productMap.forEach { (id, product) ->
                        productMap[id] = product.copy(
                            price = (product.price ?: 0.0) * (1 + tax / 100)
                        )
                    }
                    emitUpdate(productMap)
                }

            } catch (e: Exception) {
                Log.e("UseCase", "Tax API failed", e)
            }
        }


        launch(ioDispatcher) {
            try {
                val deleteIds = repository.getProductsToDelete()

                mutex.withLock {

                    deleteIds.forEach {
                        productMap.remove(it)
                    }
                    emitUpdate(productMap)
                }

            } catch (e: Exception) {
                Log.e("UseCase", "Delete API failed", e)
            }
        }


        launch(ioDispatcher) {
            try {
                val newProducts = repository.getNewProducts()

                mutex.withLock {

                    newProducts.forEach {
                        productMap[it.id] = it
                    }
                    emitUpdate(productMap)
                }

            } catch (e: Exception) {
                Log.e("UseCase", "New Products API failed", e)
            }
        }


        launch(ioDispatcher) {
            try {
                val stocks = repository.getCompanyUpdatedStocks()

                mutex.withLock {

                    stocks.forEach { (id, stock) ->
                        productMap[id]?.let { p ->
                            productMap[id] = p.copy(stock = stock)
                        }
                    }
                    emitUpdate(productMap)
                }

            } catch (e: Exception) {
                Log.e("UseCase", "Stock API failed", e)
            }
        }


        launch(ioDispatcher) {
            try {
                val prices = repository.getCompanyUpdatedPrices()

                mutex.withLock {

                    prices.forEach { (id, price) ->
                        productMap[id]?.let { p ->
                            productMap[id] = p.copy(price = price)
                        }
                    }
                    emitUpdate(productMap)
                }

            } catch (e: Exception) {
                Log.e("UseCase", "Price API failed", e)
            }
        }
    }

    private fun emitUpdate(
        map: Map<Int, Product>
    ) {
        _productUpdates.value = ProductUpdate.PricesUpdated(
            map.values.toList()
        )
    }
}