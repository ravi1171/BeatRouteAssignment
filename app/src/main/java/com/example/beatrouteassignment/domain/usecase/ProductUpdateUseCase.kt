package com.example.beatrouteassignment.domain.usecase


import com.example.beatrouteassignment.di.IoDispatcher
import com.example.beatrouteassignment.domain.model.ProductUpdate
import com.example.beatrouteassignment.domain.repository.ProductRepository
import com.example.producthandling.model.Product
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    // Called from ViewModel (viewModelScope)
    suspend fun fetchProducts() = coroutineScope {
        try {
// 1️⃣ Fetch base products first
            var currentProducts: List<Product> = repository.getAllProducts()

            _productUpdates.value = ProductUpdate.Initial(currentProducts)


// Mutex for thread-safe updates
            val mutex = Mutex()


// 2️⃣ Fetch tax in parallel
            launch(ioDispatcher) {
                val tax = repository.getPriceTax()
                mutex.withLock {
                    currentProducts = currentProducts.map {
                        it.copy(
                            price = (it.price ?: 0.0) * (1 + tax / 100)
                        )
                    }
                    _productUpdates.value = ProductUpdate.PricesUpdated(currentProducts)
                }
            }


// 3️⃣ Fetch delete list in parallel
            launch(ioDispatcher) {
                val deleteIds = repository.getProductsToDelete()
                mutex.withLock {
                    currentProducts = currentProducts.filter {
                        it.id !in deleteIds
                    }
                    _productUpdates.value = ProductUpdate.ProductsDeleted(currentProducts)
                }
            }

            // 4️⃣ Fetch new products in parallel
            launch(ioDispatcher) {
                val newProducts = repository.getNewProducts()
                mutex.withLock {
                    currentProducts = currentProducts + newProducts
                    _productUpdates.value = ProductUpdate.NewProductsAdded(currentProducts)
                }
            }

            // 5️⃣ Fetch stock updates in parallel
            launch(ioDispatcher) {
                val stockMap = repository.getCompanyUpdatedStocks().toMap()
                mutex.withLock {
                    currentProducts = currentProducts.map {
                        it.copy(
                            stock = stockMap[it.id] ?: it.stock
                        )
                    }
                    _productUpdates.value = ProductUpdate.StocksUpdated(currentProducts)
                }
            }

            // 6️⃣ Fetch price updates in parallel
            launch(ioDispatcher) {
                val priceMap = repository.getCompanyUpdatedPrices().toMap()
                mutex.withLock {
                    currentProducts = currentProducts.map {
                        it.copy(
                            price = priceMap[it.id] ?: it.price
                        )
                    }
                    _productUpdates.value = ProductUpdate.PricesUpdated(currentProducts)
                }
            }

        } catch (e: Exception) {
            println($$"ProductUpdatesUseCase Error: ${e.message}")
        }
    }
}