package com.example.pawplan.missing

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import com.example.pawplan.MainActivity
import com.example.pawplan.R
import com.example.pawplan.models.MissingPet
import com.example.pawplan.profile.ProfileFragmentArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import okhttp3.internal.filterList
import java.text.SimpleDateFormat
import java.util.*

class MissingFragment : Fragment() {

    private lateinit var missingPetsRecycler: RecyclerView
    private lateinit var lostMyPetButton: Button
    private lateinit var switchMyPosts: Switch
    private lateinit var showingPostsText: TextView

    private val missingPetsList = mutableListOf<MissingPet>()
    private lateinit var missingPetsAdapter: MissingPetAdapter

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val PICK_IMAGE_REQUEST = 100 // ✅ Defined correctly
    private lateinit var petId: String
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout // ✅ Declare SwipeRefreshLayout
    private var selectedImageUri: Uri? = null
    private var imageViewInDialog: ImageView? = null // Store reference to the ImageView in dialog

    // Lazy loading variables
    private var lastVisible: DocumentSnapshot? = null
    private var isLoading = false
    private var isLastPage = false
    private val PAGE_SIZE = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_missing, container, false)

        val args = MissingFragmentArgs.fromBundle(requireArguments())
        petId = args.petId

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout) // ✅ Find SwipeRefreshLayout
        missingPetsRecycler = view.findViewById(R.id.missingPetsRecycler)
        lostMyPetButton = view.findViewById(R.id.lostMyPetButton)
        switchMyPosts = view.findViewById(R.id.switchMyPosts)
        showingPostsText = view.findViewById(R.id.showingPostsText)

        missingPetsRecycler.layoutManager = LinearLayoutManager(requireContext())
        // Initialize the adapter only once
        missingPetsAdapter = MissingPetAdapter(missingPetsList, ::onEditClick, ::onDeleteClick)
        missingPetsRecycler.adapter = missingPetsAdapter

        // Add scroll listener for lazy loading
        missingPetsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadMoreMissingPets()
                    }
                }
            }
        })

        fetchMissingPets()

        lostMyPetButton.setOnClickListener {
            showLostPetDialog()
        }

        switchMyPosts.setOnCheckedChangeListener { _, isChecked ->
            showingPostsText.text = if (isChecked) "Showing Only My Posts" else "Showing All Posts"
            if (missingPetsList.isNotEmpty()) {
                missingPetsAdapter.filterList(petId, isChecked)
                missingPetsAdapter.notifyDataSetChanged()
            }
        }

        // ✅ Swipe Down to Refresh
        swipeRefreshLayout.setOnRefreshListener {
            fetchMissingPets()
        }

        return view
    }

    private fun fetchMissingPets() {
        isLoading = true
        isLastPage = false
        lastVisible = null
        val db = FirebaseFirestore.getInstance()
        // Use compound ordering: lostDate then document ID for tie-breaking
        val query = db.collection("missing")
            .orderBy("lostDate", Query.Direction.DESCENDING)
            .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())
        query.get()
            .addOnSuccessListener { documents ->
                missingPetsList.clear()
                if (documents.size() > 0) {
                    lastVisible = documents.documents.last()
                }
                if (documents.size() < PAGE_SIZE) {
                    isLastPage = true
                }
                for (doc in documents) {
                    val missingPet = MissingPet(
                        postId = doc.id,
                        petId = doc.getString("petId") ?: "",
                        ownerId = doc.getString("userId") ?: "",
                        description = doc.getString("description") ?: "",
                        lostDate = doc.getDate("lostDate") ?: Date(),
                        picture = doc.getString("picture") ?: ""
                    )
                    missingPetsList.add(missingPet)
                }
                // Apply filtering before updating the adapter
                missingPetsAdapter.filterList(petId, switchMyPosts.isChecked)
                missingPetsAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false // ✅ Hide refresh icon
                isLoading = false
            }.addOnFailureListener {
                swipeRefreshLayout.isRefreshing = false // ✅ Hide refresh icon
                Toast.makeText(requireContext(), "Failed to fetch missing pets!", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    private fun loadMoreMissingPets() {
        if (isLoading || isLastPage) return
        isLoading = true
        val db = FirebaseFirestore.getInstance()
        if (lastVisible == null) {
            isLoading = false
            return
        }
        // Use startAfter with the last DocumentSnapshot
        val query = db.collection("missing")
            .orderBy("lostDate", Query.Direction.DESCENDING)
            .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
            .startAfter(lastVisible!!)
            .limit(PAGE_SIZE.toLong())
        query.get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    lastVisible = documents.documents.last()
                }
                if (documents.size() < PAGE_SIZE) {
                    isLastPage = true
                }
                for (doc in documents) {
                    val missingPet = MissingPet(
                        postId = doc.id,
                        petId = doc.getString("petId") ?: "",
                        ownerId = doc.getString("userId") ?: "",
                        description = doc.getString("description") ?: "",
                        lostDate = doc.getDate("lostDate") ?: Date(),
                        picture = doc.getString("picture") ?: ""
                    )
                    // Add the document only if it isn’t already in the list
                    if (missingPetsList.none { it.postId == missingPet.postId }) {
                        missingPetsList.add(missingPet)
                    }
                }
                // Apply filtering after appending new items
                missingPetsAdapter.filterList(petId, switchMyPosts.isChecked)
                missingPetsAdapter.notifyDataSetChanged()
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load more missing pets!", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    private fun onEditClick(missingPet: MissingPet) {
        showLostPetDialog(missingPet) // Open the same popup but with existing details
    }

    private fun updateMissingPet(
        missingPet: MissingPet,
        newDescription: String,
        newImageUri: Uri?,
        dialog: AlertDialog,
        saveButton: Button,
        progressBar: ProgressBar
    ) {
        val db = FirebaseFirestore.getInstance()
        val missingRef = db.collection("missing").document(missingPet.postId)

        saveButton.isEnabled = false // ✅ Disable button
        progressBar.visibility = View.VISIBLE // ✅ Show loader

        if (newImageUri != null) {
            // ✅ Upload new image first
            val storageRef = FirebaseStorage.getInstance().reference
                .child("missing_pets/${UUID.randomUUID()}.jpg")

            storageRef.putFile(newImageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { newImageUrl ->
                        // ✅ Update Firestore with new image URL & description
                        missingRef.update(
                            mapOf(
                                "description" to newDescription,
                                "picture" to newImageUrl.toString()
                            )
                        ).addOnSuccessListener {
                            updateLocalList(missingPet, newDescription, newImageUrl.toString())
                            progressBar.visibility = View.GONE // ✅ Hide loader
                            saveButton.isEnabled = true // ✅ Re-enable button
                            dialog.dismiss() // ✅ Close dialog only when done
                        }.addOnFailureListener {
                            progressBar.visibility = View.GONE
                            saveButton.isEnabled = true
                            Toast.makeText(requireContext(), "Failed to update!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    Toast.makeText(requireContext(), "Image upload failed!", Toast.LENGTH_SHORT).show()
                }
        } else {
            // ✅ No image change, just update the description
            missingRef.update("description", newDescription)
                .addOnSuccessListener {
                    updateLocalList(missingPet, newDescription, missingPet.picture)
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    dialog.dismiss() // ✅ Close dialog only when done
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to update post!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // ✅ Updates local list without refetching
    private fun updateLocalList(missingPet: MissingPet, newDescription: String, newPictureUrl: String) {
        // ✅ Find index in the full list
        val index = missingPetsList.indexOfFirst { it.postId == missingPet.postId }
        if (index != -1) {
            // ✅ Update the full list
            missingPetsList[index] = missingPet.copy(description = newDescription, picture = newPictureUrl)
            // ✅ Refresh the displayed list based on current filter
            missingPetsAdapter.filterList(petId, switchMyPosts.isChecked)
        }
        Toast.makeText(requireContext(), "Post updated!", Toast.LENGTH_SHORT).show()
    }

    private fun showLostPetDialog(missingPet: MissingPet? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_lost_pet, null)
        imageViewInDialog = dialogView.findViewById(R.id.missingPetImage)
        val selectImageButton = dialogView.findViewById<Button>(R.id.selectImageButton)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.editMissingDescription)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.uploadProgressBar)

        selectedImageUri = null // Reset selected image
        val isEditing = missingPet != null

        // ✅ Store Original Values (for Edit Mode)
        val originalDescription = missingPet?.description ?: ""
        val originalImage = missingPet?.picture ?: ""

        // ✅ Load existing data if editing
        if (isEditing) {
            descriptionInput.setText(originalDescription)
            imageViewInDialog?.load(originalImage) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
        } else {
            imageViewInDialog?.setImageResource(R.drawable.placeholder) // Default placeholder
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(if (isEditing) "Edit Missing Pet Post" else "Report Missing Pet")
            .setPositiveButton("Save", null) // ✅ Set to null so we handle it manually
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show() // ✅ Must call show() first to access buttons

        // ✅ Get the positive button AFTER calling show()
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false // Start as disabled

        // ✅ Save Dialog Reference
        currentDialog = dialog

        // ✅ Listen for Description Input Changes
        descriptionInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                saveButton.isEnabled = shouldEnableSaveButton(
                    descriptionInput.text.toString(),
                    selectedImageUri,
                    originalDescription,
                    originalImage,
                    isEditing
                )
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ Handle Image Selection Click
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // ✅ Clicking Image Also Allows Selection
        imageViewInDialog?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // ✅ Save Button Click Handling
        saveButton.setOnClickListener {
            saveButton.isEnabled = false // Disable button while uploading
            progressBar.visibility = View.VISIBLE // Show loader

            if (isEditing) {
                missingPet?.let {
                    updateMissingPet(it, descriptionInput.text.toString(), selectedImageUri, dialog, saveButton, progressBar)
                }
            } else {
                saveMissingPet(dialogView, dialog, saveButton, progressBar)
            }
        }
    }

    // ✅ Handle Image Selection and Enable Save Button
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data

            imageViewInDialog?.load(selectedImageUri) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

            // ✅ Enable Save Button Only if Both Fields Are Valid
            currentDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = shouldEnableSaveButton(
                currentDialog?.findViewById<EditText>(R.id.editMissingDescription)?.text.toString(),
                selectedImageUri,
                "", "", false
            )
        }
    }

    // ✅ Store Dialog Reference
    private var currentDialog: AlertDialog? = null

    // ✅ Move shouldEnableSaveButton OUTSIDE to Fix the Unresolved Reference Issue
    private fun shouldEnableSaveButton(
        currentDescription: String,
        currentImageUri: Uri?,
        originalDescription: String,
        originalImage: String,
        isEditing: Boolean
    ): Boolean {
        val descriptionFilled = currentDescription.isNotBlank()
        val imageSelected = currentImageUri != null || (isEditing && originalImage.isNotEmpty())

        return if (isEditing) {
            (currentDescription != originalDescription) || (currentImageUri != null && currentImageUri.toString() != originalImage)
        } else {
            descriptionFilled && imageSelected // In create mode, everything must be filled
        }
    }

    private fun saveMissingPet(dialogView: View, dialog: AlertDialog, saveButton: Button, progressBar: ProgressBar) {
        val description = dialogView.findViewById<EditText>(R.id.editMissingDescription).text.toString()

        if (description.isEmpty() || selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please add an image and description", Toast.LENGTH_SHORT).show()
            saveButton.isEnabled = true
            progressBar.visibility = View.GONE
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
            .child("missing_pets/${UUID.randomUUID()}.jpg")

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    val missingPet = hashMapOf(
                        "userId" to FirebaseAuth.getInstance().currentUser?.uid,
                        "petId" to petId,
                        "description" to description,
                        "lostDate" to Date(),
                        "picture" to imageUrl.toString()
                    )

                    FirebaseFirestore.getInstance().collection("missing")
                        .add(missingPet)
                        .addOnSuccessListener {
                            fetchMissingPets()
                            Toast.makeText(requireContext(), "Missing pet added!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss() // ✅ Only close dialog when done
                        }
                        .addOnFailureListener {
                            saveButton.isEnabled = true
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Failed to save!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                saveButton.isEnabled = true
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Image upload failed!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onDeleteClick(missingPet: MissingPet) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this missing pet post?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMissingPetLocally(missingPet)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMissingPetLocally(missingPet: MissingPet) {
        val db = FirebaseFirestore.getInstance()
        val missingRef = db.collection("missing").document(missingPet.postId)

        missingRef.delete()
            .addOnSuccessListener {
                // ✅ Remove from full list
                missingPetsList.removeAll { it.postId == missingPet.postId }
                // ✅ Refresh the displayed list based on current filter
                missingPetsAdapter.filterList(currentUserId, switchMyPosts.isChecked)
                Toast.makeText(requireContext(), "Post deleted!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete post!", Toast.LENGTH_SHORT).show()
            }
    }
}
