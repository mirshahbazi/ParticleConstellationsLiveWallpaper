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
package com.doctoror.particleswallpaper.presentation.base

/**
 * Created by Yaroslav Mytkalyk on 31.05.17.
 *
 * [OnActivityResultCallbackHost] implementation that stores registered callbacks in a member.
 * Note that this implementation does not forward "onActivityResult()" events.
 */
class OnActivityResultCallbackHostImpl : OnActivityResultCallbackHost {

    var callbacks = listOf<OnActivityResultCallback>()
        private set

    override fun registerCallback(callback: OnActivityResultCallback) {
        if (!callbacks.contains(callback)) {
            callbacks = callbacks.plus(callback)
        }
    }

    override fun unregsiterCallback(callback: OnActivityResultCallback) {
        callbacks = callbacks.minus(callback)
    }
}