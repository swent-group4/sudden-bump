package com.swent.suddenbump.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

@Synchronized
fun isInternetAvailable(context: Context): Boolean {
  if (isMockUsingOfflineMode) return false
  if (isMockUsingOnlineDefaultValue) return testableOnlineDefaultValue
  val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  val activeNetwork = connectivityManager.activeNetwork ?: return false
  val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
  return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
