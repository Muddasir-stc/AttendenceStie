package com.stie.attendencestie

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.text.format.Time
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.gson.JsonObject
import com.stie.attendencestie.Services.Modal
import com.stie.attendencestie.Services.RetrofitClient
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener ,TimePickerDialog.OnTimeSetListener{
    lateinit var picker: DatePickerDialog
    var day = 0
    var month: Int = 0
    var year: Int = 0
    var hour: Int = 0
    var minute: Int = 0
    var myDay = 0
    var myMonth: Int = 0
    var myYear: Int = 0
    var myHour: Int = 0
    var myMinute: Int = 0
    var lattitude : String? = null
    var longnitude :String? = null
    val listoflocation = ArrayList<Modal>()
    var lattitude1 : String? = null
    var longnitude1 :String? = null
    var radiotext :String = "checkedIn"
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    lateinit var geofencingClient: GeofencingClient
    var cityName:String = ""
    // globally declare LocationCallback
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//date picker
        setSupportActionBar(toolbar)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener { view -> onBackPressed() }
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE)
        var nricc1 = (sharedPreferences.getString("nric", ""));
        var  desig1 =(sharedPreferences.getString("desig", ""));
        var name1 = (sharedPreferences.getString("name", ""));

           name.setText(name1)
        desig.setText(desig1)
          nric.setText(nricc1)

        var st= Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.e("token",st.toString())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this!!)
        geofencingClient = LocationServices.getGeofencingClient(this)
      getLocationUpdates()

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
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                Log.e("value", location.toString())
                if (location != null) {
                    locationNameSetter(location)
                }

            }
        dateSetter()
        timeSetter()











      /*  dateselected.setOnClickListener(View.OnClickListener {


            val calendar: Calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog =
                DatePickerDialog(this@MainActivity, this, year, month, day)
            datePickerDialog.show()

        })*/

    //time picker

            time.setOnClickListener(View.OnClickListener {

                val calendar: Calendar = Calendar.getInstance()
                hour = calendar.get(Calendar.HOUR)
                minute = calendar.get(Calendar.MINUTE)
                year = calendar.get(Calendar.YEAR)
                val timePickerDialog =
                    TimePickerDialog(this@MainActivity, this, hour, minute, true)
                timePickerDialog.show()
            })

        radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            val radio: RadioButton = findViewById(i)
            Log.e("value of radio", radio.text.toString())
            radiotext = radio.text.toString()
        })






        submit.setOnClickListener(View.OnClickListener {

            if (name.text.toString()!!.equals("")!!) {
                name.setError("Name empty")
                Toast.makeText(applicationContext, "Enter Name", Toast.LENGTH_LONG).show()
            } else if (desig.text.toString()!!.equals("")!!) {
                desig.setError("desig empty")
                Toast.makeText(applicationContext, "Enter Designation", Toast.LENGTH_LONG).show()
            } else if (nric.text.toString()!!.equals("")!!) {
                nric.setError("nric empty")

                Toast.makeText(applicationContext, "Enter NRIC ", Toast.LENGTH_LONG).show()
            } else if (dateselected.text.toString()!!.equals("")!!) {
                dateselected.setError("dateselected empty")
                Toast.makeText(applicationContext, "Enter Date", Toast.LENGTH_LONG).show()
            }
            else if (time.text.toString()!!.equals("")!!) {
                time.setError("time empty")
                Toast.makeText(applicationContext, "Enter Time", Toast.LENGTH_LONG).show()
            }

            else if(locationname.text.toString().equals("No Location"))
            {
                locationname.setError("Location  empty")
                Toast.makeText(applicationContext, "Please Enable Location and Gps..", Toast.LENGTH_LONG).show()
            }


            else {
                sendDataToServer()
            }


        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    fun sendDataToServer()
    {
        Log.e("value timedate", dateselected.text.toString() + " " + time.text.toString())
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Saving...")
        //set message for alert dialog
        builder.setMessage("Employee Data.......")
        builder.setIcon(android.R.drawable.progress_indeterminate_horizontal)
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
        var jsonObject : JsonObject? = JsonObject()
        if (jsonObject != null) {

      /*

            {
           "appUser":{
             "name":"",
               "nric":"",
              "designation"
           }
       checkInTime:"Datetime"
        checkOutTime:"DateTime"
            }



            "date":"dd-MM-yyyy",
"checkInTime":"hh:mm a",
"checkOutTime":"hh:mm a"

            */
            jsonObject.addProperty("latitude", lattitude1)
            jsonObject.addProperty("longitude", longnitude1)
            jsonObject.addProperty("temperature", temperature.text.toString())
            jsonObject.addProperty("date", dateselected.text.toString())

            var appuser : JsonObject? = JsonObject()
                appuser?.addProperty("name", name.text.toString())
                appuser?.addProperty("nric", nric.text.toString())
                appuser?.addProperty("designation", desig.text.toString())

            jsonObject.add("appUser", appuser)
            if(radiotext.toString().equals("CheckOut"))
            {
                jsonObject.addProperty(
                    "checkOutTime",
                    time.text.toString()
                )
            }
            else
            {
                jsonObject.addProperty(
                    "checkInTime",
                    time.text.toString()
                )

            }


         Log.e("value of json", jsonObject.toString())
          //  jsonObject.addProperty("password",temperature.text.toString())
          //  jsonObject.addProperty("password",temperature.text.toString())

        }

        RetrofitClient.instance.sendFormData(jsonObject)
            ?.enqueue(object : Callback<ResponseBody?> {
                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    alertDialog.cancel()
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    alertDialog.cancel()
                    Log.e("Value of res", response?.code().toString())

                    if (response?.code() == 200) {

                        Toast.makeText(applicationContext, "Attendance Saved", Toast.LENGTH_LONG)
                            .show()
                        onBackPressed()
                    } else if (response?.code() == 400) {
                        Toast.makeText(applicationContext, "Try again.....", Toast.LENGTH_LONG)
                            .show()
                    } else if (response?.code() == 404) {
                        Toast.makeText(
                            applicationContext,
                            "Check NRIC number and Try again....",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(applicationContext, "Try again....", Toast.LENGTH_LONG)
                            .show()
                    }

                }

            })
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()




    }


    public fun timeSetter()
    {
        val calendar: Calendar = Calendar.getInstance()
       var  p1  = calendar.get(Calendar.HOUR)
       var  p2 = calendar.get(Calendar.MINUTE)
        var p3:String = ""

        if (calendar.get(Calendar.AM_PM) == Calendar.AM)
            p3 = "AM";
        else if (calendar.get(Calendar.AM_PM) == Calendar.PM)
            p3 = "PM";


        var hr:String=""
        if(p1<10)
        {
            hr = "0"+p1.toString()
        }
        else
        {
            hr = p1.toString()
        }
        var minn:String=""
        if(p2<10)
        {
            minn = "0"+p2.toString()
        }
        else
        {
            minn = p2.toString()
        }

        var m = hr + ":"+ minn+" "+p3.toString()

        time.setText(m)
    }

    fun locationNameSetter(location: Location)
    {


        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(
            location.latitude,
            location.longitude!!,
            1
        )




        lattitude1 = location.latitude.toString()
        longnitude1 = location.longitude.toString()

        cityName = addresses.firstOrNull()?.locality.toString()

        //provider name is unnecessary


        //your coords of course
        var mod :Modal = Modal()


        var mod1 :Modal = Modal()

        mod1.locationName = "Former Shu Qun Secondary"
        mod1.latitude = 1.3400347999999958
        mod1.longtitude = 103.73990540000001


        listoflocation.add(mod1)


        var mod2 :Modal = Modal()

        mod2.locationName = "Former SigLap Secondary"
        mod2.latitude = 1.3800273999999997
        mod2.longtitude = 103.93878409999999


        listoflocation.add(mod2)



        var mod3 :Modal = Modal()

        mod3.locationName = "Former Da Qiao Primary"
        mod3.latitude = 1.3719690000000033
        mod3.longtitude = 103.85865


        listoflocation.add(mod3)


        var mod4 :Modal = Modal()

        mod4.locationName = "Former North view Secondary"
        mod4.latitude = 1.427905899999992
        mod4.longtitude = 103.8447077


        listoflocation.add(mod4)
        var mod5 :Modal = Modal()

        mod5.locationName = "696 Upper Changi"
        mod5.latitude = 1.3426153000000085
        mod5.longtitude = 103.9620293


        listoflocation.add(mod5)
        var mod6 :Modal = Modal()

        mod6.locationName = "Former Woodlands west NPC "
        mod6.latitude = 1.4333147999999953
        mod6.longtitude = 103.77906290000003


        listoflocation.add(mod6)
        var mod7 :Modal = Modal()

        mod7.locationName = "Former East view Primary"
        mod7.latitude = 1.3471182000000173
        mod7.longtitude = 103.9396741


        listoflocation.add(mod7)
        var mod8 :Modal = Modal()

        mod8.locationName = "Former Coral Primary"
        mod8.latitude = 1.3675302000000038
        mod8.longtitude = 103.9492665


        listoflocation.add(mod8)
        var mod9 :Modal = Modal()

        mod9.locationName = "Former Bedok Town Primary"
        mod9.latitude = 1.3308237000000012
        mod9.longtitude = 103.91140600000003


        listoflocation.add(mod9)
        var mod10 :Modal = Modal()

        mod10.locationName = "Sci Centre Road"
        mod10.latitude = 1.333168699999986
        mod10.longtitude = 103.7356443


        listoflocation.add(mod10)
        var mod11 :Modal = Modal()

        mod11.locationName = "ormer East view Primary "
        mod11.latitude = 1.3471182000000173
        mod11.longtitude = 103.9396741


        listoflocation.add(mod11)
















        Log.e("size of locait",listoflocation.size.toString())
        madalData()

        for (item in listoflocation) {
            val targetLocation = Location("")
            targetLocation.latitude = item.latitude!!
            targetLocation.longitude = item.longtitude!!
            Log.e("latt",location.latitude.toString())


            var m= location.distanceTo(targetLocation)
            Log.e("value", m.toString())
            if(m <10)
            {
                locationname.setText(item.locationName)

            }

        }
        if(locationname.text.toString().equals(""))
        {
            locationname.setText("latt:"+lattitude1 +" long: "+longnitude1)
        }







    }
    public fun dateSetter()
    {
        val calendar: Calendar = Calendar.getInstance()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)

        var myMonthh:String= ""
        if(month<10)
        {
            myMonthh = "0"+ (month+1).toString()

        }
        else
        {
            myMonthh =(month+1).toString()
        }
        var myday:String = ""
        if(day<10)
        {
            myday =   "0"+ day.toString()
        }
        else{
            myday =  day.toString()
        }
        var  m =  ""+myday+"-"+myMonthh+"-"+year
        dateselected.setText(m)


    }

    private fun startLocationUpdates() {

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
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }



    private fun getLocationUpdates() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this!!)
        locationRequest = LocationRequest()
        locationRequest.interval = 50000
        locationRequest.fastestInterval = 50000
        locationRequest.smallestDisplacement = 170f // 170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    val location =
                        locationResult.lastLocation
                       locationNameSetter(locationResult.lastLocation)
                    lattitude1 = location.latitude.toString()
                    longnitude1 = location.longitude.toString()



                }


            }
        }
    }
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        myDay = day
        myYear = year

        val calendar: Calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        var myMonthh:String= ""
        if(month<10)
        {
            myMonthh = "0"+ (month+1).toString()

        }
        else
        {
            myMonthh =(month+1).toString()
        }
        var myday:String = ""
           if(dayOfMonth<10)
           {
               myday =   "0"+ dayOfMonth.toString()
           }
        else{
               myday =  dayOfMonth.toString()
           }
        var  m =  ""+myday+"-"+myMonthh+"-"+year
        dateselected.setText(m)
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        var hr:String=""
        val AM_PM: String
        AM_PM = if (p1 < 12) {
            "AM"
        } else {
            "PM"
        }
        Log.e("value of ", AM_PM)
        if(p1<10)
        {
            hr = "0"+p1.toString()
        }
        else
        {
            hr = p1.toString()
        }
        var minn:String=""
        if(p2<10)
        {
            minn = "0"+p2.toString()
        }
        else
        {
            minn = p2.toString()
        }

        var m = hr + ":"+ minn

  time.setText(m)

    }


    fun madalData()
    {
        var mod11 :Modal = Modal()
        mod11.locationName = "Former AMK ITE "
        mod11.latitude = 1.3774718000000115
        mod11.longtitude = 103.85656300000002


        listoflocation.add(mod11)






        var mod12 :Modal = Modal()
          mod12.locationName = "Jurong Medical Center "
          mod12.latitude = 1.340713600000013
          mod12.longtitude =103.7044


        listoflocation.add(mod12)
        var mod13 :Modal = Modal()
        mod13.locationName = "Old Police Academy "
        mod13.latitude = 1.330411000000003
        mod13.longtitude = 103.83913399999999


        listoflocation.add(mod13)
        var mod14 :Modal = Modal()
        mod14.locationName = "Float@Marina Bay"
        mod14.latitude = 1.2881530000000083
        mod14.longtitude = 103.85902799999998


        listoflocation.add(mod14)
        var mod15 :Modal = Modal()
        mod15.locationName = "Penjuru Recreational Club "
        mod15.latitude = 1.28967
        mod15.longtitude = 103.85007


        listoflocation.add(mod15)
        var mod16 :Modal = Modal()
        mod16.locationName = "HTNS Bukit Batok"
        mod16.latitude = 1.3650810000000064
        mod16.longtitude = 103.74584910000003


        listoflocation.add(mod16)
        var mod17 :Modal = Modal()
        mod17.locationName = "Changi T4-CarPark"
        mod17.latitude = 1.28967
        mod17.longtitude = 103.85007


        listoflocation.add(mod17)
        var mod18 :Modal = Modal()
        mod18.locationName = "CSC Changi"
        mod18.latitude = 1.3914146999999935
        mod18.longtitude = 103.98624670000001


        listoflocation.add(mod18)
        var mod19 :Modal = Modal()
        mod19.locationName = "F1 Pit Building"
        mod19.latitude = 1.291363400000008
        mod19.longtitude = 103.863891


        listoflocation.add(mod19)
        var mod20 :Modal = Modal()
        mod20.locationName = "Cochrane Rec Club"
        mod20.latitude = 1.2921806003586802
        mod20.longtitude = 103.85384669999998


        listoflocation.add(mod20)

        var mod21 :Modal = Modal()
        mod21.locationName = "Changi South Lane"
        mod21.latitude = 1.2921416000000139
        mod21.longtitude = 103.8538267


        listoflocation.add(mod21)
        var mod22 :Modal = Modal()
        mod22.locationName = "Turf Club Park"
        mod22.latitude = 1.4220964000000103
        mod22.longtitude = 103.75908039999999


        listoflocation.add(mod22)
        var mod23 :Modal = Modal()
        mod23.locationName = "Terusan Rec Club"
        mod23.latitude = 1.2921416000000139
        mod23.longtitude = 103.8538267


        listoflocation.add(mod23)
        var mod24 :Modal = Modal()
        mod24.locationName = "30 Tuas South "
        mod24.latitude = 1.303140500000009
        mod24.longtitude = 103.6245662


        listoflocation.add(mod24)
        var mod25 :Modal = Modal()
        mod25.locationName = "Former Yishun Bus InterChange"
        mod25.latitude = 1.4299915000000174
        mod25.longtitude = 103.83783210000001


        listoflocation.add(mod25)
        var mod26 :Modal = Modal()
        mod26.locationName = "Sungei Tengah"
        mod26.latitude = 1.3893293737164811
        mod26.longtitude = 103.7289168


        listoflocation.add(mod26)


        var mod27 :Modal = Modal()
        mod27.locationName = "Jurong Town Hall,Annex Building "
        mod27.latitude = 1.331332100000004
        mod27.longtitude = 103.74156969999999


        listoflocation.add(mod27)





        var mod28 :Modal = Modal()
        mod28.locationName = "STIE alJunied AVE"
        mod28.latitude = 1.3203176999999986
        mod28.longtitude = 103.88988600000002


        listoflocation.add(mod28)


        var mod29 :Modal = Modal()
        mod29.locationName = "ASTE STPI Rangrath "
        mod29.latitude = 34.002317
        mod29.longtitude = 74.795319


        listoflocation.add(mod29)

    }


}