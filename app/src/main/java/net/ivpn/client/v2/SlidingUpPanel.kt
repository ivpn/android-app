package net.ivpn.client.v2

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import net.ivpn.client.R
import net.ivpn.client.v2.SlidingUpPanel.PanelState.*

class SlidingUpPanel @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : ScrollView(context, attrs, defStyle) {

    private var state: PanelState? = null

    private val singleHopHeight = resources.getDimension(R.dimen.slider_layout_single_hop_height)
    private val exitServerHeight = resources.getDimension(R.dimen.slider_layout_exit_layout_height)
    private val topPadding = resources.getDimension(R.dimen.slider_layout_top_padding)

    private var yCollapsed: Float = 0f
    private var yCollapsedMultiHop: Float = 0f
    private var yExpanded: Float = 0f

    private var view: View? = null

    private var isInit = false
    private var inAnimation = false

    fun initWith(screenHeight: Int, view: View) {
        yCollapsed = screenHeight - singleHopHeight
        yCollapsedMultiHop = screenHeight - singleHopHeight - exitServerHeight
        yExpanded = topPadding

        this.view = view

        isInit = true

        state?.let {
            applyState()
        }
    }

    //Поиграться с touch events. Сделать, как в Google Music
    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
//        if (inAnimation) return
//        println("OnOverScrolled scrollX = $scrollX scrollY = $scrollY clampedX = $clampedX clampedY = $clampedY")
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
//        println("state = $state")
//
//        if (state == COLLAPSED_MULTIHOP || state == COLLAPSED) {
//            if (scrollY == 0 && clampedY) {
//                println("Return called")
//                return
//            } else {
//                println("Expand called")
////                fling(0)
////                smoothScrollTo(0, 0)
//
//                expand()
//                super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
//            }
//        } else {
//            if (scrollY == 0 && clampedY) {
////                fling(0)
////                smoothScrollTo(0, 0)
//                println("Collapse called")
//
//                collapse()
//                super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
//            } else {
//                println("super.onOverScrolled called")
//                super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
//            }
//        }
//        println("OnOverScrolled scrollX = $scrollX scrollY = $scrollY clampedX = $clampedX clampedY = $clampedY")
    }

    override fun onScrollChanged(xScroll: Int, yScroll: Int, oldXScroll: Int, oldYScroll: Int) {
        super.onScrollChanged(xScroll, yScroll, oldXScroll, oldYScroll)
    }

    fun showView() {
        state = when (state) {
            COLLAPSED, COLLAPSED_MULTIHOP, null -> COLLAPSED_MULTIHOP
            EXPANDED, EXPANDED_MULTIHOP -> EXPANDED_MULTIHOP
        }

        if (!isInit) {
            return
        }

        applyState()
    }

    private fun applyState() {
        when (state) {
            COLLAPSED -> applyCollapsedState()
            COLLAPSED_MULTIHOP -> applyCollapsedMultiHopState()
            EXPANDED -> applyExpandedState()
            EXPANDED_MULTIHOP -> applyExpandedMultiHopState()
            null -> {
            }
        }
    }

    private fun applyCollapsedState() {
        inAnimation = true
        animate().translationY(getTranslationYFor(COLLAPSED))
                .withEndAction {
                    view?.visibility = View.GONE
                    inAnimation = false
                }.start()
    }

    private fun applyCollapsedMultiHopState() {
        inAnimation = true
        animate().translationY(getTranslationYFor(COLLAPSED_MULTIHOP))
                .withStartAction {
                    view?.visibility = View.VISIBLE
                }.withEndAction {
                    inAnimation = false
                }.start()
    }

    private fun applyExpandedState() {
        inAnimation = true
        animate().translationY(getTranslationYFor(EXPANDED))
                .withEndAction {
                    inAnimation = false
                }.start()
    }

    private fun applyExpandedMultiHopState() {
        inAnimation = true
        animate().translationY(getTranslationYFor(EXPANDED_MULTIHOP))
                .withEndAction {
                    inAnimation = false
                }.start()
    }

    fun hideView() {
        state = when (state) {
            COLLAPSED, COLLAPSED_MULTIHOP, null -> COLLAPSED
            EXPANDED, EXPANDED_MULTIHOP -> EXPANDED
        }

        if (!isInit) {
            return
        }

        applyState()
    }

    fun collapse() {
        state = when (state) {
            COLLAPSED, EXPANDED, null -> COLLAPSED
            COLLAPSED_MULTIHOP, EXPANDED_MULTIHOP -> COLLAPSED_MULTIHOP
        }

        if (!isInit) {
            return
        }

        applyState()
    }

    fun expand() {
        state = when (state) {
            COLLAPSED, EXPANDED, null -> EXPANDED
            COLLAPSED_MULTIHOP, EXPANDED_MULTIHOP -> EXPANDED_MULTIHOP
        }

        if (!isInit) {
            return
        }

        applyState()
    }

    private fun getTranslationYFor(state: PanelState): Float {
        return when (state) {
            COLLAPSED -> yCollapsed
            COLLAPSED_MULTIHOP -> yCollapsedMultiHop
            EXPANDED -> yExpanded
            EXPANDED_MULTIHOP -> yExpanded
        }
    }

    enum class PanelState {
        COLLAPSED,
        COLLAPSED_MULTIHOP,
        EXPANDED,
        EXPANDED_MULTIHOP
    }
}