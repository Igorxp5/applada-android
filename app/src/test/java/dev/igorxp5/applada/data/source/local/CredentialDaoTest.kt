package dev.igorxp5.applada.data.source.local

import androidx.room.Room
import org.junit.Test

import org.junit.Before

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.igorxp5.applada.data.Credential
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.runner.RunWith
import java.util.Date

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CredentialDaoTest {

    private lateinit var database: AppLadaDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            AppLadaDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertCredential() = runTest {
        val currentDate = Date()
        val credential = Credential(
            username = "theuser",
            accessToken = "theaccesstoken",
            refreshToken = "therefreshtoken",
            createdDate = currentDate,
            updatedDate = currentDate
        )
        database.credentialDao().insertCredential(credential)

        val fetched = database.credentialDao().getActiveCredential()

        assertThat(fetched as Credential, notNullValue())
        assertThat(fetched.username, `is`(credential.username))
        assertThat(fetched.accessToken, `is`(credential.accessToken))
        assertThat(fetched.accessToken, `is`(credential.accessToken))
        assertThat(fetched.refreshToken, `is`(credential.refreshToken))
        assertThat(fetched.createdDate, `is`(credential.createdDate))
        assertThat(fetched.updatedDate, `is`(credential.updatedDate))
        assertThat(fetched.active, `is`(credential.active))
    }
}