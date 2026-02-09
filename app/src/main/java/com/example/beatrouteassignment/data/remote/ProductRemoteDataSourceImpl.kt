package com.example.beatrouteassignment.data.remote

import com.example.producthandling.StreamLibProvider
import com.example.producthandling.model.Product

class ProductRemoteDataSourceImpl : ProductRemoteDataSource {

    private val api = StreamLibProvider.instance

    override suspend fun getAllProducts(): Collection<Product> =
        api.getAllProducts()

    override suspend fun getPriceTax(): Double =
        api.getPriceTax()

    override suspend fun getCompanyUpdatedPrices(): ArrayList<Pair<Int, Double>> =
        api.getCompanyUpdatedPrices()

    override suspend fun getCompanyUpdatedStocks(): ArrayList<Pair<Int, Int>> =
        api.getCompanyUpdatedStocks()

    override suspend fun getProductsToDelete(): List<Int> =
        api.getProductsToDelete()

    override suspend fun getNewProducts(): ArrayList<Product> =
        api.getNewProducts()
}