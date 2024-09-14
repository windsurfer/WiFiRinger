package com.abielinski.wifiringer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

class WifiListAdapter(
    private val wifiList: MutableList<String>,
    private val onItemCheckedChange: (String, Boolean) -> Unit
) : RecyclerView.Adapter<WifiListAdapter.ViewHolder>() {

    private val selectedNetworks = mutableSetOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.wifiCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.wifi_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ssid = wifiList[position]
        holder.checkBox.text = ssid
        holder.checkBox.isChecked = selectedNetworks.contains(ssid)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedNetworks.add(ssid)
            } else {
                selectedNetworks.remove(ssid)
            }
            onItemCheckedChange(ssid, isChecked)
        }
    }

    override fun getItemCount() = wifiList.size

    fun updateList(newList: List<String>) {
        wifiList.clear()
        wifiList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getSelectedNetworks(): List<String> = selectedNetworks.toList()
}