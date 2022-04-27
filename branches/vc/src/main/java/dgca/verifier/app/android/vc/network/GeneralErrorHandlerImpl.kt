/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by mykhailo.nester on 24/12/2021, 15:47
 */

package dgca.verifier.app.android.vc.network

import dgca.verifier.app.android.vc.domain.ErrorHandler
import dgca.verifier.app.android.vc.domain.ErrorType
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class GeneralErrorHandlerImpl @Inject constructor() : ErrorHandler {

    override fun getError(throwable: Throwable): ErrorType {
        return when (throwable) {
            is IOException -> ErrorType.Network
            is HttpException -> {
                when (throwable.code()) {
                    HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> ErrorType.Network
                    HttpURLConnection.HTTP_NOT_FOUND -> ErrorType.NotFound
                    HttpURLConnection.HTTP_FORBIDDEN -> ErrorType.AccessDenied
                    HttpURLConnection.HTTP_INTERNAL_ERROR -> ErrorType.InternalServer
                    HttpURLConnection.HTTP_UNAVAILABLE -> ErrorType.ServiceUnavailable
                    HttpURLConnection.HTTP_PRECON_FAILED -> ErrorType.PreconditionFailedException
                    else -> ErrorType.Unknown
                }
            }
            is UnknownHostException -> ErrorType.UnknownHostException
            is SocketTimeoutException -> ErrorType.SocketTimeoutException
            else -> ErrorType.Unknown
        }
    }
}

fun Response<*>.containsServerError(): Boolean = code() in 500..599 || code() == 412