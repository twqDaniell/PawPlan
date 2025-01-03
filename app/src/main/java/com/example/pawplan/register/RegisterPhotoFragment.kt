package com.example.pawplan.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.pawplan.R

class RegisterPhotoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register_photo, container, false)

        view.findViewById<Button>(R.id.backButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_registerPhotoFragment_to_registerDetailsFragment)
        }
//
//        view.findViewById<Button>(R.id.nameNextButton)?.setOnClickListener {
//            findNavController().navigate(R.id.action_registerPetNameFragment_to_registerPetBreedFragment)
//        }

        return view
    }
}