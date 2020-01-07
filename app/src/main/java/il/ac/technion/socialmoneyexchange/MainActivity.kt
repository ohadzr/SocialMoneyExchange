/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package il.ac.technion.socialmoneyexchange

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import il.ac.technion.socialmoneyexchange.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        drawerLayout = binding.drawerLayout
        val navController = this.findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)

        supportActionBar?.setDisplayShowTitleEnabled(false)

//        navView = findViewById(R.id.nav_host_fragment)
//        navView.setNavigationItemSelectedListener(this)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }


//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        Toast.makeText(this, "CLICK", Toast.LENGTH_SHORT).show()
//        // Handle presses on the action bar menu items
//        Log.i("ohad", item.toString())
//        when (item.itemId) {
//            R.id.action_logout -> {
//                AuthUI.getInstance().signOut(this)
//                Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show()
//                return true
//            }
//        }
//
//        drawerLayout.closeDrawer(GravityCompat.START)
//        return super.onOptionsItemSelected(item)
//    }


}
