package com.idksoftware.bcatlett.imagetaggedsearch

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE


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
@Dao
interface TaggedImageDAO {
    @Query("SELECT * FROM TaggedImage")
    fun getAll(): List<TaggedImage>

    @Query("SELECT * FROM TaggedImage WHERE Id = (:id)")
    fun findById(id: String): TaggedImage

    @Insert(onConflict = REPLACE)
    fun insert(vararg image: TaggedImage)

    @Update
    fun update(vararg image: TaggedImage)

    @Delete
    fun delete(image: TaggedImage)
}