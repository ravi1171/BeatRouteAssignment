package com.example.beatrouteassignment.domain.usecase

import com.example.beatrouteassignment.data.repository.ProductRepositoryImpl
import com.example.beatrouteassignment.domain.model.ProductUpdate
import com.example.producthandling.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProductUpdatesUseCase @Inject constructor(
    private val repository: ProductRepositoryImpl
) {

    private val _productUpdates =
        MutableStateFlow<ProductUpdate>(ProductUpdate.Initial(emptyList()))
    val productUpdates: StateFlow<ProductUpdate> = _productUpdates.asStateFlow()

    fun fetchProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var currentProducts: List<Product> = repository.getAllProducts()
                _productUpdates.value = ProductUpdate.Initial(currentProducts)

                val taxDeferred = async { repository.getPriceTax() }
                val deleteDeferred = async { repository.getProductsToDelete() }
                val newDeferred = async { repository.getNewProducts() }
                val stockDeferred = async { repository.getCompanyUpdatedStocks() }
                val priceDeferred = async { repository.getCompanyUpdatedPrices() }

                val tax = taxDeferred.await()
                currentProducts =
                    currentProducts.map { it.copy(price = (it.price ?: 0.0) * (1 + tax / 100)) }
                _productUpdates.value = ProductUpdate.PricesUpdated(currentProducts)

                val deleteIds = deleteDeferred.await()
                currentProducts = currentProducts.filter { it.id !in deleteIds }
                _productUpdates.value = ProductUpdate.ProductsDeleted(currentProducts)

                // Add new products apply tax
                val newProducts = newDeferred.await()
                val newTaxed =
                    newProducts.map { it.copy(price = (it.price ?: 0.0) * (1 + tax / 100)) }
                currentProducts = currentProducts + newTaxed
                _productUpdates.value = ProductUpdate.NewProductsAdded(currentProducts)

                // Update stocks
                val stockMap = stockDeferred.await().toMap()
                currentProducts =
                    currentProducts.map { it.copy(stock = stockMap[it.id] ?: it.stock) }
                _productUpdates.value = ProductUpdate.StocksUpdated(currentProducts)

                // Update prices
                val priceMap = priceDeferred.await().toMap()
                currentProducts =
                    currentProducts.map { it.copy(price = priceMap[it.id] ?: it.price) }
                _productUpdates.value = ProductUpdate.PricesUpdated(currentProducts)

            } catch (e: Exception) {
                println("ProductUpdatesUseCase Error: ${e.message}")
            }
        }
    }
}