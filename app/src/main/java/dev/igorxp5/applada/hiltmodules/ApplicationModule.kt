package dev.igorxp5.applada.hiltmodules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.igorxp5.applada.data.repositories.MatchRepository
import dev.igorxp5.applada.data.source.MatchDataSource
import dev.igorxp5.applada.data.source.local.AppLadaDatabase
import dev.igorxp5.applada.data.source.local.MatchLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
        @LocalMatchDataSource localMatchDataSource: MatchDataSource
    ): MatchDataSource {
        //TODO Change to use RemoteMatchDataSource
        return localMatchDataSource
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