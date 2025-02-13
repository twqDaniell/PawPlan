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
import com.example.pawplan.models.CatBreed
import com.example.pawplan.models.RegistrationViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

private lateinit var registrationViewModel: RegistrationViewModel

class RegisterPetBreedFragment : Fragment() {
    private var selectedDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        registrationViewModel = ViewModelProvider(requireActivity()).get(RegistrationViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_register_pet_breed, container, false)
        val breedAutoComplete = view.findViewById<MaterialAutoCompleteTextView>(R.id.breedAutocomplete)
        val birthDateInput = view.findViewById<TextInputEditText>(R.id.birthDateInputEdit)
        val nextButton = view.findViewById<Button>(R.id.nameNextButton)
        val title = view.findViewById<TextView>(R.id.textViewBreed1)
        val textInputLayout = view.findViewById<TextInputLayout>(R.id.textInputLayoutBreed)

        registrationViewModel.petBreed.observe(viewLifecycleOwner) { breed ->
            breedAutoComplete.setText(breed)
        }

        registrationViewModel.petBirthDate.observe(viewLifecycleOwner) { birthDate ->
            birthDateInput.setText(birthDate)
        }

        val args = RegisterPetBreedFragmentArgs.fromBundle(requireArguments())

        if(args.petType == "dog") {
            textInputLayout.hint = "Dog breed"
            if(args.petGender == "He") {
                title.text = "What type of dog is he?"
            } else {
                title.text = "What type of dog is she?"
            }
        } else {
            textInputLayout.hint = "Cat breed"
            if(args.petGender == "He") {
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

        fetchBreeds ({ breeds ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, breeds)
            breedAutoComplete.setAdapter(adapter)
        }, args.petType )

        view.findViewById<Button>(R.id.backButton)?.setOnClickListener {
            val action = RegisterPetBreedFragmentDirections
                .actionRegisterPetBreedFragmentToRegisterPetNameFragment(
                    args.phoneNumber, args.userName, args.petType
                )
            findNavController().navigate(action)
        }

        nextButton.setOnClickListener {
            registrationViewModel.setPetBreed(breedAutoComplete.text.toString())
            registrationViewModel.setPetBirthDate(birthDateInput.text.toString())
            val action = RegisterPetBreedFragmentDirections
                .actionRegisterPetBreedFragmentToRegisterPetDetailsFragment(
                    args.phoneNumber, args.userName, args.petType, args.petName, breedAutoComplete.text.toString(), args.petGender, selectedDate.toString()
                )
            findNavController().navigate(action)
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
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(today))
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a Date")
            .setSelection(today)
            .setCalendarConstraints(constraints)
            .build()

        datePicker.show(parentFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection)
            onDateSelected(date)
        }
    }

    private fun fetchBreeds(onBreedsFetched: (List<String>) -> Unit, petType: String) {
        if (petType == "dog") {
            RetrofitClient.dogApi.getBreeds().enqueue(object : Callback<BreedsResponse> {
                override fun onResponse(call: Call<BreedsResponse>, response: Response<BreedsResponse>) {
                    if (response.isSuccessful) {
                        val breeds = response.body()?.message?.keys?.toList() ?: emptyList()
                        onBreedsFetched(breeds)
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch dog breeds", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BreedsResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Error fetching dog breeds", t)
                    Toast.makeText(requireContext(), "Error fetching breeds: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            RetrofitClient.catApi.getBreeds().enqueue(object : Callback<List<CatBreed>> {
                override fun onResponse(call: Call<List<CatBreed>>, response: Response<List<CatBreed>>) {
                    if (response.isSuccessful) {
                        val breeds = response.body()?.map { it.name } ?: emptyList()
                        onBreedsFetched(breeds)
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch cat breeds", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<CatBreed>>, t: Throwable) {
                    Log.e("API_ERROR", "Error fetching cat breeds", t)
                    Toast.makeText(requireContext(), "Error fetching breeds: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}