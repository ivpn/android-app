package net.ivpn.client.v2.connect

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import net.ivpn.client.R
import net.ivpn.client.databinding.DialogueLocationBinding
import net.ivpn.client.databinding.DialogueServerListLocationBinding
import net.ivpn.client.databinding.DialogueServerLocationBinding
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.v2.map.dialogue.ServerLocationDialogueAdapter
import net.ivpn.client.v2.map.model.Location
import kotlin.math.max
import kotlin.math.min

object MapDialogs {

    fun openGatewayDialog(parent: View, location: ServerLocation, topMargin: Float, listener: GatewayListener) {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: DialogueServerLocationBinding = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.dialogue_server_location, null, false
        )
        binding.location = location

        val infoPopup = PopupWindow(binding.root,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        infoPopup.isOutsideTouchable = true
        infoPopup.animationStyle = R.style.AppTheme_PopupAnimation
        infoPopup.setBackgroundDrawable(ColorDrawable())
        binding.connectButton.setOnClickListener {
            listener.connectTo(location)
            infoPopup.dismiss()
        }

        infoPopup.showAtLocation(parent, Gravity.TOP, 0, topMargin.toInt())
    }

    fun openGatewayListDialog(parent: View, locations: ArrayList<ServerLocation>, topMargin: Float, listener: GatewayListener) {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: DialogueServerListLocationBinding = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.dialogue_server_list_location, null, false
        )
        binding.viewPager.adapter = ServerLocationDialogueAdapter(parent.context, locations)
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, _ ->
            tab.text = null
        }.attach()

        binding.prevArrow.visibility = if (binding.viewPager.currentItem == 0) View.INVISIBLE else View.VISIBLE
        binding.nextArrow.visibility = if (binding.viewPager.currentItem == locations.size - 1) View.INVISIBLE else View.VISIBLE
        binding.prevArrow.setOnClickListener {
            binding.viewPager.currentItem = max(0, binding.viewPager.currentItem - 1)
            listener.updateSelectionTo(locations[binding.viewPager.currentItem])
        }
        binding.nextArrow.setOnClickListener {
            binding.viewPager.currentItem = min(locations.size - 1, binding.viewPager.currentItem + 1)
            listener.updateSelectionTo(locations[binding.viewPager.currentItem])
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.prevArrow.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                binding.nextArrow.visibility = if (position == locations.size - 1) View.INVISIBLE else View.VISIBLE
                listener.updateSelectionTo(locations[binding.viewPager.currentItem])
                super.onPageSelected(position)
            }
        })

        val infoPopup = PopupWindow(binding.root,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        infoPopup.animationStyle = R.style.AppTheme_PopupAnimation
        infoPopup.isOutsideTouchable = true
        infoPopup.setBackgroundDrawable(ColorDrawable())
        binding.connectButton.setOnClickListener {
            listener.connectTo(locations[binding.viewPager.currentItem])
            infoPopup.dismiss()
        }

        infoPopup.showAtLocation(parent, Gravity.TOP, 0, topMargin.toInt())
    }

    fun openLocationDialogue(parent: View, location: Location?, topMargin: Float, listener: LocationListener) {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: DialogueLocationBinding = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.dialogue_location, null, false
        )
        binding.location = location
        //The default width of view should big enough. Otherwise, enter animation will be broken for devices with Android 10
        //Spent 4 hours of my life to fix it.
        location?.let {
            binding.description.text = parent.context.getString(
                    if (it.isConnected) {
                        R.string.map_dialog_connected_title
                    } else {
                        R.string.map_dialog_not_connected_title
                    }
            )
        }

        val infoPopup = PopupWindow(binding.root,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        infoPopup.isOutsideTouchable = true
        infoPopup.animationStyle = R.style.AppTheme_PopupAnimation
        infoPopup.setBackgroundDrawable(ColorDrawable())

        binding.info.setOnClickListener {
            listener.checkLocation()
            infoPopup.dismiss()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            println("Enter transition = ${infoPopup.enterTransition}")
        }
        infoPopup.showAtLocation(parent, Gravity.TOP, 0, topMargin.toInt())
    }

    interface GatewayListener {
        fun connectTo(location: ServerLocation)
        fun updateSelectionTo(location: ServerLocation)
    }

    interface LocationListener {
        fun checkLocation()
    }
}