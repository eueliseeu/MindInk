package com.mindInk.app.di

import com.mindInk.app.data.star.StarRepositoryImpl
import com.mindInk.app.domain.repository.StarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class StarModule {

    @Binds
    @Singleton
    abstract fun bindStarRepository(impl: StarRepositoryImpl): StarRepository
}