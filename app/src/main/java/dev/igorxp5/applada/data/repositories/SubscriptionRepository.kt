package dev.igorxp5.applada.data.repositories

import dev.igorxp5.applada.data.Subscription
import dev.igorxp5.applada.data.source.Result
import dev.igorxp5.applada.data.source.SubscriptionDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SubscriptionRepository(
    private val remoteSource: SubscriptionDataSource,
    private val localSource: SubscriptionDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getSubscriptions() : Result<List<Subscription>> {
        var result : Result<List<Subscription>> = remoteSource.getSubscriptions()
        if (result is Result.Error<*>) {
            result = Result.Error(result.exception, localSource.getSubscriptions())
        } else if (result is Result.Success) {
            result.data.forEach {
                localSource.createSubscription(it)
            }
        }
        return result
    }

    suspend fun getMatchSubscription(matchId: String) : Result<Subscription?> {
        var result : Result<Subscription?> = remoteSource.getSubscriptionByMatchId(matchId)
        if (result is Result.Success && result.data != null) {
            localSource.createSubscription(result.data!!)
        }
        return result
    }

    suspend fun createMatchSubscription(matchId: String) : Result<Boolean> {
        val subscription = Subscription("", matchId)
        var result : Result<Subscription> = remoteSource.createSubscription(subscription)
        if (result is Result.Success) {
            val createdSubscription = result.data
            localSource.createSubscription(createdSubscription)
            return Result.Success(true)
        }
        return Result.Error((result as Result.Error).exception)
    }

    suspend fun cancelMatchSubscription(matchId: String) : Result<Boolean> {
        var result = remoteSource.deleteSubscriptionByMatchId(matchId)
        if (result is Result.Success) {
            result = localSource.deleteSubscriptionByMatchId(matchId)
        }
        return result
    }
}