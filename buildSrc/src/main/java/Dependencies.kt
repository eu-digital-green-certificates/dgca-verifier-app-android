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
 *  Created by Mykhailo Nester on 4/23/21 9:49 AM
 */

object Deps {

    const val tools_gradle_android = "com.android.tools.build:gradle:${Versions.gradle}"
    const val tools_kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val kotlinx_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinx_coroutines}"
    const val androidx_core = "androidx.core:core-ktx:${Versions.androidx_core}"
    const val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.androidx_appcompat}"
    const val androidx_material = "com.google.android.material:material:${Versions.androidx_material}"
    const val androidx_constraint = "androidx.constraintlayout:constraintlayout:${Versions.androidx_constraint}"
    const val androidx_navigation_fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.androidx_navigation}"
    const val androidx_navigation_ui = "androidx.navigation:navigation-ui-ktx:${Versions.androidx_navigation}"
    const val androidx_navigation = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.androidx_navigation}"
    const val androidx_hilt_compiler = "androidx.hilt:hilt-compiler:${Versions.androidx_hilt_compiler}"
    const val androidx_room_runtime = "androidx.room:room-runtime:${Versions.androidx_room}"
    const val androidx_room_compiler = "androidx.room:room-compiler:${Versions.androidx_room}"
    const val retrofit2 = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val log_interceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.logging_interceptor}"
    const val gson_converter = "com.squareup.retrofit2:converter-gson:${Versions.gson_converter}"

    const val hilt_plugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt_version}"
    const val hilt = "com.google.dagger:hilt-android:${Versions.hilt_version}"
    const val hilt_compiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt_version}"
    const val hilt_viewmodel = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.androidx_hilt_viewmodel}"

    const val zxing = "com.journeyapps:zxing-android-embedded:${Versions.zxing}"
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin_reflect}"
    const val java_cose = "com.augustcellars.cose:cose-java:${Versions.java_cose}"
    const val json_validation = "com.github.fge:json-schema-validator:${Versions.json_validation}"
    const val jackson_cbor = "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:${Versions.jackson_cbor}"

    const val test_junit = "junit:junit:${Versions.junit}"
}