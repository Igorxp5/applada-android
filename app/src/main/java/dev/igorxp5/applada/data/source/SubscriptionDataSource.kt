package dev.igorxp5.applada.data.source

import dev.igorxp5.applada.data.Subscription

interface SubscriptionDataSource {
    suspend fun getSubscriptions() : Result<List<Subscription>>
    suspend fun getSubscriptionByMatchId(matchId: String) : Result<Subscription?>
    suspend fun createSubscription(subscription: Subscription) : Result<Subscription>
    suspend fun deleteSubscriptionByMatchId(matchId: String) : Result<Boolean>
}