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
 *  Created by osarapulov on 9/3/21 6:02 PM
 */

package dgca.verifier.app.android.diia.settings.debug.mode

import androidx.annotation.StringRes
import com.android.app.diia.R

enum class DebugModeState(@StringRes val stringRes: Int) {
    OFF(R.string.off),
    LEVEL_1(R.string.on_level_1),
    LEVEL_2(R.string.on_level_2),
    LEVEL_3(R.string.on_level_3)
}
