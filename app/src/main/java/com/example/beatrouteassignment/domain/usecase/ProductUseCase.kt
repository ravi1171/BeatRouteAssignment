package com.example.beatrouteassignment.domain.usecase

import com.example.beatrouteassignment.di.IoDispatcher
import com.example.beatrouteassignment.domain.model.ProductEvent
import com.example.beatrouteassignment.domain.repository.ProductRepository
import com.example.beatrouteassignment.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

class ProductUpdatesUseCase @Inject constructor(
    private val repository: ProductRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    fun events(): Flow<ProductEvent> = channelFlow {

        when (val baseProducts = repository.getAllProducts()) {
            is Result.Success -> send(ProductEvent.BaseProduct(baseProducts.data))
            is Result.Error -> {
                send(ProductEvent.Error("Failed to load products: ${baseProducts.exception.message}"))
                return@channelFlow
            }
        }

        supervisorScope {

            launch(dispatcher) {
                when (val taxResult = repository.getPriceTax()) {
                    is Result.Success -> send(ProductEvent.TaxReceived(taxResult.data))
                    is Result.Error -> send(ProductEvent.Error("Tax API failed: ${taxResult.exception.message}"))
                }
            }

            launch(dispatcher) {
                when (val idsResult = repository.getProductsToDelete()) {
                    is Result.Success -> send(ProductEvent.ProductsDeleted(idsResult.data))
                    is Result.Error -> send(ProductEvent.Error("Delete API failed: ${idsResult.exception.message}"))
                }
            }

            launch(dispatcher) {
                when (val newProductsResult = repository.getNewProducts()) {
                    is Result.Success -> send(ProductEvent.ProductsAdded(newProductsResult.data))
                    is Result.Error -> send(ProductEvent.Error("New Products API failed: ${newProductsResult.exception.message}"))
                }
            }

            launch(dispatcher) {
                when (val stocksResult = repository.getCompanyUpdatedStocks()) {
                    is Result.Success -> {
                        val stocksMap = stocksResult.data.associate { it.productId to it.newStock }
                        send(ProductEvent.StockUpdated(stocksMap))
                    }

                    is Result.Error -> send(ProductEvent.Error("Stock API failed: ${stocksResult.exception.message}"))
                }
            }

            launch(dispatcher) {
                when (val pricesResult = repository.getCompanyUpdatedPrices()) {
                    is Result.Success -> {
                        val pricesMap = pricesResult.data.associate { it.productId to it.newPrice }
                        send(ProductEvent.PriceUpdated(pricesMap))
                    }

                    is Result.Error -> send(ProductEvent.Error("Price API failed: ${pricesResult.exception.message}"))
                }
            }
        }
    }
}