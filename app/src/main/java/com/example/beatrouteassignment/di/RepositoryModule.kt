package com.example.beatrouteassignment.di

import com.example.beatrouteassignment.data.remote.ProductRemoteDataSource
import com.example.beatrouteassignment.data.remote.ProductRemoteDataSourceImpl
import com.example.beatrouteassignment.data.repository.ProductRepositoryImpl
import com.example.beatrouteassignment.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindRemoteDataSource(impl: ProductRemoteDataSourceImpl): ProductRemoteDataSource

    @Module
    @InstallIn(SingletonComponent::class)
    object DispatcherModule {
        @Provides
        @IoDispatcher
        fun provideIoDispatcher() = Dispatchers.IO
    }
}