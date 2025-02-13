package com.example.pawplan.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pawplan.R
import com.example.pawplan.models.RegistrationViewModel

private lateinit var registrationViewModel: RegistrationViewModel

class RegisterPetTypeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        registrationViewModel = ViewModelProvider(requireActivity()).get(RegistrationViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_register_pet_type, container, false)
        val args = RegisterPetTypeFragmentArgs.fromBundle(requireArguments())

        view.findViewById<ImageView>(R.id.catButton)?.setOnClickListener {
            registrationViewModel.setPetType("cat")

            val action = RegisterPetTypeFragmentDirections
                .actionRegisterPetTypeFragmentToRegisterPetNameFragment(
                    args.phoneNumber, args.userName, "cat"
                )
            findNavController().navigate(action)
        }

        view.findViewById<ImageView>(R.id.dogButton)?.setOnClickListener {
            registrationViewModel.setPetType("dog")

            val action = RegisterPetTypeFragmentDirections
                .actionRegisterPetTypeFragmentToRegisterPetNameFragment(
                    args.phoneNumber, args.userName, "dog"
                )
            findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.backButton)?.setOnClickListener {
            val action = RegisterPetTypeFragmentDirections
                .actionRegisterPetTypeFragmentToRegisterNameFragment(
                    args.phoneNumber
                )
            findNavController().navigate(action)
        }

        return view
    }
}