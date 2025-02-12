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
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pawplan.MainActivity
import com.example.pawplan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RegisterPhotoFragment : Fragment() {
    private var selectedImageUri: Uri? = null
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_photo, container, false)

        val photoImageView = view.findViewById<ImageView>(R.id.circlePhoto)
        val doneButton = view.findViewById<Button>(R.id.doneButton)
        doneButton.isEnabled = false
        progressBar = view.findViewById(R.id.registerProgressBar)

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
                progressBar.visibility = View.VISIBLE
                doneButton.isEnabled = false
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
            view?.findViewById<Button>(R.id.doneButton)?.isEnabled = true
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
        val args = RegisterPhotoFragmentArgs.fromBundle(requireArguments())
        val auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        if(user != null) {
            val userId = user.uid // ✅ Get the new user's UID

            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            val userData = hashMapOf(
                "name" to args.userName,
                "phone_number" to args.phoneNumber,
            )

            userRef.set(userData)
                .addOnSuccessListener {
                    Log.d("Firestore", "User registered and authenticated")

                    // ✅ Keep the user authenticated
                    auth.updateCurrentUser(auth.currentUser!!)
                        .addOnSuccessListener {
                            Log.d("Auth", "User session persisted")
                            savePetDataToFirestore(photoUrl)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Auth", "Session persistence failed: ${e.message}", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error saving user data: ${e.message}", e)
                }
        }
    }

    private fun savePetDataToFirestore(photoUrl: String) {
        val firestore = FirebaseFirestore.getInstance()
        val ownerId = FirebaseAuth.getInstance().currentUser?.uid
        val args = RegisterPhotoFragmentArgs.fromBundle(requireArguments())

        val petData = hashMapOf(
            "petName" to args.petName,
            "petType" to args.petType,
            "petGender" to args.petGender,
            "petBreed" to args.petBreed,
            "petBirthDate" to args.petBirthDate,
            "petWeight" to args.petWeight,
            "petColor" to args.petColor,
            "petAdoptionDate" to args.petAdoptionDate,
            "picture" to photoUrl,
            "ownerId" to ownerId
        )

        // Generate a unique ID for the pet document
        val petId = firestore.collection("pets").document().id

        firestore.collection("pets").document(petId)
            .set(petData)
            .addOnSuccessListener {
                Log.d("Firestore", "Pet data saved successfully with ownerId: $ownerId")

                val action = RegisterPhotoFragmentDirections
                    .actionRegisterPhotoFragmentToProfileFragment(
                        args.userName,
                        args.phoneNumber,
                        args.petName,
                        args.petType,
                        args.petBreed,
                        args.petWeight.toString(),
                        args.petColor,
                        args.petBirthDate,
                        args.petAdoptionDate,
                        "",
                        "",
                        petId,
                        photoUrl
                    )

                val mainActivity = requireActivity() as MainActivity
                mainActivity.petNameGlobal = args.petName
                mainActivity.userNameGlobal = args.userName
                mainActivity.phoneNumberGlobal = args.phoneNumber
                mainActivity.petTypeGlobal = args.petType
                mainActivity.petBreedGlobal = args.petBreed
                mainActivity.petWeightGlobal = args.petWeight
                mainActivity.petColorGlobal = args.petColor
                mainActivity.petBirthDateGlobal = args.petBirthDate
                mainActivity.petAdoptionDateGlobal = args.petAdoptionDate
                mainActivity.petIdGlobal = petId
                mainActivity.petPictureGlobal = photoUrl
                mainActivity.vetIdGlobal = ""
                mainActivity.foodImageGlobal = ""

                progressBar.visibility = View.GONE

                findNavController().navigate(action)
                (requireActivity() as MainActivity).showBars()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving pet data: ${e.message}", e)
                Toast.makeText(requireContext(), "Error saving pet data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
