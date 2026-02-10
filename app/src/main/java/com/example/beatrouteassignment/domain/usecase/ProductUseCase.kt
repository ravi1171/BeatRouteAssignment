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

        val baseProducts = repository.getAllProducts()
        send(ProductEvent.BaseLoaded(baseProducts))

        supervisorScope {

            launch(dispatcher) {
                runCatching { repository.getPriceTax() }
                    .onSuccess { send(ProductEvent.TaxReceived(it)) }
            }

            launch(dispatcher) {
                runCatching { repository.getProductsToDelete() }
                    .onSuccess { send(ProductEvent.ProductsDeleted(it)) }
            }

            launch(dispatcher) {
                runCatching { repository.getNewProducts() }
                    .onSuccess { send(ProductEvent.ProductsAdded(it)) }
            }

            launch(dispatcher) {
                runCatching { repository.getCompanyUpdatedStocks() }
                    .onSuccess { send(ProductEvent.StockUpdated(it.toMap())) }
            }

            launch(dispatcher) {
                runCatching { repository.getCompanyUpdatedPrices() }
                    .onSuccess { send(ProductEvent.PriceUpdated(it.toMap())) }
            }
        }
    }
}
