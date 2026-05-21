package com.example.sifrelikasam

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirebaseImageHelper {

    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    fun getImageUrls(callback: (List<String>) -> Unit) {
        val imageUrls = mutableListOf<String>()
        val imagesRef = storageRef.child("images")

        imagesRef.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { imageRef ->
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    imageUrls.add(imageUrl.toString())
                    if (imageUrls.size == listResult.items.size) {
                        callback(imageUrls)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            // Handle any errors
            Log.e("FirebaseImageHelper", "Error getting images", exception)
        }
    }
}