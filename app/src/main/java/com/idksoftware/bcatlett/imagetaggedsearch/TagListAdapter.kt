package com.idksoftware.bcatlett.imagetaggedsearch

import android.content.Context
import android.support.annotation.NonNull
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.element_tag_item.view.*

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
class TagListAdapter(val items : ArrayList<String>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): TagViewHolder {
        return TagViewHolder(LayoutInflater.from(context).inflate(R.layout.element_tag_item, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(@NonNull holder: RecyclerView.ViewHolder, position: Int) {
        var item = items.get(position)
        Log.e("onBindViewHolder", item)
        (holder as TagViewHolder).textViewHolder.text = items[position]
        holder.removeButton.tag = position
        holder.removeButton.setOnClickListener({
            items.removeAt(it.tag as Int)
            this.notifyDataSetChanged();
        })
    }
}

class TagViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val textViewHolder = view.tagname
    val removeButton = view.remove
}