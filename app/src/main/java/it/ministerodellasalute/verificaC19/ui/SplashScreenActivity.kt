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
 *  Created by danieliulianrotaru on 5/26/21 9:45 PM
 */

package it.ministerodellasalute.verificaC19.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import it.ministerodellasalute.verificaC19.databinding.ActivitySplashBinding


class SplashScreenActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Handler().postDelayed({
            val intent = Intent(this, FirstActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }
}