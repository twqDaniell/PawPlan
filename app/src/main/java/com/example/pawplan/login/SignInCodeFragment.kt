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
import androidx.fragment.app.Fragment
import com.example.pawplan.R
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import android.widget.Toast
import com.example.pawplan.MainActivity
import com.example.pawplan.register.RegisterActivity
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var auth: FirebaseAuth
private lateinit var db: FirebaseFirestore

class SignInCodeFragment : Fragment() {

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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupInputMovements(codeInputs)

        // Retrieve the verificationId
        val verificationId = arguments?.getString("verificationId")

        if (verificationId != null) {
            // Use the verificationId as needed, e.g., for verifying the entered code
            val codeInputs = listOf(codeInputs[0], codeInputs[1], codeInputs[2], codeInputs[3], codeInputs[4], codeInputs[5])
            val verifyButton = view.findViewById<Button>(R.id.button2)

            verifyButton.setOnClickListener {
                val code = codeInputs.joinToString("") { it.text.toString().trim() }
                if (code.length == 6) {
                    val credential = PhoneAuthProvider.getCredential(verificationId, code)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val phoneNumber = arguments?.getString("phoneNumber")
                                checkUserByPhoneNumber(phoneNumber)
                                // Successfully signed in
                                Toast.makeText(requireContext(), "Signed in successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                // Handle sign-in failure
                                Toast.makeText(requireContext(), "Verification failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
                            inputs[i + 1].requestFocus() // Move to the next input
                        } else {
                            hideKeyboard(inputs[i]) // Hide keyboard on the last input
                        }
                    } else if (s.isNullOrEmpty() && i > 0) {
                        inputs[i - 1].requestFocus() // Move back to the previous input if empty
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
        db.collection("users")
            .whereEqualTo("phone_number", phoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // User exists
                    Log.d("Firestore", "User found with phone number: $phoneNumber")
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    // User does not exist
                    Log.d("Firestore", "No user found with phone number: $phoneNumber")
                    startActivity(Intent(requireContext(), RegisterActivity::class.java))
                    requireActivity().finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking user existence: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
