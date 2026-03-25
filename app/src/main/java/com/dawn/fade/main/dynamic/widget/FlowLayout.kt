package com.dawn.fade.main.dynamic.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * 轻量流式布局，用于二级分类按内容宽度自动换行展示。
 */
class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private val horizontalSpacing = context.resources.displayMetrics.density.times(8).toInt()
    private val verticalSpacing = context.resources.displayMetrics.density.times(8).toInt()

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is MarginLayoutParams
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val maxWidth = widthSize - paddingLeft - paddingRight
        var lineWidth = 0
        var lineHeight = 0
        var totalHeight = paddingTop + paddingBottom
        var maxLineWidth = 0

        forEachVisibleChild { child, params ->
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, totalHeight)

            val childWidth = child.measuredWidth + params.leftMargin + params.rightMargin
            val childHeight = child.measuredHeight + params.topMargin + params.bottomMargin
            val nextWidth = if (lineWidth == 0) childWidth else lineWidth + horizontalSpacing + childWidth

            if (nextWidth > maxWidth && lineWidth > 0) {
                totalHeight += lineHeight + verticalSpacing
                maxLineWidth = maxOf(maxLineWidth, lineWidth)
                lineWidth = childWidth
                lineHeight = childHeight
            } else {
                lineWidth = nextWidth
                lineHeight = maxOf(lineHeight, childHeight)
            }
        }

        if (lineWidth > 0) {
            totalHeight += lineHeight
            maxLineWidth = maxOf(maxLineWidth, lineWidth)
        }

        val measuredWidth = resolveSize(maxLineWidth + paddingLeft + paddingRight, widthMeasureSpec)
        val measuredHeight = resolveSize(totalHeight, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val contentWidth = measuredWidth - paddingLeft - paddingRight
        var currentLeft = paddingLeft
        var currentTop = paddingTop
        var lineHeight = 0

        forEachVisibleChild { child, params ->
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            val occupiedWidth = childWidth + params.leftMargin + params.rightMargin
            val nextLeft = if (currentLeft == paddingLeft) {
                currentLeft + params.leftMargin
            } else {
                currentLeft + horizontalSpacing + params.leftMargin
            }

            if (nextLeft + childWidth + params.rightMargin - paddingLeft > contentWidth && currentLeft > paddingLeft) {
                currentLeft = paddingLeft
                currentTop += lineHeight + verticalSpacing
                lineHeight = 0
            }

            val left = if (currentLeft == paddingLeft) {
                currentLeft + params.leftMargin
            } else {
                currentLeft + horizontalSpacing + params.leftMargin
            }
            val top = currentTop + params.topMargin
            child.layout(left, top, left + childWidth, top + childHeight)

            currentLeft = left + childWidth + params.rightMargin
            lineHeight = maxOf(lineHeight, childHeight + params.topMargin + params.bottomMargin)
        }
    }

    private inline fun forEachVisibleChild(block: (View, MarginLayoutParams) -> Unit) {
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.visibility == GONE) continue
            val params = child.layoutParams as? MarginLayoutParams ?: continue
            block(child, params)
        }
    }
}
