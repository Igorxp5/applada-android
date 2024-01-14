package dev.igorxp5.applada.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.igorxp5.applada.data.Credential
import java.util.Date

@Dao
interface CredentialDao {
    @Query("SELECT * FROM credentials")
    suspend fun getCredentials(): List<Credential>

    @Query("SELECT * FROM credentials WHERE active = true LIMIT 1")
    suspend fun getActiveCredential(): Credential?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: Credential)

    suspend fun updateCredential(credential: Credential) {
        insertCredential(Credential(
            credential.username,
            credential.accessToken,
            credential.refreshToken,
            credential.createdDate,
            Date(),
            credential.active
        ))
    }

    @Query("UPDATE credentials SET active = false")
    suspend fun disableAllCredentials()

    @Delete
    suspend fun deleteCredential(credential: Credential)
}