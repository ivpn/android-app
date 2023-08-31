package net.ivpn.core.common.migration

/*
IVPN Android app
https://github.com/ivpn/android-app

Created by Oleksandr Mykhailenko.
Copyright (c) 2023 IVPN Limited.

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

import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.*
import net.ivpn.core.vpn.ProtocolController
import org.slf4j.LoggerFactory
import javax.inject.Inject

@ApplicationScope
class MigrationController @Inject constructor(
        private val userPreference: EncryptedUserPreference,
        private val preference: Preference,
        private val settingsPreference: EncryptedSettingsPreference,
        private val networkPreference: NetworkProtectionPreference,
        private val protocolController: ProtocolController,
        private val serversPreference: ServersPreference,
        private val repository: ServersRepository
) {

    fun checkForUpdates() {
        val currentVersion = preference.logicVersion
        LOGGER.info("checkForUpdates: currentVersion = $currentVersion")
        val isLogicVersionExist = preference.isLogicVersionExist
        LOGGER.info("checkForUpdates: isLogicVersionExist = $isLogicVersionExist")
        if (currentVersion == Preference.LAST_LOGIC_VERSION) {
            if (!isLogicVersionExist) {
                preference.logicVersion = Preference.LAST_LOGIC_VERSION
            }
            return
        }
        applyAllUpdates(currentVersion + 1, Preference.LAST_LOGIC_VERSION)
    }

    private fun applyMandatoryUpdates() {
        UF0T1(settingsPreference, networkPreference).update()
        preference.logicVersion = Preference.LAST_LOGIC_VERSION
    }

    private fun applyAllUpdates(from: Int, to: Int) {
        LOGGER.info("applyAllUpdates: ")
        println("Perform apply updates from $from to $to")
        for (i in from..to) {
            getUpdateFor(i)?.update()
        }
        preference.logicVersion = Preference.LAST_LOGIC_VERSION
    }

    private fun getUpdateFor(version: Int): Update? {
        println("Perform getUpdateFor $version")
        return when (version) {
            2 -> UF1T2(userPreference, protocolController)
            3 -> UF2T3(repository, serversPreference)
            else -> null
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MigrationController::class.java)
    }
}