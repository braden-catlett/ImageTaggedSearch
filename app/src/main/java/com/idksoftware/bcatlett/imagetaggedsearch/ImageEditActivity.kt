package com.idksoftware.bcatlett.imagetaggedsearch

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_image_edit.*
import java.io.File
import android.arch.persistence.room.Room
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.support.v7.widget.GridLayoutManager
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread


/**
 * NameOfFile.h
 * Purpose of this file/model/view/controller
 *
 * Created by bcatlett on 6/15/18.
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
class ImageEditActivity  : AppCompatActivity() {

    private var INSTANCE: TaggedImageDatabase? = null
    private var imageUID: String? = null
    private var tagsStore: ArrayList<String> = ArrayList()

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
        setContentView(R.layout.activity_image_edit)

        val path = intent.getStringExtra("com.idksoftware.imagetaggedsearch.ImagePath")
        imageUID = generateImageUID(path)

        imageAnchor.setImageURI(Uri.fromFile(File(path)))
        imageName.text = path.substring(path.lastIndexOf("/") + 1)

        tags.adapter = TagListAdapter(tagsStore,this)
        tags.layoutManager = GridLayoutManager(this, 2)

        addTag.setOnClickListener {
            val tag = tagsEdit.text.toString()
            if(!tag.isEmpty()) {
                tagsStore.add(tag)
                tagsEdit.text.clear()
                tags.adapter.notifyDataSetChanged()
            }
        }

        var imageObject:TaggedImage?
        thread(start = true) {
            imageObject = getDatabase(this.applicationContext)?.userDao()?.findById(imageUID as String)
            runOnUiThread {
                if(imageObject != null) {
                    (imageObject as TaggedImage).tags.split(",").forEach {
                        if(!it.isEmpty()) {
                            tagsStore.add(it)
                        }
                    }
                    tags.adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        thread(start = true) {
            var imageObject: TaggedImage? = INSTANCE?.userDao()?.findById(imageUID as String)
            if(imageObject == null) {
                imageObject = TaggedImage()
                imageObject.id = imageUID as String
                imageObject.tags = getTags()
                INSTANCE?.userDao()?.insert(imageObject)

            }
            else {
                imageObject.tags = getTags()
                INSTANCE?.userDao()?.update(imageObject)
            }
        }
    }

    fun getTags(): String {
        return tagsStore.joinToString(",")
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