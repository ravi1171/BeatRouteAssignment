package com.example.beatrouteassignment.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beatrouteassignment.domain.model.ProductEvent
import com.example.beatrouteassignment.domain.usecase.ProductUpdatesUseCase
import com.example.beatrouteassignment.presentation.component.ProductUiState
import com.example.producthandling.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val useCase: ProductUpdatesUseCase
) : ViewModel() {

    private val productStore = HashMap<Int, Product>(600_000)

    private val overriddenPriceIds = HashSet<Int>()
    private var tax: Double? = null

    private val _uiState =
        MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            useCase.events().collect { event ->
                applyEvent(event)
            }
        }
    }

    private fun applyEvent(event: ProductEvent) {
        when (event) {

            is ProductEvent.BaseProduct -> {
                productStore.clear()
                overriddenPriceIds.clear()

                event.products.forEach {
                    productStore[it.id] = it
                }
                emitSnapshot()
            }

            is ProductEvent.TaxReceived -> {
                tax = event.tax
                applyTaxToEligibleProducts()
                emitSnapshot()
            }

            is ProductEvent.ProductsDeleted -> {
                event.ids.forEach {
                    productStore.remove(it)
                    overriddenPriceIds.remove(it)
                }
                emitSnapshot()
            }

            is ProductEvent.ProductsAdded -> {
                val currentTax = tax

                event.products.forEach { product ->
                    val finalProduct =
                        if (currentTax != null) {
                            product.copy(
                                price = product.price?.let {
                                    it * (1 + currentTax / 100)
                                }
                            )
                        } else product

                    productStore[product.id] = finalProduct
                }

                emitSnapshot()
            }

            is ProductEvent.StockUpdated -> {
                event.updates.forEach { (id, stock) ->
                    productStore[id]?.let {
                        productStore[id] = it.copy(stock = stock)
                    }
                }
                emitSnapshot()
            }

            is ProductEvent.PriceUpdated -> {
                event.updates.forEach { (id, price) ->
                    productStore[id]?.let {
                        productStore[id] = it.copy(price = price)
                        overriddenPriceIds.add(id) // mark as final price
                    }
                }
                emitSnapshot()
            }

            is ProductEvent.Error -> {
                _uiState.value = ProductUiState.Error(event.message)
            }
        }
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

    private fun emitSnapshot() {
        _uiState.value =
            ProductUiState.Success(productStore.values.toList())
    }
}


