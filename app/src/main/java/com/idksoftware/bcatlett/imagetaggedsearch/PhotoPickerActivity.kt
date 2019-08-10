package com.idksoftware.bcatlett.imagetaggedsearch

import android.arch.persistence.room.Room
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.concurrent.thread

/**
 * NameOfFile.h
 * Purpose of this file/model/view/controller
 *
 * Created by bcatlett on 6/22/18.
 *
 * ImageTaggedSearch
 *
 * Copyright © 2009-2018 United States Government as represented by
 * the Chief Information Officer of the National Center for Telehealth
 * and Technology. All Rights Reserved.
 *
 * Copyright © 2009-2018 Contributors. All Rights Reserved.
 *
 * THIS OPEN SOURCE AGREEMENT ("AGREEMENT") DEFINES THE RIGHTS OF USE,
 * REPRODUCTION, DISTRIBUTION, MODIFICATION AND REDISTRIBUTION OF CERTAIN
 * COMPUTER SOFTWARE ORIGINALLY RELEASED BY THE UNITED STATES GOVERNMENT
 * AS REPRESENTED BY THE GOVERNMENT AGENCY LISTED BELOW ("GOVERNMENT AGENCY").
 * THE UNITED STATES GOVERNMENT, AS REPRESENTED BY GOVERNMENT AGENCY, IS AN
 * INTENDED THIRD-PARTY BENEFICIARY OF ALL SUBSEQUENT DISTRIBUTIONS OR
 * REDISTRIBUTIONS OF THE SUBJECT SOFTWARE. ANYONE WHO USES, REPRODUCES,
 * DISTRIBUTES, MODIFIES OR REDISTRIBUTES THE SUBJECT SOFTWARE, AS DEFINED
 * HEREIN, OR ANY PART THEREOF, IS, BY THAT ACTION, ACCEPTING IN FULL THE
 * RESPONSIBILITIES AND OBLIGATIONS CONTAINED IN THIS AGREEMENT.
 *
 * Government Agency: The National Center for Telehealth and Technology
 * Government Agency Original Software Designation: ProductName001
 * Government Agency Original Software Title: ProductName
 * User Registration Requested. Please send email
 * with your contact information to: robert.a.kayl.civ@mail.mil
 * Government Agency Point of Contact for Original Software: robert.a.kayl.civ@mail.mil
 *
 */
class PhotoPickerActivity : AppCompatActivity() {
    var fileDirectory = ""
    var filesFound: ArrayList<String> = ArrayList()
    var fullFileSet: ArrayList<String> = ArrayList()
    var imageIDToPathMap: HashMap<String, String> = HashMap()
    var handle: Handler? = null
    var runnable: Runnable? = null
    private var INSTANCE: TaggedImageDatabase? = null

    fun getDatabase(context: Context): TaggedImageDatabase? {
        if (INSTANCE == null) {
            synchronized(TaggedImageDatabase::class.java) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.applicationContext, TaggedImageDatabase::class.java, "TaggedImageDB").build()
                }
            }
        }
        return INSTANCE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photopicker)

        handle = Handler()
        runnable = Runnable {
            thread(start = true) {
                val searchText = searchText.text.toString()

                filesFound.clear()

                //Get all image paths under directory
                fullFileSet.forEach {
                    //If the search text is empty the show all files
                    if (searchText.isEmpty()) {
                        filesFound.add(it)
                    } else {
                        val imageObject = getDatabase(applicationContext)?.userDao()?.findById(imageIDToPathMap.get(it)
                                ?: "")
                        if (imageObject?.tags != null && !imageObject.tags.isEmpty()) {
                            var matchCount = 0
                            imageObject.tags.split(",").forEach {
                                var tagMatchesSearch = true
                                val count = if (it.length >= searchText.length) searchText else it
                                for (i in count.indices) {
                                    if (it[i] != searchText[i]) {
                                        tagMatchesSearch = false
                                        break
                                    }
                                }

                                if (tagMatchesSearch) {
                                    matchCount++
                                }
                            }

                            if (matchCount > 0) {
                                filesFound.add(it)
                                matchCount = 0


                                runOnUiThread {
                                    image_list.adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            }
        }

        fileDirectory = this.getSharedPreferences("taggedimagesearch", android.content.Context.MODE_PRIVATE).getString("LastUsedDirectory", "")

        if(!fileDirectory.isEmpty()) {
            filesFound.clear()
            fullFileSet.clear()

            //Get all image paths under directory
            File(fileDirectory).walk().forEach {
                if (it.absolutePath.contains(".png", true)) {
                    filesFound.add(it.absolutePath)
                    fullFileSet.add(it.absolutePath)
                }
            }

            thread(start = true) {
                fullFileSet.forEach {
                    if (it.contains(".png", true)) {
                        imageIDToPathMap.put(it, generateImageUID(it))
                    }
                }
            }
        }

        // You can use GridLayoutManager if you want multiple columns. Enter the number of columns as a parameter.
        image_list.layoutManager = GridLayoutManager(this, 2)

        // Access the RecyclerView Adapter and load the data into it
        image_list.adapter = PickerListAdapter(filesFound, this)

        searchText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                handle!!.removeCallbacks(runnable)
                handle!!.postDelayed(runnable, 500)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        //Load image paths into recyclerview
        image_list.adapter.notifyDataSetChanged()
    }

    fun generateImageUID(path:String): String {
        //encode image to base64 string
        val baos = ByteArrayOutputStream()
        val bitmap = BitmapFactory.decodeFile(path)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 1, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }
}