package com.example.kotlincashloan.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlincashloan.service.model.profile.GetResultOperationModel
import com.example.kotlincashloan.service.model.profile.ResultOperationModel
import com.example.kotlinscreenscanner.service.model.CommonResponse
import com.timelysoft.tsjdomcom.service.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel(){

    val errorListOperation = MutableLiveData<String>()
    var listListOperationDta = MutableLiveData<CommonResponse<ArrayList<ResultOperationModel>>>()

    fun listOperation(map: Map<String, String>){
        RetrofitService.apiService().listOperation(map).enqueue(object : Callback<CommonResponse<ArrayList<ResultOperationModel>>> {
            override fun onFailure(call: Call<CommonResponse<ArrayList<ResultOperationModel>>>, t: Throwable) {
                if (t.localizedMessage != "End of input at line 1 column 1 path \$"){
                    errorListOperation.postValue( "601")
                }else{
                    errorListOperation.postValue( "600")
                }
            }
            override fun onResponse(call: Call<CommonResponse<ArrayList<ResultOperationModel>>>, response: Response<CommonResponse<ArrayList<ResultOperationModel>>>) {
                if (response.isSuccessful) {
                    if (response.body()!!.code == 200){
                        listListOperationDta.postValue(response.body())
                    }else{
                        errorListOperation.postValue(response.body()!!.code.toString())
                    }
                }else{
                    errorListOperation.postValue(response.raw().code.toString())
                }
            }
        })
    }

    val errorGetOperation = MutableLiveData<String>()
    var listGetOperationDta = MutableLiveData<CommonResponse<GetResultOperationModel>>()

    fun getOperation(map: Map<String, String>){
        RetrofitService.apiService().getOperation(map).enqueue(object : Callback<CommonResponse<GetResultOperationModel>> {
            override fun onFailure(call: Call<CommonResponse<GetResultOperationModel>>, t: Throwable) {
                if (t.localizedMessage != "End of input at line 1 column 1 path \$"){
                    errorGetOperation.postValue( "601")
                }else{
                    errorGetOperation.postValue( "600")
                }
            }
            override fun onResponse(call: Call<CommonResponse<GetResultOperationModel>>, response: Response<CommonResponse<GetResultOperationModel>>) {
                if (response.isSuccessful) {
                    if (response.body()!!.code == 200){
                        listGetOperationDta.postValue(response.body())
                    }else{
                        errorGetOperation.postValue(response.body()!!.code.toString())
                    }
                }else{
                    errorGetOperation.postValue(response.raw().code.toString())
                }
            }
        })
    }
}