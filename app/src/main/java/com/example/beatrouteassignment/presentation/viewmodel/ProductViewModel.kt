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

    private val productStore = mutableMapOf<Int, Product>()
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
                event.products.forEach { productStore[it.id] = it }
                emitUi()
            }

            is ProductEvent.TaxReceived -> {
                tax = event.tax
                emitUi()
            }

            is ProductEvent.ProductsDeleted -> {
                event.ids.forEach { productStore.remove(it) }
                emitUi()
            }

            is ProductEvent.ProductsAdded -> {
                event.products.forEach {
                    productStore[it.id] = it
                }
                emitUi()
            }

            is ProductEvent.StockUpdated -> {
                event.updates.forEach { (id, stock) ->
                    productStore[id]?.let {
                        productStore[id] = it.copy(stock = stock)
                    }
                }
                emitUi()
            }

            is ProductEvent.PriceUpdated -> {
                event.updates.forEach { (id, price) ->
                    productStore[id]?.let {
                        productStore[id] = it.copy(price = price)
                    }
                }
                emitUi()
            }
        }
    }

    private fun emitUi() {
        val currentTax = tax

        val finalProducts = if (currentTax != null) {
            productStore.values.map { product ->
                product.copy(
                    price = (product.price ?: 0.0) * (1 + currentTax / 100)
                )
            }
        } else {
            productStore.values.toList()
        }

        _uiState.value = ProductUiState.Success(finalProducts)
    }
}
