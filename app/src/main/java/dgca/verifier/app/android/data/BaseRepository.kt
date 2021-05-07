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
 *  Created by mykhailo.nester on 4/26/21 1:53 PM
 */

package dgca.verifier.app.android.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseRepository {

    suspend fun <P> execute(doOnAsyncBlock: suspend () -> P): P? {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                Timber.d("Do network coroutine work")
                doOnAsyncBlock.invoke()
            } catch (e: UnknownHostException) {
                Timber.w(e, "UnknownHostException")
                null
            } catch (e: SocketTimeoutException) {
                Timber.w(e, "SocketTimeoutException")
                null
            } catch (throwable: Throwable) {
                Timber.w(throwable, "Throwable")
                null
            }
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun ResponseBody.stringSuspending() =
    withContext(Dispatchers.IO) { string() }