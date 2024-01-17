package dev.igorxp5.applada.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.igorxp5.applada.data.Credential
import dev.igorxp5.applada.data.User
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.Subscription

// TODO enable exportSchema to have database changing history tracked in Git
@Database(entities = [Match::class, User::class, Credential::class, Subscription::class], version = 4, exportSchema = false)
abstract class AppLadaDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao

    abstract fun userDao(): UserDao

    abstract fun credentialDao(): CredentialDao

    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        const val DATABASE_FILENAME = "main.db"
    }
}
