/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.particleswallpaper.presentation.preference

import android.app.AlertDialog
import android.content.Context
import android.preference.Preference
import android.util.AttributeSet
import com.doctoror.particleswallpaper.R
import com.doctoror.particleswallpaper.presentation.di.Injector
import com.doctoror.particleswallpaper.presentation.di.components.DaggerPreferenceComponent
import com.doctoror.particleswallpaper.presentation.di.modules.PreferenceModule
import com.doctoror.particleswallpaper.presentation.presenter.ResetToDefaultsPreferencePresenter
import com.doctoror.particleswallpaper.presentation.view.ResetToDefaultsPreferenceView
import javax.inject.Inject

/**
 * Created by Yaroslav Mytkalyk on 31.05.17.
 *
 * Preference for resetting configuration to default values.
 */
class ResetToDefaultsPreference @JvmOverloads constructor
(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : Preference(context, attrs),
        ResetToDefaultsPreferenceView {

    @Inject lateinit var presenter: ResetToDefaultsPreferencePresenter

    init {
        DaggerPreferenceComponent.builder()
                .configComponent(Injector.configComponent)
                .preferenceModule(PreferenceModule())
                .build()
                .inject(this)

        isPersistent = false
        presenter.onTakeView(this)
    }

    override fun onClick() {
        presenter.onClick()
    }

    override fun showWarningDialog() {
        AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(R.string.Are_you_sure_you_want_to_reset_all_settings_to_default_values)
                .setPositiveButton(R.string.Reset, { _, _ -> presenter.onResetClick() })
                .setNegativeButton(R.string.Cancel, null)
                .show()
    }
}