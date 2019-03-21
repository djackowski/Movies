package com.jackowski.movies.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class ConnectionStateMonitor(context: Context, var connectivityReceiverListener: ConnectivityReceiverListener?) :
    ConnectivityManager.NetworkCallback() {

    private var networkRequest: NetworkRequest? = null
    private var connectivityManager: ConnectivityManager? = null

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    init {
        networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun enable() {
        connectivityManager?.registerNetworkCallback(networkRequest, this)
    }

    fun disable() {
        connectivityManager?.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network?) {
        super.onAvailable(network)
        connectivityReceiverListener?.onNetworkConnectionChanged(true)
    }

    override fun onLost(network: Network?) {
        super.onLost(network)
        connectivityReceiverListener?.onNetworkConnectionChanged(false)
    }
}