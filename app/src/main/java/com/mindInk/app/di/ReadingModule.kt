package com.mindInk.app.di

import com.mindInk.app.data.reading.ReadingProgressRepositoryImpl
import com.mindInk.app.domain.repository.ReadingProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ReadingModule {

    @Binds
    @Singleton
    abstract fun bindReadingProgressRepository(
        impl: ReadingProgressRepositoryImpl
    ): ReadingProgressRepository
}