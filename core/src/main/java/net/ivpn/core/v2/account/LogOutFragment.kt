package net.ivpn.core.v2.account

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2021 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.ivpn.core.R
import net.ivpn.core.databinding.FragmentLogoutBottomSheetBinding

class LogOutFragment : BottomSheetDialogFragment() {

    private var navigator: LogOutNavigator? = null
    private lateinit var binding: FragmentLogoutBottomSheetBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is AccountFragment) {
            navigator = parentFragment as LogOutNavigator?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_logout_bottom_sheet, container, false
        )
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commit()
        } catch (e: IllegalStateException) {
            Log.d("CreateSessionFragment", "show exception: $e")
        }
    }

    private fun init() {
        binding.logOut.setOnClickListener { _ ->
            navigator?.onLogoutAction()
        }
        binding.logOutAndClear.setOnClickListener { _ ->
            navigator?.onLogoutAndClearAction()
        }
        binding.close.setOnClickListener { _ ->
            navigator?.onCloseAction()
        }
    }
}