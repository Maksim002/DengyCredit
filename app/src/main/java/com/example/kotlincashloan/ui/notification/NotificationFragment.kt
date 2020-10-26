package com.example.kotlincashloan.ui.notification


import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import androidx.lifecycle.Observer
import com.example.kotlincashloan.R
import com.example.kotlincashloan.adapter.notification.NotificationAdapter
import com.example.kotlincashloan.adapter.profile.MyOperationModel
import com.example.kotlincashloan.service.model.Notification.ResultListNoticeModel
import com.example.kotlincashloan.ui.registration.login.HomeActivity
import com.example.kotlincashloan.utils.ObservedInternet
import com.example.kotlinscreenscanner.ui.MainActivity
import com.timelysoft.tsjdomcom.service.AppPreferences
import kotlinx.android.synthetic.main.fragment_notification.*
import kotlinx.android.synthetic.main.fragment_support.*
import kotlinx.android.synthetic.main.item_access_restricted.*
import kotlinx.android.synthetic.main.item_no_connection.*
import kotlinx.android.synthetic.main.item_not_found.*
import kotlinx.android.synthetic.main.item_technical_work.*
import java.util.HashMap

class NotificationFragment : Fragment() {
    private var myAdapter = NotificationAdapter()
    private var viewModel = NotificationViewModel()
    private val map = HashMap<String, String>()
    val handler = Handler()
    private var refresh = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.show()
        requireActivity().onBackPressedDispatcher.addCallback(this) {}
        map.put("login", AppPreferences.login.toString())
        map.put("token", AppPreferences.token.toString())
        initRefresh()
        initRecycler()
        initClick()
    }

    private fun initClick() {
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
    }

    private fun initRestart() {
        ObservedInternet().observedInternet(requireContext())
        if (!AppPreferences.observedInternet) {
            notification_no_connection.visibility = View.VISIBLE
            notification_swipe.visibility = View.GONE
            notification_technical_work.visibility = View.GONE
            notification_access_restricted.visibility = View.GONE
            notification_not_found.visibility = View.GONE
            viewModel.errorNotice.value = null
        } else {
            if (viewModel.listNoticeDta.value != null) {
                viewModel.listNotice(map)
            } else {
                viewModel.errorNotice.value = null
                viewModel.listNotice(map)
            }
        }
    }

    private fun initRefresh() {
        notification_swipe.setOnRefreshListener {
            handler.postDelayed(Runnable { // Do something after 5s = 500ms
                refresh = true
                initRestart()
            }, 1000)
        }
    }

    private fun initRecycler() {
        if (!refresh) {
            HomeActivity.alert.show()
        }
        viewModel.listNoticeDta.observe(viewLifecycleOwner, Observer { result->
            if (result.result != null){
                myAdapter.update(result.result)
                notification_recycler.adapter = myAdapter
                notification_swipe.visibility = View.VISIBLE
                notification_technical_work.visibility = View.GONE
                notification_access_restricted.visibility = View.GONE
                notification_no_connection.visibility = View.GONE
                notification_not_found.visibility = View.GONE
            }else{
                if (result.error.code == 500 || result.error.code == 400){
                    notification_technical_work.visibility = View.VISIBLE
                    notification_access_restricted.visibility = View.GONE
                    notification_no_connection.visibility = View.GONE
                    notification_not_found.visibility = View.GONE
                    notification_swipe.visibility = View.GONE
                }else if (result.error.code == 403){
                    notification_access_restricted.visibility = View.VISIBLE
                    notification_technical_work.visibility = View.GONE
                    notification_no_connection.visibility = View.GONE
                    notification_not_found.visibility = View.GONE
                    notification_swipe.visibility = View.GONE
                }else if (result.error.code == 404){
                    notification_not_found.visibility = View.VISIBLE
                    notification_access_restricted.visibility = View.GONE
                    notification_technical_work.visibility = View.GONE
                    notification_no_connection.visibility = View.GONE
                    notification_swipe.visibility = View.GONE
                }else if (result.error.code == 401){
                    initAuthorized()
                }
            }
            notification_swipe.isRefreshing = false
            HomeActivity.alert.hide()
        })

        viewModel.errorNotice.observe(viewLifecycleOwner, Observer { error->
            if (error == "500" || error == "400" || error == "600"){
                notification_technical_work.visibility = View.VISIBLE
                notification_access_restricted.visibility = View.GONE
                notification_no_connection.visibility = View.GONE
                notification_not_found.visibility = View.GONE
                notification_swipe.visibility = View.GONE
            }else if (error == "403"){
                notification_access_restricted.visibility = View.VISIBLE
                notification_technical_work.visibility = View.GONE
                notification_no_connection.visibility = View.GONE
                notification_not_found.visibility = View.GONE
                notification_swipe.visibility = View.GONE
            }else if (error == "404"){
                notification_not_found.visibility = View.VISIBLE
                notification_access_restricted.visibility = View.GONE
                notification_technical_work.visibility = View.GONE
                notification_no_connection.visibility = View.GONE
                notification_swipe.visibility = View.GONE
            }else if (error == "401"){
                initAuthorized()
            }else if (error == "601"){
                notification_no_connection.visibility = View.VISIBLE
                notification_not_found.visibility = View.GONE
                notification_access_restricted.visibility = View.GONE
                notification_technical_work.visibility = View.GONE
                notification_swipe.visibility = View.GONE
            }
            notification_swipe.isRefreshing = false
            HomeActivity.alert.hide()
        })
    }

    private fun initAuthorized() {
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()
        MainActivity.timer.timeStop()
        HomeActivity.alert.hide()
        if (viewModel.listNoticeDta.value == null){
            handler.postDelayed(Runnable { // Do something after 5s = 500ms
                viewModel.listNotice(map)
                initRecycler()
            }, 500)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            requireActivity().getWindow()
                .setStatusBarColor(requireActivity().getColor(R.color.orangeColor))
            val decorView: View = (activity as AppCompatActivity).getWindow().getDecorView()
            var systemUiVisibilityFlags = decorView.systemUiVisibility
            systemUiVisibilityFlags =
                systemUiVisibilityFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            decorView.systemUiVisibility = systemUiVisibilityFlags
            val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar);
            toolbar.setBackgroundDrawable(ColorDrawable(requireActivity().getColor(R.color.orangeColor)))
            toolbar.setTitleTextColor(requireActivity().getColor(R.color.whiteColor))
        }
    }
}