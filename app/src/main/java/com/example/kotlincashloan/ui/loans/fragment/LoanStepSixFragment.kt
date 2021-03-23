package com.example.kotlincashloan.ui.loans.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.kotlincashloan.R
import com.example.kotlincashloan.adapter.general.ListenerGeneralResult
import com.example.kotlincashloan.adapter.loans.StepClickListener
import com.example.kotlincashloan.common.GeneralDialogFragment
import com.example.kotlincashloan.extension.editUtils
import com.example.kotlincashloan.service.model.Loans.ListFamilyResultModel
import com.example.kotlincashloan.service.model.Loans.ListWorkResultModel
import com.example.kotlincashloan.service.model.Loans.SixNumResultModel
import com.example.kotlincashloan.service.model.general.GeneralDialogModel
import com.example.kotlincashloan.service.model.profile.CounterNumResultModel
import com.example.kotlincashloan.ui.loans.GetLoanActivity
import com.example.kotlincashloan.ui.loans.LoansViewModel
import com.example.kotlincashloan.ui.loans.fragment.dialogue.StepBottomFragment
import com.example.kotlincashloan.ui.profile.ProfileViewModel
import com.example.kotlincashloan.ui.registration.login.HomeActivity
import com.example.kotlincashloan.utils.ObservedInternet
import com.timelysoft.tsjdomcom.service.AppPreferences
import com.timelysoft.tsjdomcom.service.AppPreferences.toFullPhone
import com.timelysoft.tsjdomcom.service.Status
import kotlinx.android.synthetic.main.actyviti_questionnaire.*
import kotlinx.android.synthetic.main.fragment_loan_step_five.*
import kotlinx.android.synthetic.main.fragment_loan_step_four.*
import kotlinx.android.synthetic.main.fragment_loan_step_six.*
import kotlinx.android.synthetic.main.item_access_restricted.*
import kotlinx.android.synthetic.main.item_no_connection.*
import kotlinx.android.synthetic.main.item_not_found.*
import kotlinx.android.synthetic.main.item_technical_work.*

class LoanStepSixFragment : Fragment(), ListenerGeneralResult, StepClickListener {
    private var viewModel = LoansViewModel()

    private var listAvailableCountryDta = ""
    private var getListFamilyDta = ""

    private var family = ""
    private var familyPosition = ""

