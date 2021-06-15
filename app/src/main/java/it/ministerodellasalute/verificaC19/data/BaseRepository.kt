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

package it.ministerodellasalute.verificaC19.data

import android.util.Log
import it.ministerodellasalute.verificaC19.di.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val TAG = "BaseRepository"

abstract class BaseRepository(private val dispatcherProvider: DispatcherProvider) : Repository {

    suspend fun <P> execute(doOnAsyncBlock: suspend () -> P): P? {
        return withContext(dispatcherProvider.getIO()) {
            return@withContext try {
                Log.v(TAG, "Do network coroutine work")
                doOnAsyncBlock.invoke()
            } catch (e: UnknownHostException) {
                Log.w(TAG, "UnknownHostException", e)
                null
            } catch (e: SocketTimeoutException) {
                Log.w(TAG, "SocketTimeoutException", e)
                null
            } catch (throwable: Throwable) {
                Log.w(TAG, "Throwable", throwable)
                null
            }
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun ResponseBody.stringSuspending(dispatcherProvider: DispatcherProvider) =
    withContext(dispatcherProvider.getIO()) { string() }