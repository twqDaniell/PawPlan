package com.example.pawplan.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawplan.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SignInFormFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_in_form, container, false)
        val phoneNumberInput = view.findViewById<TextInputEditText>(R.id.phoneNumberInput)
        val sendCodeButton = view.findViewById<Button>(R.id.sendCodeButton)
        progressBar = view.findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()

        sendCodeButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            sendCodeButton.visibility = View.GONE

            val phoneNumber = phoneNumberInput.text.toString().trim()

            if (phoneNumber.isNotEmpty()) {
                val formattedPhoneNumber = if (phoneNumber.startsWith("0")) {
                    "+972" + phoneNumber.substring(1)
                } else {
                    "+972" + phoneNumber
                }

                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(formattedPhoneNumber)
                    .setTimeout(120L, TimeUnit.SECONDS)
                    .setActivity(requireActivity())
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            // Auto-retrieval or Instant verification
                            Log.d("PhoneAuth", "Verification completed: $credential")
                            progressBar.visibility = View.GONE
                            sendCodeButton.visibility = View.VISIBLE
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            // Verification failed
                            Log.e("PhoneAuth", "Verification failed: ${e.message}", e)
                            Toast.makeText(
                                requireContext(),
                                "Verification failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            progressBar.visibility = View.GONE
                            sendCodeButton.visibility = View.VISIBLE
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            // Code sent successfully
                            Log.d("PhoneAuth", "Code sent successfully. Verification ID: $verificationId")

                            // Navigate to the next fragment
                            val bundle = Bundle().apply {
                                putString("verificationId", verificationId)
                                putString("phoneNumber", formattedPhoneNumber)
                            }
                            findNavController().navigate(
                                R.id.action_signInFormFragment_to_signInCodeFragment,
                                bundle
                            )

                            progressBar.visibility = View.GONE
                            sendCodeButton.visibility = View.VISIBLE
                        }
                    })
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid phone number",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
                sendCodeButton.visibility = View.VISIBLE
            }
        }


        return view
    }
}