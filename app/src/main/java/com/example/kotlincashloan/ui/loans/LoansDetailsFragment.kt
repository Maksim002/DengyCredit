package com.example.kotlincashloan.ui.loans

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.kotlincashloan.R
import com.example.kotlincashloan.extension.animationGenerator
import com.example.kotlincashloan.ui.registration.login.HomeActivity
import com.example.kotlincashloan.utils.ColorWindows
import com.example.kotlincashloan.utils.ObservedInternet
import com.example.kotlincashloan.utils.TransitionAnimation
import com.example.kotlinscreenscanner.ui.MainActivity
import com.timelysoft.tsjdomcom.service.AppPreferences
import kotlinx.android.synthetic.main.fragment_detail_notification.*
import kotlinx.android.synthetic.main.fragment_loans_details.*
import kotlinx.android.synthetic.main.item_access_restricted.*
import kotlinx.android.synthetic.main.item_no_connection.*
import kotlinx.android.synthetic.main.item_not_found.*
import kotlinx.android.synthetic.main.item_technical_work.*
import java.lang.Exception
import java.util.*

class LoansDetailsFragment : Fragment() {
    private var viewModel = LoansViewModel()
    private var isNews: Int = 0
    val map = HashMap<String, String>()
    val handler = Handler()
    private var errorCode = ""
    private var loansAnim = false
    private var genAnim = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loans_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // animation ???????????????? ?? ???????????? ?????????????????? ?? ????????????
        if (!loansAnim) {
            TransitionAnimation(activity as AppCompatActivity).transitionRight(loans_layout)
            loansAnim = true
        }
        (activity as AppCompatActivity).supportActionBar?.show()
        iniArgument()
        initClick()
    }

    private fun initClick() {
        no_connection_repeat.setOnClickListener {
            initRestart()
        }

        access_restricted.setOnClickListener {
            initRestart()
        }

        not_found.setOnClickListener {
            initRestart()
        }

        technical_work.setOnClickListener {
            initRestart()
        }
    }

    fun setTitle(title: String?, color: Int) {
        val activity: Activity? = activity
        if (activity is MainActivity) {
            activity.setTitle(title, color)
        }
    }

    private fun iniArgument() {
        isNews = try {
            requireArguments().getInt("idNews")
        } catch (e: Exception) {
            0
        }

        val title = try {
            requireArguments().getString("title")
        } catch (e: Exception) {
            ""
        }
        setTitle(title.toString(), resources.getColor(R.color.whiteColor))
    }


    private fun initRestart() {
        ObservedInternet().observedInternet(requireContext())
        if (!AppPreferences.observedInternet) {
            loans_detail_no_connection.visibility = View.VISIBLE
            loans_detail_layout.visibility = View.GONE
            loans_detail_access_restricted.visibility = View.GONE
            loans_detail_not_found.visibility = View.GONE
            loans_detail_technical_work.visibility = View.GONE
            viewModel.errorGet.value = null
            errorCode = "601"
        } else {
            if (viewModel.listGetDta.value == null) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                shimmer_detail_loan.startShimmerAnimation()
                handler.postDelayed(Runnable { // Do something after 5s = 500ms
                    viewModel.getNews(map)
                    initRequest()
                }, 500)
            } else {
                handler.postDelayed(Runnable { // Do something after 5s = 500ms
//                    if (viewModel.errorGet.value != null) {
//                        viewModel.errorGet.value = null
//                        viewModel.listGetDta.postValue(null)
//                    }
                    viewModel.errorGet.value = null
                    viewModel.listGetDta.postValue(null)
                    viewModel.getNews(map)
                    initRequest()
                }, 500)
            }
        }
    }

    private fun initRequest() {
        viewModel.listGetDta.observe(viewLifecycleOwner, Observer { result ->
            try {
                if (result.result != null) {
                    loans_details_name.setText(result.result.name)
                    loans_details_description.setText(result.result.description)
                    loans_details_text.loadMarkdown(result.result.text)
                    Glide
                        .with(loans_details_image)
                        .load(result.result.thumbnail)
                        .into(loans_details_image)
                    errorCode = result.code.toString()
                    loans_detail_no_connection.visibility = View.GONE
                    loans_detail_access_restricted.visibility = View.GONE
                    loans_detail_not_found.visibility = View.GONE
                    loans_detail_technical_work.visibility = View.GONE
                    if (genAnim){
                        shimmer_detail_loan.visibility = View.GONE
                    }else{
                        shimmer_detail_loan.visibility = View.VISIBLE
                    }
                    loans_detail_layout.visibility = View.VISIBLE
                    if (!genAnim) {
                        //???????????????????? ???????????????? ????????????????
                        animationGenerator(shimmer_detail_loan, handler, requireActivity())
                        genAnim = true
                    }
                } else {
                    if (result.error.code != null) {
                        errorCode = result.error.code.toString()
                        if (result.error.code == 403) {
                            loans_detail_access_restricted.visibility = View.VISIBLE
                            loans_detail_not_found.visibility = View.GONE
                            loans_detail_no_connection.visibility = View.GONE
                            loans_detail_layout.visibility = View.GONE
                            loans_detail_technical_work.visibility = View.GONE
                        } else if (result.error.code == 404) {
                            loans_detail_not_found.visibility = View.VISIBLE
                            loans_detail_no_connection.visibility = View.GONE
                            loans_detail_layout.visibility = View.GONE
                            loans_detail_access_restricted.visibility = View.GONE
                            loans_detail_technical_work.visibility = View.GONE
                        } else if (result.error.code == 401) {
                            initAuthorized()
                        } else if (result.error.code == 500 || result.error.code == 400 || result.error.code == 409 || result.error.code == 429) {
                            loans_detail_technical_work.visibility = View.VISIBLE
                            loans_detail_no_connection.visibility = View.GONE
                            loans_detail_layout.visibility = View.GONE
                            loans_detail_access_restricted.visibility = View.GONE
                            loans_detail_not_found.visibility = View.GONE
                        }
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        if (!genAnim) {
                            //???????????????????? ???????????????? ????????????????
                            animationGenerator(shimmer_detail_loan, handler, requireActivity())
                            genAnim = true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        viewModel.errorGet.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                errorCode = error
                if (error == "403") {
                    loans_detail_access_restricted.visibility = View.VISIBLE
                    loans_detail_not_found.visibility = View.GONE
                    loans_detail_no_connection.visibility = View.GONE
                    loans_detail_layout.visibility = View.GONE
                    loans_detail_technical_work.visibility = View.GONE
                } else if (error == "404") {
                    loans_detail_not_found.visibility = View.VISIBLE
                    loans_detail_no_connection.visibility = View.GONE
                    loans_detail_layout.visibility = View.GONE
                    loans_detail_access_restricted.visibility = View.GONE
                    loans_detail_technical_work.visibility = View.GONE
                } else if (error == "401") {
                    initAuthorized()
                } else if (error == "500" || error == "400" || error == "409" || error == "429" || error == "601") {
                    loans_detail_technical_work.visibility = View.VISIBLE
                    loans_detail_no_connection.visibility = View.GONE
                    loans_detail_layout.visibility = View.GONE
                    loans_detail_access_restricted.visibility = View.GONE
                    loans_detail_not_found.visibility = View.GONE
                }else if (error == "600"){
                    loans_detail_no_connection.visibility = View.VISIBLE
                    loans_detail_technical_work.visibility = View.GONE
                    loans_detail_layout.visibility = View.GONE
                    loans_detail_access_restricted.visibility = View.GONE
                    loans_detail_not_found.visibility = View.GONE
                }
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                if (!genAnim) {
                    //???????????????????? ???????????????? ????????????????
                    animationGenerator(shimmer_detail_loan, handler, requireActivity())
                    genAnim = true
                }
            }
        })
    }

    private fun initAuthorized() {
        val intent = Intent(context, HomeActivity::class.java)
        AppPreferences.token = ""
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        map.put("login", AppPreferences.login.toString())
        map.put("token", AppPreferences.token.toString())
        map.put("id", isNews.toString())
        if (viewModel.listGetDta.value != null) {
            if (errorCode == "200") {
                initRequest()
            }else{
                initRestart()
            }
        } else {
            initRestart()
        }

        //???????????? ?????????? ?????????????????????????? ????????????
        ColorWindows(activity as AppCompatActivity).rollback()
    }
}