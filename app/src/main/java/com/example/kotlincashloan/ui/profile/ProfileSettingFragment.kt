package com.example.kotlincashloan.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.text.method.PasswordTransformationMethod
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cookiebar.CookieBar
import com.example.kotlincashloan.R
import com.example.kotlincashloan.service.model.profile.ClientInfoResultModel
import com.example.kotlincashloan.service.model.profile.CounterNumResultModel
import com.example.kotlincashloan.ui.registration.login.HomeActivity
import com.example.kotlincashloan.ui.registration.recovery.ContactingServiceActivity
import com.example.kotlincashloan.utils.ColorWindows
import com.example.kotlincashloan.utils.ObservedInternet
import com.example.kotlincashloan.utils.TransitionAnimation
import com.example.kotlinscreenscanner.ui.MainActivity
import com.timelysoft.tsjdomcom.service.AppPreferences
import com.timelysoft.tsjdomcom.service.AppPreferences.toFullPhone
import com.timelysoft.tsjdomcom.utils.MyUtils
import kotlinx.android.synthetic.main.activity_number.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile_setting.*
import kotlinx.android.synthetic.main.item_access_restricted.*
import kotlinx.android.synthetic.main.item_no_connection.*
import kotlinx.android.synthetic.main.item_not_found.*
import kotlinx.android.synthetic.main.item_technical_work.*
import java.text.SimpleDateFormat
import java.util.*

class ProfileSettingFragment : Fragment() {
    private var viewModel = ProfileViewModel()
    private var errorCodeGender = ""
    private var errorCodeNationality = ""
    private var errorListAvailableCountry = ""
    private var errorListSecretQuestion = ""
    private var errorSaveProfile = ""
    private var errorClientInfo = ""
    private var errorCheckPassword = ""
    val handler = Handler()
    var clientResult = ClientInfoResultModel()
    private lateinit var simpleDateFormat: SimpleDateFormat
    private var list: ArrayList<CounterNumResultModel> = arrayListOf()
    private var codeNationality = 0
    private var numberAvailable = 0
    private var checkNumber = 0
    private var codeMack = ""
    private var reView = false
    private var reNum = ""
    private var question = ""
    private var profileSettingAnim = false
    private var profileSettingAnimR = false

