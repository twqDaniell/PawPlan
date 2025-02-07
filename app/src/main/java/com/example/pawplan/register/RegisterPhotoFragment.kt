package com.example.pawplan.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pawplan.MainActivity
import com.example.pawplan.R
import com.example.pawplan.models.RegistrationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RegisterPhotoFragment : Fragment() {
    private lateinit var viewModel: RegistrationViewModel
    private var selectedImageUri: Uri? = null

    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_photo, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(RegistrationViewModel::class.java)

        val photoImageView = view.findViewById<ImageView>(R.id.circlePhoto)
        val doneButton = view.findViewById<Button>(R.id.doneButton)

        // Handle image selection
        photoImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Save data and navigate to MainActivity on "Done"
        doneButton.setOnClickListener {
            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Please select a photo", Toast.LENGTH_SHORT).show()
            } else {
                uploadImageAndSaveData(selectedImageUri!!)
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && data != null && data.data != null) {
            selectedImageUri = data.data
            view?.findViewById<ImageView>(R.id.circlePhoto)?.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageAndSaveData(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "photos/${System.currentTimeMillis()}_${imageUri.lastPathSegment}"
        val fileRef = storageRef.child(fileName)

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val photoUrl = downloadUrl.toString()
                    viewModel.petPicture = photoUrl // Save the photo URL to ViewModel
                    saveDataToFirestore(photoUrl)
                }.addOnFailureListener { e ->
                    Log.e("FirebaseStorage", "Failed to get download URL: ${e.message}", e)
                    Toast.makeText(requireContext(), "Failed to upload photo", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseStorage", "Photo upload failed: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to upload photo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveDataToFirestore(photoUrl: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Get the current user's UID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Save user data to the `users` collection
        val userData = hashMapOf(
            "name" to viewModel.userName,
            "phone_number" to viewModel.phoneNumber,
        )

        firestore.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d("Firestore", "User data saved successfully")

                // Save pet data to the `pets` collection
                savePetDataToFirestore(photoUrl)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving user data: ${e.message}", e)
                Toast.makeText(requireContext(), "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePetDataToFirestore(photoUrl: String) {
        val firestore = FirebaseFirestore.getInstance()
        val ownerId = FirebaseAuth.getInstance().currentUser?.uid

        val petData = hashMapOf(
            "petName" to viewModel.petName,
            "petType" to viewModel.petType,
            "petGender" to viewModel.petGender,
            "petBreed" to viewModel.petBreed,
            "petBirthDate" to viewModel.petBirthDate,
            "petWeight" to viewModel.petWeight,
            "petColor" to viewModel.petColor,
            "petAdoptionDate" to viewModel.petAdoptionDate,
            "picture" to photoUrl,
            "ownerId" to ownerId
        )

        // Generate a unique ID for the pet document
        val petId = firestore.collection("pets").document().id

        firestore.collection("pets").document(petId)
            .set(petData)
            .addOnSuccessListener {
                Log.d("Firestore", "Pet data saved successfully with ownerId: $ownerId")
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving pet data: ${e.message}", e)
                Toast.makeText(requireContext(), "Error saving pet data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
