/*
 * Copyright (C) 2013-2017 microG Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.microg.gms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.R;

import org.microg.tools.ui.Condition;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;

public class Conditions {

    private static final String[] REQUIRED_PERMISSIONS = new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, GET_ACCOUNTS, READ_PHONE_STATE};
    public static final Condition PERMISSIONS = new Condition.Builder()
            .title(R.string.cond_perm_title)
            .summaryPlurals(R.plurals.cond_perm_summary)
            .evaluation(new Condition.Evaluation() {
                int count = 0;
                @Override
                public boolean isActive(Context context) {
                    count = 0;
                    if (SDK_INT >= 23) {
                        for (String permission : REQUIRED_PERMISSIONS) {
                            if (ContextCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED)
                                count++;
                        }
                    }
                    return count > 0;
                }

                @Override
                public int getPluralsCount() {
                    return count;
                }
            })
            .firstActionPlurals(R.plurals.cond_perm_action, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getContext() instanceof Activity) {
                        ActivityCompat.requestPermissions((Activity) v.getContext(), REQUIRED_PERMISSIONS, 0);
                    }
                }
            }).build();
}
