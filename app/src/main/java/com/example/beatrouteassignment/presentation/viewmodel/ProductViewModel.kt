package com.example.beatrouteassignment.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beatrouteassignment.domain.model.ProductUpdate
import com.example.beatrouteassignment.domain.usecase.ProductUpdatesUseCase
import com.example.beatrouteassignment.presentation.component.ProductUiState
import com.example.producthandling.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProductViewModel @Inject constructor(
    private val updatesUseCase: ProductUpdatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
        updatesUseCase.fetchProducts() // Start fetching immediately
    }

    private fun observeProducts() {
        viewModelScope.launch {
            updatesUseCase.productUpdates
                .onStart { _uiState.value = ProductUiState.Loading }
                .catch { e -> _uiState.value = ProductUiState.Error(e.message ?: "Unknown error") }
                .collect { update ->
                    _uiState.value = ProductUiState.Success(update.products())
                }
        }
    }

    private fun ProductUpdate.products(): List<Product> = when (this) {
        is ProductUpdate.Initial -> products
        is ProductUpdate.PricesUpdated -> products
        is ProductUpdate.StocksUpdated -> products
        is ProductUpdate.ProductsDeleted -> products
        is ProductUpdate.NewProductsAdded -> products
    }
}
