package com.stie.attendencestie

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.JsonObject
import com.stie.attendencestie.Services.RetrofitClient
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    lateinit var sharedPreferences:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.createfualtreport)
     /*  sharedPreferences = getSharedPreferences("data", MODE_PRIVATE)







           var nricc = (sharedPreferences.getString("nric", ""));
           Log.e("value",nricc.toString())

        if(!nricc.equals(""))
        {
            val intent = Intent(this@LoginActivity,MainActivity::class.java);

            startActivity(intent);
        }


        login.setOnClickListener(View.OnClickListener {

            if (nric.text.toString().equals("")) {
                nric.setError("Please Enter Valid NRIC")
            } else {

                setUpLocation()
            }

        })*/
    }

    fun setUpLocation() {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Login..")
        //set message for alert dialog
        builder.setMessage("Checking NRIC...")
        builder.setIcon(android.R.drawable.progress_indeterminate_horizontal)
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
        var jsonObject: JsonObject? = JsonObject()
        if (jsonObject != null) {
            jsonObject.addProperty("nric", nric.text.toString())

        }
        RetrofitClient.instance.login(jsonObject)
            ?.enqueue(object : Callback<ResponseBody?> {
                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    alertDialog.cancel()

                    if (response?.code() == 200) {

                     try {
                            val jsonObject = JSONObject(response.body()?.string())

                            var name: String = jsonObject.getString("name")
                         var desig:String = jsonObject.getString("designation")

                         val editor = sharedPreferences.edit()
                         editor.putString("nric", nric.text.toString())
                         editor.putString("desig",desig)
                         editor.putString("name",name)
                         editor.commit()
                         val intent = Intent(this@LoginActivity,MainActivity::class.java);

                         startActivity(intent);
                                 Log.e("value", name)
                        } catch (err: JSONException) {
                            Log.e("error", err.toString())
                        }




                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Please Enter Valid NRIC ",
                            Toast.LENGTH_LONG
                        ).show()
                        nric.setError("Please Enter Valid NRIC")

                    }

                }

            })
    }

    override fun onStart() {
        super.onStart()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET
                ), 10
            )
            return
        }
    }
}