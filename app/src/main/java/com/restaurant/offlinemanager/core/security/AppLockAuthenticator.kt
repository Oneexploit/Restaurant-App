package com.restaurant.offlinemanager.core.security

import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

data class AppLockAvailability(
    val canUse: Boolean,
    val statusLabel: String,
    val supportedMethodsLabel: String,
    val needsDeviceSetup: Boolean
)

object AppLockAuthenticator {
    private const val MODERN_AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

    fun availability(context: Context): AppLockAvailability {
        val biometricStatus = BiometricManager.from(context).canAuthenticate(supportedAuthenticatorsForCheck())
        val keyguardManager = context.getSystemService(KeyguardManager::class.java)
        val hasSecureDeviceCredential = keyguardManager?.isDeviceSecure == true
        val supportedMethods = supportedMethodsLabel(context, hasSecureDeviceCredential)
        val canUseLegacyDeviceCredential = Build.VERSION.SDK_INT < Build.VERSION_CODES.R && hasSecureDeviceCredential

        return when (biometricStatus) {
            BiometricManager.BIOMETRIC_SUCCESS -> AppLockAvailability(
                canUse = true,
                statusLabel = "آماده استفاده",
                supportedMethodsLabel = supportedMethods,
                needsDeviceSetup = false
            )

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AppLockAvailability(
                canUse = false,
                statusLabel = "ابتدا اثر انگشت، چهره یا قفل صفحه را روی گوشی تنظیم کنید",
                supportedMethodsLabel = supportedMethods,
                needsDeviceSetup = true
            )

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> AppLockAvailability(
                canUse = canUseLegacyDeviceCredential,
                statusLabel = if (hasSecureDeviceCredential) "قفل صفحه دستگاه قابل استفاده است" else "این دستگاه حسگر بیومتریک ندارد",
                supportedMethodsLabel = supportedMethods,
                needsDeviceSetup = !hasSecureDeviceCredential
            )

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> AppLockAvailability(
                canUse = canUseLegacyDeviceCredential,
                statusLabel = if (hasSecureDeviceCredential) "حسگر بیومتریک فعلا در دسترس نیست؛ قفل صفحه قابل استفاده است" else "احراز هویت دستگاه فعلا در دسترس نیست",
                supportedMethodsLabel = supportedMethods,
                needsDeviceSetup = !hasSecureDeviceCredential
            )

            else -> AppLockAvailability(
                canUse = canUseLegacyDeviceCredential,
                statusLabel = if (hasSecureDeviceCredential) "قفل صفحه دستگاه قابل استفاده است" else "احراز هویت امن روی دستگاه آماده نیست",
                supportedMethodsLabel = supportedMethods,
                needsDeviceSetup = !hasSecureDeviceCredential
            )
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        runCatching {
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        onError(errString.toString())
                    }

                    override fun onAuthenticationFailed() {
                        onError("هویت تایید نشد؛ دوباره تلاش کنید")
                    }
                }
            )

            val promptInfo = promptInfo(title, subtitle)
            prompt.authenticate(promptInfo)
        }.onFailure { error ->
            onError(error.message ?: "امکان اجرای احراز هویت روی این دستگاه پیدا نشد")
        }
    }

    @Suppress("DEPRECATION")
    private fun promptInfo(title: String, subtitle: String): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(MODERN_AUTHENTICATORS)
        } else {
            builder.setDeviceCredentialAllowed(true)
        }
        return builder.build()
    }

    fun openDeviceSecuritySettings(context: Context) {
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    private fun supportedMethodsLabel(context: Context, hasSecureDeviceCredential: Boolean): String {
        val packageManager = context.packageManager
        val methods = buildList {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) add("اثر انگشت")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)) add("تشخیص چهره")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && packageManager.hasSystemFeature(PackageManager.FEATURE_IRIS)) add("عنبیه")
            if (hasSecureDeviceCredential) add("PIN / Pattern / Password")
        }
        return methods.distinct().takeIf { it.isNotEmpty() }?.joinToString("، ") ?: "قفل امنی روی دستگاه شناسایی نشد"
    }

    private fun supportedAuthenticatorsForCheck(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) MODERN_AUTHENTICATORS else BIOMETRIC_STRONG
}

fun Context.findFragmentActivity(): FragmentActivity? =
    when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
