package com.example.pawplan.profile

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.android.volley.toolbox.JsonArrayRequest
import com.example.pawplan.MainActivity
import com.example.pawplan.R
import com.example.pawplan.externalAPI.RetrofitClient
import com.example.pawplan.models.BreedsResponse
import com.example.pawplan.models.CatBreed
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import okhttp3.internal.format
import java.text.SimpleDateFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

// Room imports
import com.example.pawplan.AppDatabase
import com.example.pawplan.models.Pet
import com.example.pawplan.models.Memory

class ProfileFragment : Fragment() {

    private lateinit var userName: String
    private lateinit var phoneNumber: String
    private lateinit var petName: String
    private lateinit var petType: String
    private lateinit var petBreed: String
    private var petWeight: Int = 0
    private lateinit var petColor: String
    private lateinit var petBirthDate: String
    private lateinit var petAdoptionDate: String
    private lateinit var foodImage: String
    private lateinit var vetId: String
    private lateinit var petId: String
    private lateinit var petPicture: String

    private lateinit var memoriesRecyclerView: RecyclerView
    private val memoryList = mutableListOf<String>()
    private lateinit var memoryAdapter: MemoriesAdapter
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private lateinit var uploadProgressBar: ProgressBar
    private lateinit var profilePictureProgressBar: ProgressBar
    private lateinit var petImageView: ImageView
    private var isProfilePictureUpload = false

    // Room database instance
    private lateinit var appDatabase: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Room database
        appDatabase = AppDatabase(requireContext())

        // Check if pet data exists in Room; if yes, use it; otherwise, use SafeArgs and store in Room
        val localPet = appDatabase.petDao.getPet()
        if (localPet != null) {
            petName = localPet.name
            petBreed = localPet.breed
            petWeight = localPet.weight
            petColor = localPet.color
            petBirthDate = localPet.birthDate
            petAdoptionDate = localPet.adoptionDate
            petPicture = localPet.picture
            foodImage = localPet.foodImage

            // For additional fields not in Room, use SafeArgs
            val args = ProfileFragmentArgs.fromBundle(requireArguments())
            userName = args.userName
            phoneNumber = args.phoneNumber
            petType = args.petType
            vetId = args.vetId
            petId = args.petId
        } else {
            val args = ProfileFragmentArgs.fromBundle(requireArguments())
            userName = args.userName
            phoneNumber = args.phoneNumber
            petName = args.petName
            petType = args.petType
            petBreed = args.petBreed
            petWeight = args.petWeight.toInt()
            petColor = args.petColor
            petBirthDate = args.petBirthDate
            petAdoptionDate = args.petAdoptionDate
            petPicture = args.petPicture
            foodImage = args.foodImage
            vetId = args.vetId
            petId = args.petId

            // Insert pet data into Room
            val pet = Pet(
                id = petId,
                name = petName,
                breed = petBreed,
                weight = petWeight,
                color = petColor,
                birthDate = petBirthDate,
                adoptionDate = petAdoptionDate,
                picture = petPicture,
                foodImage = foodImage
            )
            appDatabase.petDao.insertPet(pet)
        }

        // Update UI with received data
        view.findViewById<TextView>(R.id.userNameTextView).text =
            "$userName- ${phoneNumber.replace("+972", "0")}"
        view.findViewById<TextView>(R.id.petNameTextView).text = petName
        view.findViewById<TextView>(R.id.petBreedTextView).text = "Breed: $petBreed"
        view.findViewById<TextView>(R.id.petWeightTextView).text = "Weight: $petWeight kg"
        view.findViewById<TextView>(R.id.petColorTextView).text = "Color: $petColor"
        view.findViewById<TextView>(R.id.petBirthDateTextView).text =
            "Birth Date: ${formatDate(petBirthDate)}"
        view.findViewById<TextView>(R.id.petAdoptionDateTextView).text =
            "Adoption Date: ${formatDate(petAdoptionDate)}"

        petImageView = view.findViewById(R.id.petImageView)

        // Load Pet Image
        petImageView.load(petPicture) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        // Initialize RecyclerView
        memoriesRecyclerView = view.findViewById(R.id.memories_recycler)
        memoriesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        memoryAdapter = MemoriesAdapter(memoryList)
        memoriesRecyclerView.adapter = memoryAdapter

        // Upload Button & Progress Bar
        val uploadButton = view.findViewById<Button>(R.id.upload_button)
        uploadProgressBar = view.findViewById(R.id.uploadProgressBar)
        profilePictureProgressBar = view.findViewById(R.id.profileProgressBar)

        uploadButton.setOnClickListener {
            openImagePicker(isProfilePicture = false)
        }

        // Click to Change Profile Picture
        petImageView.setOnClickListener {
            openImagePicker(isProfilePicture = true)
        }

        // Edit Profile Button Listener
        view.findViewById<Button>(R.id.edit_profile_button).setOnClickListener {
            showEditProfileDialog()
        }

        // Fetch Memories from Room instead of Firestore
        fetchMemories()

