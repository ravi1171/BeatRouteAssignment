package com.example.beatrouteassignment.domain.model

import com.example.producthandling.model.Product

sealed class ProductEvent {
    data class BaseLoaded(val products: List<Product>) : ProductEvent()
    data class TaxReceived(val tax: Double) : ProductEvent()
    data class ProductsDeleted(val ids: List<Int>) : ProductEvent()
    data class ProductsAdded(val products: List<Product>) : ProductEvent()
    data class StockUpdated(val updates: Map<Int, Int>) : ProductEvent()
    data class PriceUpdated(val updates: Map<Int, Double>) : ProductEvent()
}

