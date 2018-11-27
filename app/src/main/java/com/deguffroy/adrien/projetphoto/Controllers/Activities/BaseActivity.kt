package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.MapFragment
import com.deguffroy.adrien.projetphoto.R
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.lifecycle.ViewModelProviders
import com.deguffroy.adrien.projetphoto.Api.UserHelper
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.HomeFragment
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.MyPicFragment
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.ProfileFragment
import com.deguffroy.adrien.projetphoto.Models.User
import com.deguffroy.adrien.projetphoto.Utils.Constants
import com.deguffroy.adrien.projetphoto.ViewModels.CommunicationViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import org.imperiumlabs.geofirestore.GeoFirestore


/**
 * Created by Adrien Deguffroy on 21/11/2018.
 */
open class BaseActivity : AppCompatActivity(){

    lateinit var mViewModel: CommunicationViewModel
    lateinit var modelCurrentUser:User
    lateinit var geoFirestore:GeoFirestore

    lateinit var photoURI: Uri
    lateinit var photoFilePath: String

    private lateinit var snackbar:Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel::class.java)
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    fun configureBottomView(){
        bottom_navigation_view.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.bnv_home -> showFragment(HomeFragment.newInstance())
                R.id.bnv_map -> showFragment(MapFragment.newInstance())
                R.id.bnv_my_pic -> showFragment(MyPicFragment.newInstance())
                R.id.bnv_profile -> showFragment(ProfileFragment.newInstance())
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    fun initDb(){
        val db = FirebaseFirestore.getInstance()
        val picturesCollection = db.collection("pictures")
        this.geoFirestore = GeoFirestore(picturesCollection)
    }

    // -------------------
    // UI
    // -------------------

    fun showFragment(newFragment:Fragment){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_view, newFragment)
            transaction.commit()
    }

    fun showSnackbarMessage(coordinatorLayout: CoordinatorLayout,messageToShow:String){
        snackbar = Snackbar.make(coordinatorLayout,messageToShow,Snackbar.LENGTH_SHORT)
        snackbar.show()
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    protected fun isCurrentUserLogged(): Boolean? {
        return this.getCurrentUser() != null
    }

    fun getCurrentUserFromFirestore() {
        UserHelper().getUser(getCurrentUser()?.uid!!)
            .addOnSuccessListener {
                this.modelCurrentUser = it.toObject<User>(User::class.java)!!
                mViewModel.updateCurrentModelUser(this.modelCurrentUser)
            }
    }

    // --------------------
    // ERROR HANDLER
    // --------------------

    protected fun onFailureListener(): OnFailureListener {
        return OnFailureListener {
            this.showSnackbarMessage(main_activity_layout, getString(R.string.error_unknown_error))
        }
    }
}