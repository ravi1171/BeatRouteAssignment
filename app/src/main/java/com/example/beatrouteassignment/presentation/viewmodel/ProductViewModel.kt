package com.example.beatrouteassignment.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beatrouteassignment.domain.model.ProductEvent
import com.example.beatrouteassignment.domain.usecase.ProductUpdatesUseCase
import com.example.beatrouteassignment.presentation.component.ProductUiState
import com.example.producthandling.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val useCase: ProductUpdatesUseCase
) : ViewModel() {

    private val productStore = HashMap<Int, Product>()
    private val overriddenPriceIds = HashSet<Int>()
    private var tax: Double? = null

    private var fetchJob: Job? = null

    private var lastSnapshot: List<Product>? = null

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        fetchJob?.cancel()

        _uiState.value = ProductUiState.Loading

        fetchJob = viewModelScope.launch {
            useCase.events().collect { event ->

                val snapshot = withContext(Dispatchers.Default) {
                    processEvent(event)
                }

                snapshot?.let { newList ->
                    if (newList != lastSnapshot) {
                        lastSnapshot = newList
                        _uiState.value = ProductUiState.Success(newList)
                    }
                }
            }
        }
    }

    fun retry() {
        fetchProducts()
    }

    private fun processEvent(event: ProductEvent): List<Product>? {

        when (event) {

            is ProductEvent.BaseProduct -> {
                productStore.clear()
                overriddenPriceIds.clear()
                tax = null
                event.products.forEach { productStore[it.id] = it }
            }

            is ProductEvent.TaxReceived -> {
                tax = event.tax
                applyTaxToEligibleProducts()
            }

            is ProductEvent.ProductsDeleted -> {
                event.ids.forEach {
                    productStore.remove(it)
                    overriddenPriceIds.remove(it)
                }
            }

            is ProductEvent.ProductsAdded -> {
                val currentTax = tax
                event.products.forEach { product ->
                    val finalProduct = if (currentTax != null) {
                        product.copy(
                            price = product.price?.let {
                                it * (1 + currentTax / 100)
                            }
                        )
                    } else product

                    productStore[product.id] = finalProduct
                }
            }

            is ProductEvent.StockUpdated -> {
                event.updates.forEach { (id, stock) ->
                    productStore[id]?.let {
                        productStore[id] = it.copy(stock = stock)
                    }
                }
            }

            is ProductEvent.PriceUpdated -> {
                event.updates.forEach { (id, price) ->
                    productStore[id]?.let {
                        productStore[id] = it.copy(price = price)
                        overriddenPriceIds.add(id)
                    }
                }
            }

            is ProductEvent.Error -> {
                _uiState.value = ProductUiState.Error(event.message)
                return null
            }
        }

        return productStore.values.toList()
    }

    private fun applyTaxToEligibleProducts() {
        val currentTax = tax ?: return

        productStore.forEach { (id, product) ->
            if (!overriddenPriceIds.contains(id)) {
                val updatedPrice = product.price?.let {
                    it * (1 + currentTax / 100)
                }
                productStore[id] = product.copy(price = updatedPrice)
            }
        }
    }
}
