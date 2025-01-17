package com.example.pawplan.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pawplan.R
import com.example.pawplan.externalAPI.RetrofitClient
import com.example.pawplan.models.BreedsResponse
import com.example.pawplan.models.RegistrationViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class RegisterPetBreedFragment : Fragment() {
    private var selectedDate: Date? = null
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register_pet_breed, container, false)
        val breedAutoComplete = view.findViewById<MaterialAutoCompleteTextView>(R.id.breedAutocomplete)
        val birthDateInput = view.findViewById<TextInputEditText>(R.id.birthDateInputEdit)
        val nextButton = view.findViewById<Button>(R.id.nameNextButton)
        val title = view.findViewById<TextView>(R.id.textViewBreed1)
        viewModel = ViewModelProvider(requireActivity()).get(RegistrationViewModel::class.java)

        if(viewModel.petType == "dog") {
            breedAutoComplete.hint = "Dog breed"
            if(viewModel.petGender == "He") {
                title.text = "What type of dog is he?"
            } else {
                title.text = "What type of dog is she?"
            }
        } else {
            breedAutoComplete.hint = "Cat breed"
            if(viewModel.petGender == "He") {
                title.text = "What type of cat is he?"
            } else {
                title.text = "What type of cat is she?"
            }
        }

        birthDateInput.setOnClickListener {
            showDatePicker { date ->
                selectedDate = date // Save the selected Date
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                birthDateInput.setText(formattedDate) // Set the formatted date in the TextInputEditText
            }
        }

        fetchBreeds { breeds ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, breeds)
            breedAutoComplete.setAdapter(adapter)
        }

        view.findViewById<Button>(R.id.backButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_registerPetBreedFragment_to_registerPetNameFragment)
        }

        nextButton.setOnClickListener {
            viewModel.petBreed = breedAutoComplete.text.toString()
            viewModel.petBirthDate = selectedDate
            findNavController().navigate(R.id.action_registerPetBreedFragment_to_registerPetDetailsFragment)
        }

        var isBreedSelected = false

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isFormFilled = birthDateInput.text.toString().isNotEmpty() && isBreedSelected
                nextButton.isEnabled = isFormFilled
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        val textWatcherSelect = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isBreedSelected = true
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        birthDateInput.addTextChangedListener(textWatcher)
        breedAutoComplete.addTextChangedListener(textWatcherSelect)
        breedAutoComplete.addTextChangedListener(textWatcher)

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

    private fun fetchBreeds(onBreedsFetched: (List<String>) -> Unit) {
        val api = RetrofitClient.instance
        api.getBreeds().enqueue(object : Callback<BreedsResponse> {
            override fun onResponse(call: Call<BreedsResponse>, response: Response<BreedsResponse>) {
                if (response.isSuccessful) {
                    val breedsMap = response.body()?.message ?: emptyMap()
                    val breeds = breedsMap.keys.toList()
                    onBreedsFetched(breeds)
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch breeds", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BreedsResponse>, t: Throwable) {
                Log.e("DogApi", "Error fetching breeds", t)
                Toast.makeText(requireContext(), "Error fetching breeds: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}