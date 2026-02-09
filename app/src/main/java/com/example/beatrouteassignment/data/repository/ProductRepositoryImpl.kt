package com.example.beatrouteassignment.data.repository

import com.example.beatrouteassignment.data.remote.ProductRemoteDataSource
import com.example.beatrouteassignment.di.IoDispatcher
import com.example.beatrouteassignment.domain.model.ProductUpdate
import com.example.beatrouteassignment.domain.repository.ProductRepository
import com.example.producthandling.model.Product
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    private val _productUpdates =
        MutableStateFlow<ProductUpdate>(ProductUpdate.Initial(emptyList()))
    override val productUpdates: StateFlow<ProductUpdate> = _productUpdates.asStateFlow()

    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override fun observeProductUpdates(): StateFlow<ProductUpdate> {
        repositoryScope.launch {
            try {
                val currentProducts = remoteDataSource.getAllProducts().toMutableList()
                _productUpdates.value = ProductUpdate.Initial(currentProducts.toList())

                val tax = remoteDataSource.getPriceTax()
                currentProducts.applyTax(tax)
                _productUpdates.value = ProductUpdate.PricesUpdated(currentProducts.toList())

                supervisorScope {
                    val deferredDelete = async { remoteDataSource.getProductsToDelete() }
                    val deferredNew = async { remoteDataSource.getNewProducts() }
                    val deferredStock = async { remoteDataSource.getCompanyUpdatedStocks() }
                    val deferredPrices = async { remoteDataSource.getCompanyUpdatedPrices() }

                    deferredDelete.await().let { ids ->
                        currentProducts.removeAll { it.id in ids }
                        _productUpdates.value =
                            ProductUpdate.ProductsDeleted(currentProducts.toList())
                    }

                    deferredNew.await().let { newProducts ->
                        currentProducts.addAll(newProducts.applyTaxToList(tax))
                        _productUpdates.value =
                            ProductUpdate.NewProductsAdded(currentProducts.toList())
                    }

                    deferredStock.await().toMap().let { stockMap ->
                        currentProducts.updateStocks(stockMap)
                        _productUpdates.value =
                            ProductUpdate.StocksUpdated(currentProducts.toList())
                    }

                    deferredPrices.await().toMap().let { priceMap ->
                        currentProducts.updatePrices(priceMap)
                        _productUpdates.value =
                            ProductUpdate.PricesUpdated(currentProducts.toList())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return productUpdates
    }

    private fun MutableList<Product>.applyTax(tax: Double) = this.replaceAll { p ->
        val price = p.price ?: 0.0
        p.copy(price = price * (1 + tax / 100))
    }

    private fun List<Product>.applyTaxToList(tax: Double) = this.map { p ->
        val price = p.price ?: 0.0
        p.copy(price = price * (1 + tax / 100))
    }

    private fun MutableList<Product>.updateStocks(stockMap: Map<Int, Int>) = this.replaceAll { p ->
        stockMap[p.id]?.let { p.copy(stock = it) } ?: p
    }

    private fun MutableList<Product>.updatePrices(priceMap: Map<Int, Double>) =
        this.replaceAll { p ->
            priceMap[p.id]?.let { p.copy(price = it) } ?: p
    }
}
