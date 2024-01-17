package dev.igorxp5.applada.data.source.local

import android.util.Log
import dev.igorxp5.applada.data.Subscription
import dev.igorxp5.applada.data.source.Result
import dev.igorxp5.applada.data.source.SubscriptionDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionLocalDataSource internal constructor(
    private val subscriptionDao: SubscriptionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SubscriptionDataSource {

    override suspend fun getSubscriptions(): Result<List<Subscription>> = withContext(ioDispatcher) {
        return@withContext try {
            val subscriptions = subscriptionDao.getSubscriptions()
            Result.Success(subscriptions)
        } catch (exc: Exception) {
            Log.e(LOG_TAG, exc.stackTraceToString())
            Result.Error<List<Subscription>>(exc)
        }
    }

    override suspend fun getSubscriptionByMatchId(matchId: String): Result<Subscription?> = withContext(ioDispatcher) {
        return@withContext try {
            val subscription = subscriptionDao.getSubscriptionByMatchId(matchId)
            Result.Success(subscription)
        } catch (exc: Exception) {
            Log.e(LOG_TAG, exc.stackTraceToString())
            Result.Error<Subscription?>(exc)
        }
    }

    override suspend fun createSubscription(subscription: Subscription): Result<Subscription> = withContext(ioDispatcher) {
        return@withContext try {
            subscriptionDao.insertOrUpdateSubscription(subscription)
            Result.Success(subscription)
        } catch (exc: Exception) {
            Log.e(LOG_TAG, exc.stackTraceToString())
            Result.Error<Subscription>(exc)
        }
    }

    override suspend fun deleteSubscriptionByMatchId(matchId: String): Result<Boolean> = withContext(ioDispatcher) {
        return@withContext try {
            subscriptionDao.deleteSubscriptionByMatchId(matchId)
            Result.Success(true)
        } catch (exc: Exception) {
            Log.e(LOG_TAG, exc.stackTraceToString())
            Result.Error<Boolean>(exc)
        }
    }

    companion object {
        const val LOG_TAG = "SubscriptionLocalDataSource"
    }
}