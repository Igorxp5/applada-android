package dev.igorxp5.applada.hiltmodules

import android.content.Context
import androidx.room.Room
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.igorxp5.applada.data.repositories.MatchRepository
import dev.igorxp5.applada.data.source.MatchDataSource
import dev.igorxp5.applada.data.source.local.AppLadaDatabase
import dev.igorxp5.applada.data.source.local.MatchLocalDataSource
import dev.igorxp5.applada.data.source.remote.AppLadaApi
import dev.igorxp5.applada.data.source.remote.MatchRemoteDataSource
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
        val jsonObjectMapper = ObjectMapper().registerKotlinModule()

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
    fun provideTasksRepository(
        @RemoteMatchDataSource remoteMatchDataSource: MatchDataSource,
        @LocalMatchDataSource localMatchDataSource: MatchDataSource,
        ioDispatcher: CoroutineDispatcher
    ): MatchRepository {
        return MatchRepository(
            remoteMatchDataSource, localMatchDataSource, ioDispatcher
        )
    }
}