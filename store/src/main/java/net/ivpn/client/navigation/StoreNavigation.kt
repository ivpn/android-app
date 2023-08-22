package net.ivpn.client.navigation

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import net.ivpn.core.common.navigation.CustomNavigation

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

object StoreNavigation: CustomNavigation {

    override fun expandNavigation(context: Context, navController: NavController): NavGraph {
        val destinationGraphName = "store_nav_graph"
        val destinationPackageName = "net.ivpn.client"

        // Find the resourceId of the graph we want to attach
        val navigationId =
                context.resources.getIdentifier(destinationGraphName, "navigation", destinationPackageName)

        // inflate the graph using the id obtained above
        val destinationGraph = navController.navInflater.inflate(navigationId)

        // Dynamically add the destination target to our primary graph
        navController.graph.addDestination(destinationGraph)

        return destinationGraph
    }
}