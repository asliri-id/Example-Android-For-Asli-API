package com.yusuffirdaus.aslirisalesdemo.upil

import android.app.Activity
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.util.*


/**
 * Created by Faisal on 12/12/2016.
 *
 */

class HttpRequest(private val activity: Activity) {


//    private val pDialog: SweetAlertDialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)


    private fun showLoading() {
        if (activity.isFinishing) {
            return
        }

        activity.runOnUiThread {
            //            pDialog.show()
        }
    }

    private fun hideLoading() {
        activity.runOnUiThread {
            //            pDialog.dismiss()
        }
    }


    fun <T> httpHandler(
            method: Int,
            mUrl: String,
            body: T,
            callback: Callback
    ) {
        var url = mUrl


        val jsonObjectRequest: JsonObjectRequest?

        jsonObjectRequest = object : JsonObjectRequest(
                method,
                url,
                if (body is JSONObject) body else JSONObject(GsonBuilder().create().toJson(body)),
                Response.Listener { data ->

                    try {
                        callback.onHttpPostSuccess(data.toString())
                    } catch (e: Exception) {
                        Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                    }

                }, Response.ErrorListener { error ->

            //            var json: String? = null


            val response = error.networkResponse
            if (response?.data != null) {
                onhandleError(response, callback)



            }
        }) {

            /**
             * Passing some request headers
             */

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()

                //prefer jwt token first

//                val jwtToken = util.jwtToken
//                if (!jwtToken.isEmpty()) {
//                    params["Authorization"] = "Bearer " + jwtToken
//                }

                return params
            }
        }

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                CONFIGURATION.CONNECTION_TIMEOUT.CODE,
                CONFIGURATION.CONNECTION_RETRY.CODE,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val requestQueue = Volley.newRequestQueue(activity)

        requestQueue.add(jsonObjectRequest)
    }


    fun stringRequest(method: Int, url: String, body: JSONObject, callback: Callback) {
        val requestBody = body.toString()

        val requestQueue = Volley.newRequestQueue(activity)

        val stringRequest = object : StringRequest(method, url,
                Response.Listener { response ->
                    callback.onHttpPostSuccess(response)
                },
                Response.ErrorListener { error ->
                    error.message?.let { callback.onHttpPostError(it) }
                }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray? {
                return try {
                    if (requestBody == null) null else requestBody.toByteArray()
                } catch (uee: UnsupportedEncodingException) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8")
                    null
                }

            }

        }

        requestQueue.add(stringRequest)
    }


    interface Callback {
        fun onHttpPostSuccess(result: String)
        fun onHttpPostError(error: String)

    }


    fun trimMessage(json: String, key: String): String? {
        var trimmedString: String?

        try {
            val obj = JSONObject(json)
            trimmedString = obj.getString(key)
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }

        return trimmedString
    }

    //Somewhere that has access to a context
    fun displayMessage(toastString: String) {
        Toast.makeText(activity, toastString, Toast.LENGTH_LONG).show()
    }


    fun onhandleError(response: NetworkResponse, callback: Callback?) {
        var json: String?

        when (response.statusCode) {
            403 -> {
                json = String(response.data)
                json = trimMessage(json, "message")
                if (json != null) callback?.onHttpPostError(json)
//                json = String(response.data)
//                json = trimMessage(json, "message")
//                if (json != null) {
//                    util.sessionClear()
//
//                    Toast.makeText(
//                        activity,
//                        activity.getString(R.string.token_expired),
//                        Toast.LENGTH_LONG
//                    ).show()
//
//                    val intent = Intent(activity, LoginActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    activity.startActivity(intent)
//                }

            }

            500 -> {
                json = String(response.data)
                json = trimMessage(json, "message")
                if (json != null) callback?.onHttpPostError(json)

            }
            else -> {
                json = String(response.data)
                json = trimMessage(json, "message")
                if (json != null) callback?.onHttpPostError(json)
            }
        }
    }

}