    private var textPasswordOne = ""
    private var textPasswordTwo = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {}
        //форма даты
        simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        setTitle("Профиль", resources.getColor(R.color.whiteColor))
        initClick()
    }

    private fun initRestart() {
        val mapNationality = HashMap<String, String>()
        mapNationality.put("login", clientResult.gender.toString())

        val mapGender = HashMap<String, String>()
        mapGender.put("login", clientResult.nationality.toString())

        val mapRegistration = HashMap<String, String>()
        mapRegistration.put("id", "")

        val mapInfo = HashMap<String, String>()
        mapInfo.put("login", AppPreferences.login.toString())
        mapInfo.put("token", AppPreferences.token.toString())

        //проверка на интернет
        ObservedInternet().observedInternet(requireContext())
        if (!AppPreferences.observedInternet) {
            profile_s_no_connection.visibility = View.VISIBLE
            profile_s_technical_work.visibility = View.GONE
            profile_s_access_restricted.visibility = View.GONE
            profile_s_not_found.visibility = View.GONE
            profile_s_swipe.visibility = View.GONE
            viewModel.errorClientInfo.value = null
            viewModel.errorListGender.value = null
            viewModel.errorListNationality.value = null
            viewModel.errorListAvailableCountry.value = null
            viewModel.errorListSecretQuestion.value = null
            viewModel.errorSaveProfile.value = null
            errorClientInfo = "601"
            errorCodeGender = "601"
            errorCodeNationality = "601"
            errorListAvailableCountry = "601"
            errorListSecretQuestion = "601"
        } else {
            if (viewModel.errorListGender.value == null && viewModel.errorListNationality.value == null && viewModel.errorListAvailableCountry.value == null &&
                viewModel.errorListSecretQuestion.value == null && viewModel.errorSaveProfile.value == null && viewModel.errorClientInfo.value == null
            ) {
                if (!viewModel.refreshCode) {
                    HomeActivity.alert.show()
                }
                handler.postDelayed(Runnable { // Do something after 5s = 500ms
                    viewModel.refreshCode = false
                    viewModel.clientInfo(mapInfo)
                    viewModel.listGender(mapGender)
                    viewModel.getListNationality(mapNationality)
                    viewModel.listAvailableCountry(mapRegistration)
                    viewModel.listSecretQuestion(mapRegistration)
                    initResult()
                }, 500)
            } else {
                handler.postDelayed(Runnable { // Do something after 5s = 500ms
                    if (viewModel.errorListGender.value != null) {
                        viewModel.errorListGender.value = null
                        viewModel.listGenderDta.postValue(null)
                        viewModel.listGender(mapGender)
                    } else if (viewModel.errorListNationality.value != null) {
                        viewModel.errorListNationality.value = null
                        viewModel.listNationalityDta.postValue(null)
                        viewModel.getListNationality(mapNationality)
                    } else if (viewModel.errorListAvailableCountry.value != null) {
                        viewModel.errorListAvailableCountry.value = null
                        viewModel.listAvailableCountryDta.postValue(null)
                        viewModel.listAvailableCountry(mapRegistration)
                    } else if (viewModel.errorListSecretQuestion.value != null) {
                        viewModel.errorListSecretQuestion.value = null
                        viewModel.listSecretQuestionDta.postValue(null)
                        viewModel.listSecretQuestion(mapRegistration)
                    } else if (viewModel.errorClientInfo.value != null) {
                        viewModel.listClientInfoDta.postValue(null)
                        viewModel.errorClientInfo.value = null
                        viewModel.clientInfo(mapInfo)
                    } else {
                        viewModel.clientInfo(mapInfo)
                        viewModel.listGender(mapGender)
                        viewModel.getListNationality(mapNationality)
                        viewModel.listAvailableCountry(mapRegistration)
                        viewModel.listSecretQuestion(mapRegistration)
                        initResult()
                    }
                    initResult()
                }, 500)
            }
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    //получение полов
    private fun gettingFloors() {
        viewModel.listGenderDta.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { result ->
                if (result.result != null) {
                    profile_setting_gender.setText(result.result[clientResult.gender!!.toInt() - 1].name)
                    errorCodeGender = result.code.toString()
                    resultSuccessfully()
                } else {
                    listListResult(result.error.code!!)
                }
            })

        viewModel.errorListGender.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { error ->
                if (error != null) {
                    errorCodeGender = error
                    errorList(error)
                }
            })
    }

    //получение гражданства
    private fun obtainingCitizenship() {
        viewModel.listNationalityDta.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { result ->
                try {
                    if (result.result != null) {
                        profile_s_nationality.setText(result.result[clientResult.nationality!!.toInt() - 1].name)
                        errorCodeNationality = result.code.toString()
                        resultSuccessfully()
                    } else {
                        listListResult(result.error.code!!)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })

        viewModel.errorListNationality.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { error ->
                try {
                    if (error != null) {
                        errorCodeNationality = error
                        errorList(error)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
    }

    //Список доступных стран
    private fun listCountries() {
        viewModel.listAvailableCountryDta.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { result ->
                try {
                    // первый номер
                    if (result.result != null) {
                        if (clientResult.phoneFirst != "") {
                            profile_setting_first.mask = null

                            val firstNationality = clientResult.phoneFirst!!.toInt() - 1
                            numberAvailable = result.result[checkNumber].phoneLength!!.toInt()
                            profile_setting_first.mask = result.result[firstNationality].phoneMask
                            profile_setting_first.setText(
                                MyUtils.toMask(
                                    clientResult.firstPhone.toString(),
                                    result.result[firstNationality].phoneCode!!.length,
                                    result.result[firstNationality].phoneLength!!.toInt()
                                )
                            )

                            list = result.result

                        }
                        // второй номер
                        if (clientResult.secondPhone != "") {
                            profile_setting_second_phone.mask = null

                            val secondNationality = clientResult.phoneSecond!!.toInt() - 1

                            codeNationality = secondNationality

                            checkNumber = secondNationality

                            codeMack = result.result[secondNationality].phoneCode.toString()

                            profile_setting_second_phone.mask =
                                result.result[secondNationality].phoneMaskSmall
                            profile_setting_second_phone.setText(
                                MyUtils.toMask(
                                    clientResult.secondPhone.toString(),
                                    result.result[secondNationality].phoneCode!!.length,
                                    result.result[secondNationality].phoneLength!!.toInt()
                                )
                            )
                            profile_s_mask.setText("+" + result.result[secondNationality].phoneCode)
                        } else {
                            profile_setting_second_phone.text = null
                            codeMack = result.result[codeNationality].phoneCode.toString()
                            profile_s_mask.setText("+" + list[codeNationality].phoneCode)
                        }

                        val adapterListCountry = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            result.result
                        )

                        profile_s_mask.setAdapter(adapterListCountry)

                        profile_s_mask.keyListener = null
                        profile_s_mask.setOnItemClickListener { adapterView, view, position, l ->
                            codeNationality = position
                            codeMack = result.result[position].phoneCode.toString()
                            numberAvailable = result.result[position].phoneLength!!.toInt()
                            profile_setting_second_phone.setText("")
                            profile_s_mask.showDropDown()
                            profile_s_mask.clearFocus()
                        }
                        profile_s_mask.setOnClickListener {
                            profile_s_mask.showDropDown()
                        }
                        profile_s_mask.onFocusChangeListener =
                            View.OnFocusChangeListener { view, hasFocus ->
                                try {
                                    if (hasFocus) {
                                        closeKeyboard()
                                        profile_s_mask.showDropDown()
                                    }
                                } catch (e: Exception) {
                                }
                            }
                        errorListAvailableCountry = result.code.toString()
                        resultSuccessfully()
                    } else {
                        listListResult(result.error.code!!)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })

        viewModel.errorListAvailableCountry.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { error ->
                if (error != null) {
                    errorListAvailableCountry = error
                    errorList(error)
                }
            })
    }

    //Список секретных вопросов
    private fun listQuestions() {
        viewModel.listSecretQuestionDta.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { result ->
                try {
                    if (result.result != null) {
                        profile_s_question.setText(result.result[clientResult.question!!.toInt() - 1].name)
                        var numberPosition = 0
                        if (question == "") {
                            numberPosition = clientResult.question!!.toInt()
                            question = numberPosition.toString()
                        }
                        val adapterListCountry = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            result.result
                        )
                        profile_s_question.setAdapter(adapterListCountry)

                        profile_s_question.keyListener = null
                        profile_s_question.setOnItemClickListener { adapterView, view, position, l ->

                            question = result.result[position].id.toString()
                            profile_s_question.showDropDown()
                            profile_s_question.clearFocus()
                        }
                        click_s_question.setOnClickListener {
                            closeKeyboard()
                            profile_s_question.showDropDown()
                        }
                        profile_s_question.onFocusChangeListener =
                            View.OnFocusChangeListener { view, hasFocus ->
                                try {
                                    if (hasFocus) {
                                        closeKeyboard()
                                        profile_s_question.showDropDown()
                                    }
                                } catch (e: Exception) {
                                }
                            }
                        errorListSecretQuestion = result.code.toString()
                        resultSuccessfully()
                    } else {
                        listListResult(result.error.code!!)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                profile_s_swipe.isRefreshing = false
            })

        viewModel.errorListSecretQuestion.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { error ->
                if (error != null) {
                    errorListSecretQuestion = error
                    errorList(error)
                }
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                profile_s_swipe.isRefreshing = false
            })
    }

    private fun checkPassword() {
        //проверка старого пороля
        viewModel.listCheckPasswordDta.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { result ->
                try {
                    if (result.result != null) {
                        initPassword()
                    } else {
                        listListResult(result.error.code!!)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })

        viewModel.errorCheckPassword.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { error ->
                if (error != null) {
                    errorCheckPassword = error
                    errorList(error)
                }
            })
    }

    // данные для сохролнения
    private fun initPassword() {
        reView = true
        if (textPasswordOne == textPasswordTwo) {
            if (textPasswordOne != "") {
                AppPreferences.password = textPasswordOne
            }
        }
        val mapProfile = HashMap<String, String>()
        mapProfile.put("login", AppPreferences.login.toString())
        mapProfile.put("token", AppPreferences.token.toString())
        mapProfile.put("password", AppPreferences.password.toString())
        mapProfile.put("second_phone", reNum)
        mapProfile.put("question", question)
        mapProfile.put("response", profile_s_response.text.toString())
        if (isValid()) {
            viewModel.saveProfile(mapProfile)
        }
    }

    private fun initResult() {
        //если все успешно получает информацию о пользователе
        viewModel.listClientInfoDta.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { result ->
                try {
                    if (result.result != null) {
                        clientResult = result.result
                        profile_setting_fio.setText(clientResult.firstName + " " + clientResult.lastName)
                        profile_setting_second_name.setText(clientResult.secondName)
                        profile_setting_first_name.setText(clientResult.firstName)
                        profile_setting_last_name.setText(clientResult.lastName)
                        profile_setting_data.setText(MyUtils.toMyDate(clientResult.uDate.toString()))
                        profile_s_response.setText(clientResult.response)
                        errorClientInfo = result.code.toString()
                        resultSuccessfully()
                        if (!profileSettingAnim) {
                            //profileAnim анимация для перехода с адного дествия в другое
                            TransitionAnimation(activity as AppCompatActivity).transitionRight(
                                profile_setting_anim
                            )
                            profileSettingAnim = true
                        }

                        //получение полов
                        gettingFloors()

                        //получение гражданства
                        obtainingCitizenship()

                        //Список доступных стран
                        listCountries()

                        //Список секретных вопросов
                        listQuestions()

                    } else {
                        listListResult(result.error.code!!)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                profile_s_swipe.isRefreshing = false
            })

        //listClientInfoDta Проверка на ошибки
        viewModel.errorClientInfo.observe(viewLifecycleOwner, androidx.lifecycle.Observer { error ->
            try {
                errorClientInfo = error
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (error != null) {
                errorList(error)
            }
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            profile_s_swipe.isRefreshing = false
        })

        //результат о сохронение данных
        viewModel.listSaveProfileDta.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { result ->
                try {
                    if (result.result != null) {
                        if (reView) {
                            CookieBar.build(requireActivity())
                                .setTitle("Успешно сохранено")
                                .setTitleColor(R.color.blackColor)
                                .setDuration(5000)
                                .setCookiePosition(Gravity.TOP)
                                .show()
                            val bundle = Bundle()
                            bundle.putBoolean("false", true)
                            findNavController().navigate(R.id.profile_navigation, bundle)
                        }
                        reView = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })

        viewModel.errorSaveProfile.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { error ->
                if (error != null) {
                    errorSaveProfile = error
                    errorList(error)
                }
            })
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

    private fun initClick() {

        profile_s_swipe.setOnRefreshListener {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            handler.postDelayed(Runnable { // Do something after 5s = 500ms
                viewModel.refreshCode = true
                profile_s_one_password.text = null
                profile_s_two_password.text = null
                initRestart()
            }, 500)
            profile_s_two_password.error = null
            profile_s_two_password.error = null
            profile_s_one_password.error = null
            profile_s_response.error = null
        }
        profile_s_swipe.setColorSchemeResources(android.R.color.holo_orange_dark)


        profile_s_one_password.addTextChangedListener {
            textPasswordOne = it.toString()
        }

        profile_s_one_password.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (profile_s_one_password.text.isNotEmpty()) {
                    profile_s_one_password.setSelection(profile_s_one_password.text!!.length);
                }
            }
        }
        profile_s_two_password.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (profile_s_two_password.text.isNotEmpty()) {
                    profile_s_two_password.setSelection(profile_s_two_password.text!!.length);
                }
            }
        }

        //Дополнительный номер
        profile_setting_second_phone.viewTreeObserver
            .addOnGlobalLayoutListener {
                try {
                    val r = Rect()
                    profile_setting_second_phone.getWindowVisibleDisplayFrame(r)
                    val heightDiff: Int = requireView().rootView.height - (r.bottom - r.top)
                    if (heightDiff > 100) {

                    } else {
                        profile_setting_second_phone.clearFocus()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        //Ответ
        profile_s_response.viewTreeObserver
            .addOnGlobalLayoutListener {
                try {
                    val r = Rect()
                    profile_s_response.getWindowVisibleDisplayFrame(r)
                    val heightDiff: Int = requireView().rootView.height - (r.bottom - r.top)
                    if (heightDiff > 100) {

                    } else {
                        clearFocus()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        //Введите старый пороль
        profile_s_old_password.viewTreeObserver
            .addOnGlobalLayoutListener {
                try {
                    val r = Rect()
                    profile_s_old_password.getWindowVisibleDisplayFrame(r)
                    val heightDiff: Int = requireView().rootView.height - (r.bottom - r.top)
                    if (heightDiff > 100) {

                    } else {
                        clearFocus()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        //Введите новый пароль
        profile_s_one_password.viewTreeObserver
            .addOnGlobalLayoutListener {
                try {
                    val r = Rect()
                    profile_s_one_password.getWindowVisibleDisplayFrame(r)
                    val heightDiff: Int = requireView().rootView.height - (r.bottom - r.top)
                    if (heightDiff > 100) {

                    } else {
                        clearFocus()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        //Повторите пароль
        profile_s_two_password.viewTreeObserver
            .addOnGlobalLayoutListener {
                try {
                    val r = Rect()
                    profile_s_two_password.getWindowVisibleDisplayFrame(r)
                    val heightDiff: Int = requireView().rootView.height - (r.bottom - r.top)
                    if (heightDiff > 100) {

                    } else {
                        clearFocus()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        click_s_old_password_image.setOnClickListener {
            profile_s_old_password.requestFocus()
            profile_s_old_password.setSelection(profile_s_old_password.text!!.length);
            val img =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            img.showSoftInput(profile_s_old_password, 0)
        }

        click_s_one_password.setOnClickListener {
            profile_s_one_password.requestFocus()
            profile_s_one_password.setSelection(profile_s_one_password.text!!.length);
            val img =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            img.showSoftInput(profile_s_one_password, 0)
        }

        profile_s_two_password.addTextChangedListener {
            textPasswordTwo = it.toString()
        }

        click_s_two_password.setOnClickListener {
            profile_s_two_password.requestFocus()
            profile_s_two_password.setSelection(profile_s_two_password.text!!.length);
            val img =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            img.showSoftInput(profile_s_two_password, 0)
        }

        //метод удаляет все символы из строки
        profile_setting_second_phone.addTextChangedListener {
            if (profile_setting_second_phone.text.toString() != "") {
                val matchedResults =
                    Regex(pattern = """\d+""").findAll(input = codeMack + profile_setting_second_phone.text.toString())
                val result = StringBuilder()
                for (matchedText in matchedResults) {
                    reNum = result.append(matchedText.value).toString()
                }
            }
        }

        // видем пороль или нет
        var isValidPassword = false
        profile_s_old_password_image.setOnClickListener {
            if (!isValidPassword) {
                profile_s_old_password.transformationMethod = null
                profile_s_old_password.setSelection(profile_s_old_password.text!!.length);
                isValidPassword = true
            } else {
                profile_s_old_password.transformationMethod = PasswordTransformationMethod()
                profile_s_old_password.setSelection(profile_s_old_password.text!!.length);
                isValidPassword = false
            }
        }

        // видем пороль или нет
        var isValidOne = false
        profile_s_one_password_show.setOnClickListener {
            if (!isValidOne) {
                profile_s_one_password.transformationMethod = null
                profile_s_one_password.setSelection(profile_s_one_password.text!!.length);
                isValidOne = true
            } else {
                profile_s_one_password.transformationMethod = PasswordTransformationMethod()
                profile_s_one_password.setSelection(profile_s_one_password.text!!.length);
                isValidOne = false
            }
        }
        // видем пороль или нет
        var isValidTwo = false
        profile_s_two_password_show.setOnClickListener {
            if (!isValidTwo) {
                profile_s_two_password.transformationMethod = null
                profile_s_two_password.setSelection(profile_s_two_password.text!!.length);
                isValidTwo = true
            } else {
                profile_s_two_password.transformationMethod = PasswordTransformationMethod()
                profile_s_two_password.setSelection(profile_s_two_password.text!!.length);
                isValidTwo = false
            }
        }


        access_restricted.setOnClickListener {
            initRestart()
        }

        no_connection_repeat.setOnClickListener {
            initRestart()
        }

        technical_work.setOnClickListener {
            initRestart()
        }

        not_found.setOnClickListener {
            initRestart()
        }

        home_forget_password.setOnClickListener {
            val intent = Intent(context, ContactingServiceActivity::class.java)
            profileSettingAnimR = true
            intent.putExtra("number", "1")
            startActivity(intent)
        }


        profile_s_enter.setOnClickListener {
            if (profile_s_one_password.text.toString() != "" && profile_s_two_password.text.toString() != "") {
                if (AppPreferences.password == profile_s_old_password.text.toString()) {
                    val mapProfilePassword = HashMap<String, String>()
                    mapProfilePassword.put("login", AppPreferences.login.toString())
                    if (profile_s_old_password.text.toString().isNotEmpty()) {
                        mapProfilePassword.put("password", profile_s_old_password.text.toString())
                    } else {
                        mapProfilePassword.put("password", AppPreferences.password.toString())
                    }
                    viewModel.checkPassword(mapProfilePassword)
                    checkPassword()
                } else {
                    isValidPassword()
                }
            } else {
                if (profile_s_old_password.text.toString() == "") {
                    initPassword()
                } else {
                    if (AppPreferences.password == profile_s_old_password.text.toString()) {
                        initPassword()
                    } else {
                        isValidPassword()
                    }
                }
            }
        }

        profile_setting_second_phone.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if (profile_setting_second_phone != null) {
                    profile_setting_second_phone.mask = null
                    profile_setting_second_phone.mask = list[codeNationality].phoneMaskSmall
                    profile_setting_second_phone.setSelection(profile_setting_second_phone.text!!.length);
                    profile_setting_second_phone.isFocusableInTouchMode = true
                }
            }

        click_s_response.setOnClickListener {
            profile_s_response.requestFocus()
            profile_s_response.setSelection(profile_s_response.text!!.length);
            val img =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            img.showSoftInput(profile_s_response, 0)
        }

        click_s_second.setOnClickListener {
            profile_setting_second_phone.requestFocus()
            val img = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            img.showSoftInput(profile_setting_second_phone, 0)
        }
    }

    private fun listListResult(result: Int) {
        if (result == 400 || result == 500 || result == 409 || result == 429) {
            profile_s_technical_work.visibility = View.VISIBLE
            profile_s_no_connection.visibility = View.GONE
            profile_s_access_restricted.visibility = View.GONE
            profile_s_not_found.visibility = View.GONE
            profile_s_swipe.visibility = View.GONE
        } else if (result == 403) {
            profile_s_access_restricted.visibility = View.VISIBLE
            profile_s_technical_work.visibility = View.GONE
            profile_s_no_connection.visibility = View.GONE
            profile_s_not_found.visibility = View.GONE
            profile_s_swipe.visibility = View.GONE
        } else if (result == 404) {
            profile_s_not_found.visibility = View.VISIBLE
            profile_s_access_restricted.visibility = View.GONE
            profile_s_technical_work.visibility = View.GONE
            profile_s_no_connection.visibility = View.GONE
            profile_s_swipe.visibility = View.GONE
        } else if (result == 401) {
            initAuthorized()
        }
    }

    private fun errorList(error: String) {
        if (error == "400" || error == "500" || error == "600" || error == "429" || error == "409") {
            profile_s_technical_work.visibility = View.VISIBLE
            profile_s_no_connection.visibility = View.GONE
            profile_s_access_restricted.visibility = View.GONE
            profile_s_not_found.visibility = View.GONE
            profile_s_swipe.visibility = View.GONE
        } else if (error == "403") {
            profile_s_access_restricted.visibility = View.VISIBLE
            profile_s_technical_work.visibility = View.GONE
            profile_s_no_connection.visibility = View.GONE
            profile_s_not_found.visibility = View.GONE
            profile_s_swipe.visibility = View.GONE
        } else if (error == "404") {
            profile_s_not_found.visibility = View.VISIBLE
            profile_s_access_restricted.visibility = View.GONE
            profile_s_technical_work.visibility = View.GONE
            profile_s_no_connection.visibility = View.GONE
            profile_s_swipe.visibility = View.GONE
        } else if (error == "601") {
            profile_s_no_connection.visibility = View.VISIBLE
            profile_s_technical_work.visibility = View.GONE
            profile_s_access_restricted.visibility = View.GONE
            profile_s_not_found.visibility = View.GONE
            profile_s_swipe.visibility = View.GONE
        } else if (error == "401") {
            initAuthorized()
        }
    }

    // проверка если errorCode и errorCodeClient == 200
    private fun resultSuccessfully() {
        if (errorCodeGender == "200" && errorCodeNationality == "200" && errorListAvailableCountry == "200" && errorListSecretQuestion == "200" && errorClientInfo == "200") {
            profile_s_technical_work.visibility = View.GONE
            profile_s_no_connection.visibility = View.GONE
            profile_s_access_restricted.visibility = View.GONE
            profile_s_not_found.visibility = View.GONE
            profile_s_swipe.visibility = View.VISIBLE
            if (profileSettingAnimR) {
                TransitionAnimation(activity as AppCompatActivity).transitionLeft(profile_setting_anim)
                profileSettingAnimR = true
            }
        }
    }

    private fun initAuthorized() {
        val intent = Intent(context, HomeActivity::class.java)
        AppPreferences.token = ""
        startActivity(intent)
    }

    fun setTitle(title: String?, color: Int) {
        val activity: Activity? = activity
        if (activity is MainActivity) {
            activity.setTitle(title, color)
        }
    }

    override fun onResume() {
        super.onResume()
//        if (profileSettingAnimR) {
//            TransitionAnimation(activity as AppCompatActivity).transitionLeft(profile_setting_anim)
//            profileSettingAnimR = true
//        }
        profile_s_one_password.text = null
        profile_s_two_password.text = null
        profile_s_two_password.transformationMethod = PasswordTransformationMethod()
        profile_s_one_password.transformationMethod = PasswordTransformationMethod()
        profile_s_old_password.text = null
        profile_s_old_password.transformationMethod = PasswordTransformationMethod()
        if (viewModel.listGenderDta.value != null && viewModel.listGenderDta.value != null && viewModel.listNationalityDta.value != null
            && viewModel.listAvailableCountryDta.value != null && viewModel.listSecretQuestionDta.value != null && viewModel.listClientInfoDta.value != null
        ) {
            if (errorCodeGender == "200" && errorCodeNationality == "200" && errorListAvailableCountry == "200" && errorListSecretQuestion == "200" && errorClientInfo == "200") {
                AppPreferences.reviewCode = 1
                initResult()
            } else {
                AppPreferences.reviewCode = 1
                profileSettingAnim = true
                initRestart()
            }
        } else {
            AppPreferences.reviewCode = 0
            profileSettingAnim = false
            viewModel.refreshCode = false
            initRestart()
        }

        //меняет цвета навигационной понели
        ColorWindows(activity as AppCompatActivity).rollback()

        val backArrow = resources.getDrawable(R.drawable.ic_baseline_arrow_back_24)
        backArrow.setColorFilter(
            resources.getColor(android.R.color.white),
            PorterDuff.Mode.SRC_ATOP
        )
        (activity as AppCompatActivity?)!!.getSupportActionBar()!!.setHomeAsUpIndicator(backArrow)
        profile_s_owner.requestFocus()
    }

    //Блакирует фокус на обекте
    private fun clearFocus() {
        profile_s_old_password.clearFocus()
        profile_s_two_password.clearFocus()
        profile_s_one_password.clearFocus()
        profile_s_response.clearFocus()
        focus_prof.requestFocus()
    }

    private fun isValid(): Boolean {
        var valid = true
        if (profile_s_response.text!!.toString().isEmpty()) {
            profile_s_response.error = "Ответ не должно быть пустым"
            valid = false
        } else {
            profile_s_response.error = null
        }

        if (profile_setting_second_phone.text.toString() != "") {
            if (reNum.length != list[codeNationality].phoneLength!!.toInt()) {
                profile_setting_second_phone.error = "Введите валидный номер"
                valid = false
            } else {
                profile_setting_second_phone.error = null
            }
        }


        if (profile_s_one_password.text.toString()
                .isNotEmpty() && profile_s_two_password.text.toString().isNotEmpty()
        ) {
            if (profile_s_one_password.text.toString()
                    .toFullPhone() != profile_s_two_password.text.toString().toFullPhone()
            ) {
                profile_s_two_password.error = "Пароль должны совпадать"
                valid = false
            } else {
                profile_s_two_password.error = null
            }
        } else {
            if (profile_s_one_password.text.toString()
                    .isNotEmpty() && profile_s_two_password.text.toString().isEmpty()
            ) {
                profile_s_two_password.error = "Поле не должно быть пустым"
                valid = false
            } else {
                profile_s_two_password.error = null
            }

            if (profile_s_one_password.text.toString()
                    .isEmpty() && profile_s_two_password.text.toString().isNotEmpty()
            ) {
                profile_s_one_password.error = "Поле не должно быть пустым"
                valid = false
            } else {
                profile_s_one_password.error = null
            }
        }

        return valid
    }

    private fun isValidPassword(): Boolean {
        var valid = true
        if (profile_s_old_password.text.toString().isEmpty()) {
            profile_s_old_password.error = "Поле не должно быть пустым"
            valid = false
        } else if (profile_s_old_password.text.toString() != AppPreferences.password) {
            profile_s_old_password.error = "Пароль введен неверно!"
            valid = false
        } else {
            profile_s_old_password.error = null
        }
        return valid
    }


    override fun onStart() {
        super.onStart()
        // проверка если с timer приходит token null
        if (AppPreferences.token == "") {
            initAuthorized()
        }
    }
}