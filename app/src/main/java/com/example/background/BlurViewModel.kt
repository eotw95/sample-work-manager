/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.example.background

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanupWorker
import com.example.background.workers.SaveImageToFileWorker


class BlurViewModel(application: Application) : ViewModel() {

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null

    private val workManager = WorkManager.getInstance(application)

    init {
        imageUri = getImageUri(application.applicationContext)
    }
    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    @SuppressLint("EnqueueWork")
    internal fun applyBlur(blurLevel: Int) {
        var continuation = workManager.beginWith(
            OneTimeWorkRequest.from(CleanupWorker::class.java)
        )

        for (i in 0 until blurLevel) {
            val builder = OneTimeWorkRequest.Builder(BlurWorker::class.java)

            if (i == 0) {
                builder.setInputData(createInputDataForUri())
            }

            continuation = continuation.then(builder.build())
        }

        val saveImageRequest = OneTimeWorkRequest.Builder(SaveImageToFileWorker::class.java).build()

        continuation.then(saveImageRequest).enqueue()

    }

    /**
     * Creates the input data bundle which includes the Uri to operate on
     * @return Data which contains the Image Uri as a String
     */
    @SuppressLint("RestrictedApi")
    private fun createInputDataForUri(): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, imageUri.toString())
        }

        return builder.build()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    private fun getImageUri(context: Context): Uri {
        val resources = context.resources

        val imageUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.android_cupcake))
            .appendPath(resources.getResourceTypeName(R.drawable.android_cupcake))
            .appendPath(resources.getResourceEntryName(R.drawable.android_cupcake))
            .build()

        return imageUri
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }

    class BlurViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(BlurViewModel::class.java)) {
                BlurViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

/**
 * URI（Uniform Resource Identifier）とURL（Uniform Resource Locator）は関連するが異なる概念です。
 *
 * URI (Uniform Resource Identifier): これは、識別子（Identifier）です。
 * リソースを一意に特定するための識別子で、URLはURIの一種です。
 * URIはLocator（場所を示すもの）とName（名前を示すもの）の二つのサブセットに分かれます。URLはLocatorの一例です。
 * URIは、リソースを識別するための一般的な仕組みを指します。
 *
 * URL (Uniform Resource Locator): これはURIの一部で、特定のリソースの場所を示します。
 * URLは、通常、ウェブ上のリソースのアドレスを指します。例えば、https://www.example.com はURLの一例です。
 *
 * 簡潔に言えば、URLはURIの一部で、URIはリソースを識別するための一般的な概念です。
 * URIにはURLとURN（Uniform Resource Name）が含まれます。URLはリソースの場所を示すためのもので、URNはリソースの名前を示すためのものです。
 *
 * このコードの中での Uri は、Androidで使われるURIクラスで、具体的にはリソースへの参照（この場合はDrawableリソース）を表現しています。
 * URIはリソースの識別子を表すため、このコードではDrawableリソースに対するURIを構築しています。
 */
