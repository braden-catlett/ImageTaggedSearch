package com.idksoftware.bcatlett.imagetaggedsearch

import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.storage.StorageVolume
import android.support.v7.widget.GridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import net.theluckycoder.materialchooser.Chooser
import java.io.File
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    var REQUEST_CODE = 10
    var fileDirectory = ""
    var imageIDToPathMap: HashMap<String, String> = HashMap()
    var handle: Handler? = null
    var runnable: Runnable? = null
    var fullFileSet: ArrayList<String> = ArrayList()
    var filesFound: ArrayList<String> = ArrayList()
    private var INSTANCE: TaggedImageDatabase? = null
    private var prefs: SharedPreferences? = null

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
        setContentView(R.layout.activity_main)

        fileDirectory = this.getSharedPreferences("taggedimagesearch", android.content.Context.MODE_PRIVATE).getString("LastUsedDirectory", "")

        if(!fileDirectory.isEmpty()) {
            filesFound.clear()
            fullFileSet.clear()

            //Get all image paths under directory
            File(fileDirectory).walk().forEach {
                if(it.absolutePath.contains(".png", true)) {
                    filesFound.add(it.absolutePath)
                    fullFileSet.add(it.absolutePath)
                }
            }
        }


        thread(start = true) {
            fullFileSet.forEach {
                if (it.contains(".png", true)) {
                    imageIDToPathMap.put(it, generateImageUID(it))
                }
            }
        }

        handle = Handler()
        runnable = Runnable {
            thread(start = true) {
                val searchText = searchText.text.toString()

                filesFound.clear()

                //Get all image paths under directory
                fullFileSet.forEach {
                    if (it.contains(".png", true)) {
                        //If the search text is empty the show all files
                        if (searchText.isEmpty()) {
                            filesFound.add(it)
                        } else {
                            val imageObject = getDatabase(applicationContext)?.userDao()?.findById(imageIDToPathMap.get(it)?: "")
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
        }

        // You can use GridLayoutManager if you want multiple columns. Enter the number of columns as a parameter.
        image_list.layoutManager = GridLayoutManager(this, 2)

        // Access the RecyclerView Adapter and load the data into it
        image_list.adapter = ImageListAdapter(filesFound, this)

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
    }

    fun selectFolderClicked(v: View?) {
        var path = getExternalCardDirectory()?.absolutePath
        Chooser(this, REQUEST_CODE)
                .setChooserType(Chooser.FOLDER_CHOOSER)
                .setStartPath(path.orEmpty())
                .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            fileDirectory = data.getStringExtra(Chooser.RESULT_PATH)
            prefs = this.getSharedPreferences("taggedimagesearch", android.content.Context.MODE_PRIVATE)
            prefs!!.edit().putString("LastUsedDirectory", fileDirectory).apply()

            filesFound.clear()
            fullFileSet.clear()

            //Get all image paths under directory
            File(fileDirectory).walk().forEach {
                if(it.absolutePath.contains(".png", true)) {
                    filesFound.add(it.absolutePath)
                    fullFileSet.add(it.absolutePath)
                }
            }

            thread(start = true) {
                imageIDToPathMap.clear()
                fullFileSet.forEach {
                    if (it.contains(".png", true)) {
                        imageIDToPathMap.put(it, generateImageUID(it))
                    }
                }
            }

            //Load image paths into recyclerview
            image_list.adapter.notifyDataSetChanged()
        }
    }

    private fun getExternalCardDirectory(): File? {
        val storageManager = getSystemService(Context.STORAGE_SERVICE)
        try {
            val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
            val getPath = storageVolumeClazz.getMethod("getPath")
            val isRemovable = storageVolumeClazz.getMethod("isRemovable")
            val result = getVolumeList.invoke(storageManager) as Array<StorageVolume>
            result.forEach {
                if (isRemovable.invoke(it) as Boolean) {
                    return File(getPath.invoke(it) as String)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
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
