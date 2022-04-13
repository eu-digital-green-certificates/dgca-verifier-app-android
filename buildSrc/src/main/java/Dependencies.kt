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
    const val androidx_navigation_safe_args_plugin = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.androidx_navigation}"

    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val androidx_core = "androidx.core:core-ktx:${Versions.androidx_core}"
    const val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.androidx_appcompat}"
    const val androidx_material = "com.google.android.material:material:${Versions.androidx_material}"
    const val androidx_constraint = "androidx.constraintlayout:constraintlayout:${Versions.androidx_constraint}"
    const val desugar_jdk_libs = "com.android.tools:desugar_jdk_libs:${Versions.desugar_jdk_libs}"
    const val kotlinx_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinx_coroutines}"
    const val kotlinx_coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinx_coroutines_core}"
    const val androidx_navigation_fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.androidx_navigation}"
    const val androidx_navigation_ui = "androidx.navigation:navigation-ui-ktx:${Versions.androidx_navigation}"
    const val androidx_fragment_ktx = "androidx.fragment:fragment-ktx:${Versions.androidx_fragment_ktx}"
    const val androidx_hilt_compiler = "androidx.hilt:hilt-compiler:${Versions.androidx_hilt_compiler}"
    const val androidx_lifecycle_livedata_ktx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.androidx_lifecycle_livedata_ktx}"
    const val room_runtime = "androidx.room:room-runtime:${Versions.androidx_room}"
    const val room_compiler = "androidx.room:room-compiler:${Versions.androidx_room}"
    const val room_ktx = "androidx.room:room-ktx:${Versions.room_ktx}"
    const val androidx_worker_ktx = "androidx.work:work-runtime-ktx:${Versions.androidx_worker_ktx}"
    const val retrofit2 = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val log_interceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.logging_interceptor}"
    const val gson_converter = "com.squareup.retrofit2:converter-jackson:${Versions.gson_converter}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"

    const val android_tools_desugar_jdk_libs = "com.android.tools:desugar_jdk_libs:${Versions.android_tools_desugar_jdk_libs_version}"

    const val hilt_plugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt_version}"
    const val hilt = "com.google.dagger:hilt-android:${Versions.hilt_version}"
    const val hilt_android_compiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt_version}"
    const val hilt_viewmodel = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.androidx_hilt_viewmodel}"
    const val hilt_work = "androidx.hilt:hilt-work:${Versions.androidx_hilt_work}"
    const val hilt_compiler = "com.google.dagger:hilt-compiler:${Versions.hilt_compiler_version}"

    const val google_licenses_plugin = "com.google.android.gms:oss-licenses-plugin:${Versions.google_licenses_version}"

    const val zxing = "com.journeyapps:zxing-android-embedded:${Versions.zxing}"
    const val zxing_core = "com.google.zxing:core:${Versions.zxing_core}"
    const val guava_conflict_resolver = "com.google.guava:listenablefuture:${Versions.guave_conflict_resolver_version}"
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin_reflect}"
    const val java_cose = "com.augustcellars.cose:cose-java:${Versions.java_cose}"
    const val json_validation = "com.github.java-json-tools:json-schema-validator:${Versions.json_validation}"
    const val jackson_cbor = "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:${Versions.jackson_cbor}"
    const val bouncy_castle = "org.bouncycastle:bcpkix-jdk15to18:${Versions.bouncy_castle}"
    const val jackson_kotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson_kotlin}"
    const val jackson_datetype = "com.fasterxml.jackson.datatype:${Versions.jackson_datetype}"
    const val appache_commons = "commons-io:commons-io:${Versions.appache_commons}"
    const val appache_compress = "org.apache.commons:commons-compress:${Versions.appache_compress}"

    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    const val google_licenses = "com.google.android.gms:play-services-oss-licenses:${Versions.google_licenses}"

    const val test_junit = "junit:junit:${Versions.junit}"
    const val test_junit_jupiter_api = "org.junit.jupiter:junit-jupiter-api:${Versions.junit_jupiter}"
    const val test_junit_jupiter_params = "org.junit.jupiter:junit-jupiter-params:${Versions.junit_jupiter}"
    const val test_runtime_only = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit_jupiter}"
    const val test_hamcrest = "org.hamcrest:hamcrest:${Versions.hamcrest}"
    const val test_runner = "androidx.test:runner:${Versions.test_version}}"
    const val test_rules = "androidx.test:rules:${Versions.test_version}"
    const val test_ext = "androidx.test.ext:junit:${Versions.test_ext_version}"

    const val mockito_core = "org.mockito:mockito-core:${Versions.mockito_core}"
    const val mockito_kotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.mockito_kotlin}"
}