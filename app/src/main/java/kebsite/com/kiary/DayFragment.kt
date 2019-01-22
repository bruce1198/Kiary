package kebsite.com.kiary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shashank.sony.fancydialoglib.Animation
import com.shashank.sony.fancydialoglib.FancyAlertDialog
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import java.time.LocalDate


private const val ARG_PARAM1 = "param1"

class DayFragment : Fragment(), MyRecyclerViewAdapter.OnRecyclerItemClickListener {

    companion object {
        private const val PERMISSION_ALL = 1
        private val permissions : Array<String> = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        fun newInstance(d: Int): DayFragment {

            val fragment = DayFragment()
            val args = Bundle()
            args.putInt(ARG_PARAM1, d)
            fragment.arguments = args
            return fragment
        }
    }

    private var d: Int = 0
    private var diaries : ArrayList<Map<String, String>> = ArrayList()
    private var rv: RecyclerView? = null
    private var hurryTxt: TextView? = null
    private var mCurrentPhotoPath: String? = null
    private var myAdapter : MyRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            d = arguments!!.getInt(ARG_PARAM1)
        }
    }

    override fun onRecyclerItemClick(position: Int, id: Int, type: Int) {
        if(type==1) {
            deleteDialog(position, id)
        }
    }

    override fun onRecyclerEditClick(date: String, id: Int, type: String, content: String, mCurrentPhotoPath: String) {

        val i = when(type) {
            "diary" -> Intent(this.activity, EditActivity::class.java)
            "reminder" -> Intent(this.activity, ReminderActivity::class.java)
            else -> Intent(this.activity, EditActivity::class.java)
        }
        val b = Bundle()
        b.putBoolean("modify", true)
        b.putString("date", date)
        b.putString("content", content)
        b.putString("picture", mCurrentPhotoPath)
        b.putString("delete", date+id)
        i.putExtras(b)
        context?.startActivity(i)
    }

    private fun deleteDialog(position: Int, id: Int) {

        FancyAlertDialog.Builder(activity)
            .setTitle("Delete")
            .setBackgroundColor(Color.parseColor("#e5c6a8"))  //Don't pass R.color.colorvalue
            .setMessage("Do you really want to delete ?")
            .setNegativeBtnText("Cancel")
            .setPositiveBtnBackground(Color.parseColor("#f84a48"))  //Don't pass R.color.colorvalue
            .setPositiveBtnText("Delete")
            .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
            .setAnimation(Animation.POP)
            .isCancellable(true)
            .OnPositiveClicked {
                myAdapter?.remove(position, id)
            }
            .OnNegativeClicked {

            }
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_day, container, false)
        rv = root.findViewById(R.id.rv)
        rv!!.layoutManager = LinearLayoutManager(context)
        rv!!.itemAnimator = SlideInLeftAnimator()
        hurryTxt = root.findViewById(R.id.hurry_txt)
        getPermissions()
        return root
    }

    override fun onResume() {
        addDiaries()
        super.onResume()
    }

    private fun addDiaries() {
        diaries.clear()
        val day = d - 7000
        val date = LocalDate.now().plusDays(day.toLong())
        Log.d("date", date.toString())
        val sp = context?.getSharedPreferences("diaryData", Activity.MODE_PRIVATE)
        var x:Int
        val id = sp?.getInt(date.toString()+"id", -1)
        x = id!!
        while(x>=0) {
            val content = sp.getString(date.toString()+x, "noData")
            Log.d("get", date.toString()+x)
            Log.d("get", content)
            mCurrentPhotoPath = sp.getString(date.toString()+"picture"+x, "noPicture")
            val time = sp.getString(date.toString()+"editTime"+x, "")
            val type = sp.getString(date.toString()+"type"+x, "")
            if(content != "noData") {
                Log.d("content", content)
                val map = mapOf(
                    "content" to content!!,
                    "picture" to mCurrentPhotoPath!!,
                    "id" to x.toString(),
                    "time" to time,
                    "type" to type)
                diaries.add(map)
            }
            x--
        }
        if(diaries.size==0)
            hurryTxt?.text = resources.getString(R.string.hurry)
        else
            hurryTxt?.text = ""
        rv?.layoutManager = LinearLayoutManager(context)
        myAdapter = MyRecyclerViewAdapter(diaries, date.toString(), context!!, this)
        rv?.adapter = myAdapter
    }

    private fun getPermissions() {

        if(!hasPermissions(context!!, permissions)){
            ActivityCompat.requestPermissions(activity!!, permissions, PERMISSION_ALL)
        }
        else {
            addDiaries()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_ALL -> {
                for(permission in permissions) {
                    Log.d("request", permission)
                }
                for(result:Int in grantResults) {
                    Log.d("get", result.toString())
                    if(result != PackageManager.PERMISSION_GRANTED)
                        return
                }
                addDiaries()
            }
        }
    }
    private fun hasPermissions(context: Context, permissions: Array<String>) : Boolean{
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}

