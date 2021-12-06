/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by nicolamcornelio on 11/29/21, 2:26 PM
 */

package it.ministerodellasalute.verificaC19.ui

import android.app.AlertDialog
import android.content.Context
import android.provider.Settings.Global.getString
import it.ministerodellasalute.verificaC19.R
import it.ministerodellasalute.verificaC19sdk.model.VerificationViewModel

class AlertDialogCaller {

    companion object {
        fun showScanModeChoiceAlertDialog(
            context: Context,
            title: String,
            message: String,
        ) {
            val mBuilder = AlertDialog.Builder(context)
            mBuilder.setTitle(title)
            mBuilder.setMessage(message)
            val mDialog = mBuilder.create()
            mDialog.show()
        }
    }
}