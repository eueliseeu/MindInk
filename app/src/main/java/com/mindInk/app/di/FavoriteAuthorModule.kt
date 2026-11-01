package com.mindInk.app.di

import com.mindInk.app.data.favorite.FavoriteAuthorRepositoryImpl
import com.mindInk.app.domain.repository.FavoriteAuthorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FavoriteAuthorModule {

    @Binds
    @Singleton
    abstract fun bindFavoriteAuthorRepository(
        impl: FavoriteAuthorRepositoryImpl
    ): FavoriteAuthorRepository
}