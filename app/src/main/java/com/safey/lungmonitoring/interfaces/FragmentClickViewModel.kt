package com.safey.lungmonitoring.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragmentClickViewModel : ViewModel() {
    private val fragmentClick = MutableLiveData<String>()
    fun setFragmentClick(item: String) {
        fragmentClick.value = item
    }

    fun getFragmentClick(): LiveData<String> {
        return fragmentClick
    }
}