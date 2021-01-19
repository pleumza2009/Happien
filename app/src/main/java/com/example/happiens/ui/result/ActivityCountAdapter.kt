package com.example.happiens.ui.result

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.example.happiens.R
import com.example.happiens.dataclass.ActivityCount
import com.example.happiens.dataclass.FileNameStringHelper
import kotlinx.android.synthetic.main.act_cunt_grid_layerout_list_item.view.*

class ActivityCountAdapter(var context: Context, var arrayList: ArrayList<ActivityCount>) : RecyclerView.Adapter<ActivityCountAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context).inflate(R.layout.act_cunt_grid_layerout_list_item,parent,false )
        return ItemHolder(itemHolder)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        var activityCount : ActivityCount = arrayList.get(position)
        holder.actvityText.text = activityCount.activityName
       holder.actvitynumber.text= activityCount.countActivity.toString()
        holder.activityImage.setImageResource(activityCount.image)
    }

    override fun getItemCount(): Int {
        return  arrayList.size
    }

    class  ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var activityImage = itemView.findViewById<ImageView>(R.id.activity_image)
        var actvityText = itemView.findViewById<TextView>(R.id.activity_count_text)
        var actvitynumber = itemView.findViewById<TextView>(R.id.activity_count_number)
    }

}