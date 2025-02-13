package com.example.pawplan.missing

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import com.example.pawplan.R
import com.example.pawplan.models.MissingPet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MissingFragment : Fragment() {
    private lateinit var missingPetsRecycler: RecyclerView
    private lateinit var lostMyPetButton: Button
    private lateinit var switchMyPosts: Switch
    private lateinit var showingPostsText: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var missingLoader: ProgressBar

    private val missingPetsList = mutableListOf<MissingPet>()
    private lateinit var missingPetsAdapter: MissingPetAdapter

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val PICK_IMAGE_REQUEST = 100
    private lateinit var petId: String
    private var selectedImageUri: Uri? = null
    private var imageViewInDialog: ImageView? = null

    private var lastVisible: DocumentSnapshot? = null
    private var isLoading = false
    private var isLastPage = false
    private val PAGE_SIZE = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_missing, container, false)
        val args = MissingFragmentArgs.fromBundle(requireArguments())
        petId = args.petId

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        missingPetsRecycler = view.findViewById(R.id.missingPetsRecycler)
        lostMyPetButton = view.findViewById(R.id.lostMyPetButton)
        switchMyPosts = view.findViewById(R.id.switchMyPosts)
        showingPostsText = view.findViewById(R.id.showingPostsText)
        missingLoader = view.findViewById(R.id.missingLoader)

        missingPetsRecycler.layoutManager = LinearLayoutManager(requireContext())
        missingPetsAdapter = MissingPetAdapter(missingPetsList, ::onEditClick, ::onDeleteClick)
        missingPetsRecycler.adapter = missingPetsAdapter

        missingPetsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && !isLastPage && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    loadMoreMissingPets()
                }
            }
        })

        fetchMissingPets()

        lostMyPetButton.setOnClickListener { showLostPetDialog() }

        switchMyPosts.setOnCheckedChangeListener { _, isChecked ->
            showingPostsText.text = if (isChecked) "Showing Only My Posts" else "Showing All Posts"
            missingPetsAdapter.filterList(petId, isChecked)
            missingPetsAdapter.notifyDataSetChanged()
        }

        swipeRefreshLayout.setOnRefreshListener { fetchMissingPets() }
        return view
    }

    private fun fetchMissingPets() {
        isLoading = true
        isLastPage = false
        lastVisible = null
        missingLoader.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("missing")
            .orderBy("lostDate", Query.Direction.DESCENDING)
            .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())
        query.get()
            .addOnSuccessListener { documents ->
                missingPetsList.clear()
                if (documents.size() > 0) lastVisible = documents.documents.last()
                if (documents.size() < PAGE_SIZE) isLastPage = true
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
                missingPetsAdapter.filterList(petId, switchMyPosts.isChecked)
                missingPetsAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
                missingLoader.visibility = View.GONE
                isLoading = false
            }
            .addOnFailureListener {
                swipeRefreshLayout.isRefreshing = false
                missingLoader.visibility = View.GONE
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
        val query = db.collection("missing")
            .orderBy("lostDate", Query.Direction.DESCENDING)
            .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
            .startAfter(lastVisible!!)
            .limit(PAGE_SIZE.toLong())
        query.get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) lastVisible = documents.documents.last()
                if (documents.size() < PAGE_SIZE) isLastPage = true
                for (doc in documents) {
                    val missingPet = MissingPet(
                        postId = doc.id,
                        petId = doc.getString("petId") ?: "",
                        ownerId = doc.getString("userId") ?: "",
                        description = doc.getString("description") ?: "",
                        lostDate = doc.getDate("lostDate") ?: Date(),
                        picture = doc.getString("picture") ?: ""
                    )
                    if (missingPetsList.none { it.postId == missingPet.postId }) missingPetsList.add(missingPet)
                }
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
        showLostPetDialog(missingPet)
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
                missingPetsList.removeAll { it.postId == missingPet.postId }
                missingPetsAdapter.filterList(currentUserId, switchMyPosts.isChecked)
                Toast.makeText(requireContext(), "Post deleted!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete post!", Toast.LENGTH_SHORT).show()
            }
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
        saveButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        if (newImageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("missing_pets/${UUID.randomUUID()}.jpg")
            storageRef.putFile(newImageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { newImageUrl ->
                        missingRef.update(mapOf("description" to newDescription, "picture" to newImageUrl.toString()))
                            .addOnSuccessListener {
                                updateLocalList(missingPet, newDescription, newImageUrl.toString())
                                progressBar.visibility = View.GONE
                                saveButton.isEnabled = true
                                dialog.dismiss()
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
            missingRef.update("description", newDescription)
                .addOnSuccessListener {
                    updateLocalList(missingPet, newDescription, missingPet.picture)
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    saveButton.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to update post!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateLocalList(missingPet: MissingPet, newDescription: String, newPictureUrl: String) {
        val index = missingPetsList.indexOfFirst { it.postId == missingPet.postId }
        if (index != -1) {
            missingPetsList[index] = missingPet.copy(description = newDescription, picture = newPictureUrl)
            missingPetsAdapter.filterList(petId, switchMyPosts.isChecked)
        }
        Toast.makeText(requireContext(), "Post updated!", Toast.LENGTH_SHORT).show()
    }

    private fun showLostPetDialog(missingPet: MissingPet? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_lost_pet, null)
        imageViewInDialog = dialogView.findViewById(R.id.missingPetImage)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.editMissingDescription)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.uploadProgressBar)
        selectedImageUri = null
        val isEditing = missingPet != null
        val originalDescription = missingPet?.description ?: ""
        val originalImage = missingPet?.picture ?: ""
        if (isEditing) {
            descriptionInput.setText(originalDescription)
            imageViewInDialog?.scaleType = ImageView.ScaleType.CENTER_CROP
            imageViewInDialog?.load(originalImage) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
        } else {
            imageViewInDialog?.setImageResource(R.drawable.ic_add_photo)
        }
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(if (isEditing) "Edit Missing Pet Post" else "Report Missing Pet")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false
        currentDialog = dialog
        descriptionInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
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
        imageViewInDialog?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        saveButton.setOnClickListener {
            saveButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            if (isEditing) {
                missingPet?.let {
                    updateMissingPet(it, descriptionInput.text.toString(), selectedImageUri, dialog, saveButton, progressBar)
                }
            } else {
                saveMissingPet(dialogView, dialog, saveButton, progressBar)
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            imageViewInDialog?.scaleType = ImageView.ScaleType.CENTER_CROP
            imageViewInDialog?.load(selectedImageUri) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
            currentDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = shouldEnableSaveButton(
                currentDialog?.findViewById<EditText>(R.id.editMissingDescription)?.text.toString(),
                selectedImageUri,
                "", "", false
            )
        }
    }

    private var currentDialog: AlertDialog? = null

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
            descriptionFilled && imageSelected
        }
    }
}
