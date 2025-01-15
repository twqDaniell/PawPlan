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
import androidx.lifecycle.ViewModelProvider
import com.example.pawplan.MainActivity
import com.example.pawplan.R
import com.example.pawplan.models.RegistrationViewModel
import com.example.pawplan.register.RegisterActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var auth: FirebaseAuth
private lateinit var db: FirebaseFirestore

class SignInCodeFragment : Fragment() {
    private lateinit var viewModel: RegistrationViewModel
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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupInputMovements(codeInputs)

        val verificationId = arguments?.getString("verificationId")
        val verifyButton = view.findViewById<Button>(R.id.button2)

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
                                val phoneNumber = arguments?.getString("phoneNumber")
                                checkUserByPhoneNumber(phoneNumber)
                                Toast.makeText(requireContext(), "Signed in successfully!", Toast.LENGTH_SHORT).show()
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
        progressBar.visibility = View.VISIBLE

        db.collection("users")
            .whereEqualTo("phone_number", phoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                if (!documents.isEmpty) {
                    Log.d("Firestore", "User found with phone number: $phoneNumber")
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    Log.d("Firestore", "No user found with phone number: $phoneNumber")
                    viewModel = ViewModelProvider(requireActivity()).get(RegistrationViewModel::class.java)
                    viewModel.phoneNumber = phoneNumber
                    Log.d("SignInCodeFragment", "Phone number saved in ViewModel: ${viewModel.phoneNumber}")

                    val intent = Intent(requireContext(), RegisterActivity::class.java).apply {
                        putExtra("phoneNumber", phoneNumber)
                    }
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e("Firestore", "Error checking user existence: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
