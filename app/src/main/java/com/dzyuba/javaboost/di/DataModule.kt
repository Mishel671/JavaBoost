package com.dzyuba.javaboost.di

import com.dzyuba.javaboost.data.FirebaseRepositoryImpl
import com.dzyuba.javaboost.domain.FirebaseRepository
import dagger.Binds
import dagger.Module

@Module
interface DataModule {

    @Binds
    @AppScope
    fun bindFirebaseRepository(impl: FirebaseRepositoryImpl): FirebaseRepository

}