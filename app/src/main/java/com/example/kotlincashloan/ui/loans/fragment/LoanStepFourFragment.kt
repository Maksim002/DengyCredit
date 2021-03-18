package com.example.kotlincashloan.ui.loans.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.kotlincashloan.R
import com.example.kotlincashloan.adapter.general.ListenerGeneralResult
import com.example.kotlincashloan.adapter.loans.StepClickListener
import com.example.kotlincashloan.common.GeneralDialogFragment
import com.example.kotlincashloan.extension.editUtils
import com.example.kotlincashloan.service.model.Loans.*
import com.example.kotlincashloan.service.model.general.GeneralDialogModel
import com.example.kotlincashloan.ui.loans.GetLoanActivity
import com.example.kotlincashloan.ui.loans.LoansViewModel
import com.example.kotlincashloan.ui.loans.fragment.dialogue.StepBottomFragment
import com.example.kotlincashloan.ui.registration.login.HomeActivity
import com.example.kotlincashloan.utils.ObservedInternet
import com.timelysoft.tsjdomcom.service.AppPreferences
import com.timelysoft.tsjdomcom.service.Status
import kotlinx.android.synthetic.main.actyviti_questionnaire.*
import kotlinx.android.synthetic.main.fragment_loan_step_five.*
import kotlinx.android.synthetic.main.fragment_loan_step_four.*
import kotlinx.android.synthetic.main.item_access_restricted.*
import kotlinx.android.synthetic.main.item_no_connection.*
import kotlinx.android.synthetic.main.item_not_found.*
import kotlinx.android.synthetic.main.item_technical_work.*

class LoanStepFourFragment : Fragment(), ListenerGeneralResult, StepClickListener {
    private var viewModel = LoansViewModel()

    private var getListCityDta = ""
    private var getListFamilyStatusDta = ""
    private var getListNumbersDta = ""
    private var getListYearsDta = ""
    private var listAvailableCountryDta = ""

    private var cityId = ""
    private var statusId = ""
    private var familyId = ""
    private var childrenId = ""
    private var liveId = ""
    private var cardId = ""
    private var reNum = ""
    private var phoneLength = ""

    private var cityPosition = ""
    private var familyPosition = ""
    private var numbersPosition = ""
    private var childrenPosition = ""
    private var yearsPosition = ""
    private var catsNamesPosition = ""
    private var sixPosition = ""

