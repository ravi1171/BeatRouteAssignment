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
    private var tax: Double = 0.0

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
                event.products.forEach {
                    productStore[it.id] = it
                }
                emitSnapshot()
            }

            is ProductEvent.TaxReceived -> {
                tax = event.tax
                applyTaxToExistingProducts()
                emitSnapshot()
            }

            is ProductEvent.ProductsDeleted -> {
                event.ids.forEach { productStore.remove(it) }
                emitSnapshot()
            }

            is ProductEvent.ProductsAdded -> {
                event.products.forEach {
                    val taxedPrice = it.price?.let { price ->
                        price * (1 + tax / 100)
                    }
                    productStore[it.id] = it.copy(price = taxedPrice)
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
                    }
                }
                emitSnapshot()
            }

            is ProductEvent.Error -> {
                _uiState.value = ProductUiState.Error(event.message)
            }
        }
    }

    private fun applyTaxToExistingProducts() {
        productStore.forEach { (id, product) ->
            val updatedPrice = product.price?.let {
                it * (1 + tax / 100)
            }
            productStore[id] = product.copy(price = updatedPrice)
        }
    }

    private fun emitSnapshot() {
        _uiState.value = ProductUiState.Success(productStore.values.toList())
    }
}

