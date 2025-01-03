package com.example.pawplan.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.example.pawplan.R

class RegisterPetTypeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register_pet_type, container, false)

        view.findViewById<ImageView>(R.id.catButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_registerPetTypeFragment_to_registerPetNameFragment)
        }

        view.findViewById<ImageView>(R.id.dogButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_registerPetTypeFragment_to_registerPetNameFragment)
        }

        view.findViewById<Button>(R.id.backButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_registerPetTypeFragment_to_registerNameFragment)
        }

        return view
    }
}