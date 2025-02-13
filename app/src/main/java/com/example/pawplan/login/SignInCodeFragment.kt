package com.example.pawplan.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawplan.MainActivity
import com.example.pawplan.R
import com.example.pawplan.foods.FoodFragmentArgs
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
// Import Room database and models
import com.example.pawplan.AppDatabase
import com.example.pawplan.models.User
import com.example.pawplan.models.Pet

class SignInCodeFragment : Fragment() {
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in_code, container, false)
        val codeInputs = listOf(
            view.findViewById<TextInputEditText>(R.id.code1InputEdit),
            view.findViewById<TextInputEditText>(R.id.code2InputEdit),
            view.findViewById<TextInputEditText>(R.id.code3InputEdit),
            view.findViewById<TextInputEditText>(R.id.code4InputEdit),
            view.findViewById<TextInputEditText>(R.id.code5InputEdit),
            view.findViewById<TextInputEditText>(R.id.code6InputEdit)
        )
        progressBar = view.findViewById(R.id.progressBar)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        setupInputMovements(codeInputs)

        val args = SignInCodeFragmentArgs.fromBundle(requireArguments())

        val verificationId = args.verificationId
        val phoneNumber = args.phoneNumber
        val verifyButton = view.findViewById<Button>(R.id.button2)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isFormFilled = codeInputs.all { it.text.toString().isNotEmpty() }
                verifyButton.isEnabled = isFormFilled
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        codeInputs.forEach { it.addTextChangedListener(textWatcher) }

        if (verificationId != null) {
            verifyButton.setOnClickListener {
                val code = codeInputs.joinToString("") { it.text.toString().trim() }
                if (code.length == 6) {
                    progressBar.visibility = View.VISIBLE
                    verifyButton.visibility = View.GONE

                    val credential = PhoneAuthProvider.getCredential(verificationId, code)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                checkUserByPhoneNumber(phoneNumber)
                            } else {
                                progressBar.visibility = View.GONE
                                verifyButton.visibility = View.VISIBLE
                                Toast.makeText(
                                    requireContext(),
                                    "Verification failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Verification ID is missing", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun setupInputMovements(inputs: List<TextInputEditText>) {
        for (i in inputs.indices) {
            inputs[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        if (i < inputs.size - 1) {
                            inputs[i + 1].requestFocus()
                        } else {
                            hideKeyboard(inputs[i])
                        }
                    } else if (s.isNullOrEmpty() && i > 0) {
                        inputs[i - 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun checkUserByPhoneNumber(phoneNumber: String?) {
        if (phoneNumber == null) {
            Toast.makeText(requireContext(), "Phone number is null", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        val usersRef = FirebaseFirestore.getInstance().collection("users")
        usersRef.whereEqualTo("phone_number", phoneNumber).get()
            .addOnSuccessListener { userDocs ->
                if (!userDocs.isEmpty) {
                    val userDoc = userDocs.documents[0]
                    val userName = userDoc.getString("name") ?: "Unknown"
                    val userId = userDoc.id // This is the `ownerId` in pets collection

                    // Fetch pet details using `ownerId`
                    fetchPetDetails(userId, userName, phoneNumber)
                } else {
                    progressBar.visibility = View.GONE

                    val action = SignInCodeFragmentDirections
                        .actionSignInCodeFragmentToRegisterNameFragment(
                            phoneNumber
                        )
                    findNavController().navigate(action)
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e("Firestore", "Error fetching user: ${e.message}", e)
            }
    }

    private fun fetchPetDetails(ownerId: String, userName: String, phoneNumber: String) {
        val petsRef = FirebaseFirestore.getInstance().collection("pets")
        petsRef.whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { petDocs ->
                progressBar.visibility = View.GONE
                if (!petDocs.isEmpty) {
                    val petDoc = petDocs.documents[0]
                    val petName = petDoc.getString("petName") ?: "Unknown"
                    val petType = petDoc.getString("petType") ?: "Unknown"
                    val petBreed = petDoc.getString("petBreed") ?: "Unknown"
                    val petWeight = petDoc.getLong("petWeight")?.toInt() ?: 0
                    val petColor = petDoc.getString("petColor") ?: "Unknown"
                    val petBirthDate = petDoc.getDate("petBirthDate") ?: "Unknown"
                    val petAdoptionDate = petDoc.getDate("petAdoptionDate") ?: "Unknown"
                    val foodImage = petDoc.getString("foodImage") ?: ""
                    val vetId = petDoc.getString("vetId") ?: ""
                    val petId = petDoc.id
                    val petPicture = petDoc.getString("picture") ?: ""

                    // Update Room database with user and pet data
                    val appDatabase = AppDatabase(requireContext())
                    val user = User(
                        id = ownerId,
                        name = userName,
                        phoneNumber = phoneNumber
                    )
                    appDatabase.userDao.insertUser(user)

                    val pet = Pet(
                        id = petId,
                        name = petName,
                        breed = petBreed,
                        weight = petWeight,
                        color = petColor,
                        birthDate = petBirthDate.toString(),
                        adoptionDate = petAdoptionDate.toString(),
                        picture = petPicture,
                        foodImage = foodImage
                    )
                    appDatabase.petDao.insertPet(pet)

                    // Use SafeArgs to pass data to ProfileFragment
                    val action = SignInCodeFragmentDirections
                        .actionSignInCodeFragmentToProfileFragment(
                            userName, phoneNumber, petName, petType, petBreed,
                            petWeight.toString(), petColor, petBirthDate.toString(), petAdoptionDate.toString(), foodImage, vetId, petId, petPicture
                        )
                    findNavController().navigate(action)

                    val mainActivity = requireActivity() as MainActivity
                    mainActivity.showBars()
                    mainActivity.petIdGlobal = petId
                    mainActivity.petNameGlobal = petName
                    mainActivity.petBreedGlobal = petBreed
                    mainActivity.petWeightGlobal = petWeight
                    mainActivity.petPictureGlobal = petPicture
                    mainActivity.petColorGlobal = petColor
                    mainActivity.petTypeGlobal = petType
                    mainActivity.petBirthDateGlobal = petBirthDate.toString()
                    mainActivity.petAdoptionDateGlobal = petAdoptionDate.toString()
                    mainActivity.userNameGlobal = userName
                    mainActivity.phoneNumberGlobal = phoneNumber
                    mainActivity.vetIdGlobal = vetId
                    mainActivity.foodImageGlobal = foodImage
                } else {
                    Toast.makeText(requireContext(), "No pets found for this user", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e("Firestore", "Error fetching pet: ${e.message}", e)
            }
    }
}
