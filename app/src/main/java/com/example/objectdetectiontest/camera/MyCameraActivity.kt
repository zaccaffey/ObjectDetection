package com.example.objectdetectiontest.camera

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.renderscript.ScriptGroup
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.objectdetectiontest.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.PredefinedCategory

class MyCameraActivity : AppCompatActivity() {
    private var imageView: ImageView? = null

    // Multiple object detection in static images
    val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .build()

    val objectDetector = ObjectDetection.getClient(options)

    @RequiresApi(Build.VERSION_CODES.M)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById<View>(R.id.imageView1) as ImageView
        val photoButton = findViewById<View>(R.id.button1) as Button


        photoButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    MY_CAMERA_PERMISSION_CODE
                )
            } else {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {

            // this is the photo taken from the camera
            val photo = data?.extras!!["data"] as Bitmap?
            imageView!!.setImageBitmap(photo)

            if (photo != null) {
                val image = InputImage.fromBitmap(photo, 0)

                // here we are processing the image to detect objects
                objectDetector.process(image)
                    .addOnSuccessListener { detectedObjects ->

                        // loop through the detected objects
                        for (detectedObject in detectedObjects) {
                            val boundingBox = detectedObject.boundingBox
                            val trackingId = detectedObject.trackingId

                            // loop through the labels for each detected object
                            for (label in detectedObject.labels) {
                                val text = label.text
                                
                                if (PredefinedCategory.FOOD == text) {
                                    Log.i(TAG, "onActivityResult: this is text = $text")
                                }
                                val index = label.index
                                if (PredefinedCategory.FOOD_INDEX == index) {
                                    Log.i(TAG, "onActivityResult: Predefined cat for index label")
                                }
                                val confidence = label.confidence
                                Log.i(TAG, "onActivityResult: $confidence")
                            }
                        }
                        Log.i(TAG, "onActivityResult: Object detected successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.i(TAG, "onActivityResult: Object not detected")
                    }
            }
        }
    }

    companion object {
        private const val CAMERA_REQUEST = 1888
        private const val MY_CAMERA_PERMISSION_CODE = 100
    }
}