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
 *  Created by osarapulov on 8/30/21 8:58 AM
 */

package dgca.verifier.app.android.verification

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dgca.verifier.app.android.dpToPx

abstract class BaseVerificationDialogFragment<T : ViewBinding> : BottomSheetDialogFragment() {
    private val hideLiveData: MutableLiveData<Void?> = MutableLiveData()

    abstract fun viewModel(): BaseVerificationResultViewModel

    abstract fun contentLayout(): ViewGroup.LayoutParams
    open fun timerView(): View? = null
    open fun actionButton(): Button? = null
    open fun progressBar(): ProgressBar? = null

    abstract fun qrCodeText(): String
    abstract fun countryIsoCode(): String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        contentLayout().apply {
            height = displayMetrics.heightPixels - TOP_MARGIN.dpToPx()
        }
        timerView()?.apply {
            translationX = -displayMetrics.widthPixels.toFloat()
        }

        dialog.expand()

        hideLiveData.observe(viewLifecycleOwner, {
            dismiss()
        })

        actionButton()?.setOnClickListener { dismiss() }

        viewModel().init(qrCodeText(), countryIsoCode())

        viewModel().isApplicable.observe(viewLifecycleOwner, {
            if (it) {
                startTimer()
            } else {
                hideLiveData.value = null
            }
        })
        viewModel().inProgress.observe(viewLifecycleOwner, { progressBar()?.isVisible = it })
    }

    private fun startTimer() {
        timerView()?.animate()
            ?.setDuration(COLLAPSE_TIME)
            ?.translationX(0F)
            ?.withEndAction {
                hideLiveData.value = null
            }
            ?.start()
    }

    private var _binding: T? = null
    val binding get() = _binding!!

    abstract fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): T

    open fun onDestroyBinding(binding: T) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val innerBinding = onCreateBinding(inflater, container)
        _binding = innerBinding
        return innerBinding.root
    }

    override fun onDestroyView() {
        val innerBinding = _binding
        if (innerBinding != null) {
            onDestroyBinding(innerBinding)
        }

        _binding = null

        super.onDestroyView()
    }

    companion object {
        private const val TOP_MARGIN = 50
        private const val COLLAPSE_TIME = 15000L // 15 sec
    }
}

fun Dialog?.expand() {
    this?.let { dialog ->
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheetInternal =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetInternal?.let {
                val bottomSheetBehavior = BottomSheetBehavior.from(it)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior.peekHeight = it.height
                it.setBackgroundResource(android.R.color.transparent)
            }
        }
    }
}