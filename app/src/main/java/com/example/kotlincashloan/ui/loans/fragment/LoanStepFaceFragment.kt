package com.example.kotlincashloan.ui.loans.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.kotlincashloan.R
import com.example.kotlincashloan.adapter.loans.StepClickListener
import com.example.kotlincashloan.extension.*
import com.example.kotlincashloan.ui.loans.GetLoanActivity
import com.example.kotlincashloan.ui.loans.LoansViewModel
import com.example.kotlincashloan.ui.loans.fragment.dialogue.StepBottomFragment
import com.example.kotlincashloan.ui.profile.ProfileViewModel
import com.example.kotlincashloan.utils.*
import com.regula.facesdk.Face.Instance
import com.regula.facesdk.enums.eInputFaceType
import com.regula.facesdk.results.LivenessResponse
import com.regula.facesdk.structs.Image
import com.regula.facesdk.structs.MatchFacesRequest
import com.timelysoft.tsjdomcom.service.AppPreferences
import com.timelysoft.tsjdomcom.service.Status
import kotlinx.android.synthetic.main.activity_get_loan.*
import kotlinx.android.synthetic.main.fragment_loan_step_face.*
import kotlinx.android.synthetic.main.item_access_restricted.*
import kotlinx.android.synthetic.main.item_no_connection.*
import kotlinx.android.synthetic.main.item_not_found.*
import kotlinx.android.synthetic.main.item_technical_work.*
import java.text.DecimalFormat

