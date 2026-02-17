package com.example.beatrouteassignment.di

import com.example.producthandling.StreamLibAPI
import com.example.producthandling.StreamLibProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideStreamLibAPI(): StreamLibAPI {
        return StreamLibProvider.instance
    }
}