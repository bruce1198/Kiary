package kebsite.com.kiary

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.widget.LinearLayout
import androidx.core.content.ContextCompat




class MyRecyclerViewAdapter(
    private val items: ArrayList<Map<String, String>>,
    private val date: String,
    private val context: Context,
    private val onRecyclerItemClickListener: OnRecyclerItemClickListener) : RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder>() {

    interface OnRecyclerItemClickListener{
        fun onRecyclerItemClick(position: Int, id: Int, type: Int)
        fun onRecyclerEditClick(date: String, id: Int, type: String, content: String, mCurrentPhotoPath: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.diary_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setMyContent(items[position]["content"]!!)
        holder.setTitleType(items[position]["type"]!!)
        holder.setTime(items[position]["time"]!!)
        val mCurrentPhotoPath = items[position]["picture"]
        val id = items[position]["id"]
        holder.setId(id!!.toInt())
        holder.setDate(date)
        if(mCurrentPhotoPath != "noPicture") {
            holder.setPic(mCurrentPhotoPath!!)
        }
    }

    fun remove(position: Int, id: Int) {
        items.removeAt(position)
        val sp = context.getSharedPreferences("diaryData", Activity.MODE_PRIVATE)
        //Log.d("p", date+id)
        //Log.d("p", sp.getString(date+id, "noData"))
        if(sp.edit().remove(date+id).commit())
            notifyItemRemoved(position)
    }

    /*fun add(text: Map<String, String>, position: Int) {
        items.add(text)
        notifyItemInserted(position)
    }*/

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var contentTxt: TextView? = null
        private var editBtn:Button? = null
        private var deleteBtn:Button? = null
        private var editTime:TextView? = null
        private var typeTxt:TextView? = null
        private var titleLayout: LinearLayout? = null
        private var mCurrentPhotoPath: String? = null
        private var date: String? = null
        private var id:Int? = null
        private var type: String? = null
        private var content: String? = null
        init {
            titleLayout = itemView.findViewById(R.id.title_layout)
            contentTxt = itemView.findViewById(R.id.list_item_title)
            typeTxt = itemView.findViewById(R.id.type_txt)
            editTime = itemView.findViewById(R.id.list_item_time)
            editBtn = itemView.findViewById(R.id.edit_btn)
            editBtn!!.setOnClickListener {
                onClick(it)
            }
            deleteBtn = itemView.findViewById(R.id.delete_btn)
            deleteBtn!!.setOnClickListener {
                onClick(it)
            }
        }
        private val mImageView = itemView.findViewById<ImageView>(R.id.list_item_image)
        fun setTitleType(type: String) {
            typeTxt?.text = type
            //Log.d("type", "type")
            this.type = type
            when(type) {
                "diary" -> titleLayout?.background = ContextCompat.getDrawable(itemView.context, R.color.m2)
                "reminder" -> titleLayout?.background = ContextCompat.getDrawable(itemView.context, R.color.blue)
                else -> titleLayout?.background = ContextCompat.getDrawable(itemView.context, R.color.gray)
            }
        }
        fun setMyContent(content: String) {
            contentTxt?.text = content
            this.content = content
        }
        fun setTime(time: String) {
            val msg:String = itemView.context.resources.getString(R.string.last_update) + time
            editTime?.text = msg
        }
        fun setId(id: Int) {
            this.id = id
        }
        fun setDate(date: String) {
            this.date = date
        }
        fun setPic(mCurrentPhotoPath: String) {
            // Get the dimensions of the View
            this.mCurrentPhotoPath = mCurrentPhotoPath
            val metrics = itemView.context.resources.displayMetrics
            val targetW = (100 * metrics.density).toInt()
            val targetH = (100 * metrics.density).toInt()


            val bmOptions = BitmapFactory.Options().apply {
                // Get the dimensions of the bitmap
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(mCurrentPhotoPath, this)
                val photoW: Int = outWidth
                val photoH: Int = outHeight

                // Determine how much to scale down the image
                val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

                // Decode the image file into a Bitmap sized to fill the View
                inJustDecodeBounds = false
                inSampleSize = scaleFactor
                //inPurgeable = true
            }
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)?.also { bitmap ->
                mImageView?.setImageBitmap(bitmap)
            }
        }
        override fun onClick(view: View?) {
            when(view?.id) {
                R.id.delete_btn -> onRecyclerItemClickListener.onRecyclerItemClick(adapterPosition, id!!, 1)
                R.id.edit_btn -> {
                    if(mCurrentPhotoPath==null)
                        mCurrentPhotoPath = "noPicture"
                    Log.d("null", content)
                    onRecyclerItemClickListener.onRecyclerEditClick(date!!, id!!, type!!, content!!, mCurrentPhotoPath!!)
                }
            }
        }
    }
}