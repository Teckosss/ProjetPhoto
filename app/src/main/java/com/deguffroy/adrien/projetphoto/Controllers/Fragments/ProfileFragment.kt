package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import com.deguffroy.adrien.projetphoto.Api.UserHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.LoginActivity

import com.deguffroy.adrien.projetphoto.R
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.fragment_profile.*
import com.google.firebase.auth.FirebaseAuth
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Api.CommentsHelper
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper


/**
 * A simple [Fragment] subclass.
 *
 */
class ProfileFragment : BaseFragment() {

    private lateinit var username:String

    companion object {
        fun newInstance():ProfileFragment{
            return ProfileFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("ProfileActivity","USER INFO || USER IS ANONYMOUS : ${FirebaseAuth.getInstance().currentUser?.isAnonymous}")

        this.configureOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        this.updateUI()
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private fun configureOnClickListener(){
        profile_sign_out_button.setOnClickListener {this.createSignOutAlertDialog() }
        profile_username.setOnClickListener { this.createChangeUsernameAlertDialog() }
    }

    // Creating AlertDialog for SignOut
    private fun createSignOutAlertDialog(){
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AlertDialog.Builder(activity!!, android.R.style.Theme_Material_Light_Dialog_Alert)
        }else{
            AlertDialog.Builder(activity!!)
        }
        builder.setTitle(resources.getString(R.string.profile_fragment_sign_out_title))
        builder.setMessage(resources.getString(R.string.profile_fragment_sign_out_message))

        val positiveText = resources.getString(R.string.profile_fragment_sign_out_positive_button)
        val negativeText = resources.getString(R.string.profile_fragment_sign_out_negative_button)

        builder.setPositiveButton(positiveText) { p0, _ ->
            p0.dismiss()
            this.signOutUserFromFirebase()
        }

        builder.setNegativeButton(negativeText) { p0, _ ->
            p0.dismiss()
        }

        val dialog = builder.create()

        dialog.setOnShowListener{
            val posButton = (it as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            val negButton = it.getButton(DialogInterface.BUTTON_NEGATIVE)
            posButton.setTextColor(Color.WHITE)
            negButton.setTextColor(Color.WHITE)

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(20,0,0,0)
            posButton.layoutParams = params

        }

        dialog.show()
    }

    // Creating AlertDialog for username
    private fun createChangeUsernameAlertDialog(){
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AlertDialog.Builder(activity!!, android.R.style.Theme_Material_Light_Dialog_Alert)
        }else{
            AlertDialog.Builder(activity!!)
        }

        val editText = EditText(activity!!)
        editText.setSingleLine()
        editText.hint = this.username

        val container = FrameLayout(activity!!)
        val editTextParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        editTextParams.leftMargin = dpToPx(16)
        editTextParams.rightMargin = dpToPx(16)

        editText.layoutParams = editTextParams
        container.addView(editText)

        builder.setTitle(resources.getString(R.string.profile_fragment_username_title))
        builder.setMessage(resources.getString(R.string.profile_fragment_username_message))
        builder.setView(container)

        val positiveText = resources.getString(R.string.profile_fragment_username_positive_button)
        val negativeText = resources.getString(R.string.profile_fragment_username_negative_button)

        builder.setPositiveButton(positiveText) { p0, _ ->
            p0.dismiss()
            this.updateUsername(editText.text.toString())
        }

        builder.setNegativeButton(negativeText) { p0, _ ->
            p0.dismiss()
        }

        val dialog = builder.create()

        dialog.setOnShowListener{
            val posButton = (it as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            val negButton = it.getButton(DialogInterface.BUTTON_NEGATIVE)
            posButton.setTextColor(Color.WHITE)
            negButton.setTextColor(Color.WHITE)

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(20,0,0,0)
            posButton.layoutParams = params

        }

        dialog.show()
    }

    // -------------------
    // UI
    // -------------------

    private fun updateUI(){
        val glide = Glide.with(activity!!)
        UserHelper().getUser(FirebaseAuth.getInstance().currentUser?.uid!!).addOnCompleteListener {
            if (it.isSuccessful){
                this.username = it.result?.get("username").toString()
                if (profile_image_view != null){
                    if (it.result?.get("userPicture") == null){
                        glide.load(R.drawable.ic_no_image_available).apply(RequestOptions().diskCacheStrategy(
                            DiskCacheStrategy.ALL)).into(profile_image_view)
                    }else{
                        glide.load(it.result?.get("userPicture")).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(profile_image_view)
                    }
                }

                if (profile_username != null) profile_username.text = it.result?.get("username").toString()
            }
        }
    }

    // -------------------
    // ACTION
    // -------------------

    private fun updateUsername(username:String){
        val userUID = FirebaseAuth.getInstance().currentUser?.uid!!
        if (username.isNotBlank()){
            // If there is a username, updating user's username
            UserHelper().updateUsername(userUID, username).addOnSuccessListener {
                Log.i("ProfileFragment","Successfully update username!")
                profile_username.text = username

                PicturesHelper().getAllPicturesFromUser(userUID).get().addOnCompleteListener {pictureTask ->
                    // Update all user's picture with his new username
                    if (pictureTask.isSuccessful){
                        for (document in pictureTask.result!!){
                            PicturesHelper().updatePictureDocumentUsername(document.id, username).addOnSuccessListener {
                                Log.i("ProfileFragment","Successfully update picture's username")
                                CommentsHelper().getAllCommentsForUser(userUID).get().addOnCompleteListener { commentTask->
                                    // Update all user's comment with his new username
                                    if (commentTask.isSuccessful){
                                        for (comment in commentTask.result!!){
                                            CommentsHelper().updateCommentDocumentUsername(comment.id, username).addOnSuccessListener {
                                                Log.i("ProfileFragment","Successfully update comment's username")
                                            }.addOnFailureListener { commentFail->
                                                Log.e("ProfileFragment","Error updating comment's username! ${commentFail.localizedMessage}")
                                            }
                                        }
                                    }
                                }
                            }.addOnFailureListener { pictureFail->
                                Log.e("ProfileFragment","Error updating picture's username! ${pictureFail.localizedMessage}")
                            }
                        }
                    }
                }

            }.addOnFailureListener { failure ->
                Log.e("ProfileFragment","Fail to update username ${failure.localizedMessage}")
            }
        }
    }

    // -------------------
    // UTILS
    // -------------------

    // Convert pixel density to pixel
    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    // Disconnect user from firestore and display LoginActivity
    private fun signOutUserFromFirebase(){
        mViewModel.currentPicture.removeObserver {  }

        AuthUI.getInstance().signOut(activity!!).addOnCompleteListener {
            val intent = Intent(context,LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
