/*
 * SPDX-FileCopyrightText: 2022 microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.fido.core.regular

import android.app.KeyguardManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Parcel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.Feature
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.common.internal.ConnectionInfo
import com.google.android.gms.common.internal.GetServiceRequest
import com.google.android.gms.common.internal.IGmsCallbacks
import com.google.android.gms.fido.fido2.api.IBooleanCallback
import com.google.android.gms.fido.fido2.api.ICredentialListCallback
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions
import com.google.android.gms.fido.fido2.internal.regular.IFido2RegularCallbacks
import com.google.android.gms.fido.fido2.internal.regular.IFido2RegularService
import org.microg.gms.BaseService
import org.microg.gms.common.GmsService
import org.microg.gms.common.GmsService.FIDO2_REGULAR
import org.microg.gms.fido.core.ui.AuthenticatorActivity
import org.microg.gms.fido.core.ui.AuthenticatorActivity.Companion.SOURCE_APP
import org.microg.gms.fido.core.ui.AuthenticatorActivity.Companion.KEY_SOURCE
import org.microg.gms.fido.core.ui.AuthenticatorActivity.Companion.KEY_OPTIONS
import org.microg.gms.fido.core.ui.AuthenticatorActivity.Companion.KEY_SERVICE
import org.microg.gms.fido.core.ui.AuthenticatorActivity.Companion.KEY_TYPE
import org.microg.gms.fido.core.ui.AuthenticatorActivity.Companion.TYPE_REGISTER
import org.microg.gms.fido.core.ui.AuthenticatorActivity.Companion.TYPE_SIGN
import org.microg.gms.utils.warnOnTransactionIssues

const val TAG = "Fido2Regular"

class Fido2RegularService : BaseService(TAG, FIDO2_REGULAR) {
    override fun handleServiceRequest(callback: IGmsCallbacks, request: GetServiceRequest, service: GmsService) {
        callback.onPostInitCompleteWithConnectionInfo(
            CommonStatusCodes.SUCCESS,
            Fido2RegularServiceImpl(this, lifecycle).asBinder(),
            ConnectionInfo().apply {
                features = arrayOf(
                    Feature("is_user_verifying_platform_authenticator_available", 1),
                    Feature("is_user_verifying_platform_authenticator_available_for_credential", 1)
                )
            }
        );
    }
}

class Fido2RegularServiceImpl(private val context: Context, private val lifecycle: Lifecycle) :
    IFido2RegularService.Stub(), LifecycleOwner {
    override fun getRegisterPendingIntent(callbacks: IFido2RegularCallbacks, options: PublicKeyCredentialCreationOptions) {
        lifecycleScope.launchWhenStarted {
            val intent = Intent(context, AuthenticatorActivity::class.java)
                .putExtra(KEY_SERVICE, FIDO2_REGULAR.SERVICE_ID)
                .putExtra(KEY_SOURCE, SOURCE_APP)
                .putExtra(KEY_TYPE, TYPE_REGISTER)
                .putExtra(KEY_OPTIONS, options.serializeToBytes())

            val pendingIntent =
                PendingIntent.getActivity(context, options.hashCode(), intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
            callbacks.onPendingIntent(Status.SUCCESS, pendingIntent)
        }
    }

    override fun getSignPendingIntent(callbacks: IFido2RegularCallbacks, options: PublicKeyCredentialRequestOptions) {
        lifecycleScope.launchWhenStarted {
            val intent = Intent(context, AuthenticatorActivity::class.java)
                .putExtra(KEY_SERVICE, FIDO2_REGULAR.SERVICE_ID)
                .putExtra(KEY_SOURCE, SOURCE_APP)
                .putExtra(KEY_TYPE, TYPE_SIGN)
                .putExtra(KEY_OPTIONS, options.serializeToBytes())

            val pendingIntent =
                PendingIntent.getActivity(context, options.hashCode(), intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
            callbacks.onPendingIntent(Status.SUCCESS, pendingIntent)
        }
    }

    override fun isUserVerifyingPlatformAuthenticatorAvailable(callbacks: IBooleanCallback) {
        lifecycleScope.launchWhenStarted {
            if (SDK_INT < 24) {
                callbacks.onBoolean(false)
            } else {
                val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as? KeyguardManager?
                callbacks.onBoolean(keyguardManager?.isDeviceSecure == true)
            }
        }
    }

    override fun getCredentialList(callbacks: ICredentialListCallback, rpId: String) {
        lifecycleScope.launchWhenStarted {
            runCatching { callbacks.onCredentialList(emptyList()) }
        }
    }

    override fun getLifecycle(): Lifecycle = lifecycle

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean =
        warnOnTransactionIssues(code, reply, flags, TAG) { super.onTransact(code, data, reply, flags) }
}
