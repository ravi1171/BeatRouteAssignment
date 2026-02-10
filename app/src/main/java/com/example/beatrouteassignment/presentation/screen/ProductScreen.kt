package com.example.beatrouteassignment.presentation.screen

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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.beatrouteassignment.presentation.component.ProductUiState
import com.example.beatrouteassignment.presentation.viewmodel.ProductViewModel
import com.example.producthandling.model.Product

@Composable
fun ProductScreen(viewModel: ProductViewModel) {
    val uiState = viewModel.uiState.collectAsState()

    when (val state = uiState.value) {
        is ProductUiState.Loading -> Box(
            Modifier.fillMaxSize(),
            Alignment.Center
        ) { CircularProgressIndicator() }

        is ProductUiState.Success -> ProductList(state.products)
        is ProductUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error: ${state.message}")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { /* Retry */ }) { Text("Retry") }
            }
        }
    }
}

@Composable
fun ProductList(products: List<Product>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(products) { ProductItem(it); Divider() }
    }
}

@Composable
fun ProductItem(product: Product) = Column(Modifier
    .fillMaxWidth()
    .padding(8.dp)) {
    Text(product.name ?: "Unknown", style = MaterialTheme.typography.titleMedium)
    Text(product.description ?: "-", style = MaterialTheme.typography.bodyMedium)
    Text("Stock: ${product.stock ?: 0}", style = MaterialTheme.typography.bodyMedium)
    Text("Price: $${product.price ?: 0.0}", style = MaterialTheme.typography.bodyMedium)
}