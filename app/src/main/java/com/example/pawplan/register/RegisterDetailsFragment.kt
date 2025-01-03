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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class RegisterDetailsFragment : Fragment() {
    private var selectedDate: Date? = null
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register_details, container, false)
        val colorAutoComplete = view.findViewById<MaterialAutoCompleteTextView>(R.id.autoCompleteTextViewColor)
        val adoptionDateInput = view.findViewById<TextInputEditText>(R.id.textInputEditTextAdoptionDate)
        val weight = view.findViewById<TextInputEditText>(R.id.textInputEditTextWeight)
        viewModel = ViewModelProvider(requireActivity()).get(RegistrationViewModel::class.java)

        val colors = listOf("Brown", "Black", "White", "Orange", "Gray", "Multicolor")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, colors)
        colorAutoComplete.setAdapter(adapter)

        adoptionDateInput.setOnClickListener {
            showDatePicker { date ->
                selectedDate = date // Save the selected Date
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                adoptionDateInput.setText(formattedDate) // Set the formatted date in the TextInputEditText
            }
        }

        view.findViewById<Button>(R.id.backButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_registerDetailsFragment_to_registerPetBreedFragment)
        }

        view.findViewById<Button>(R.id.nameNextButton)?.setOnClickListener {
            viewModel.petAdoptionDate = selectedDate
            viewModel.petWeight = weight.text.toString().toInt()
            viewModel.petColor = colorAutoComplete.text.toString()
            findNavController().navigate(R.id.action_registerDetailsFragment_to_registerPhotoFragment)
        }

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