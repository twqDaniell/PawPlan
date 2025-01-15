package com.example.pawplan.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pawplan.R
import com.example.pawplan.models.RegistrationViewModel
import com.google.android.material.textfield.TextInputEditText

class RegisterNameFragment : Fragment() {
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register_name, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(RegistrationViewModel::class.java)

        val nameInput = view.findViewById<TextInputEditText>(R.id.nameInput)
        val nextButton = view.findViewById<Button>(R.id.nameNextButton)

        nextButton.setOnClickListener {
            viewModel.userName = nameInput.text.toString()
            findNavController().navigate(R.id.action_registerNameFragment_to_registerPetTypeFragment)
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isFormFilled = nameInput.text.toString().isNotEmpty()
                nextButton.isEnabled = isFormFilled
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        nameInput.addTextChangedListener(textWatcher)

        return view
    }
}