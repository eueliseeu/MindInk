package com.mindInk.app.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val connectivityManager: ConnectivityManager
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Checagem síncrona do estado atual da rede.
     * Usada ao voltar do segundo plano para não depender de callbacks perdidos.
     */
    fun isCurrentlyOnline(): Boolean {
        val manager = connectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasUsableInternet()
    }

    fun observe(): Flow<Boolean> = callbackFlow {
        val manager = connectivityManager
        val usesDefaultCallback = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        trySend(isCurrentlyOnline())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val online = if (usesDefaultCallback) {
                    networkCapabilities.hasUsableInternet()
                } else {
                    isCurrentlyOnline()
                }
                trySend(online)
            }

            override fun onLost(network: Network) {
                val online = if (usesDefaultCallback) false else isCurrentlyOnline()
                trySend(online)
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        if (usesDefaultCallback) {
            manager.registerDefaultNetworkCallback(callback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            manager.registerNetworkCallback(request, callback)
        }

        awaitClose {
            runCatching {
                manager.unregisterNetworkCallback(callback)
            }
        }
    }.distinctUntilChanged()

    private fun NetworkCapabilities.hasUsableInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}