    private var itemDialog: ArrayList<GeneralDialogModel> = arrayListOf()
    private var listCity: ArrayList<ListCityResultModel> = arrayListOf()
    private var listFamilyStatus: ArrayList<ListFamilyStatusModel> = arrayListOf()
    private var listNumbers: ArrayList<ListNumbersResultModel> = arrayListOf()
    private var listNumbersChildren: ArrayList<ListNumbersResultModel> = arrayListOf()
    private var listYears: ArrayList<ListYearsResultModel> = arrayListOf()
    private var listAvailableSix: ArrayList<SixNumResultModel> = arrayListOf()
    //Наличие банковской карты
    private var listCatsNames = arrayOf("Нет", "Да")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loan_step_four, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClick()
        initView()
    }

    private fun initClick() {
        six_number_phone.addTextChangedListener {
            editUtils(layout_phone_number,six_number_phone, six_number_phone_error, "", false)
            initCleaningRoom()
        }

        access_restricted.setOnClickListener {
            initInternet()

        }

        no_connection_repeat.setOnClickListener {
            initInternet()
        }

        technical_work.setOnClickListener {
            initInternet()
        }

        not_found.setOnClickListener {
            initInternet()
        }

        bottom_loan_four.setOnClickListener {
            if (validate()){
                initSaveLoan()
            }
        }

        four_cross_back.setOnClickListener {
            (activity as GetLoanActivity?)!!.get_loan_view_pagers.setCurrentItem(1)
            hidingErrors()
        }

        six_available_country.setOnClickListener {
            initClearList()
            //Мутод заполняет список данными дя адапера
            if (itemDialog.size == 0) {
                for (i in 1..listAvailableSix.size) {
                    if (i <= listAvailableSix.size) {
                        itemDialog.add(
                            GeneralDialogModel(
                                listAvailableSix[i - 1].name.toString(), "listAvailableSix", i - 1, 0, listAvailableSix[i - 1].name.toString()))
                    }
                }
            }
            if (itemDialog.size != 0) {
                initBottomSheet(itemDialog, sixPosition, "Список доступных стран")
            }
        }

        loans_step_four_city.setOnClickListener {
            initClearList()
            //Мутод заполняет список данными дя адапера
            if (itemDialog.size == 0) {
                for (i in 1..listCity.size) {
                    if (i <= listCity.size) {
                        itemDialog.add(
                            GeneralDialogModel(listCity[i - 1].name.toString(), "listCity", i - 1,0, listCity[i - 1].name.toString()))
                    }
                }
            }
            if (itemDialog.size != 0) {
                initBottomSheet(itemDialog, cityPosition, "Выберите город")
            }
        }

        loans_step_four_status.setOnClickListener {
            initClearList()
            //Мутод заполняет список данными дя адапера
            if (itemDialog.size == 0) {
                for (i in 1..listFamilyStatus.size) {
                    if (i <= listFamilyStatus.size) {
                        itemDialog.add(
                            GeneralDialogModel(listFamilyStatus[i - 1].name.toString(), "listFamilyStatus", i - 1, 0, listFamilyStatus[i - 1].name.toString()))
                    }
                }
            }
            if (itemDialog.size != 0) {
                initBottomSheet(itemDialog, familyPosition, "Семейное положение")
            }
        }

        loans_step_four_family.setOnClickListener {
            initClearList()
            //Мутод заполняет список данными дя адапера
            if (itemDialog.size == 0) {
                for (i in 1..listNumbers.size) {
                    if (i <= listNumbers.size) {
                        itemDialog.add(
                            GeneralDialogModel(listNumbers[i - 1].name.toString(), "listNumbers", i - 1, 0, listNumbers[i - 1].name.toString()))
                    }
                }
            }
            if (itemDialog.size != 0) {
                initBottomSheet(itemDialog, numbersPosition, "Количество членов семьи")
            }
        }

        loans_step_four_children.setOnClickListener {
            initClearList()
            //Мутод заполняет список данными дя адапера
            if (itemDialog.size == 0) {
                for (i in 1..listNumbersChildren.size) {
                    if (i <= listNumbersChildren.size) {
                        itemDialog.add(
                            GeneralDialogModel(
                                listNumbersChildren[i - 1].name.toString(),
                                "listNumbersChildren",
                                i - 1,
                            0, listNumbersChildren[i - 1].name.toString()
                            )
                        )
                    }
                }
            }
            if (itemDialog.size != 0) {
                initBottomSheet(itemDialog, childrenPosition, "Количество детей")
            }
        }

        loans_step_four_federation.setOnClickListener {
            initClearList()
            //Мутод заполняет список данными дя адапера
            if (itemDialog.size == 0) {
                for (i in 1..listYears.size) {
                    if (i <= listYears.size) {
                        itemDialog.add(
                            GeneralDialogModel(
                                listYears[i - 1].name.toString(),
                                "listYears",
                                i - 1, 0,
                                listYears[i - 1].name.toString()
                            )
                        )
                    }
                }
            }
            if (itemDialog.size != 0) {
                initBottomSheet(itemDialog, yearsPosition, "Сколько лет проживаете в РФ")
            }
        }

        loans_step_four_card.setOnClickListener {
            initClearList()
            //Мутод заполняет список данными дя адапера
            if (itemDialog.size == 0) {
                for (i in 1..listCatsNames.size) {
                    if (i <= listCatsNames.size) {
                        itemDialog.add(
                            GeneralDialogModel(
                                listCatsNames[i - 1], "listCatsNames", i - 1, 0,  listCatsNames[i - 1]))
                    }
                }
            }
            if (itemDialog.size != 0) {
                initBottomSheet(itemDialog, catsNamesPosition, "Есть банковская карта")
            }
        }
    }

    //метод удаляет все символы из строки
    private fun initCleaningRoom() {
        if (six_number_phone.text.toString() != "") {
            val matchedResults = Regex(pattern = """\d+""").findAll(input = six_available_country.text.toString() + six_number_phone.text.toString())
            val result = StringBuilder()
            for (matchedText in matchedResults) {
                reNum = result.append(matchedText.value).toString()
            }
        } else {
            reNum = ""
        }
    }

    //очещает список
    private fun initClearList(){
        itemDialog.clear()
    }

    // TODO: 21-2-12 Получает информацию из адаптера
    override fun listenerClickResult(model: GeneralDialogModel) {
        if (model.key == "listAvailableSix") {
            six_number_phone.error = null
            //Очещает старую маску при выборе новой
            six_number_phone.mask = ""
            // Очещает поле
            six_number_phone.text = null
            sixPosition = listAvailableSix[model.position].name.toString()
            phoneLength = listAvailableSix[model.position].phoneLength.toString()
            six_available_country.setText("+" + listAvailableSix[model.position].phoneCode)
            six_number_phone.mask = listAvailableSix[model.position].phoneMaskSmall
        }

        if (model.key == "listCity") {
            loans_step_four_city.error = null
            loans_step_four_city.setText(listCity[model.position].name)
            cityPosition = listCity[model.position].name.toString()
            cityId = listCity[model.position].id!!
        }
        if (model.key == "listFamilyStatus") {
            loans_step_four_status.error = null
            loans_step_four_status.setText(listFamilyStatus[model.position].name)
            familyPosition = listFamilyStatus[model.position].name.toString()
            statusId = listFamilyStatus[model.position].id!!
        }

        if (model.key == "listNumbers") {
            loans_step_four_family.error = null
            loans_step_four_family.setText(listNumbers[model.position].name)
            numbersPosition = listNumbers[model.position].name.toString()
            familyId = listNumbers[model.position].id!!
        }

        if (model.key == "listNumbersChildren") {
            loans_step_four_children.error = null
            loans_step_four_children.setText(listNumbersChildren[model.position].name)
            childrenPosition = listNumbersChildren[model.position].name.toString()
            childrenId = listNumbersChildren[model.position].id!!
        }

        if (model.key == "listYears") {
            loans_step_four_federation.error = null
            loans_step_four_federation.setText(listYears[model.position].name)
            yearsPosition = listYears[model.position].name.toString()
            liveId = listYears[model.position].id!!
        }

        if (model.key == "listCatsNames") {
            loans_step_four_card.error = null
            loans_step_four_card.setText(listCatsNames[model.position])
            catsNamesPosition = listCatsNames[model.position]
            cardId = model.position.toString()
        }
    }

    private fun initInternet() {
        getValueNull()
        ObservedInternet().observedInternet(requireContext())
        if (!AppPreferences.observedInternet) {
            loans_ste_no_connection.visibility = View.VISIBLE
            loans_step_layout.visibility = View.GONE
            loans_ste_technical_work.visibility = View.GONE
            loans_ste_access_restricted.visibility = View.GONE
            loans_ste_not_found.visibility = View.GONE
        } else {
            viewModel.errorSaveLoan.value = null
            initListCity()
            initListFamilyStatus()
            initListNumbers()
            initListNumbersChildren()
            initListYears()
            initAvailableCountry()
        }
    }

    // TODO: 21-2-8 Список доступных стран
    private fun initAvailableCountry() {
        val mapCountry = mutableMapOf<String, String>()
        mapCountry["id"] = ""
        viewModel.listAvailableSix(mapCountry)

        viewModel.listAvailableSixDta.observe(viewLifecycleOwner, Observer { result ->
            if (result.result != null) {
                listAvailableCountryDta = result.code.toString()
                getResultOk()
                listAvailableSix = result.result
                six_number_phone.mask = ""
                six_number_phone.text = null
                sixPosition = result.result[0].name.toString()
                six_available_country.setText("+" + result.result[0].phoneCode)
                six_number_phone.mask = result.result[0].phoneMaskSmall
                phoneLength = result.result[0].phoneLength.toString()
            } else {
                listResult(result.error.code!!)
            }
        })

        viewModel.errorListAvailableSix.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                listAvailableCountryDta = error
                errorList(error)
            }
        })
    }

    // TODO: 21-2-12  выберите город
    private fun initListCity() {
        val mapCity = mutableMapOf<String, String>()
        mapCity["login"] = AppPreferences.login.toString()
        mapCity["token"] = AppPreferences.token.toString()
        mapCity["id"] = "0"
        viewModel.listCity(mapCity)

        viewModel.getListCityDta.observe(viewLifecycleOwner, Observer { result ->
            if (result.result != null) {
                getListCityDta = result.code.toString()
                getResultOk()
                listCity = result.result
            } else {
                getListCityDta = result.error.code.toString()
                listResult(result.error.code!!)
            }
        })

        viewModel.errorListCity.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                getListCityDta = error
                errorList(error)
            }
        })
    }

    // TODO: 21-2-12 Ваше семейное положение
    private fun initListFamilyStatus() {
        val mapStatus = mutableMapOf<String, String>()
        mapStatus["login"] = AppPreferences.login.toString()
        mapStatus["token"] = AppPreferences.token.toString()
        mapStatus["id"] = "0"
        viewModel.listFamilyStatus(mapStatus)

        viewModel.getListFamilyStatusDta.observe(viewLifecycleOwner, Observer { result ->
            if (result.result != null) {
                getListFamilyStatusDta = result.code.toString()
                getResultOk()
                listFamilyStatus = result.result
            } else {
                getListFamilyStatusDta = result.error.code.toString()
                listResult(result.error.code!!)
            }
        })

        viewModel.errorListFamilyStatus.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                getListFamilyStatusDta = error
                errorList(error)
            }
        })
    }

    // TODO: 21-2-12 Численность семьи
    private fun initListNumbers() {
        val mapNumbers = mutableMapOf<String, String>()
        mapNumbers["login"] = AppPreferences.login.toString()
        mapNumbers["token"] = AppPreferences.token.toString()
        mapNumbers["id"] = "0"
        viewModel.listNumbers(mapNumbers)

        viewModel.getListNumbersDta.observe(viewLifecycleOwner, Observer { result ->
            if (result.result != null) {
                getListNumbersDta = result.code.toString()
                getResultOk()
                listNumbers = result.result
            } else {
                getListNumbersDta = result.error.code.toString()
                listResult(result.error.code!!)
            }
        })

        viewModel.errorListNumbers.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                getListNumbersDta = error
                errorList(error)
            }
        })
    }

    // TODO: 21-2-12 Количество детей
    private fun initListNumbersChildren() {
        val mapNumbers = mutableMapOf<String, String>()
        mapNumbers["login"] = AppPreferences.login.toString()
        mapNumbers["token"] = AppPreferences.token.toString()
        mapNumbers["id"] = "0"
        viewModel.listNumbers(mapNumbers)

        viewModel.getListNumbersDta.observe(viewLifecycleOwner, Observer { result ->
            if (result.result != null) {
                getListNumbersDta = result.code.toString()
                getResultOk()
                listNumbersChildren = result.result

            } else {
                getListNumbersDta = result.error.code.toString()
                listResult(result.error.code!!)
            }
        })

        viewModel.errorListNumbers.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                getListNumbersDta = error
                errorList(error)
            }
        })
    }

    // TODO: 21-2-12 Сколько лет проживаете в РФ
    private fun initListYears() {
        val mapYears = mutableMapOf<String, String>()
        mapYears["login"] = AppPreferences.login.toString()
        mapYears["token"] = AppPreferences.token.toString()
        mapYears["id"] = "0"
        viewModel.listYears(mapYears)

        viewModel.getListYearsDta.observe(viewLifecycleOwner, Observer { result ->
            if (result.result != null) {
                getListYearsDta = result.code.toString()
                getResultOk()
                listYears = result.result
            } else {
                getListYearsDta = result.error.code.toString()
                listResult(result.error.code!!)
            }
        })

        viewModel.errorListYears.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                getListYearsDta = error
                errorList(error)
            }
        })
    }

    //Сохронение на сервер данных
    private fun initSaveLoan() {
        GetLoanActivity.alert.show()
        val mapSave = mutableMapOf<String, String>()
        mapSave.put("login", AppPreferences.login.toString())
        mapSave.put("token", AppPreferences.token.toString())
        mapSave.put("id", AppPreferences.idApplications.toString())
        mapSave.put("city", cityId)
        mapSave["second_phone"] = reNum
        mapSave.put("address", loans_step_four_residence.text.toString())
        mapSave.put("family_status", statusId)
        mapSave.put("count_family", familyId)
        mapSave.put("count_family_work", childrenId)
        mapSave.put("live_in_ru", liveId)
        mapSave.put("bank_card", cardId)
        mapSave.put("step", "2")

        viewModel.saveLoans(mapSave).observe(viewLifecycleOwner, Observer { result ->
            val data = result.data
            val msg = result.msg
            when (result.status) {
                Status.SUCCESS -> {
                    if (data!!.result != null) {
                        loans_step_layout.visibility = View.VISIBLE
                        loans_ste_technical_work.visibility = View.GONE
                        loans_ste_no_connection.visibility = View.GONE
                        loans_ste_access_restricted.visibility = View.GONE
                        loans_ste_not_found.visibility = View.GONE
                        (activity as GetLoanActivity?)!!.get_loan_view_pagers.setCurrentItem(4)
                    }else if (data.error.code != null) {
                        listResult(data.error.code!!)
                    }else if (data.reject != null) {
                        initBottomSheet(data.reject.message!!)
                        loans_step_layout.visibility = View.VISIBLE
                        loans_ste_technical_work.visibility = View.GONE
                        loans_ste_no_connection.visibility = View.GONE
                        loans_ste_not_found.visibility = View.GONE
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
    private fun initBottomSheet(
        list: ArrayList<GeneralDialogModel>, selectionPosition: String, title: String) {
        val stepBottomFragment = GeneralDialogFragment(this, list, selectionPosition, title)
        stepBottomFragment.show(requireActivity().supportFragmentManager, stepBottomFragment.tag)
    }

    private fun initBottomSheet(message: String) {
        val stepBottomFragment =
            StepBottomFragment(
                this, message
            )
        stepBottomFragment.isCancelable = false
        stepBottomFragment.show(requireActivity().supportFragmentManager, stepBottomFragment.tag)
    }

    private fun getResultOk() {
        if (getListCityDta == "200" && getListFamilyStatusDta == "200" && getListNumbersDta == "200" && getListYearsDta == "200") {
            loans_step_layout.visibility = View.VISIBLE
            loans_ste_technical_work.visibility = View.GONE
            loans_ste_no_connection.visibility = View.GONE
            loans_ste_access_restricted.visibility = View.GONE
            loans_ste_not_found.visibility = View.GONE
        }
    }

    private fun getValueNull() {
        getListCityDta = ""
        getListFamilyStatusDta = ""
        getListNumbersDta = ""
        getListYearsDta = ""
    }

    private fun listResult(result: Int) {
        if (result == 400 || result == 500 || result == 409 || result == 429) {
            loans_ste_technical_work.visibility = View.VISIBLE
            loans_ste_no_connection.visibility = View.GONE
            loans_step_layout.visibility = View.GONE
            loans_ste_access_restricted.visibility = View.GONE
            loans_ste_not_found.visibility = View.GONE
        } else if (result == 403) {
            loans_ste_access_restricted.visibility = View.VISIBLE
            loans_ste_technical_work.visibility = View.GONE
            loans_ste_no_connection.visibility = View.GONE
            loans_step_layout.visibility = View.GONE
            loans_ste_not_found.visibility = View.GONE
        } else if (result == 404) {
            loans_ste_not_found.visibility = View.VISIBLE
            loans_ste_access_restricted.visibility = View.GONE
            loans_ste_technical_work.visibility = View.GONE
            loans_ste_no_connection.visibility = View.GONE
            loans_step_layout.visibility = View.GONE
        } else if (result == 401) {
            initAuthorized()
        }
    }

    private fun errorList(error: String) {
        if (error == "400" || error == "500" || error == "600" || error == "429" || error == "409") {
            loans_ste_technical_work.visibility = View.VISIBLE
            loans_ste_no_connection.visibility = View.GONE
            loans_step_layout.visibility = View.GONE
            loans_ste_access_restricted.visibility = View.GONE
            loans_ste_not_found.visibility = View.GONE
        } else if (error == "403") {
            loans_ste_access_restricted.visibility = View.VISIBLE
            loans_ste_technical_work.visibility = View.GONE
            loans_ste_no_connection.visibility = View.GONE
            loans_step_layout.visibility = View.GONE
            loans_ste_not_found.visibility = View.GONE
        } else if (error == "404") {
            loans_ste_not_found.visibility = View.VISIBLE
            loans_ste_access_restricted.visibility = View.GONE
            loans_ste_technical_work.visibility = View.GONE
            loans_ste_no_connection.visibility = View.GONE
            loans_step_layout.visibility = View.GONE
        } else if (error == "601") {
            loans_ste_no_connection.visibility = View.VISIBLE
            loans_step_layout.visibility = View.GONE
            loans_ste_technical_work.visibility = View.GONE
            loans_ste_access_restricted.visibility = View.GONE
            loans_ste_not_found.visibility = View.GONE
        } else if (error == "401") {
            initAuthorized()
        }
    }

    private fun initAuthorized() {
        val intent = Intent(context, HomeActivity::class.java)
        AppPreferences.token = ""
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // TODO: 16.03.21 Когда заработает сканер удолить
        AppPreferences.idApplications = "3"
    }

    override fun onStart() {
        super.onStart()
        initInternet()
    }

    private fun validate(): Boolean {
        var valid = true
        if (loans_step_four_residence.text.isEmpty()) {
            editUtils(loans_step_four_residence, loans_step_four_error, "Заполните поле", true)
            valid = false
        }

        if (loans_step_four_city.text.isEmpty()) {
            editUtils(loans_step_four_city, step_four_city_error, "Выберите из списка", true)
            valid = false
        }

        if (loans_step_four_status.text.isEmpty()) {
            editUtils(loans_step_four_status, step_four_status_error, "Выберите из списка", true)
            valid = false
        }

        if (loans_step_four_family.text.isEmpty()) {
            editUtils(loans_step_four_family, step_four_family_error, "Выберите из списка", true)
            valid = false
        }

        if (loans_step_four_children.text.isEmpty()) {
            editUtils(loans_step_four_children, step_four_children_error, "Выберите из списка", true)
            valid = false
        }

        if (loans_step_four_federation.text.isEmpty()) {
            editUtils(loans_step_four_federation, step_four_federation_error, "Выберите из списка", true)
            valid = false
        }

        if (loans_step_four_card.text.isEmpty()) {
            editUtils(loans_step_four_card, step_four_card_error, "Выберите из списка", true)
            valid = false
        }

        if (six_number_phone.text!!.isNotEmpty()){
            if (phoneLength != reNum.length.toString()) {
                editUtils(layout_phone_number,six_number_phone, six_number_phone_error, "Видите правильный номер", true)
                valid = false
            }
        }

        return valid
    }

    private fun initView(){
        loans_step_four_residence.addTextChangedListener {
            editUtils(loans_step_four_residence, loans_step_four_error, "", false)
        }
        loans_step_four_city.addTextChangedListener {
            editUtils(loans_step_four_city, step_four_city_error, "", false)
        }
        loans_step_four_status.addTextChangedListener {
            editUtils(loans_step_four_status, step_four_status_error, "", false)
        }
        loans_step_four_family.addTextChangedListener {
            editUtils(loans_step_four_family, step_four_family_error, "", false)
        }
        loans_step_four_children.addTextChangedListener {
            editUtils(loans_step_four_children, step_four_children_error, "", false)
        }
        loans_step_four_federation.addTextChangedListener {
            editUtils(loans_step_four_federation, step_four_federation_error, "", false)
        }
        loans_step_four_card.addTextChangedListener {
            editUtils(loans_step_four_card, step_four_card_error, "", false)
        }
    }

    //проверяет если был откат назад отключает ошибки
    private fun hidingErrors(){
        editUtils(loans_step_four_residence, loans_step_four_error, "", false)
        editUtils(loans_step_four_city, step_four_city_error, "", false)
        editUtils(loans_step_four_status, step_four_status_error, "", false)
        editUtils(loans_step_four_family, step_four_family_error, "", false)
        editUtils(loans_step_four_children, step_four_children_error, "", false)
        editUtils(loans_step_four_federation, step_four_federation_error, "", false)
        editUtils(loans_step_four_card, step_four_card_error, "", false)
        editUtils(layout_phone_number,six_number_phone, six_number_phone_error, "", false)
    }

    override fun onClickStepListener() {
        requireActivity().finish()
    }
}