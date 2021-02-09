package com.example.kotlincashloan.ui.loans.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.kotlincashloan.R
import com.example.kotlincashloan.ui.loans.GetLoanActivity
import com.example.kotlincashloan.ui.loans.LoansViewModel
import com.example.kotlincashloan.ui.loans.fragment.dialogue.StepBottomFragment
import com.example.kotlincashloan.ui.profile.ProfileViewModel
import com.example.kotlincashloan.ui.registration.login.HomeActivity
import com.timelysoft.tsjdomcom.service.AppPreferences
import com.timelysoft.tsjdomcom.service.AppPreferences.toFullPhone
import kotlinx.android.synthetic.main.fragment_loan_step_six.*

class LoanStepSixFragment : Fragment() {
    private var viewModel = LoansViewModel()
    private var viewModelProfile = ProfileViewModel()

    private var listAvailableCountryDta = ""
    private var getListFamilyDta = ""

    private var family = ""
    private var codeMaskId = ""
    private var phoneLength = ""
    private var reNum = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loan_step_six, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        six_loan_phone.mask = "+7 (###)-###-##-##"

        initListFamily()
        initAvailableCountry()

        initClick()
    }

    private fun initClick() {
        bottom_loan_six.setOnClickListener {
            initSaveLoan()
        }
    }

    // TODO: 21-2-8 Список доступных стран
    private fun initAvailableCountry() {
        val mapCountry = mutableMapOf<String, String>()
        mapCountry["id"] = ""
        viewModelProfile.listAvailableCountry(mapCountry)

        viewModelProfile.listAvailableCountryDta.observe(viewLifecycleOwner, Observer { result ->
            if (result.result != null) {
                listAvailableCountryDta = result.code.toString()
                getResultOk()
                val adapterIdCountry = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, result.result)
                six_available_country.setAdapter(adapterIdCountry)

                six_available_country.keyListener = null
                six_available_country.setOnItemClickListener { adapterView, view, position, l ->
                    six_available_country.showDropDown()
                    six_number_phone.mask = ""
                    six_number_phone.text = null
                    six_number_phone.visibility = View.VISIBLE
                    phoneLength = result.result[position].phoneLength.toString()
                    six_number_phone.mask = result.result[position].phoneMask
                    codeMaskId = result.result[position].isoCode.toString()
                }
                six_available_country.setOnClickListener {
                    six_available_country.showDropDown()
                }
                six_available_country.onFocusChangeListener =
                    View.OnFocusChangeListener { view, hasFocus ->
                        try {
                            if (hasFocus) {
                                closeKeyboard()
                                six_available_country.showDropDown()
                            }
                        } catch (e: Exception) {
                        }
                    }
            } else {
                listResult(result.error.code!!)
            }
        })

        viewModelProfile.errorListAvailableCountry.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                listAvailableCountryDta = error
                errorList(error)
            }
        })
    }

    // TODO: 21-2-8 Список кем приходится
    private fun initListFamily() {
        val mapFamily = mutableMapOf<String, String>()
        mapFamily["login"] = AppPreferences.login.toString()
        mapFamily["token"] = AppPreferences.token.toString()
        mapFamily["id"] = ""
        viewModel.listFamily(mapFamily)

        viewModel.getListFamilyDta.observe(viewLifecycleOwner, Observer { result ->
            if (result.result != null) {
                getListFamilyDta = result.code.toString()
                getResultOk()

                val adapterIdFamily = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    result.result
                )
                six_loan_family.setAdapter(adapterIdFamily)

                six_loan_family.keyListener = null
                six_loan_family.setOnItemClickListener { adapterView, view, position, l ->
                    family = result.result[position].id!!
                    six_loan_family.error = null
                    six_loan_family.showDropDown()
                }
                six_loan_family.setOnClickListener {
                    six_loan_family.showDropDown()
                    closeKeyboard()
                }
                six_loan_family.onFocusChangeListener =
                    View.OnFocusChangeListener { view, hasFocus ->
                        try {
                            if (hasFocus) {
                                six_loan_family.showDropDown()
                            }
                        } catch (e: Exception) {
                        }
                    }
            } else {
                listResult(result.error.code!!)
            }
        })


        viewModel.errorListFamily.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                getListFamilyDta = error
                errorList(error)
            }
        })
    }

    // TODO: 21-2-8 Сохронение доверительные контакты.
    private fun initSaveLoan() {
        initCleaningRoom()
        val mapSave= mutableMapOf<String, String>()
        mapSave["login"] = AppPreferences.login.toString()
        mapSave["token"] = AppPreferences.token.toString()
        mapSave["id"] = AppPreferences.sum.toString()
        mapSave["second_phone"] = reNum
        mapSave["last_name"] = six_loan_surname.text.toString()
        mapSave["first_name"] = six_loan_name.text.toString()
        mapSave["second_name"] = six_loan_middle_name.text.toString()
        mapSave["type"] = family
        mapSave["phone"] = six_loan_phone.text.toString()
        mapSave["step"] = "4"
        if (validate()){
            viewModel.saveLoan(mapSave)
        }

        viewModel.getSaveLoan.observe(viewLifecycleOwner, Observer { result->
            if (result.reject != null){
                layout_loan_six.visibility = View.VISIBLE
                six_ste_technical_work.visibility = View.GONE
                six_ste_no_connection.visibility = View.GONE
                six_ste_access_restricted.visibility = View.GONE
                six_ste_not_found.visibility = View.GONE
                (activity as GetLoanActivity?)!!.get_loan_view_pagers.setCurrentItem(6)
            }else if (result.reject != null){
                initBottomSheet(result.reject!!.message.toString())
            }else if (result.error != null){
                listResult(result.error.code!!)
            }
        })

        viewModel.errorSaveLoan.observe(viewLifecycleOwner, Observer { error->
            if (error != null){
                errorList(error)
            }
        })
    }

    private fun initBottomSheet(message: String) {
        val stepBottomFragment =
            StepBottomFragment(
                message
            )
        stepBottomFragment.isCancelable = false
        stepBottomFragment.show(requireActivity().supportFragmentManager, stepBottomFragment.tag)
    }

    //метод удаляет все символы из строки
    private fun initCleaningRoom(){
        if (six_number_phone.text.toString() != "") {
            val matchedResults = Regex(pattern = """\d+""").findAll(input = codeMaskId + six_number_phone.text.toString())
            val result = StringBuilder()
            for (matchedText in matchedResults) {
                reNum = result.append(matchedText.value).toString()
            }
        }else{
            reNum = ""
        }
    }

    private fun getResultOk() {
        if (listAvailableCountryDta == "200" && getListFamilyDta == "200") {
            layout_loan_six.visibility = View.VISIBLE
            six_ste_technical_work.visibility = View.GONE
            six_ste_no_connection.visibility = View.GONE
            six_ste_access_restricted.visibility = View.GONE
            six_ste_not_found.visibility = View.GONE
        }
    }

    private fun listResult(result: Int) {
        if (result == 400 || result == 500 || result == 409 || result == 429) {
            six_ste_technical_work.visibility = View.VISIBLE
            layout_loan_six.visibility = View.GONE
            six_ste_no_connection.visibility = View.GONE
            six_ste_access_restricted.visibility = View.GONE
            six_ste_not_found.visibility = View.GONE
        } else if (result == 403) {
            six_ste_access_restricted.visibility = View.VISIBLE
            layout_loan_six.visibility = View.GONE
            six_ste_technical_work.visibility = View.GONE
            six_ste_no_connection.visibility = View.GONE
            six_ste_not_found.visibility = View.GONE
        } else if (result == 404) {
            six_ste_not_found.visibility = View.VISIBLE
            layout_loan_six.visibility = View.GONE
            six_ste_technical_work.visibility = View.GONE
            six_ste_no_connection.visibility = View.GONE
            six_ste_access_restricted.visibility = View.GONE
        } else if (result == 401) {
            initAuthorized()
        }
    }

    private fun errorList(error: String) {
        if (error == "400" || error == "500" || error == "600" || error == "429" || error == "409") {
            six_ste_technical_work.visibility = View.VISIBLE
            layout_loan_six.visibility = View.GONE
            six_ste_no_connection.visibility = View.GONE
            six_ste_access_restricted.visibility = View.GONE
            six_ste_not_found.visibility = View.GONE
        } else if (error == "403") {
            six_ste_access_restricted.visibility = View.VISIBLE
            layout_loan_six.visibility = View.GONE
            six_ste_technical_work.visibility = View.GONE
            six_ste_no_connection.visibility = View.GONE
            six_ste_not_found.visibility = View.GONE
        } else if (error == "404") {
            six_ste_not_found.visibility = View.VISIBLE
            layout_loan_six.visibility = View.GONE
            six_ste_technical_work.visibility = View.GONE
            six_ste_no_connection.visibility = View.GONE
            six_ste_access_restricted.visibility = View.GONE
        } else if (error == "601") {
            six_ste_no_connection.visibility = View.VISIBLE
            six_ste_not_found.visibility = View.GONE
            layout_loan_six.visibility = View.GONE
            six_ste_technical_work.visibility = View.GONE
            six_ste_access_restricted.visibility = View.GONE
        } else if (error == "401") {
            initAuthorized()
        }
    }

    private fun initAuthorized() {
        val intent = Intent(context, HomeActivity::class.java)
        AppPreferences.token = ""
        startActivity(intent)
    }

    //Метотд для скрытия клавиатуры
    private fun closeKeyboard() {
        val view: View = requireActivity().currentFocus!!
        if (view != null) {
            // now assign the system
            // service to InputMethodManager
            val manager = requireActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager?
            manager!!.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun validate(): Boolean {
        var valid = true
        if (six_loan_surname.text.isEmpty()) {
            six_loan_surname.error = "Поле не должно быть пустым"
            valid = false
        }else {
            six_loan_surname.error = null
        }

        if (six_loan_name.text.isEmpty()){
            six_loan_name.error = "Поле не должно быть пустым"
            valid = false
        }else {
            six_loan_name.error = null
        }

        if (six_loan_name.text.isEmpty()){
            six_loan_name.error = "Поле не должно быть пустым"
            valid = false
        }else {
            six_loan_name.error = null
        }

        if (six_loan_family.text.isEmpty()){
            six_loan_family.error = "Поле не должно быть пустым"
            valid = false
        }else {
            six_loan_family.error = null
        }

        if (six_loan_phone.text!!.isEmpty()){
            six_loan_phone.error = "Поле не должно быть пустым"
            valid = false
        }else if (six_loan_phone.text.toString().toFullPhone().length != 20){
            six_loan_phone.error = "Введите валидный номер"
            valid = false
        }else{
            six_loan_phone.error = null
        }

        if (phoneLength == "11"){
            if (six_number_phone.text.toString().toFullPhone().length != 20){
                six_number_phone.error = "Введите валидный номер"
                valid = false
            }else{
                six_number_phone.error = null
            }
        }else{
            if (six_number_phone.text.toString().toFullPhone().length != 21){
                six_number_phone.error = "Введите валидный номер"
                valid = false
            }else{
                six_number_phone.error = null
            }
        }



        return valid
    }
}