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
package com.doctoror.particleswallpaper.data.engine

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.doctoror.particlesdrawable.ParticlesDrawable
import com.doctoror.particleswallpaper.domain.config.DrawableConfigurator
import com.doctoror.particleswallpaper.domain.repository.NO_URI
import com.doctoror.particleswallpaper.domain.repository.SettingsRepository
import com.doctoror.particleswallpaper.presentation.di.Injector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Created by Yaroslav Mytkalyk on 18.04.17.
 *
 * The [WallpaperService] implementation.
 */
class WallpaperServiceImpl : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return EngineImpl()
    }

    inner class EngineImpl : Engine() {

        private val TAG = "WallpaperService:Engine"

        @Inject lateinit var mConfigurator: DrawableConfigurator
        @Inject lateinit var mSettings: SettingsRepository

        private lateinit var mGlide: RequestManager

        private var mFrameDelayDisposable: Disposable? = null
        private var mBackgroundDisposable: Disposable? = null
        private var mBackgroundColorDisposable: Disposable? = null

        private val DEFAULT_DELAY = 10L
        private val MIN_DELAY = 5L

        private val mBackgroundPaint = Paint()

        private val mHandler = Handler(Looper.getMainLooper())
        private val mDrawable = ParticlesDrawable()

        private var mVisible = false

        private var mWidth = 0
        private var mHeight = 0

        private var mBackground: Drawable? = null
        private var mDelay = DEFAULT_DELAY

        private var mLastUsedImageLoadTarget: ImageLoadTarget? = null

        init {
            mBackgroundPaint.style = Paint.Style.FILL
            mBackgroundPaint.color = Color.BLACK
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            Injector.configComponent.inject(this)

            mConfigurator.subscribe(mDrawable, mSettings)
            mGlide = Glide.with(this@WallpaperServiceImpl)

            mFrameDelayDisposable = mSettings.getFrameDelay()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ d -> mDelay = d.toLong() })

            mBackgroundDisposable = mSettings.getBackgroundUri()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ u -> handleBackground(u) })

            mBackgroundColorDisposable = mSettings.getBackgroundColor()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ c -> mBackgroundPaint.color = c })
        }

        override fun onDestroy() {
            super.onDestroy()
            mVisible = false
            mConfigurator.dispose()
            mFrameDelayDisposable?.dispose()
            mBackgroundDisposable?.dispose()
            mBackgroundColorDisposable?.dispose()
        }

        private fun handleBackground(uri: String) {
            mGlide.clear(mLastUsedImageLoadTarget)
            if (uri == NO_URI) {
                mBackground = null
                mLastUsedImageLoadTarget = null
            } else if (mWidth != 0 && mHeight != 0) {
                val target = ImageLoadTarget(mWidth, mHeight)
                mGlide
                        .load(uri)
                        .apply(RequestOptions.noAnimation())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.centerCropTransform())
                        .into(target)

                mLastUsedImageLoadTarget = target
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int,
                                      height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            mDrawable.setBounds(0, 0, width, height)
            mBackground?.setBounds(0, 0, width, height)
            mWidth = width
            mHeight = height
            handleBackground(mSettings.getBackgroundUri().blockingFirst())
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            mVisible = false
            mHandler.removeCallbacks(mDrawRunnable)
            mDrawable.stop()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            mVisible = visible
            if (visible) {
                mDrawable.start()
                mHandler.post(mDrawRunnable)
            } else {
                mHandler.removeCallbacks(mDrawRunnable)
                mDrawable.stop()
            }
        }

        private fun draw() {
            mHandler.removeCallbacks(mDrawRunnable)
            if (mVisible) {
                val startTime = SystemClock.uptimeMillis()
                val holder = surfaceHolder
                var canvas: Canvas? = null
                try {
                    canvas = holder.lockCanvas()
                    if (canvas != null) {
                        drawBackground(canvas)
                        mDrawable.draw(canvas)
                        mDrawable.nextFrame()
                    }
                } finally {
                    if (canvas != null) {
                        try {
                            holder.unlockCanvasAndPost(canvas)
                        } catch (e: IllegalArgumentException) {
                            Log.wtf(TAG, e)
                        }
                    }
                }
                mHandler.postDelayed(mDrawRunnable,
                        Math.max(mDelay - (SystemClock.uptimeMillis() - startTime), MIN_DELAY))
            }
        }

        private fun drawBackground(c: Canvas) {
            val background = mBackground
            if (background == null) {
                c.drawRect(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mBackgroundPaint)
            } else {
                background.draw(c)
            }
        }

        private val mDrawRunnable = Runnable { this.draw() }

        private inner class ImageLoadTarget(width: Int, height: Int)
            : SimpleTarget<Drawable>(width, height) {

            override fun onResourceReady(resource: Drawable?, transition: Transition<in Drawable>?) {
                resource?.setBounds(0, 0, mWidth, mHeight)
                mBackground = resource
            }
        }
    }
}
