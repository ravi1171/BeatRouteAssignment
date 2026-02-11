package com.example.beatrouteassignment.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.beatrouteassignment.presentation.component.ProductUiState
import com.example.beatrouteassignment.presentation.viewmodel.ProductViewModel
import com.example.producthandling.model.Product


@Composable
fun ProductScreen(viewModel: ProductViewModel) {

    val state by viewModel.uiState.collectAsState()

    when (state) {

        is ProductUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is ProductUiState.Success -> {
            val products = (state as ProductUiState.Success).products

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No products available")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = products, key = { it.id }) { product ->
                        ProductItem(product)
                        HorizontalDivider()
                    }
                }
            }
        }

        is ProductUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Something went wrong",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product) = Column(
    Modifier
        .fillMaxWidth()
        .padding(8.dp)
) {
    Text(product.name ?: "Unknown", style = MaterialTheme.typography.titleMedium)
    Text(product.description ?: "-", style = MaterialTheme.typography.bodyMedium)
    Text("Stock: ${product.stock ?: 0}", style = MaterialTheme.typography.bodyMedium)
    Text("Price: $${product.price ?: 0.0}", style = MaterialTheme.typography.bodyMedium)
}
