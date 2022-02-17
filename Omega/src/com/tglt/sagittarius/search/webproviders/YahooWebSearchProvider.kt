/*
 *  Copyright (c) 2020 Sagittarius Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.tglt.sagittarius.search.webproviders

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.tglt.sagittarius.search.WebSearchProvider

class YahooWebSearchProvider(context: Context) :
        WebSearchProvider(context) {
    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_yahoo, null)!!

    override val packageName: String
        get() = "https://search.yahoo.com/search?q=%s"

    override val suggestionsUrl: String
        get() = "https://ff.search.yahoo.com/gossip?output=fxjson&command=%s"

    override val name: String
        get() = context.getString(R.string.web_search_yahoo)
}