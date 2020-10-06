package com.example.kotlincashloan.ui.Loans


import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.kotlincashloan.R
import com.example.kotlincashloan.adapter.loans.LoansAdapter
import com.example.kotlincashloan.adapter.loans.LoansListener
import com.example.kotlincashloan.ui.registration.login.HomeActivity
import com.timelysoft.tsjdomcom.service.AppPreferences
import kotlinx.android.synthetic.main.fragment_loans.*
import kotlinx.android.synthetic.main.fragment_support.*
import kotlinx.android.synthetic.main.item_access_restricted.*
import kotlinx.android.synthetic.main.item_no_connection.*
import kotlinx.android.synthetic.main.item_not_found.*
import kotlinx.android.synthetic.main.item_technical_work.*

class LoansFragment : Fragment(), LoansListener {
    private var myAdapter = LoansAdapter(this)
    private var viewModel = LoansViewModel()
    val map = HashMap<String, String>()
    val handler = Handler()
    private var listNewsId: String = ""
    private var listLoanId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loans, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()
        map.put("login", AppPreferences.login.toString())
        map.put("token", AppPreferences.token.toString())
        map.put("v", "3")

        initLogicSeekBar()
        initRecycler()
        initClick()
        initRefresh()
        initResult()
    }

    fun initCode(){
        listLoanId = viewModel.listLoanId
        listNewsId = viewModel.listNewsId
    }

    private fun initResult() {
        HomeActivity.alert.show()
        viewModel.listLoanInfo.observe(viewLifecycleOwner, Observer { result ->
            if (result.error != null) {
                listLoanId = result.error.code.toString()
            }else{
                initCode()
                if (result.result != null) {
                    if (listLoanId == "200" && listNewsId == "200") {
                        if (result.result.getParallelLoan == false) {
                            loan_layout.visibility = View.GONE
                        } else {
                            loan_layout.visibility = View.VISIBLE
                        }
                        if (result.result.activeLoan!!.status == false) {
                            loan_status.visibility = View.GONE
                        } else {
                            loan_layout.visibility = View.VISIBLE
                        }
                        if (result.result.getActiveLoan == false) {
                            loan_get_active.visibility = View.GONE
                        } else {
                            loan_get_active.visibility = View.VISIBLE
                        }
                        if (result.result.getParallelLoan == false) {
                            loan_get_parallel.visibility = View.GONE
                        } else {
                            loan_get_parallel.visibility = View.VISIBLE
                        }

                        if (result.result.activeLoan!!.balance == null
                            || result.result.activeLoan!!.paid == null
                            || result.result.activeLoan!!.total == null
                            || result.result.activeLoan!!.paymentSum == null
                            || result.result.activeLoan!!.paymentDate == null
                        ) {

                            loans_sum.text = "0"
                            loan_paid.text = "0"
                            loan_total.text = "0"
                            loan_payment_sum.text = "0"
                            loan_payment_date.text = "0-0-0"
                        } else {
                            if (!loan_switch.isChecked) {
                                loans_sum.text = result.result.activeLoan!!.balance.toString()
                                loan_paid.text = result.result.activeLoan!!.paid.toString()
                                loan_total.text = result.result.activeLoan!!.total.toString()
                                loan_payment_sum.text = result.result.activeLoan!!.paymentSum.toString()
                                loan_payment_date.text = result.result.activeLoan!!.paymentDate
                            } else {
                                loans_sum.text = result.result.parallelLoan!!.balance.toString()
                                loan_paid.text = result.result.parallelLoan!!.paid.toString()
                                loan_total.text = result.result.parallelLoan!!.total.toString()
                                loan_payment_sum.text =
                                    result.result.parallelLoan!!.paymentSum.toString()
                                loan_payment_date.text = result.result.parallelLoan!!.paymentDate
                            }

                            loan_switch.setOnClickListener {
                                if (!loan_switch.isChecked) {
                                    loans_sum.text = result.result.activeLoan!!.balance.toString()
                                    loan_paid.text = result.result.activeLoan!!.paid.toString()
                                    loan_total.text = result.result.activeLoan!!.total.toString()
                                    loan_payment_sum.text =
                                        result.result.activeLoan!!.paymentSum.toString()
                                    loan_payment_date.text = result.result.activeLoan!!.paymentDate
                                } else {
                                    loans_sum.text = result.result.parallelLoan!!.balance.toString()
                                    loan_paid.text = result.result.parallelLoan!!.paid.toString()
                                    loan_total.text = result.result.parallelLoan!!.total.toString()
                                    loan_payment_sum.text =
                                        result.result.parallelLoan!!.paymentSum.toString()
                                    loan_payment_date.text = result.result.parallelLoan!!.paymentDate

                                    if (result.result.parallelLoan!!.balance == null
                                        || result.result.parallelLoan!!.paid == null
                                        || result.result.parallelLoan!!.total == null
                                        || result.result.parallelLoan!!.paymentSum == null
                                        || result.result.parallelLoan!!.paymentDate == null
                                    ) {

                                        loans_sum.text = "0"
                                        loan_paid.text = "0"
                                        loan_total.text = "0"
                                        loan_payment_sum.text = "0"
                                        loan_payment_date.text = "0-0-0"
                                    }
                                }
                            }
                        }
                        loans_layout.visibility = View.VISIBLE
                        loans_no_connection.visibility = View.GONE
                    }
                } else {
                    initErrorResult(result.error.code!!)
                }
            }
            HomeActivity.alert.hide()
        })

        viewModel.errorLoanInfo.observe(viewLifecycleOwner, Observer { error ->
            if (error != null){
                initError(error)
                listLoanId = error
            }
            HomeActivity.alert.hide()
        })
    }

    override fun onResume() {
        super.onResume()
        val handler = Handler()
        if (viewModel.listNewsDta.value == null) {
            handler.postDelayed(Runnable { // Do something after 5s = 500ms
                viewModel.listNews(map)
                initRecycler()
            }, 500)
        }

        if (viewModel.listLoanInfo.value == null) {
            handler.postDelayed(Runnable { // Do something after 5s = 500ms
                viewModel.getLoanInfo(map)
                initRecycler()
            }, 500)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().getWindow()
                .setStatusBarColor(requireActivity().getColor(R.color.whiteColor))
            requireActivity().getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar);
            toolbar.setBackgroundDrawable(ColorDrawable(requireActivity().getColor(R.color.whiteColor)))
            toolbar.setTitleTextColor(requireActivity().getColor(R.color.orangeColor))
        }
    }

    private fun initClick() {
        no_connection_repeat.setOnClickListener {
            initRestart()
            initResult()

        }

        access_restricted.setOnClickListener {
            initRestart()
            initResult()
        }

        not_found.setOnClickListener {
            initRestart()
            initResult()

        }

        technical_work.setOnClickListener {
            initRestart()
            initResult()
        }
    }

    private fun initRefresh() {
        loans_layout.setOnRefreshListener {
            handler.postDelayed(Runnable {
                initRestart()
                loans_layout.isRefreshing = false
            }, 1000)
        }
        loans_layout.setColorSchemeResources(android.R.color.holo_orange_dark)
    }

    private fun initRestart() {
        initRecycler()
        if (viewModel.listNewsDta.value != null && viewModel.listLoanInfo.value != null) {
            viewModel.listNews(map)
            viewModel.getLoanInfo(map)
        }else {
            viewModel.errorNews.value = null
            viewModel.listNews(map)
            viewModel.errorLoanInfo.value = null
            viewModel.getLoanInfo(map)
        }
    }

    private fun initRecycler() {
        HomeActivity.alert.show()
        viewModel.listNewsDta.observe(viewLifecycleOwner, Observer { result ->
            if (result.error != null) {
                listNewsId = result.error.toString()
            }else{
                initCode()
                if (result.result != null) {
                    if (listLoanId == "200" && listNewsId == "200") {
                        myAdapter.update(result.result)
                        loans_recycler.adapter = myAdapter
                        loans_layout.visibility = View.VISIBLE
                        loans_no_connection.visibility = View.GONE
                    }
                } else {
                    initErrorResult(result.error.code!!)
                }
            }
            HomeActivity.alert.hide()
        })

        viewModel.errorNews.observe(viewLifecycleOwner, Observer { error ->
            if (error != null){
                initError(error)
                listNewsId = error
            }
            HomeActivity.alert.hide()
        })
    }

    private fun initAuthorized() {
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun initLogicSeekBar() {
        loans_sum.setText("1000")
        loans_seekBar.max = 2000
        loans_seekBar.isEnabled = false
        loans_seekBar.progress = loans_sum.text.toString().toInt()
    }

    override fun loansClickListener(position: Int, idNews: Int) {
        val build = Bundle()
        build.putInt("idNews", idNews)
        findNavController().navigate(R.id.loans_details_navigation, build)
    }

    private fun initErrorResult(result: Int) {
        if (result == 403) {
            loans_no_connection.visibility = View.GONE
            loans_access_restricted.visibility = View.VISIBLE
            loans_layout.visibility = View.GONE
        } else if (result == 404) {
            loans_no_connection.visibility = View.GONE
            loans_not_found.visibility = View.VISIBLE
            loans_layout.visibility = View.GONE
        } else if (result == 401) {
            initAuthorized()
        } else if (result == 500 || result == 400) {
            loans_no_connection.visibility = View.GONE
            loans_technical_work.visibility = View.VISIBLE
            loans_layout.visibility = View.GONE
        }
    }

    private fun initError(error: String) {
        if (error == "600") {
            loans_no_connection.visibility = View.VISIBLE
            loans_layout.visibility = View.GONE
            loans_access_restricted.visibility = View.GONE
            loans_not_found.visibility = View.GONE
            loans_technical_work.visibility = View.GONE
        } else if (error == "403") {
            loans_no_connection.visibility = View.GONE
            loans_access_restricted.visibility = View.VISIBLE
            loans_layout.visibility = View.GONE
        } else if (error == "404") {
            loans_no_connection.visibility = View.GONE
            loans_not_found.visibility = View.VISIBLE
            loans_layout.visibility = View.GONE
        } else if (error == "401") {
            initAuthorized()
        } else if (error == "500" || error == "400") {
            loans_no_connection.visibility = View.GONE
            loans_technical_work.visibility = View.VISIBLE
            loans_layout.visibility = View.GONE
        }
    }
}