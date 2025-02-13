package com.example.pawplan.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

private lateinit var registrationViewModel: RegistrationViewModel

class RegisterDetailsFragment : Fragment() {
    private var selectedDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        registrationViewModel = ViewModelProvider(requireActivity()).get(RegistrationViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_register_details, container, false)
        val colorAutoComplete = view.findViewById<MaterialAutoCompleteTextView>(R.id.autoCompleteTextViewColor)
        val adoptionDateInput = view.findViewById<TextInputEditText>(R.id.textInputEditTextAdoptionDate)
        val weight = view.findViewById<TextInputEditText>(R.id.textInputEditTextWeight)
        val nextButton = view.findViewById<Button>(R.id.nameNextButton)

        val args = RegisterDetailsFragmentArgs.fromBundle(requireArguments())

        val colors = listOf("Brown", "Black", "White", "Orange", "Gray", "Multicolor")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, colors)
        colorAutoComplete.setAdapter(adapter)

        registrationViewModel.petColor.observe(viewLifecycleOwner) { color ->
            colorAutoComplete.setText(color)
        }
        registrationViewModel.petWeight.observe(viewLifecycleOwner) { weightText ->
            weight.setText(weightText.toString())
        }
        registrationViewModel.petAdoptionDate.observe(viewLifecycleOwner) { adoptionDate ->
            adoptionDateInput.setText(adoptionDate)
        }

        adoptionDateInput.setOnClickListener {
            showDatePicker { date ->
                selectedDate = date
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                adoptionDateInput.setText(formattedDate)
            }
        }

        view.findViewById<Button>(R.id.backButton)?.setOnClickListener {
            val action = RegisterDetailsFragmentDirections
                .actionRegisterDetailsFragmentToRegisterPetBreedFragment(
                    args.phoneNumber, args.userName, args.petType, args.petName, args.petGender
                )
            findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.nameNextButton)?.setOnClickListener {
            registrationViewModel.setPetAdoptionDate(adoptionDateInput.text.toString())
            registrationViewModel.setPetColor(colorAutoComplete.text.toString())
            registrationViewModel.setPetWeight(weight.text.toString().toInt())
            val action = RegisterDetailsFragmentDirections
                .actionRegisterDetailsFragmentToRegisterPhotoFragment(
                    args.phoneNumber, args.userName, args.petType, args.petName, args.petBreed, args.petGender, args.petBirthDate, weight.text.toString().toInt(), colorAutoComplete.text.toString(), selectedDate.toString()
                )
            findNavController().navigate(action)
        }

        var isColorSelected = false

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isFormFilled = adoptionDateInput.text.toString().isNotEmpty() && weight.text.toString().isNotEmpty() && isColorSelected
                nextButton.isEnabled = isFormFilled
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        val textWatcherSelect = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isColorSelected = true
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        adoptionDateInput.addTextChangedListener(textWatcher)
        weight.addTextChangedListener(textWatcher)
        colorAutoComplete.addTextChangedListener(textWatcherSelect)
        colorAutoComplete.addTextChangedListener(textWatcher)

        return view
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        // Get today's date in milliseconds
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        // Create CalendarConstraints to restrict future dates
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(today)) // Only allow dates up to today
            .build()

        // Build the MaterialDatePicker with constraints
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a Date")
            .setSelection(today) // Default selection to today's date
            .setCalendarConstraints(constraints) // Apply the constraints
            .build()

        // Show the DatePicker
        datePicker.show(parentFragmentManager, "DATE_PICKER")

        // Handle the date selection
        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection) // Convert the selection to a Date object
            onDateSelected(date) // Pass the selected Date to the callback
        }
    }
}