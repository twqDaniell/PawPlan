package com.example.pawplan.foods

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pawplan.MainActivity
import com.example.pawplan.R
import com.example.pawplan.adapters.AllergiesAdapter
import com.example.pawplan.models.Allergy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class FoodFragment : Fragment() {

    private lateinit var foodImage: ImageView
    private lateinit var addAllergyButton: ImageButton
    private lateinit var allergiesRecycler: RecyclerView

    private lateinit var allergiesAdapter: AllergiesAdapter
    private val allergiesList = mutableListOf<Allergy>()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var petId: String
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        foodImage.load(foodImageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_add) // Placeholder should be a '+' icon
            error(R.drawable.ic_add)
        }

        allergiesRecycler.layoutManager = LinearLayoutManager(requireContext())
        allergiesAdapter = AllergiesAdapter(allergiesList)
        allergiesRecycler.adapter = allergiesAdapter

        fetchAllergies()

        addAllergyButton.setOnClickListener {
            showAddAllergyDialog()
        }

        foodImage.setOnClickListener {
            selectFoodImage()
        }

        return view
    }

    private fun fetchAllergies() {
        val db = FirebaseFirestore.getInstance()
        db.collection("allergies").whereEqualTo("petId", petId).get()
            .addOnSuccessListener { documents ->
                allergiesList.clear()
                for (doc in documents) {
                    val allergy = Allergy(
                        id = doc.id,
                        allergyName = doc.getString("allergyName") ?: "",
                        petId = doc.getString("petId") ?: ""
                    )
                    allergiesList.add(allergy)
                }
                allergiesAdapter.notifyDataSetChanged()
            }
    }

    private fun showAddAllergyDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_allergy, null)
        val allergyInput = dialogView.findViewById<EditText>(R.id.editAllergyName)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Allergy")
            .setPositiveButton("Save", null) // Set to null, we handle it manually
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show() // Must call show() before accessing buttons

        // ✅ Reference Save Button AFTER show()
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false // Start as disabled

        // ✅ Function to Check if Input is Filled
        fun shouldEnableSaveButton(): Boolean {
            return allergyInput.text.toString().isNotBlank()
        }

        // ✅ Listen for Text Changes
        allergyInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                saveButton.isEnabled = shouldEnableSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ Handle Save Button Click
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
        val allergy = hashMapOf(
            "allergyName" to allergyName,
            "petId" to petId
        )

        db.collection("allergies").add(allergy)
            .addOnSuccessListener {
                fetchAllergies()
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
        val storageRef = FirebaseStorage.getInstance().reference
            .child("food_images/${UUID.randomUUID()}.jpg")

        // ✅ Show the loader
        val progressBar = view?.findViewById<ProgressBar>(R.id.foodImageProgressBar)
        progressBar?.visibility = View.VISIBLE

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    saveFoodImageUrl(imageUrl.toString(), progressBar)
                }
            }
            .addOnFailureListener {
                progressBar?.visibility = View.GONE // ✅ Hide loader on failure
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveFoodImageUrl(imageUrl: String, progressBar: ProgressBar?) {
        val db = FirebaseFirestore.getInstance()
        val petRef = db.collection("pets").document(petId)

        petRef.update("foodImage", imageUrl)
            .addOnSuccessListener {
                foodImage.load(imageUrl)
                val mainActivity = requireActivity() as MainActivity
                mainActivity.foodImageGlobal = imageUrl
                Toast.makeText(requireContext(), "Food image updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update food image", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                progressBar?.visibility = View.GONE // ✅ Hide loader when done (success or failure)
            }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 101
    }
}
