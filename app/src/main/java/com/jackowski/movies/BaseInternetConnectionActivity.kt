package com.jackowski.movies

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.jackowski.movies.utils.ConnectionStateMonitor
import com.jackowski.movies.utils.InternetConnectivityManager

abstract class BaseInternetConnectionActivity : AppCompatActivity(),
    ConnectionStateMonitor.ConnectivityReceiverListener {
    private var connectionStateMonitor: ConnectionStateMonitor? = null
    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionStateMonitor = ConnectionStateMonitor(this, this)

    }

    override fun onResume() {
        super.onResume()
        connectionStateMonitor?.enable()
        val online = InternetConnectivityManager.isOnline(this)
        if(!online) {
            doOnConnectionLost()
        }
    }

    override fun onPause() {
        connectionStateMonitor?.disable()
        super.onPause()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            doOnConnectionAvailable()
        } else {
            doOnConnectionLost()
        }
    }

    private fun doOnConnectionLost() {
        snackBar = Snackbar.make(findViewById(android.R.id.content),
            getString(R.string.no_internet_connection_message),
            Snackbar.LENGTH_INDEFINITE)

        snackBar?.show()

        runOnUiThread {
            onInternetConnectionLost()
        }
    }

    private fun doOnConnectionAvailable() {
        runOnUiThread {
            onInternetConnectionAvailable()
        }
        snackBar?.dismiss()
    }


    abstract fun onInternetConnectionAvailable()

    abstract fun onInternetConnectionLost()
}