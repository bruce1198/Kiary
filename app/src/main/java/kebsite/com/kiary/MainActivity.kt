package kebsite.com.kiary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.nshmura.recyclertablayout.RecyclerTabLayout
import java.time.DayOfWeek
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TIME_INTERVAL = 2000
    }
    private var dayVp: ViewPager? = null
    private var mDrawerLayout: DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        getXML()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout?.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getXML() {
        //Log.d("data", sp.getString("2019-01-120", "no data"))
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        mDrawerLayout = findViewById(R.id.mDrawerLayout)
        dayVp = findViewById(R.id.day_vp)
        val myAdapter = DayPagerAdapter(supportFragmentManager)
        val rcyTL = findViewById<RecyclerTabLayout>(R.id.rcy_tl)
        dayVp?.adapter = myAdapter
        dayVp?.currentItem = 7000
        rcyTL.setUpWithViewPager(dayVp)
        val fabMenu = findViewById<FloatingActionMenu>(R.id.menu_fab)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        //fabMenu.addMenuButton(fab)
        fab.setOnClickListener {
            val position = dayVp?.currentItem
            val day = position!! - 7000
            val date = LocalDate.now().plusDays(day.toLong())
            val i = Intent(this, EditActivity::class.java)
            val b = Bundle()
            b.putString("date", date.toString())
            b.putInt("number", position)
            i.putExtras(b)
            fabMenu.close(true)
            startActivityForResult(i, 5487)
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up )
        }
        val fabRemind = findViewById<FloatingActionButton>(R.id.fab_work)
        fabRemind.setOnClickListener {
            val position = dayVp?.currentItem
            val day = position!! - 7000
            val date = LocalDate.now().plusDays(day.toLong())
            val i = Intent(this, ReminderActivity::class.java)
            val b = Bundle()
            b.putString("date", date.toString())
            b.putInt("number", position)
            i.putExtras(b)
            fabMenu.close(true)
            startActivityForResult(i, 5487)
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up )
        }
        val calenderTxt = findViewById<TextView>(R.id.calender_txt)
        calenderTxt.setOnClickListener{
            startActivity(Intent(this, CalenderActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 5487) {
            if(resultCode == 200) {
                //val position = data?.extras?.getInt("number")
                //dayVp?.currentItem = position!!
            }
            else if(resultCode == 404)
                Log.d("error", "oops")
        }
    }
    inner class DayPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return DayFragment.newInstance(position)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val day = position - 7000
            val date = LocalDate.now().plusDays(day.toLong())
            val dayOfWeek = parseWeekDay(date.dayOfWeek)
            return date.toString() + " ($dayOfWeek)"
        }

        override fun getCount(): Int {
            return Int.MAX_VALUE
        }

        private fun parseWeekDay(dayOfWeek: DayOfWeek): String {
            return dayOfWeek.toString().substring(0, 3)
        }
    }

    private var mBackPressed: Long = 0

    override fun onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            Toast.makeText(baseContext, "點擊兩次返回鍵以退出", Toast.LENGTH_SHORT).show()
        }

        mBackPressed = System.currentTimeMillis()
    }
}
