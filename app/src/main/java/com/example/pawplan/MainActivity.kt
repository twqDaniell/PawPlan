package com.example.pawplan

import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pawplan.foods.FoodFragmentDirections
import com.example.pawplan.health.HealthFragmentDirections
import com.example.pawplan.missing.MissingFragmentDirections
import com.example.pawplan.profile.ProfileFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    lateinit var petNameGlobal: String
    lateinit var petBreedGlobal: String
    var petWeightGlobal: Int = 0
    lateinit var vetIdGlobal: String
    lateinit var petIdGlobal: String
    lateinit var petTypeGlobal: String
    lateinit var petColorGlobal: String
    lateinit var petBirthDateGlobal: String
    lateinit var petAdoptionDateGlobal: String
    lateinit var petPictureGlobal: String
    lateinit var foodImageGlobal: String
    lateinit var userNameGlobal: String
    lateinit var phoneNumberGlobal: String

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // ✅ Setup Navigation Controller properly
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navController = requireNotNull(navHostFragment) { "NavHostFragment not found! Check activity_main.xml for ID: nav_host_fragment" }.navController



        // ✅ Setup Bottom Navigation
        bottomNav.setupWithNavController(navController)

        val loadingScreen = findViewById<View>(R.id.loading_screen) // ✅ Reference loading screen
        val fragmentSection = findViewById<FragmentContainerView>(R.id.nav_host_fragment)

        // ✅ Hide UI elements until user authentication is checked
        bottomNav.visibility = View.GONE
        loadingScreen.visibility = View.VISIBLE
        fragmentSection.visibility = View.GONE

        val auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid

        if (userId == null) {
            // ✅ User not logged in → Navigate to Sign In Fragment
            hideBars()
            fragmentSection.visibility = View.VISIBLE
            navigateToSignIn()
        } else {
            // ✅ User is logged in → Fetch User & Pet Data
            fetchUserData(userId, loadingScreen, bottomNav)
        }

        // ✅ Logout Button Functionality
        findViewById<ImageButton>(R.id.logout_button)?.setOnClickListener {
            handleLogout()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profileFragment -> {
                    navigateToProfileFragment()
                    true
                }
                R.id.healthFragment -> {
                    navigateToHealthFragment()
                    true
                }
                R.id.missingFragment -> {
                    navigateToMissingFragment()
                    true
                }
                R.id.foodFragment -> {
                    navigateToFoodFragment()
                    true
                }
                else -> false
            }
        }
    }

    // ✅ Fetch User & Pet Data from Firestore and Navigate with SafeArgs
    private fun fetchUserData(userId: String, loadingScreen: View, bottomNav: BottomNavigationView) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)
        val petRef = db.collection("pets").whereEqualTo("ownerId", userId)

        userRef.get().addOnSuccessListener { userDoc ->
            if (!userDoc.exists()) {
                val fragmentSection = findViewById<FragmentContainerView>(R.id.nav_host_fragment)
                navigateToSignIn()
                loadingScreen.visibility = View.GONE
                fragmentSection.visibility = View.VISIBLE
                hideBars()
                return@addOnSuccessListener
            }

            val userName = userDoc.getString("name") ?: "Unknown User"
            val phoneNumber = userDoc.getString("phone_number") ?: ""

            userNameGlobal = userName
            phoneNumberGlobal = phoneNumber

                petRef.get().addOnSuccessListener { petDocs ->
                if (petDocs.isEmpty) {
                    navigateToSignIn() // No pet found → Go to login
                    return@addOnSuccessListener
                }

                val petDoc = petDocs.documents[0]
                val petName = petDoc.getString("petName") ?: "No Pet Name"
                val petType = petDoc.getString("petType") ?: "Unknown Type"
                val petBreed = petDoc.getString("petBreed") ?: "Unknown Breed"
                val petWeight = petDoc.getLong("petWeight")?.toInt() ?: 0
                val petColor = petDoc.getString("petColor") ?: "Unknown Color"
                val petBirthDate = petDoc.getString("petBirthDate") ?: "Unknown Birth Date"
                val petAdoptionDate = petDoc.getString("petAdoptionDate") ?: "Unknown Adoption Date"
                val petPicture = petDoc.getString("picture") ?: ""
                val foodImage = petDoc.getString("foodImage") ?: ""
                val vetId = petDoc.getString("vetId") ?: ""
                val petId = petDoc.id // Unique pet document ID

                petNameGlobal = petDoc.getString("petName") ?: "No Pet Name"
                petBreedGlobal = petDoc.getString("petBreed") ?: "Unknown Breed"
                petWeightGlobal = petDoc.getLong("petWeight")?.toInt() ?: 0
                vetIdGlobal = petDoc.getString("vetId") ?: ""
                petIdGlobal = petDoc.id
                petPictureGlobal = petPicture
                petTypeGlobal = petType
                petColorGlobal = petColor
                petBirthDateGlobal = petBirthDate.toString()
                petAdoptionDateGlobal = petAdoptionDate.toString()
                foodImageGlobal = foodImage

                // ✅ Navigate to ProfileFragment with SafeArgs
                val action = ProfileFragmentDirections
                    .actionGlobalProfileFragment(
                        userName, phoneNumber, petName, petType, petBreed,
                        petWeight.toString(), petColor, petBirthDate.toString(), petAdoptionDate.toString(), foodImage, vetId, petId, petPicture
                    )

                // ✅ Ensure we navigate only if we're NOT already there
                if (navController.currentDestination?.id != R.id.profileFragment) {
                    val fragmentSection = findViewById<FragmentContainerView>(R.id.nav_host_fragment)

                    loadingScreen.visibility = View.GONE
                    fragmentSection.visibility = View.VISIBLE
                    showBars()
                    navController.navigate(action)
                }
            }.addOnFailureListener {
                navigateToSignIn()
            }
        }.addOnFailureListener {
            navigateToSignIn()
        }
    }

    private fun navigateToHealthFragment() {
        val action = HealthFragmentDirections
            .actionGlobalHealthFragment(petNameGlobal, petBreedGlobal, petWeightGlobal, vetIdGlobal, petIdGlobal)

        navController.navigate(action)
    }

    private fun navigateToMissingFragment() {
        val action = MissingFragmentDirections
            .actionGlobalMissingFragment(petIdGlobal)

        navController.navigate(action)
    }

    private fun navigateToFoodFragment() {
        val action = FoodFragmentDirections
            .actionGlobalFoodFragment(petIdGlobal, petNameGlobal, foodImageGlobal)

        navController.navigate(action)
    }

    private fun navigateToProfileFragment() {
        val action = ProfileFragmentDirections
            .actionGlobalProfileFragment(
                userNameGlobal, phoneNumberGlobal, petNameGlobal, petTypeGlobal, petBreedGlobal,
                petWeightGlobal.toString(), petColorGlobal, petBirthDateGlobal, petAdoptionDateGlobal, foodImageGlobal,
                vetIdGlobal, petIdGlobal, petPictureGlobal
            )

        navController.navigate(action)
    }

    // ✅ Navigate to Sign In Fragment
    private fun navigateToSignIn() {
        if (navController.currentDestination?.id != R.id.signInFormFragment) {
            navController.navigate(R.id.signInFormFragment)
        }
    }

    private fun handleLogout() {
        FirebaseAuth.getInstance().signOut() // Log out from Firebase

        // Clear user data
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        hideBars()

        // Navigate back to login
        navigateToSignIn()
    }

    fun showBars() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.top_bar)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        toolbar.visibility = View.VISIBLE
        bottomNav.visibility = View.VISIBLE
    }

    fun hideBars() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.top_bar)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        toolbar.visibility = View.GONE
        bottomNav.visibility = View.GONE
    }
}
