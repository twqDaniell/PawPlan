package com.example.pawplan.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pawplan.R
import com.example.pawplan.models.RegistrationViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

class RegisterPetNameFragment : Fragment() {
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register_pet_name, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(RegistrationViewModel::class.java)
        val petName = view.findViewById<TextInputEditText>(R.id.textInputEditTextPetName)
        val genderAutoComplete = view.findViewById<MaterialAutoCompleteTextView>(R.id.autoCompleteTextViewGender)

        val genders = listOf("She", "He")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        genderAutoComplete.setAdapter(adapter)

        view.findViewById<Button>(R.id.backButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_registerPetNameFragment_to_registerPetTypeFragment)
        }

        view.findViewById<Button>(R.id.nameNextButton)?.setOnClickListener {
            viewModel.petName = petName.text.toString()
            viewModel.petGender = genderAutoComplete.text.toString()
            findNavController().navigate(R.id.action_registerPetNameFragment_to_registerPetBreedFragment)
        }

        return view
    }
}