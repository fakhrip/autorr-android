package com.trime.f4r4w4y.autorr.gql

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class QueryViewModel(application: Application) : AndroidViewModel(application) {
    private var gqLService: Service = Instance.graphQLService

    fun sendData(csvVal: String, rrVal: String): LiveData<String> {
        val result = MutableLiveData<String>()

        val base64val = Base64.encodeToString(csvVal.toByteArray(), Base64.NO_WRAP)
        val token = getApplication<Application>().getSharedPreferences(
            "autorr_pref",
            AppCompatActivity.MODE_PRIVATE
        ).getString("login_token", "nope")

        val paramObject = JSONObject()
        paramObject.put(
            "query", "mutation fileUploadMut {\n" +
                    "  fileUpload(base64str: \"$base64val\", name: \"$rrVal\", token: \"$token\") {\n" +
                    "    file_path\n" +
                    "  }\n" +
                    "}"
        )

        viewModelScope.launch {
            try {
                val response = gqLService.postAuthenticatedQuery(
                    "Bearer ${token.toString()}",
                    paramObject.toString()
                )
                if (response.isSuccessful) result.postValue(
                    JSONObject(
                        response.body().toString()
                    ).getJSONObject("data").getJSONObject("fileUpload").getString("file_path")
                )
                else {
                    result.postValue("nope")
                    Log.e("GQL_ERR", response.errorBody().toString())
                }
                Log.d("GQL", response.toString())
            } catch (e: java.lang.Exception) {
                result.postValue(e.message.toString())
                e.printStackTrace()
            }
        }

        return result
    }

    fun login(email: String, password: String): LiveData<String> {
        val token = MutableLiveData<String>()

        val paramObject = JSONObject()
        paramObject.put(
            "query", "mutation LoginMut {\n" +
                    "  login(email: \"$email\", password: \"$password\") {\n" +
                    "    accessToken\n" +
                    "  }\n" +
                    "}"
        )

        viewModelScope.launch {
            try {
                val response = gqLService.postGuestQuery(paramObject.toString())
                if (response.isSuccessful) token.postValue(
                    JSONObject(
                        response.body().toString()
                    ).getJSONObject("data").getJSONObject("login").getString("accessToken")
                )
                else {
                    token.postValue("nope")
                    Log.e("GQL_ERR", response.errorBody().toString())
                }
                Log.d("GQL", response.toString())
            } catch (e: java.lang.Exception) {
                token.postValue(e.message.toString())
                e.printStackTrace()
            }
        }

        return token
    }
}