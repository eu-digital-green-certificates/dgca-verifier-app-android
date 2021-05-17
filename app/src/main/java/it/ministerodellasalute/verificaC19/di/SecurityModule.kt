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
 *  Created by osarapulov on 4/30/21 1:53 AM
 */

package it.ministerodellasalute.verificaC19.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.ministerodellasalute.verificaC19.security.DefaultKeyStoreCryptor
import it.ministerodellasalute.verificaC19.security.KeyStoreCryptor
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SecurityModule {
    @Singleton
    @Binds
    abstract fun bindKeyStoreCryptor(keyStoreCryptor: DefaultKeyStoreCryptor): KeyStoreCryptor
}