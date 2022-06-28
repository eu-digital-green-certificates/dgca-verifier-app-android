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
 *  Created by mykhailo.nester on 24/12/2021, 15:45
 */

package dcc.app.revocation.domain

/**
 * Error handler that can convert data layer errors into domain layer types.
 */
interface ErrorHandler {

    /**
     * Convert Throwable into app readable error type.
     *
     * @param throwable data storage or network error
     * @return dcc.app.revocation.domain.ErrorType domain app type
     */
    fun getError(throwable: Throwable): ErrorType
}