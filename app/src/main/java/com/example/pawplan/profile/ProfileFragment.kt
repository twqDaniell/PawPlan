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
import com.example.pawplan.MainActivity
import com.example.pawplan.R
import com.example.pawplan.externalAPI.RetrofitClient
import com.example.pawplan.models.BreedsResponse
import com.example.pawplan.models.CatBreed
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import com.example.pawplan.AppDatabase
import com.example.pawplan.formatDateString
import com.example.pawplan.models.Pet
import com.example.pawplan.models.Memory
import com.example.pawplan.showDatePickerDialog
import com.example.pawplan.uploadImageToFirebase
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.MaterialAutoCompleteTextView

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

    private lateinit var appDatabase: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        appDatabase = AppDatabase(requireContext())
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
            val pet = Pet(petId, petName, petBreed, petWeight, petColor, petBirthDate, petAdoptionDate, petPicture, foodImage)
            appDatabase.petDao.insertPet(pet)
        }
        view.findViewById<TextView>(R.id.userNameTextView).text = "Owner: $userName ${phoneNumber.replace("+972", "0")}"
        view.findViewById<TextView>(R.id.petNameTextView).text = petName
        view.findViewById<TextView>(R.id.petBreedTextView).text = "Breed: $petBreed"
        view.findViewById<TextView>(R.id.petWeightTextView).text = "Weight: $petWeight kg"
        view.findViewById<TextView>(R.id.petColorTextView).text = "Color: $petColor"
        view.findViewById<TextView>(R.id.petBirthDateTextView).text = "Birth Date: ${formatDateString(petBirthDate)}"
        view.findViewById<TextView>(R.id.petAdoptionDateTextView).text = "Adoption Date: ${formatDateString(petAdoptionDate)}"
        petImageView = view.findViewById(R.id.petImageView)
        Picasso.get().load(petPicture).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(petImageView)
        memoriesRecyclerView = view.findViewById(R.id.memories_recycler)
        memoriesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        memoryAdapter = MemoriesAdapter(memoryList)
        memoriesRecyclerView.adapter = memoryAdapter
        val uploadButton = view.findViewById<Button>(R.id.upload_button)
        uploadProgressBar = view.findViewById(R.id.uploadProgressBar)
        profilePictureProgressBar = view.findViewById(R.id.profileProgressBar)
        uploadButton.setOnClickListener { openImagePicker(isProfilePicture = false) }
        petImageView.setOnClickListener { openImagePicker(isProfilePicture = true) }
        view.findViewById<Button>(R.id.edit_profile_button).setOnClickListener { showEditProfileDialog() }
        fetchMemories()
        return view
    }

    private fun fetchMemories() {
        val localMemories = appDatabase.memoryDao.getMemoriesByPetId(petId)
        memoryList.clear()
        localMemories.forEach { memory -> memoryList.add(memory.picture) }
        memoryAdapter.notifyDataSetChanged()
        FirebaseFirestore.getInstance().collection("memories")
            .whereEqualTo("petId", petId)
            .get()
            .addOnSuccessListener { documents ->
                appDatabase.memoryDao.clearMemories()
                for (doc in documents) {
                    val id = doc.id
                    val remotePetId = doc.getString("petId") ?: ""
                    val picture = doc.getString("picture") ?: ""
                    val memory = Memory(id, remotePetId, picture)
                    appDatabase.memoryDao.insertMemory(memory)
                }
                val updatedMemories = appDatabase.memoryDao.getMemoriesByPetId(petId)
                memoryList.clear()
                updatedMemories.forEach { memory -> memoryList.add(memory.picture) }
                memoryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch memories", Toast.LENGTH_SHORT).show()
            }
    }

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
                    uploadProfilePicture(uri)
                } else {
                    uploadMemoryImage(uri)
                }
            }
        }
    }

    private fun uploadMemoryImage(imageUri: Uri) {
        uploadProgressBar.visibility = View.VISIBLE

        uploadImageToFirebase(
            imageUri = imageUri,
            folder = "memories",
            onSuccess = { downloadUrl ->
                saveMemory(downloadUrl)
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Memory upload failed", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveMemory(imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val memoryData = hashMapOf("petId" to petId, "picture" to imageUrl, "createdAt" to Date())
        db.collection("memories")
            .add(memoryData)
            .addOnSuccessListener { docRef ->
                val memory = Memory(docRef.id, petId, imageUrl)
                appDatabase.memoryDao.insertMemory(memory)
                memoryList.add(imageUrl)
                memoryAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Memory added!", Toast.LENGTH_SHORT).show()
                uploadProgressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                uploadProgressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to save memory to Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val colorDropdown = dialogView.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.editPetColor)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Profile")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false
        val petNameInput = dialogView.findViewById<TextInputEditText>(R.id.editPetName)
        val petWeightInput = dialogView.findViewById<TextInputEditText>(R.id.editPetWeight)
        val breedDropdown = dialogView.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.editPetBreed)
        val birthDateInput = dialogView.findViewById<TextInputEditText>(R.id.editPetBirthDate)
        val adoptionDateInput = dialogView.findViewById<TextInputEditText>(R.id.editPetAdoptionDate)
        petNameInput.setText(petName)
        petWeightInput.setText(petWeight.toString())
        birthDateInput.setText(formatDateString(petBirthDate))
        adoptionDateInput.setText(formatDateString(petAdoptionDate))
        val colors = arrayOf("Brown", "Black", "White", "Orange", "Gray", "Multicolor")
        val colorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, colors)
        colorDropdown.setAdapter(colorAdapter)
        colorDropdown.setText(petColor, false)
        birthDateInput.setOnClickListener { showDatePickerDialog(requireContext(), birthDateInput) }
        adoptionDateInput.setOnClickListener { showDatePickerDialog(requireContext(), adoptionDateInput) }
        loadBreedsForType(petType, breedDropdown)
        val originalValues = listOf(petName, petWeight.toString(), petBreed, formatDateString(petBirthDate), formatDateString(petAdoptionDate), petColor)
        val inputFields = listOf(petNameInput, petWeightInput, birthDateInput, adoptionDateInput, breedDropdown)
        fun hasChangesAndValidInput(): Boolean {
            val allFilled = inputFields.all { it.text.toString().isNotBlank() }
            return allFilled && (petNameInput.text.toString() != originalValues[0] ||
                    petWeightInput.text.toString() != originalValues[1] ||
                    breedDropdown.text.toString() != originalValues[2] ||
                    birthDateInput.text.toString() != originalValues[3] ||
                    adoptionDateInput.text.toString() != originalValues[4] ||
                    colorDropdown.text.toString() != originalValues[5])
        }
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { saveButton.isEnabled = hasChangesAndValidInput() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        inputFields.forEach { it.addTextChangedListener(textWatcher) }
        colorDropdown.setOnItemClickListener { _, _, _, _ -> saveButton.isEnabled = hasChangesAndValidInput() }
        breedDropdown.setOnItemClickListener { _, _, _, _ -> saveButton.isEnabled = hasChangesAndValidInput() }
        saveButton.setOnClickListener {
            val updatedPetColor = colorDropdown.text.toString()
            saveProfileChanges(dialogView, updatedPetColor)
            dialog.dismiss()
        }
    }


    private fun loadBreedsForType(petType: String, dropdown: AutoCompleteTextView) {
        if (petType.lowercase() == "dog") {
            RetrofitClient.dogApi.getBreeds().enqueue(object : Callback<BreedsResponse> {
                override fun onResponse(call: Call<BreedsResponse>, response: Response<BreedsResponse>) {
                    if (response.isSuccessful) {
                        val breeds = response.body()?.toList() ?: emptyList()
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

    private fun setupBreedDropdown(dropdown: AutoCompleteTextView, breeds: List<String>, currentBreed: String?) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, breeds)
        dropdown.setAdapter(adapter)
        dropdown.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) { dropdown.showDropDown() } }
        dropdown.setOnClickListener { dropdown.showDropDown() }
        if (currentBreed != null && breeds.contains(currentBreed)) {
            dropdown.setText(currentBreed, false)
        }
    }

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
                val updatedPet = Pet(petId, petName, petBreed, petWeight, petColor, petBirthDate, petAdoptionDate, petPicture, foodImage)
                appDatabase.petDao.insertPet(updatedPet)
                val action = ProfileFragmentDirections.actionGlobalProfileFragment(userName, phoneNumber, petName, petType, petBreed, petWeight.toString(), petColor, petBirthDate, petAdoptionDate, foodImage, vetId, petId, petPicture)
                findNavController().navigate(action)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProfilePicture(imageUri: Uri) {
        profilePictureProgressBar.visibility = View.VISIBLE

        uploadImageToFirebase(
            imageUri = imageUri,
            folder = "profile_images",
            onSuccess = { downloadUrl ->
                updateProfilePicture(downloadUrl)
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateProfilePicture(imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val petRef = db.collection("pets").document(petId)
        petRef.update("picture", imageUrl)
            .addOnSuccessListener {
                profilePictureProgressBar.visibility = View.GONE
                petPicture = imageUrl
                Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(petImageView)
                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()
                val updatedPet = Pet(petId, petName, petBreed, petWeight, petColor, petBirthDate, petAdoptionDate, petPicture, foodImage)
                appDatabase.petDao.insertPet(updatedPet)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show()
            }
    }
}
