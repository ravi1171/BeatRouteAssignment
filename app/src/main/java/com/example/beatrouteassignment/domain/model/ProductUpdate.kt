package com.example.beatrouteassignment.domain.model

import com.example.producthandling.model.Product

sealed class ProductUpdate {
    data class Initial(val productList: List<Product>) : ProductUpdate()
    data class PricesUpdated(val productList: List<Product>) : ProductUpdate()
    data class StocksUpdated(val productList: List<Product>) : ProductUpdate()
    data class ProductsDeleted(val productList: List<Product>) : ProductUpdate()
    data class NewProductsAdded(val productList: List<Product>) : ProductUpdate()
}
