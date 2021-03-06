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
package com.doctoror.particleswallpaper.presentation.di.modules

import android.content.Context
import com.doctoror.particleswallpaper.data.repository.SettingsRepositoryDefault
import com.doctoror.particleswallpaper.domain.repository.SettingsRepository
import com.doctoror.particleswallpaper.presentation.di.scopes.PerPreference
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * Created by Yaroslav Mytkalyk on 14.06.17.
 *
 * Preference module
 */
@Module class PreferenceModule {

    @PerPreference @Provides @Named(DEFAULT) fun provideDefaultSettings(context: Context):
            SettingsRepository = SettingsRepositoryDefault.getInstance(context.resources!!, context.theme!!)
}