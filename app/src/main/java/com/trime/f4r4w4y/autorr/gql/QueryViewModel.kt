package com.trime.f4r4w4y.autorr.gql

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class QueryViewModel : ViewModel() {
    private var gqLService: Service = Instance.graphQLService

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
                val response = gqLService.postDynamicQuery(paramObject.toString())
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