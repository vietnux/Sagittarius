/*
 *  This file is part of Sagittarius Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tglt.sagittarius.icons

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.ItemIconShapeBinding
import com.android.launcher3.util.Themes
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import java.util.*

class ItemIconShape(val context: Context, val item: ShapeModel) :
    AbstractBindingItem<ItemIconShapeBinding>() {
    override val type: Int
        get() = R.id.fastadapter_item

    private var prefs = Utilities.getOmegaPrefs(context)
    private val backgroundColor = Themes.getAttrColor(context, R.attr.iconShapeTint)
    private val highlightColor = Themes.getAttrColor(context, android.R.attr.textColorPrimary)

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?) =
        ItemIconShapeBinding.inflate(inflater, parent, false)

    override fun bindView(binding: ItemIconShapeBinding, payloads: List<Any>) {

        val drawable: Drawable? = if (item.shapeName != "system") {
            item.getIcon(context, item.shapeName)
        } else {
            val sd = ShapeDrawable(
                PathShape(
                    IconShapeManager.getSystemIconShape(context).getMaskPath(),
                    100f,
                    100f
                )
            )
            sd.paint.color = backgroundColor
            sd
        }
        binding.shapeIcon.background = drawable

        if (prefs.iconShape.toString() == item.shapeName) {
            binding.shapeIcon.backgroundTintList = ColorStateList.valueOf(prefs.accentColor)
            item.isSelected = true
            binding.checkMark.drawable.setTint(highlightColor)
            binding.checkMark.visibility = View.VISIBLE
        } else {
            binding.shapeIcon.backgroundTintList = ColorStateList.valueOf(backgroundColor)
            item.isSelected = false
            binding.checkMark.visibility = View.INVISIBLE
        }

        binding.shapeName.text =
            item.shapeName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}