class LoanStepFaceFragment(var statusValue: Boolean, var applicationStatus: Boolean, var listener: LoanClearListener) : Fragment(),
    StepClickListener {
    private var viewModel = LoansViewModel()
    private var photoViewModel = ProfileViewModel()
    private lateinit var imageFace: Bitmap
    private lateinit var textViewLiveliness: String
    private var percent = 0.00

    private var handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loan_step_face, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Instance().serviceUrl = "https://faceapi.molbulak.com"

        if (!applicationStatus) {
            // ?????????????????????? ???????????? ???????? ???????????? true ?????????? ????????????????
            (activity as GetLoanActivity?)!!.loan_cross_clear.visibility = View.GONE
        } else {
            (activity as GetLoanActivity?)!!.loan_cross_clear.visibility = View.VISIBLE
        }

        initClick()
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        handler.postDelayed(Runnable { // Do something after 5s = 500ms
            if (menuVisible && isResumed) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                initRestart()
            }
        }, 500)
    }

    private fun initRestart() {
        ObservedInternet().observedInternet(requireContext())
        if (!AppPreferences.observedInternet) {
            (activity as GetLoanActivity?)!!.get_loan_no_connection.visibility = View.VISIBLE
            (activity as GetLoanActivity?)!!.layout_get_loan_con.visibility = View.GONE
            (activity as GetLoanActivity?)!!.get_loan_technical_work.visibility = View.GONE
            (activity as GetLoanActivity?)!!.get_loan_access_restricted.visibility = View.GONE
            (activity as GetLoanActivity?)!!.get_loan_not_found.visibility = View.GONE
        } else {
            initResult()
        }
    }

    // ?????????? ???????????????????????? ??????????
    private fun initClick() {
        if (statusValue == true) {
            if (applicationStatus == false) {
                (activity as GetLoanActivity?)!!.loan_cross_clear.visibility = View.GONE
            } else {
                // ?????????????????????? ???????????? ???????? ???????????? ?????????? ????????????????
                (activity as GetLoanActivity?)!!.loan_cross_clear.visibility = View.VISIBLE
            }
        }

        bottom_loan_face.setOnClickListener {
            AppPreferences.isRepeat = false
            ObservedInternet().observedInternet(requireContext())
            if (!AppPreferences.observedInternet) {
                (activity as GetLoanActivity?)!!.get_loan_no_connection.visibility = View.VISIBLE
                (activity as GetLoanActivity?)!!.layout_get_loan_con.visibility = View.GONE
                (activity as GetLoanActivity?)!!.get_loan_access_restricted.visibility = View.GONE
                (activity as GetLoanActivity?)!!.get_loan_technical_work.visibility = View.GONE
                (activity as GetLoanActivity?)!!.get_loan_not_found.visibility = View.GONE
            } else {
                requestFace()
                //?????????????????? ??????????????
                initSuspendTime()
                thee_incorrect_face.visibility = View.GONE
            }
        }

        (activity as GetLoanActivity?)!!.access_restricted.setOnClickListener {
            listener.loanClearClickListener()
        }

        (activity as GetLoanActivity?)!!.no_connection_repeat.setOnClickListener {
            listener.loanClearClickListener()
        }

        (activity as GetLoanActivity?)!!.technical_work.setOnClickListener {
            listener.loanClearClickListener()
        }

        (activity as GetLoanActivity?)!!.not_found.setOnClickListener {
            listener.loanClearClickListener()
        }

        (activity as GetLoanActivity?)!!.face_cross_back.setOnClickListener {
            AppPreferences.isRepeat = false
            shimmerStart((activity as GetLoanActivity?)!!.shimmer_step_loan, requireActivity())
            (activity as GetLoanActivity?)!!.get_loan_view_pagers.currentItem = 6
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onResume() {
        super.onResume()
        if (statusValue) {
            if (!applicationStatus) {
                face_cross_back.visibility = View.GONE
            }
        }
    }

    private fun initResult() {
        // ?????????????????? ???????????????????? ???? ??????????????
        val mapPhoto = HashMap<String, String>()
        mapPhoto.put("login", AppPreferences.login.toString())
        mapPhoto.put("token", AppPreferences.token.toString())
        mapPhoto.put("type", "doc")
        mapPhoto.put("doc_id", AppPreferences.applicationId.toString())
        mapPhoto.put("type_id", "passport_photo")

        photoViewModel.getImg(mapPhoto)
        photoViewModel.listGetImgDta.observe(viewLifecycleOwner, Observer { result ->
            val data = result.result
            val msg = result.error
            if (data != null) {
                baseToBitmap(data.data.toString())
                percent = data.match!!.toDouble()
                (activity as GetLoanActivity?)!!.get_loan_no_connection.visibility = View.GONE
                (activity as GetLoanActivity?)!!.get_loan_access_restricted.visibility = View.GONE
                (activity as GetLoanActivity?)!!.get_loan_technical_work.visibility = View.GONE
                (activity as GetLoanActivity?)!!.get_loan_not_found.visibility = View.GONE
                (activity as GetLoanActivity?)!!.layout_get_loan_con.visibility = View.VISIBLE
                if (!AppPreferences.isRepeat) {
                    //???????????????????? ???????????????? ????????????????
                    animationGeneratorLoan((activity as GetLoanActivity?)!!.shimmer_step_loan,handler,  requireActivity())
                    AppPreferences.isRepeat = true
                }
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            } else if (msg != null) {
                listListResult(
                    result.error.code!!.toInt(),
                    (activity as GetLoanActivity?)!!.get_loan_technical_work as LinearLayout,
                    (activity as GetLoanActivity?)!!.get_loan_no_connection
                            as LinearLayout,
                    (activity as GetLoanActivity?)!!.layout_get_loan_con,
                    (activity as GetLoanActivity?)!!.get_loan_access_restricted
                            as LinearLayout,
                    (activity as GetLoanActivity?)!!.get_loan_not_found as LinearLayout,
                    requireActivity(),
                    true
                )
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

        photoViewModel.errorGetImg.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                listListResult(
                    error,
                    (activity as GetLoanActivity?)!!.get_loan_technical_work as LinearLayout,
                    (activity as GetLoanActivity?)!!.get_loan_no_connection
                            as LinearLayout,
                    (activity as GetLoanActivity?)!!.layout_get_loan_con,
                    (activity as GetLoanActivity?)!!.get_loan_access_restricted
                            as LinearLayout,
                    (activity as GetLoanActivity?)!!.get_loan_not_found as LinearLayout,
                    requireActivity(),
                    true)
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }

    private fun requestFace() {
        //?????????? ?????????????????? ???????? ?????????????????? ???? ????????????????
        Instance().startLivenessMatching(requireContext(), 1) { livenessResponse: LivenessResponse? ->
            if (livenessResponse != null && livenessResponse.bitmap != null) {
                //???????? ???????????????????????? ???????????? ??????????????
                if (livenessResponse.liveness == 0) {
                    imageFace = livenessResponse.bitmap!!
                    imageConverter(livenessResponse.bitmap!!)
                    comparingPhotos()
                }
            } else {
                handler.postDelayed(Runnable { // Do something after 5s = 500ms
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }, 500)
            }
            Instance().stopLivenessProcessing(requireContext());
        }
    }

    // ?????????? ???????????????????? 2 ???????????????????? imageView1 && imageView2
    private fun comparingPhotos() {
        if (setBitmapIm.sameAs(setBitmapIm) && imageFace.sameAs(imageFace)) {
            matchFaces(setBitmapIm, imageFace)
        }
    }

    //?????????? ?????????????????? 2 ???????????????????? ???????????????????? ?????????????????? ???????????????? ???????? ??????????
    private fun matchFaces(first: Bitmap, second: Bitmap) {
        val matchRequest = MatchFacesRequest()
        val firstImage = Image()
        firstImage.setImage(first)
        firstImage.imageType = (eInputFaceType.ift_DocumentPrinted)
        matchRequest.images.add(firstImage)

        val secondImage = Image()
        secondImage.setImage(second)
        secondImage.imageType = (eInputFaceType.ift_Live)
        matchRequest.images.add(secondImage)

        Instance().matchFaces(matchRequest) { matchFacesResponse ->
            if (matchFacesResponse?.matchedFaces!!.size != 0) {
                val similarity = matchFacesResponse.matchedFaces[0].similarity
                if (similarity.toString() == "NaN") {
                    //???????? ???????????????????????? ?????????? ???????? && ??????????????????
                    textViewLiveliness = "0.0"
                } else {
                    //???????? ???????????????????????? ?????????? ???????? && ????????
                    textViewLiveliness = String.format("%.2f", similarity * 100)
                    calculatingPercentages(textViewLiveliness)
                }
            } else {
                // ???????? ?????????????????? ???????????????? 2 ?????????????????? ????????????????????
                val similarity = matchFacesResponse.unmatchedFaces[0].similarity
                if (similarity.toString() == "NaN") {
                    //???????? ???????????????????????? ?????????? ???????? && ??????????????????
                    textViewLiveliness = "????????????????: 0.0%"
                } else {
                    //???????? ???????????????????????? ?????????? ???????? && ????????
                    textViewLiveliness = String.format("%.2f", similarity * 100)
                    calculatingPercentages(textViewLiveliness)
                }
            }
        }
    }

    private fun calculatingPercentages(string: String) {
        val formattedDouble: String = DecimalFormat("#0.00").format(percent)
        if (string >= formattedDouble) {
            initSaveImage()
        } else {
            thee_incorrect_face.visibility = View.VISIBLE
        }
    }

    //???????????????????? ????????????????
    private fun initSaveImage() {
        val mapImage = mutableMapOf<String, String>()
        mapImage["login"] = AppPreferences.login.toString()
        mapImage["token"] = AppPreferences.token.toString()
        mapImage.put("id", AppPreferences.applicationId.toString())
        mapImage["live_photo_1"] = getBitmapIm
        mapImage.put("step", "0")

        viewModel.saveLoans(mapImage).observe(viewLifecycleOwner, Observer { result ->
            val msg = result.msg
            val data = result.data
            when (result.status) {
                Status.SUCCESS -> {
                    if (data!!.result != null) {
                        initSaveLoan()
                    } else if (data.error.code != null) {
                        if (data.error.code == 409) {
                            thee_incorrect_face.visibility = View.VISIBLE
                        } else {
                            listListResult(data.error.code!!.toInt(), activity as AppCompatActivity)
                        }
                    } else if (data.reject != null) {
                        initBottomSheet(data.reject.message!!)
                    }
                }
                Status.ERROR, Status.NETWORK -> {
                    if (msg != null) {
                        listListResult(msg, activity as AppCompatActivity)
                    }
                }
            }
        })
    }

    //???????????????????? ???????? ???????????????????? 99%
    private fun initSaveLoan() {
        val mapSaveLoan = HashMap<String, String>()
        mapSaveLoan.put("login", AppPreferences.login.toString())
        mapSaveLoan.put("token", AppPreferences.token.toString())
        mapSaveLoan.put("id", AppPreferences.applicationId.toString())
        mapSaveLoan.put("step", "7")

        viewModel.saveLoans(mapSaveLoan).observe(viewLifecycleOwner, Observer { result ->
            val msg = result.msg
            val data = result.data
            when (result.status) {
                Status.SUCCESS -> {
                    if (data!!.result != null) {
                        if (applicationStatus == false) {
                            if (statusValue == true) {
                                requireActivity().finish()
                            } else {
                                (activity as GetLoanActivity?)!!.get_loan_view_pagers.currentItem = 8
                                shimmerStart((activity as GetLoanActivity?)!!.shimmer_step_loan, requireActivity())
                            }
                        } else {
                            (activity as GetLoanActivity?)!!.get_loan_view_pagers.currentItem = 8
                            shimmerStart((activity as GetLoanActivity?)!!.shimmer_step_loan, requireActivity())
                        }
                    } else if (data.error.code != null) {
                        listListResult(data.error.code!!.toInt(), activity as AppCompatActivity)
                    } else if (data.reject != null) {
                        initBottomSheet(data.reject.message!!)
                    }
                }
                Status.ERROR, Status.NETWORK -> {
                    if (msg != null) {
                        listListResult(msg, activity as AppCompatActivity)
                    }
                }
            }
        })
    }

    // ???????? ?????????????????????? reject
    private fun initBottomSheet(message: String) {
        val stepBottomFragment = StepBottomFragment(this, message)
        stepBottomFragment.isCancelable = false
        stepBottomFragment.show(requireActivity().supportFragmentManager, stepBottomFragment.tag)
    }

    override fun onClickStepListener() {
        requireActivity().finish()
    }
}
