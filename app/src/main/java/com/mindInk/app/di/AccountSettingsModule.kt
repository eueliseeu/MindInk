package com.mindInk.app.di

import com.mindInk.app.data.settings.AccountSettingsRepositoryImpl
import com.mindInk.app.domain.repository.AccountSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AccountSettingsModule {

    @Binds
    @Singleton
    abstract fun bindAccountSettingsRepository(
        impl: AccountSettingsRepositoryImpl
    ): AccountSettingsRepository
}