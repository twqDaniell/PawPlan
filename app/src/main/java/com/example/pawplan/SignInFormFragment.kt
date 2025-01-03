package com.example.pawplan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

class SignInFormFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_in_form, container, false)

        view.findViewById<Button>(R.id.sendCodeButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_signInFormFragment_to_signInCodeFragment)
        }

        return view
    }
}