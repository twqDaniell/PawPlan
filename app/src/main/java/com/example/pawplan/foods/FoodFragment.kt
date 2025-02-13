package com.example.pawplan.foods

import android.app.Activity
import android.app.AlertDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawplan.MainActivity
import com.example.pawplan.R
import com.example.pawplan.adapters.AllergiesAdapter
import com.example.pawplan.adapters.OnAllergyDeletedListener
import com.example.pawplan.models.Allergy
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.example.pawplan.Dao.AppDatabase
import com.example.pawplan.uploadImageToFirebase

class FoodFragment : Fragment(), OnAllergyDeletedListener {
    private lateinit var foodImage: ImageView
    private lateinit var addAllergyButton: ImageButton
    private lateinit var allergiesRecycler: RecyclerView
    private lateinit var allergiesTitle: TextView

    private lateinit var allergiesAdapter: AllergiesAdapter
    private val allergiesList = mutableListOf<Allergy>()

    private lateinit var petId: String
    private var selectedImageUri: Uri? = null

    private lateinit var appDatabase: AppDatabase

    companion object {
        private const val PICK_IMAGE_REQUEST = 101
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_food, container, false)
        val args = FoodFragmentArgs.fromBundle(requireArguments())
        petId = args.petId
        val petName = args.petName
        val foodImageUrl = args.foodImage

        view.findViewById<TextView>(R.id.foodTitle).text = "Keep forgetting $petName’s food brand?"
        view.findViewById<TextView>(R.id.foodSubtitle).text = "Keep a picture of $petName's food here"

        foodImage = view.findViewById(R.id.foodImage)
        addAllergyButton = view.findViewById(R.id.addAllergyButton)
        allergiesRecycler = view.findViewById(R.id.allergiesRecycler)
        allergiesTitle = view.findViewById(R.id.allergiesTitle)

        if (foodImageUrl.isNullOrEmpty()) {
            foodImage.setImageResource(R.drawable.ic_add_photo)
        } else {
            Picasso.get()
                .load(foodImageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_add_photo)
                .into(foodImage)
        }

        appDatabase = AppDatabase(requireContext())

        allergiesRecycler.layoutManager = LinearLayoutManager(requireContext())
        allergiesAdapter = AllergiesAdapter(allergiesList, appDatabase, this)
        allergiesRecycler.adapter = allergiesAdapter

        fetchAllergies()

        addAllergyButton.setOnClickListener { showAddAllergyDialog() }
        foodImage.setOnClickListener { selectFoodImage() }

        return view
    }

    private fun updateAllergiesTitle() {
        allergiesTitle.text = "Food Allergies (${allergiesList.size})"
    }

    private fun fetchAllergies() {
        petId.let { id ->
            val localAllergies = appDatabase.allergyDao.getAllergiesByPetId(id)
            if (localAllergies.isNotEmpty()) {
                allergiesList.clear()
                allergiesList.addAll(localAllergies)
                updateAllergiesTitle()
                allergiesAdapter.notifyDataSetChanged()
            } else {
                FirebaseFirestore.getInstance().collection("allergies")
                    .whereEqualTo("petId", id)
                    .get()
                    .addOnSuccessListener { documents ->
                        allergiesList.clear()
                        for (doc in documents) {
                            val allergy = Allergy(
                                id = doc.id,
                                allergyName = doc.getString("allergyName") ?: "",
                                petId = doc.getString("petId") ?: ""
                            )
                            allergiesList.add(allergy)
                            appDatabase.allergyDao.insertAllergy(allergy, id)
                        }
                        updateAllergiesTitle()
                        allergiesAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to fetch allergies", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun showAddAllergyDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_allergy, null)
        val allergyInput = dialogView.findViewById<EditText>(R.id.editAllergyName)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Allergy")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false
        allergyInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveButton.isEnabled = allergyInput.text.toString().isNotBlank()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        saveButton.setOnClickListener {
            val allergyName = allergyInput.text.toString().trim()
            if (allergyName.isNotEmpty()) {
                saveAllergy(allergyName)
                dialog.dismiss()
            }
        }
    }

    private fun saveAllergy(allergyName: String) {
        val db = FirebaseFirestore.getInstance()
        val allergyData = hashMapOf("allergyName" to allergyName, "petId" to petId)
        db.collection("allergies")
            .add(allergyData)
            .addOnSuccessListener { docRef ->
                val newAllergy = Allergy(id = docRef.id, allergyName = allergyName, petId = petId)
                appDatabase.allergyDao.insertAllergy(newAllergy, petId)
                allergiesList.add(newAllergy)
                updateAllergiesTitle()
                allergiesAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Allergy added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add allergy", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectFoodImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            uploadFoodImage()
        }
    }

    private fun uploadFoodImage() {
        val progressBar = view?.findViewById<ProgressBar>(R.id.foodImageProgressBar)
        progressBar?.visibility = View.VISIBLE

        uploadImageToFirebase(
            imageUri = selectedImageUri!!,
            folder = "food_images",
            onSuccess = { downloadUrl ->
                saveFoodImageUrl(downloadUrl, progressBar)
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveFoodImageUrl(imageUrl: String, progressBar: ProgressBar?) {
        val db = FirebaseFirestore.getInstance()
        val petRef = db.collection("pets").document(petId)
        petRef.update("foodImage", imageUrl)
            .addOnSuccessListener {
                if (imageUrl.isNullOrEmpty()) {
                    foodImage.setImageResource(R.drawable.ic_add_photo)
                } else {
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_add_photo)
                        .into(foodImage)
                }
                val mainActivity = requireActivity() as MainActivity
                mainActivity.foodImageGlobal = imageUrl
                Toast.makeText(requireContext(), "Food image updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update food image", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                progressBar?.visibility = View.GONE
            }
    }

    override fun onAllergyDeleted() {
        updateAllergiesTitle()
    }
}
