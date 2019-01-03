package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.BaseActivity
import com.deguffroy.adrien.projetphoto.Controllers.Activities.FullscreenActivity
import com.deguffroy.adrien.projetphoto.Controllers.Activities.MainActivity

import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.UID_EXTRA_NAME
import kotlinx.android.synthetic.main.fragment_detail.*


/**
 * A simple [Fragment] subclass.
 *
 */
class DetailFragment : BaseFragment() {

    private var documentId:String? = null
    private var imageURL:String? = null

    companion object {
        fun newInstance(documentId:String?):DetailFragment{
            val newFragment = DetailFragment()
            if(documentId != null){
                val args = Bundle()
                args.putString(UID_EXTRA_NAME,documentId)
                newFragment.arguments = args
            }
            return newFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).hideFab()

        this.documentId = arguments?.get(UID_EXTRA_NAME) as String?
        if (documentId != null){
            updateUIWhenCreating()
        }else{
            BaseActivity().showSnackbarMessage(detail_fragment_coordinator_layout,resources.getString(R.string.detail_fragment_error_retrieving_document_uid))
        }

        this.setOnClickListener()
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private fun setOnClickListener(){
        detail_fragment_image.setOnClickListener {
            if (this.imageURL != null) {
                startActivity(FullscreenActivity.newInstance(context!! , this.imageURL!!))
            }
        }
    }

    // -------------------
    // UI
    // -------------------

    private fun updateUIWhenCreating(){
        val glide = Glide.with(activity!!)
        PicturesHelper().getPictureById(documentId!!).addOnSuccessListener {
            this.imageURL = (it.get("urlImage") as String)
            glide.load(this.imageURL).into(detail_fragment_image)
            detail_fragment_desc.text = it.get("description").toString()
        }
    }

}
