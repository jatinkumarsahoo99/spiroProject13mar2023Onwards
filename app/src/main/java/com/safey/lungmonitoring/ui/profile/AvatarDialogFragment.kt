package com.safey.lungmonitoring.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.ui.dashboard.profile.viewmodel.ProfileViewModel
import com.safey.lungmonitoring.utils.Constants
import com.safey.lungmonitoring.utils.Utility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_avatar_dialog_list_dialog.*
import java.util.*

// TODO: Customize parameter argument names
const val ARG_AVATAR = "avatar"


@AndroidEntryPoint
class AvatarDialogFragment : Fragment(), AvatarAdapater.AvatarInterface {

    private var characterType: String = ""
    private var avatarList: List<Avatar> = ArrayList()
    val viewModel : ProfileViewModel by activityViewModels()
    lateinit var adapter: AvatarAdapater
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_avatar_dialog_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        arguments?.getString(ARG_AVATAR)?.let {  }
        arguments?.getInt(ARG_GENDER)?.let {
            characterType =if(it == 1) "male" else "female"
            when {

                characterType.lowercase(Locale.ROOT) == "male" -> {
                    chooseAvatarMaleName.setText(R.string.male)
                }
                characterType.lowercase(Locale.ROOT) == "female" -> {
                    chooseAvatarMaleName.setText(R.string.female)
                }
            }
        }

        list.isNestedScrollingEnabled =false
        listcharacter.isNestedScrollingEnabled =false
        listchildren.isNestedScrollingEnabled =false

        list.layoutManager = GridLayoutManager(context, 4)
        listcharacter.layoutManager = GridLayoutManager(context, 4)
        listchildren.layoutManager = GridLayoutManager(context, 4)

        avatarList = requireActivity().applicationContext?.let {
            Utility.getImageData(it, Constants.AVATAR_JSON, Constants.AVATAR, characterType)
        }!!
        adapter = requireActivity().applicationContext?.let { AvatarAdapater(it, avatarList, this) }!!
        list.adapter = adapter

        avatarList = requireActivity().applicationContext?.let {
            Utility.getImageData(it, Constants.AVATAR_JSON, Constants.AVATAR, "${characterType}_children")
        }!!
        adapter = requireActivity().applicationContext?.let { AvatarAdapater(it, avatarList, this) }!!
        listcharacter.adapter = adapter

        avatarList = requireActivity().applicationContext?.let {
            Utility.getImageData(it, Constants.AVATAR_JSON, Constants.AVATAR, "character")
        }!!
        adapter = requireActivity().applicationContext?.let { AvatarAdapater(it, avatarList, this) }!!
        listchildren.adapter = adapter


    }


    companion object {
        private const val ARG_GENDER: String= "gender"
    }


    override fun selectedAvatar(avatar: Avatar?) {

       /* viewModel.patientData.avatarResourceId = avatar?.resourceId!!
        viewModel.patientData.avatar = avatar.imageURL.toString()*/
        viewModel.avatar.postValue(avatar?.imageURL.toString())
        Constants.fromAvatar = true
       findNavController().popBackStack()


    }
}