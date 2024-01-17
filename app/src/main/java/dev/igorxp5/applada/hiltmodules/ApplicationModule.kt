package dev.igorxp5.applada.hiltmodules

import android.content.Context
import androidx.room.Room
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.igorxp5.applada.data.repositories.MatchRepository
import dev.igorxp5.applada.data.repositories.SubscriptionRepository
import dev.igorxp5.applada.data.source.MatchDataSource
import dev.igorxp5.applada.data.source.SubscriptionDataSource
import dev.igorxp5.applada.data.source.local.AppLadaDatabase
import dev.igorxp5.applada.data.source.local.MatchLocalDataSource
import dev.igorxp5.applada.data.source.local.SubscriptionLocalDataSource
import dev.igorxp5.applada.data.source.remote.AppLadaApi
import dev.igorxp5.applada.data.source.remote.MatchRemoteDataSource
import dev.igorxp5.applada.data.source.remote.SubscriptionRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class LocalMatchDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class RemoteMatchDataSource

    @Singleton
    @LocalMatchDataSource
    @Provides
    fun provideLocalMatchDataSource(
        database: AppLadaDatabase,
        ioDispatcher: CoroutineDispatcher
    ): MatchDataSource {
        return MatchLocalDataSource(
            database.matchDao(), ioDispatcher
        )
    }

    @Singleton
    @RemoteMatchDataSource
    @Provides
    fun provideRemoteMatchDataSource(
        api: AppLadaApi,
        ioDispatcher: CoroutineDispatcher
    ): MatchDataSource {
        return MatchRemoteDataSource(api, ioDispatcher)
    }

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class LocalSubscriptionDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class RemoteSubscriptionDataSource

    @Singleton
    @LocalSubscriptionDataSource
    @Provides
    fun provideLocalSubscriptionDataSource(
        database: AppLadaDatabase,
        ioDispatcher: CoroutineDispatcher
    ): SubscriptionDataSource {
        return SubscriptionLocalDataSource(
            database.subscriptionDao(), ioDispatcher
        )
    }

    @Singleton
    @RemoteSubscriptionDataSource
    @Provides
    fun provideRemoteSubscriptionDataSource(
        api: AppLadaApi,
        ioDispatcher: CoroutineDispatcher
    ): SubscriptionDataSource {
        return SubscriptionRemoteDataSource(api, ioDispatcher)
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppLadaDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppLadaDatabase::class.java,
            AppLadaDatabase.DATABASE_FILENAME
        ).build()
    }

    @Singleton
    @Provides
    fun provideAppLadaApi(): AppLadaApi {
        // Enable Kotlin-way to set default value for JSON deserialization
        //  and using ISO8601 for Date due to the API server limitation
        val jsonObjectMapper = ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(StdDateFormat().withColonInTimeZone(true))
            .registerKotlinModule()

        return Retrofit.Builder()
            .baseUrl(AppLadaApi.API_BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create(jsonObjectMapper))
            .build()
            .create(AppLadaApi::class.java)
    }

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Singleton
    @Provides
    fun provideMatchRepository(
        @RemoteMatchDataSource remoteMatchDataSource: MatchDataSource,
        @LocalMatchDataSource localMatchDataSource: MatchDataSource,
        ioDispatcher: CoroutineDispatcher
    ): MatchRepository {
        return MatchRepository(
            remoteMatchDataSource, localMatchDataSource, ioDispatcher
        )
    }

    @Singleton
    @Provides
    fun provideSubscriptionRepository(
        @RemoteSubscriptionDataSource remoteSubscriptionDataSource: SubscriptionDataSource,
        @LocalSubscriptionDataSource localSubscriptionDataSource: SubscriptionDataSource,
        ioDispatcher: CoroutineDispatcher
    ): SubscriptionRepository {
        return SubscriptionRepository(
            remoteSubscriptionDataSource, localSubscriptionDataSource, ioDispatcher
        )
    }
}