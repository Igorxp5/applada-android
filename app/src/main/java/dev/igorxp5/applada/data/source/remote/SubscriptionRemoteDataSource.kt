package dev.igorxp5.applada.data.source.remote

import android.util.Log
import dev.igorxp5.applada.data.Subscription
import dev.igorxp5.applada.data.source.Result
import dev.igorxp5.applada.data.source.SubscriptionDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionRemoteDataSource internal constructor(
    private val api: AppLadaApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SubscriptionDataSource {
    override suspend fun getSubscriptions(): Result<List<Subscription>> = withContext(ioDispatcher) {
        return@withContext try {
            val subscriptions = api.getUserSubscriptions()
            Result.Success(subscriptions)
        } catch (exc: Exception) {
            Log.e(LOG_TAG, exc.stackTraceToString())
            Result.Error<List<Subscription>>(exc)
        }
    }

    override suspend fun getSubscriptionByMatchId(matchId: String): Result<Subscription?> = withContext(ioDispatcher) {
        // FIXME: On real-world /subscriptions?match_id=ID could be used. Since this application uses
        //  a mock API the lookup need to be done in client side.
        return@withContext try {
            val subscriptions = api.getUserSubscriptions()
            Result.Success(subscriptions.find { it.matchId == matchId })
        } catch (exc: Exception) {
            Log.e(LOG_TAG, exc.stackTraceToString())
            Result.Error<Subscription?>(exc)
        }
    }

    override suspend fun createSubscription(subscription: Subscription): Result<Subscription> = withContext(ioDispatcher) {
        return@withContext try {
            val createdSubscription = api.createUserSubscription(subscription)
            Result.Success(createdSubscription)
        } catch (exc: Exception) {
            Log.e(LOG_TAG, exc.stackTraceToString())
            Result.Error<Subscription>(exc)
        }
    }

    override suspend fun deleteSubscriptionByMatchId(matchId: String): Result<Boolean> = withContext(ioDispatcher) {
        return@withContext try {
            val subscriptionResult = getSubscriptionByMatchId(matchId)
            if (subscriptionResult is Result.Success) {
                if (subscriptionResult.data != null) {
                    api.deleteUserSubscription(subscriptionResult.data.id)
                    Result.Success(true)
                } else {
                    throw Exception("No subscription found for the match")
                }
            } else {
                Result.Error((subscriptionResult as Result.Error).exception)
            }
        } catch (exc: Exception) {
            Log.e(LOG_TAG, exc.stackTraceToString())
            Result.Error<Boolean>(exc)
        }
    }

    companion object {
        const val LOG_TAG = "SubscriptionRemoteDataSource"
    }
}