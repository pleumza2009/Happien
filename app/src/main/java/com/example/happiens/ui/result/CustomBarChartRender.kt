package com.example.happiens.ui.result

import android.graphics.*
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler


class CustomBarChartRender(
    chart: BarDataProvider?,
    animator: ChartAnimator?,
    viewPortHandler: ViewPortHandler?
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val mBarShadowRectBuffer = RectF()

    private var mRadius = 0


    fun setRadius(mRadius: Int) {
        this.mRadius = mRadius
    }


    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans: Transformer = mChart.getTransformer(dataSet.axisDependency)
        mBarBorderPaint.color = dataSet.barBorderColor
        mBarBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)
        mShadowPaint.color = dataSet.barShadowColor
        val drawBorder = dataSet.barBorderWidth > 0f
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY
        if (mChart.isDrawBarShadowEnabled) {
            mShadowPaint.color = dataSet.barShadowColor
            val barData = mChart.barData
            val barWidth = barData.barWidth
            val barWidthHalf = barWidth / 2.0f
            var x: Float
            var i = 0
            val count = Math.min(
                Math.ceil(
                    (dataSet.entryCount
                        .toFloat() * phaseX).toDouble()
                ), dataSet.entryCount.toDouble()
            )
            while (i < count) {
                val e = dataSet.getEntryForIndex(i)
                x = e.x
                mBarShadowRectBuffer.left = x - barWidthHalf
                mBarShadowRectBuffer.right = x + barWidthHalf
                trans.rectValueToPixel(mBarShadowRectBuffer)
                if (!mViewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right)) {
                    i++
                    continue
                }
                if (!mViewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left)) break
                mBarShadowRectBuffer.top = mViewPortHandler.contentTop()
                mBarShadowRectBuffer.bottom = mViewPortHandler.contentBottom()
                c.drawRoundRect(mBarRect, mRadius.toFloat(), mRadius.toFloat(), mShadowPaint)
                i++
            }
        }

        // initialize the buffer
        val buffer = mBarBuffers[index]
        buffer.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(mChart.barData.barWidth)
        buffer.feed(dataSet)
        trans.pointValuesToPixel(buffer.buffer)
        val isSingleColor = dataSet.colors.size == 1
        if (isSingleColor) {
            mRenderPaint.color = dataSet.color
        }
        var j = 0
        while (j < buffer.size()) {
            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                j += 4
                continue
            }
            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break
            if (!isSingleColor) {
                // Set the color for the currently drawn value. If the index
                // is out of bounds, reuse colors.
                mRenderPaint.color = dataSet.getColor(j / 4)
            }
            if (dataSet.gradientColor != null) {
                val gradientColor = dataSet.gradientColor
                mRenderPaint.shader = LinearGradient(
                    buffer.buffer[j],
                    buffer.buffer[j + 3],
                    buffer.buffer[j],
                    buffer.buffer[j + 1],
                    gradientColor.startColor,
                    gradientColor.endColor,
                    Shader.TileMode.MIRROR
                )
            }
            if (dataSet.gradientColors != null) {
                mRenderPaint.shader = LinearGradient(
                    buffer.buffer[j],
                    buffer.buffer[j + 3],
                    buffer.buffer[j],
                    buffer.buffer[j + 1],
                    dataSet.getGradientColor(j / 4).startColor,
                    dataSet.getGradientColor(j / 4).endColor,
                    Shader.TileMode.MIRROR
                )
            }
            val path2: Path = roundRect(
                RectF(
                    buffer.buffer[j],
                    buffer.buffer[j + 1],
                    buffer.buffer[j + 2],
                    buffer.buffer[j + 3]
                ), mRadius.toFloat(), mRadius.toFloat(), true, true, false, false
            )
            c.drawPath(path2, mRenderPaint)
            if (drawBorder) {
                val path: Path = roundRect(RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2], buffer.buffer[j + 3]),
                    mRadius.toFloat(), mRadius.toFloat(), true, true, false, false)
                c.drawPath(path, mBarBorderPaint)
            }
            j += 4

        }
    }

    private fun roundRect(rectF: RectF, rx: Float, ry: Float, tl: Boolean, tr: Boolean, br: Boolean, bl: Boolean): Path {
        val top: Float = rectF.top
        val left: Float = rectF.left
        val right: Float = rectF.right
        val bottom: Float = rectF.bottom
        val path = Path()
        var rx1 = rx
        var ry1 = ry
       if (rx1 < 0F) rx1 = 0F
       if (rx1 < 0F) ry1 = 0F
        val width = right - left
        val height = bottom - top
        if (rx1 > width / 2F) rx1 = width / 2F
       if (ry1 > height / 2F) rx1 = height / 2F
        val widthMinusCorners: Float = width - 2F * rx1
        val heightMinusCorners: Float = height - 2F * ry1

        path.moveTo(right, top + ry1)
        if (tr) path.rQuadTo(0F, -ry, -rx1, -ry1) //top-right corner
        else {
            path.rLineTo(0F, -ry1)
            path.rLineTo(-rx1, 0F)
        }
        path.rLineTo(-widthMinusCorners, 0F)
        if (tl) path.rQuadTo(-rx1, 0F, -rx1, ry1) //top-left corner
        else {
            path.rLineTo(-rx1, 0F)
            path.rLineTo(0F, ry1)
        }
        path.rLineTo(0F, heightMinusCorners)

        if (bl) path.rQuadTo(0F, ry1, rx1, ry1) //bottom-left corner
        else {
            path.rLineTo(0F, ry1)
            path.rLineTo(rx1, 0F)
        }

        path.rLineTo(widthMinusCorners, 0F)
        if (br) path.rQuadTo(rx1, 0F, rx1, -ry1) //bottom-right corner
        else {
            path.rLineTo(rx1, 0F)
            path.rLineTo(0F, -ry1)
        }

        path.rLineTo(0F, -heightMinusCorners)

        path.close() //Given close, last lineto can be removed.


        return path

    }

    }


