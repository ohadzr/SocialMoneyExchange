
package il.ac.technion.socialmoneyexchange

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import il.ac.technion.socialmoneyexchange.databinding.ActivityMainBinding

object GlobalVariable {

    lateinit var apiData: CurrencyApi
}
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val TAG = "MainActivity"
        const val SIGN_IN_REQUEST_CODE = 7921
    }

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController : NavController
    private val viewModel by viewModels<LoginViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val intent = intent
        drawerLayout = binding.drawerLayout
        navView = binding.navView
        observeAuthenticationState()

        navController = this.findNavController(R.id.nav_host_fragment)

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        navView.setNavigationItemSelectedListener(this)
        if(!intent.getStringExtra("vote").isNullOrBlank()){
            val bundle = Bundle()
            bundle.putString("offerId",intent.getStringExtra("offerId"))
            bundle.putString("userID1",intent.getStringExtra("userID1"))
            bundle.putString("userID2",intent.getStringExtra("userID2"))
            navController?.navigate(R.id.voteFragment, bundle)

        }
        if(!intent.getStringExtra("fromOffer").isNullOrBlank()){
            val bundle = Bundle()
            bundle.putString("offerId",intent.getStringExtra("offerId"))
            bundle.putString("userID1",intent.getStringExtra("userID1"))
            bundle.putString("userID2",intent.getStringExtra("userID2"))
            bundle.putString("coinAmount1",intent.getStringExtra("coinAmount1"))
            bundle.putString("coinAmount2",intent.getStringExtra("coinAmount2"))
            bundle.putString("coinName1",intent.getStringExtra("coinName1"))
            bundle.putString("coinName2",intent.getStringExtra("coinName2"))
            bundle.putString("lastUpdater",intent.getStringExtra("lastUpdater"))
            bundle.putString("status",intent.getStringExtra("status"))
            navController?.navigate(R.id.offerFragment, bundle)

        }

        if(!intent.getStringExtra("loadProfile").isNullOrBlank()){
            val bundle = Bundle()
            bundle.putString("offerId",intent.getStringExtra("offerId"))
            bundle.putString("userID1",intent.getStringExtra("userID1"))
            bundle.putString("userID2",intent.getStringExtra("userID2"))
            bundle.putString("coinAmount1",intent.getStringExtra("coinAmount1"))
            bundle.putString("coinAmount2",intent.getStringExtra("coinAmount2"))
            bundle.putString("coinName1",intent.getStringExtra("coinName1"))
            bundle.putString("coinName2",intent.getStringExtra("coinName2"))
            bundle.putString("lastUpdater",intent.getStringExtra("lastUpdater"))
            bundle.putString("status",intent.getStringExtra("status"))
            navController?.navigate(R.id.userProfilePublicFragment, bundle)

        }

        if(!intent.getStringExtra("fromMap").isNullOrEmpty()||!intent.getStringExtra("fromEdit").isNullOrEmpty()) {
            val bundle = Bundle()
            bundle.putString("fromMapOrEdit","true")
            bundle.putDouble("Radius",intent.getStringExtra("Radius").toDouble())
            bundle.putDouble("Lat",intent.getStringExtra("Lat").toDouble())
            bundle.putDouble("Long",intent.getStringExtra("Long").toDouble())
            bundle.putString("PickedCurrency",intent.getStringExtra("PickedCurrency"))
            bundle.putFloat("savedAddedCoins",intent.getStringExtra("savedAddedCoins").toFloat())
            bundle.putString("pickedAmount",intent.getStringExtra("pickedAmount"))
            bundle.putStringArrayList("savedRequestedCurrencies",intent.getStringArrayListExtra("savedRequestedCurrencies"))
            bundle.putString("savedRequestId",intent.getStringExtra("savedRequestId"))
            navController?.navigate(R.id.requestFragment, bundle)
        }


        createChannel(
            getString(R.string.app_notification_channel_id),
            getString(R.string.app_notification_channel_name)
        )

    }

//    private fun fetchData() {
//        val url = "https://api.exchangeratesapi.io/latest"
//        val request = Request.Builder().url(url).build()
//        val client = OkHttpClient()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onResponse(call: Call, response: Response) {
//                val body = response.body()?.string()
////
//                val gson = GsonBuilder().create()
//                runOnUiThread {
//                    GlobalVariable.apiData = gson.fromJson(body,CurrencyApi::class.java)
//                    GlobalVariable.apiData.rates["EUR"] = 1.0
//                }
//
//            }
//            override fun onFailure(call: Call, e: IOException) {
//
//            }
//        })
//
//
//    }


    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.rgb(244, 67, 54)
            notificationChannel.enableVibration(true)
            notificationChannel.description = "We've found an offer for you!"

            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        Log.i("ohad", item.toString())
        when (item.itemId) {
            R.id.requestFragment -> {
            }
            R.id.userProfilePublicFragment -> {
            }
            R.id.offerFragment -> {
            }
            R.id.aboutFragment -> {
            }
            R.id.action_login_logout -> {
                AuthUI.getInstance().signOut(this)
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item)
    }


    /**
     * Observes the authentication state and changes the UI accordingly.
     * If there is a logged in user: (1) show a logout item in menu and (2) display their name.
     * If there is no logged in user: show a login item in menu
     */
    private fun observeAuthenticationState() {
//        val factToDisplay = viewModel.getFactToDisplay(this)

        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            // in LoginViewModel and change the UI accordingly.
            when (authenticationState) {
                // When user is logged in
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
//                    Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show()
                    navView.menu.findItem(R.id.action_login_logout).setTitle(R.string.logout_button_text)
                }
                else -> {
                    // When user is not logged in (logged out, error, etc.)
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                    launchSignInFlow()
                    navView.menu.findItem(R.id.action_login_logout).setTitle(R.string.login_button_text)
                }
            }
        })
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email,
        // they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()

            // This is where you can provide more ways for users to register and
            // sign in.
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_REQUEST_CODE
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.new_swap_logo)
                .setTosAndPrivacyPolicyUrls(
                    getString(R.string.terms_of_service_url),
                    getString(R.string.privacy_policy_url))
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

}
