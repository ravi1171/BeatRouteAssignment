package com.example.beatrouteassignment.domain.usecase

import com.example.beatrouteassignment.di.IoDispatcher
import com.example.beatrouteassignment.domain.model.ProductEvent
import com.example.beatrouteassignment.domain.repository.ProductRepository
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

        try {
            val baseProducts = repository.getAllProducts()
            send(ProductEvent.BaseProduct(baseProducts))
        } catch (e: Exception) {
            send(ProductEvent.Error("Failed to load products: ${e.message}"))
            return@channelFlow
        }

        supervisorScope {

            launch(dispatcher) {
                try {
                    val tax = repository.getPriceTax()
                    send(ProductEvent.TaxReceived(tax))
                } catch (e: Exception) {
                    send(ProductEvent.Error("Tax API failed: ${e.message}"))
                }
            }

            launch(dispatcher) {
                try {
                    val ids = repository.getProductsToDelete()
                    send(ProductEvent.ProductsDeleted(ids))
                } catch (e: Exception) {
                    send(ProductEvent.Error("Delete API failed: ${e.message}"))
                }
            }

            launch(dispatcher) {
                try {
                    val newProducts = repository.getNewProducts()
                    send(ProductEvent.ProductsAdded(newProducts))
                } catch (e: Exception) {
                    send(ProductEvent.Error("New Products API failed: ${e.message}"))
                }
            }

            launch(dispatcher) {
                try {
                    val stocks = repository.getCompanyUpdatedStocks()
                    send(ProductEvent.StockUpdated(stocks.toMap()))
                } catch (e: Exception) {
                    send(ProductEvent.Error("Stock API failed: ${e.message}"))
                }
            }

            launch(dispatcher) {
                try {
                    val prices = repository.getCompanyUpdatedPrices()
                    send(ProductEvent.PriceUpdated(prices.toMap()))
                } catch (e: Exception) {
                    send(ProductEvent.Error("Price API failed: ${e.message}"))
                }
            }
        }
    }
}
