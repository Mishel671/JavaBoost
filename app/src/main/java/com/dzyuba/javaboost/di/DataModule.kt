package com.dzyuba.javaboost.di

import com.dzyuba.javaboost.data.repositories.LessonRepositoryImpl
import com.dzyuba.javaboost.data.repositories.ProfileRepositoryImpl
import com.dzyuba.javaboost.domain.LessonRepository
import com.dzyuba.javaboost.domain.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @Binds
    @AppScope
    fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @AppScope
    fun bindLessonRepository(impl: LessonRepositoryImpl): LessonRepository

    companion object{
        @AppScope
        @Provides
        fun provideFirebaseAuth():FirebaseAuth = Firebase.auth

        @AppScope
        @Provides
        fun provideFirebaseDatabase():DatabaseReference = Firebase.database.reference

        @AppScope
        @Provides
        fun provideFirebaseStorage():StorageReference = Firebase.storage.reference
    }

}