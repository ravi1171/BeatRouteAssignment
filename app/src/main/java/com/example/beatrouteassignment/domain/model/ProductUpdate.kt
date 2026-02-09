package com.example.beatrouteassignment.domain.model

import com.example.producthandling.model.Product

sealed class ProductUpdate {
    data class Initial(val products: List<Product>) : ProductUpdate()
    data class PricesUpdated(val products: List<Product>) : ProductUpdate()
    data class StocksUpdated(val products: List<Product>) : ProductUpdate()
    data class ProductsDeleted(val products: List<Product>) : ProductUpdate()
    data class NewProductsAdded(val products: List<Product>) : ProductUpdate()
}
