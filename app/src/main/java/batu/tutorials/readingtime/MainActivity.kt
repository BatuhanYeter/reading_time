package batu.tutorials.readingtime

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import batu.tutorials.readingtime.databinding.ActivityMainBinding
import com.android.volley.RequestQueue
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private lateinit var binding: ActivityMainBinding
    private var mRequestQueue: RequestQueue? = null
    private var bookInfoArrayList: ArrayList<BookInfo>? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: ImageButton

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        toggle.isDrawerIndicatorEnabled = true

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)

        navView.setNavigationItemSelectedListener { item ->
            item.isChecked = true
            when (item.itemId) {
                R.id.nav_item_home -> replaceFragment(HomeFragment(), "Home")
                R.id.nav_item_reading -> replaceFragment(ReadingListFragment(), "Reading List")
                R.id.nav_item_finished -> replaceFragment(FinishedListFragment(), "Finished Reading")
                R.id.nav_item_profile -> replaceFragment(ProfileFragment(), "Profile")
                R.id.nav_item_signout -> signOut()
            }
            true
        }

        // done: this changed to profile to test
        navView.setCheckedItem(R.id.nav_item_home)
        val defFragment = HomeFragment()
        replaceFragment(defFragment, "Home")
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment).commit()
        supportActionBar?.title = title
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            // getBooks()
        } else {
            createSignInIntent()
        }
    }



    private fun createSignInIntent() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.TwitterBuilder().build(),
            AuthUI.IdpConfig.AnonymousBuilder().build()
        )


        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.book)
            .setIsSmartLockEnabled(false)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            val database = Firebase.firestore
            database.collection("users").document(user!!.uid).addSnapshotListener { value, error ->
                if (value!!.exists()) {
                    Log.e("User", "Already exists!")
                    Toast.makeText(this, "Welcome, ${user.displayName}", Toast.LENGTH_SHORT)
                        .show()

                } else {
                    val userData = hashMapOf(
                        "name" to user.displayName,
                        "email" to user.email,
                        "total_time" to 1
                    )
                    database.collection("users").document(user.uid).set(userData)
                        .addOnSuccessListener {
                            Log.d(
                                "Success",
                                "DocumentSnapshot successfully written!"
                            )
                        }
                        .addOnFailureListener { e -> Log.w("Error", "Error writing document", e) }
                }
            }


        } else {
            Toast.makeText(this, "Sign in failed! ${response?.error}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                createSignInIntent()
            }
    }
}