        return view
    }

    // Fetch memories from Room database for this pet
    private fun fetchMemories() {
        // Assumes you have a Memory entity and memoryDao.getMemoriesByPetId(petId: String): List<Memory>
        val memories = appDatabase.memoryDao.getMemoriesByPetId(petId)
        memoryList.clear()
        for (memory in memories) {
            memoryList.add(memory.picture)
        }
        memoryAdapter.notifyDataSetChanged()
    }

    // Open Image Picker
    private fun openImagePicker(isProfilePicture: Boolean = false) {
        isProfilePictureUpload = isProfilePicture
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                if (isProfilePictureUpload) {
                    uploadProfilePicture(uri) // Upload as profile picture
                } else {
                    uploadMemoryImage(uri) // Upload as memory
                }
            }
        }
    }

    private fun uploadMemoryImage(imageUri: Uri) {
        uploadProgressBar.visibility = View.VISIBLE
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "memories/${UUID.randomUUID()}.jpg"
        val fileRef = storageRef.child(fileName)

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Instead of saving to Firestore, save memory to Room
                    saveMemoryToRoom(downloadUrl.toString())
                }.addOnFailureListener {
                    uploadProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to upload memory", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                uploadProgressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Memory upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    // Save memory image URL to Room database and update UI
    private fun saveMemoryToRoom(imageUrl: String) {
        // Create a new Memory object (assumes Memory entity has id, petId, picture)
        val memory = Memory(
            id = UUID.randomUUID().toString(),
            petId = petId,
            picture = imageUrl
        )
        appDatabase.memoryDao.insertMemory(memory)
        uploadProgressBar.visibility = View.GONE
        memoryList.add(imageUrl)
        memoryAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), "Memory added!", Toast.LENGTH_SHORT).show()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date: Date = inputFormat.parse(dateString) ?: return dateString
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString // Return the original if parsing fails
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.editPetColor)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Profile")
            .setPositiveButton("Save", null) // Handle manually
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show() // Must call show() before accessing buttons

        // Reference Save Button AFTER show()
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false // Initially disabled

        // Reference Views
        val petNameInput = dialogView.findViewById<EditText>(R.id.editPetName)
        val petWeightInput = dialogView.findViewById<EditText>(R.id.editPetWeight)
        val breedDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.editPetBreed)
        val birthDateInput = dialogView.findViewById<EditText>(R.id.editPetBirthDate)
        val adoptionDateInput = dialogView.findViewById<EditText>(R.id.editPetAdoptionDate)

        // Set Current Values (But don't trigger change listeners yet)
        petNameInput.setText(petName)
        petWeightInput.setText(petWeight.toString())
        birthDateInput.setText(formatDate(petBirthDate))
        adoptionDateInput.setText(formatDate(petAdoptionDate))

        // Set Color Dropdown
        val colors = arrayOf("Brown", "Black", "White", "Orange", "Gray", "Multicolor")
        val colorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, colors)
        colorSpinner.adapter = colorAdapter
        colorSpinner.setSelection(colors.indexOf(petColor))

        // Handle Date Pickers
        birthDateInput.setOnClickListener { showDatePicker(birthDateInput) }
        adoptionDateInput.setOnClickListener { showDatePicker(adoptionDateInput) }

        loadBreedsForType(petType, breedDropdown)

        // Store Original Values for Comparison
        val originalValues = listOf(
            petName,
            petWeight.toString(),
            petBreed,
            formatDate(petBirthDate),
            formatDate(petAdoptionDate),
            petColor
        )

        val inputFields = listOf(petNameInput, petWeightInput, birthDateInput, adoptionDateInput, breedDropdown)

        // Function to Check for Changes & Empty Fields
        fun hasChangesAndValidInput(): Boolean {
            val allFilled = inputFields.all { it.text.toString().isNotBlank() }
            return allFilled && (
                    petNameInput.text.toString() != originalValues[0] ||
                            petWeightInput.text.toString() != originalValues[1] ||
                            breedDropdown.text.toString() != originalValues[2] ||
                            birthDateInput.text.toString() != originalValues[3] ||
                            adoptionDateInput.text.toString() != originalValues[4] ||
                            colorSpinner.selectedItem.toString() != originalValues[5]
                    )
        }

        // Add Text Change Listener
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveButton.isEnabled = hasChangesAndValidInput()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        inputFields.forEach { it.addTextChangedListener(textWatcher) }

        // Listen for Spinner Changes
        colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                saveButton.isEnabled = hasChangesAndValidInput()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Listen for Breed Dropdown Changes
        breedDropdown.setOnItemClickListener { _, _, _, _ ->
            saveButton.isEnabled = hasChangesAndValidInput()
        }

        // Ensure Button is Disabled Initially
        saveButton.isEnabled = false

        // Save Button Click Handling
        saveButton.setOnClickListener {
            val updatedPetColor = colorSpinner.selectedItem.toString()
            saveProfileChanges(dialogView, updatedPetColor)
            dialog.dismiss()
        }
    }

    private fun loadBreedsForType(petType: String, dropdown: AutoCompleteTextView) {
        if (petType.lowercase() == "dog") {
            RetrofitClient.dogApi.getBreeds().enqueue(object : Callback<BreedsResponse> {
                override fun onResponse(call: Call<BreedsResponse>, response: Response<BreedsResponse>) {
                    if (response.isSuccessful) {
                        val breeds = response.body()?.toList() ?: emptyList() // Convert map to list
                        setupBreedDropdown(dropdown, breeds, petBreed)
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch dog breeds", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<BreedsResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error fetching dog breeds", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            RetrofitClient.catApi.getBreeds().enqueue(object : Callback<List<CatBreed>> {
                override fun onResponse(call: Call<List<CatBreed>>, response: Response<List<CatBreed>>) {
                    if (response.isSuccessful) {
                        val breeds = response.body()?.map { it.name } ?: emptyList()
                        setupBreedDropdown(dropdown, breeds, petBreed)
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch cat breeds", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<CatBreed>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error fetching cat breeds", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Helper function to set up dropdown
    private fun setupBreedDropdown(dropdown: AutoCompleteTextView, breeds: List<String>, currentBreed: String?) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, breeds)
        dropdown.setAdapter(adapter)
        dropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                dropdown.showDropDown()
            }
        }
        dropdown.setOnClickListener {
            dropdown.showDropDown()
        }
        if (currentBreed != null && breeds.contains(currentBreed)) {
            dropdown.setText(currentBreed, false)
        }
    }

    // Show Date Picker
    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
            editText.setText(formattedDate)
        }, year, month, day)
        datePicker.show()
    }

    // Save Changes to Firestore and update Room
    private fun saveProfileChanges(dialogView: View, updatedPetColor: String) {
        val updatedPetName = dialogView.findViewById<EditText>(R.id.editPetName).text.toString()
        val updatedPetBreed = dialogView.findViewById<EditText>(R.id.editPetBreed).text.toString()
        val updatedPetWeight = dialogView.findViewById<EditText>(R.id.editPetWeight).text.toString().toIntOrNull() ?: petWeight
        val updatedPetBirthDate = dialogView.findViewById<EditText>(R.id.editPetBirthDate).text.toString()
        val updatedPetAdoptionDate = dialogView.findViewById<EditText>(R.id.editPetAdoptionDate).text.toString()

        val db = FirebaseFirestore.getInstance()
        val petRef = db.collection("pets").document(petId)

        val updates = hashMapOf<String, Any>(
            "petName" to updatedPetName,
            "petBreed" to updatedPetBreed,
            "petWeight" to updatedPetWeight,
            "petColor" to updatedPetColor,
            "petBirthDate" to updatedPetBirthDate,
            "petAdoptionDate" to updatedPetAdoptionDate
        )

        petRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                petName = updatedPetName
                petBreed = updatedPetBreed
                petWeight = updatedPetWeight
                petColor = updatedPetColor
                petBirthDate = updatedPetBirthDate
                petAdoptionDate = updatedPetAdoptionDate

                val mainActivity = requireActivity() as MainActivity
                mainActivity.petNameGlobal = petName
                mainActivity.petBreedGlobal = petBreed
                mainActivity.petWeightGlobal = petWeight
                mainActivity.petColorGlobal = petColor
                mainActivity.petBirthDateGlobal = petBirthDate
                mainActivity.petAdoptionDateGlobal = petAdoptionDate

                // Update Room with new pet data
                val updatedPet = Pet(
                    id = petId,
                    name = petName,
                    breed = petBreed,
                    weight = petWeight,
                    color = petColor,
                    birthDate = petBirthDate,
                    adoptionDate = petAdoptionDate,
                    picture = petPicture,
                    foodImage = foodImage
                )
                appDatabase.petDao.insertPet(updatedPet)

                val action = ProfileFragmentDirections
                    .actionGlobalProfileFragment(
                        userName, phoneNumber, petName, petType, petBreed,
                        petWeight.toString(), petColor, petBirthDate, petAdoptionDate, foodImage, vetId, petId, petPicture
                    )
                findNavController().navigate(action)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Upload New Profile Picture to Firebase Storage and update Room
    private fun uploadProfilePicture(imageUri: Uri) {
        profilePictureProgressBar.visibility = View.VISIBLE
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "pet_pictures/${UUID.randomUUID()}.jpg"
        val fileRef = storageRef.child(fileName)

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    updateProfilePicture(downloadUrl.toString())
                }.addOnFailureListener { e ->
                    profilePictureProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                profilePictureProgressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfilePicture(imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val petRef = db.collection("pets").document(petId)

        petRef.update("picture", imageUrl)
            .addOnSuccessListener {
                profilePictureProgressBar.visibility = View.GONE
                petPicture = imageUrl
                petImageView.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.placeholder)
                    error(R.drawable.placeholder)
                }
                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()

                // Update Room with new profile picture
                val updatedPet = Pet(
                    id = petId,
                    name = petName,
                    breed = petBreed,
                    weight = petWeight,
                    color = petColor,
                    birthDate = petBirthDate,
                    adoptionDate = petAdoptionDate,
                    picture = petPicture,
                    foodImage = foodImage
                )
                appDatabase.petDao.insertPet(updatedPet)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show()
            }
    }
}
