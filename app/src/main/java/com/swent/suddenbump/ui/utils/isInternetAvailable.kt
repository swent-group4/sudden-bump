package com.swent.suddenbump.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Checks whether the internet is available on the device.
 *
 * This function is thread-safe and uses a synchronized block to prevent concurrent modifications.
 * It evaluates the network availability based on the device's connectivity state and active network
 * capabilities.
 *
 * Special cases:
 * - If the `isMockUsingOfflineMode` flag is set to `true`, the function immediately returns
 *   `false`.
 * - If the `isMockUsingOnlineDefaultValue` flag is set to `true`, the function returns the value of
 *   `testableOnlineDefaultValue`.
 *
 * @param context The application or activity context required to access the system's connectivity
 *   services.
 * @return `true` if the internet is available, `false` otherwise.
 *
 * The function works as follows:
 * 1. Retrieves the `ConnectivityManager` system service using the provided context.
 * 2. Checks if there is an active network connection; if not, returns `false`.
 * 3. Obtains the network capabilities of the active network and verifies if the network supports
 *    internet connectivity.
 *
 * @throws ClassCastException If the context does not return a valid `ConnectivityManager` when
 *   accessing the connectivity service.
 * @throws NullPointerException If the system services return null values, indicating the device may
 *   be in an invalid state.
 */
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
