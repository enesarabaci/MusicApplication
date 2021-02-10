package com.example.musicapplication.DependencyInjection

import android.content.Context
import android.content.Intent
import com.example.musicapplication.Model.IntentClass
import com.example.musicapplication.Service.ForegroundService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Singleton
    @Provides
    fun getIntent(@ApplicationContext context: Context): Intent = Intent(context, ForegroundService::class.java)

    @Singleton
    @Provides
    fun getIntentClass(intent: Intent, @ApplicationContext context: Context): IntentClass = IntentClass(intent, context)

}