    private var itemDialog: ArrayList<GeneralDialogModel> = arrayListOf()
    private var listFamily: ArrayList<ListFamilyResultModel> = arrayListOf()

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
        initRestart()
        initClick()
        initView()
    }

    private fun initClick() {

        access_restricted.setOnClickListener {
            initRestart()
        }

        no_connection_repeat.setOnClickListener {
            initRestart()
        }

        bottom_loan_six.setOnClickListener {
            if (validate()) {
                initSaveLoan()
            }
        }

        technical_work.setOnClickListener {
            if (validate()) {
                initSaveLoan()
            }
        }

        not_found.setOnClickListener {
            if (validate()) {
                initSaveLoan()
            }
        }

        four_cross_six.setOnClickListener {
            (activity as GetLoanActivity?)!!.get_loan_view_pagers.setCurrentItem(4)
            hidingErrors()
        }

        six_loan_family.setOnClickListener {
            six_loan_family.isClickable = false
            initClearList()
            //Мутод заполняет список данными дя адапера
            if (itemDialog.size == 0) {
                for (i in 1..listFamily.size) {
                    if (i <= listFamily.size) {
                        itemDialog.add(GeneralDialogModel(listFamily[i-1].name.toString(), "listFamily", i-1, 0, listFamily[i-1].name.toString())
                        )
                    }
                }
            }
            if (itemDialog.size != 0) {
                initBottomSheet(itemDialog, familyPosition, "Кем приходится")
            }
        }
    }

    //очещает список
    private fun initClearList() {
        itemDialog.clear()
    }


    private fun initRestart(){
        ObservedInternet().observedInternet(requireContext())
        if (!AppPreferences.observedInternet) {
            six_ste_no_connection.visibility = View.VISIBLE
            layout_loan_six.visibility = View.GONE
            six_ste_technical_work.visibility = View.GONE
            six_ste_access_restricted.visibility = View.GONE
            six_ste_not_found.visibility = View.GONE
        }else{
            viewModel.errorSaveLoan.value = null
            initListFamily()
//            initAvailableCountry()
        }
    }

    override fun onStart() {
        super.onStart()
        initRestart()
    }

    // TODO: 21-2-12 Получает информацию из адаптера
    override fun listenerClickResult(model: GeneralDialogModel) {
        if (model.key == "listFamily") {
            six_loan_family.isClickable = true
            six_loan_family.setText(listFamily[model.position].name)
            familyPosition = listFamily[model.position].name.toString()
            family = listFamily[model.position].id.toString()
            six_loan_family.error = null
        }
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
                listFamily = result.result
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
        GetLoanActivity.alert.show()
        val mapSave = mutableMapOf<String, String>()
        mapSave["login"] = AppPreferences.login.toString()
        mapSave["token"] = AppPreferences.token.toString()
        mapSave["id"] = AppPreferences.idApplications.toString()
        mapSave["last_name"] = six_loan_surname.text.toString()
        mapSave["first_name"] = six_loan_name.text.toString()
        mapSave["second_name"] = six_loan_middle_name.text.toString()
        mapSave["type"] = family
        mapSave["phone"] = six_loan_phone.text.toString()
        mapSave["step"] = "4"

        viewModel.saveLoans(mapSave).observe(viewLifecycleOwner, Observer { result ->
            val data = result.data
            val msg = result.msg
            when (result.status) {
                Status.SUCCESS -> {
                    if (data!!.result != null) {
                        layout_loan_six.visibility = View.VISIBLE
                        six_ste_technical_work.visibility = View.GONE
                        six_ste_no_connection.visibility = View.GONE
                        six_ste_access_restricted.visibility = View.GONE
                        six_ste_not_found.visibility = View.GONE
                        (activity as GetLoanActivity?)!!.get_loan_view_pagers.currentItem = 6
                    }else if (data.error.code != null) {
                        listResult(data.error.code!!)
                    }else if (data.reject != null) {
                        initBottomSheet(data.reject.message.toString())
                    }
                }
                Status.ERROR -> {
                    errorList(msg!!)
                }
                Status.NETWORK -> {
                    errorList(msg!!)
                }
            }
            GetLoanActivity.alert.hide()
        })
    }

    //Вызов деалоговова окна с отоброжением получаемого списка.
    private fun initBottomSheet(list: ArrayList<GeneralDialogModel>, selectionPosition: String, title: String) {
        val stepBottomFragment = GeneralDialogFragment(this, list, selectionPosition, title)
        stepBottomFragment.show(requireActivity().supportFragmentManager, stepBottomFragment.tag)
    }

    private fun initBottomSheet(message: String) {
        val stepBottomFragment = StepBottomFragment(this, message)
        stepBottomFragment.isCancelable = false
        stepBottomFragment.show(requireActivity().supportFragmentManager, stepBottomFragment.tag)
    }

    override fun onClickStepListener() {
        requireActivity().finish()
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
            editUtils(six_loan_surname, six_loan_surname_error, "Заполните поле", true)
            valid = false
        }

        if (six_loan_name.text.isEmpty()) {
            editUtils(six_loan_name, six_loan_name_error, "Заполните поле", true)
            valid = false
        }

        if (six_loan_family.text.isEmpty()) {
            editUtils(six_loan_family, six_loan_error, "Выберите из списка", true)
            valid = false
        }

        if (six_loan_phone.text!!.isEmpty()) {
            editUtils(six_loan_phone, six_loan_phone_error, "Заполните поле", true)
            valid = false
        } else if (six_loan_phone.text.toString().toFullPhone().length != 20) {
            editUtils(six_loan_phone, six_loan_phone_error, "Ввидите правильный номер", true)
            valid = false
        }
        return valid
    }

    private fun initView(){
        six_loan_surname.addTextChangedListener {
            editUtils(six_loan_surname, six_loan_surname_error, "", false)
        }

        six_loan_name.addTextChangedListener {
            editUtils(six_loan_name, six_loan_name_error, "", false)
        }

        six_loan_family.addTextChangedListener {
            editUtils(six_loan_family, six_loan_error, "", false)
        }

        six_loan_phone.addTextChangedListener {
            editUtils(six_loan_phone, six_loan_phone_error, "", false)
        }
    }

    //проверяет если был откат назад отключает ошибки
    private fun hidingErrors(){
        editUtils(six_loan_surname, six_loan_surname_error, "", false)
        editUtils(six_loan_name, six_loan_name_error, "", false)
        editUtils(six_loan_family, six_loan_error, "", false)
        editUtils(six_loan_phone, six_loan_phone_error, "", false)
    }
}