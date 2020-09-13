package net.ivpn.client.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import net.ivpn.client.R

class WrapContentHeightViewPager : ViewPager {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var height = 0
        for (i in 0 until childCount) {
            val child: View = getChildAt(i)
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val h: Int = child.getMeasuredHeight()
            if (h > height) height = h
        }

        println("WrapContentHeightViewPager content max size = $height")
        println("WrapContentHeightViewPager screen size = ${getScreenHeight()}")
        val screenHeight = getScreenHeight()
        val toolbarHeight = resources.getDimension(R.dimen.network_default_toolbar_height)
        val tabsHeight = resources.getDimension(R.dimen.network_tab_layout_height)
        val statusBarHeight = resources.getDimension(R.dimen.network_status_bar_height)
        val finalHeight = Math.min(height, (screenHeight - toolbarHeight - tabsHeight - statusBarHeight).toInt())

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun getScreenHeight(): Int {
        return context.resources.displayMetrics.heightPixels
    }
}