package ru.acurresearch.mosdombyt.Activities

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_select_good.*
import ru.acurresearch.mosdombyt.Adapters.PopGoodsListAdapter
import ru.acurresearch.mosdombyt.App.App
import ru.acurresearch.mosdombyt.OrderPostition
import ru.acurresearch.mosdombyt.R
import ru.acurresearch.mosdombyt.ServiceItemCustom
import ru.acurresearch.mosdombyt.Utils.addSeconds
import ru.acurresearch.mosdombyt.Utils.getdate
import java.util.*
import kotlin.collections.ArrayList

import kotlin.math.roundToInt





class SelectPositionActivity : Activity() {
    var currProdInPopUpPrice: ServiceItemCustom? = null
    var currSelectedPostionPrice: Double = 0.0
    var currSelectedPostionExpDate: Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_good)

        initDisplay()
        hidePriceCard()
        hideDeadlineCard()
        hideEditingPostionName()

        initSetPricePopUp()


        createProdsList()


        pop_up_deadline_add_btn.setOnClickListener {
            val calendar = Calendar.getInstance()
            val deadlineDate = Date(datePickerDeadline.year,
                         datePickerDeadline.month,
                         datePickerDeadline.dayOfMonth,
                         timePickerDeadline.currentHour,
                         timePickerDeadline.currentMinute)
            currSelectedPostionExpDate = deadlineDate
            addSelectedPosition()
            finish()
        }





    }
    fun initDeadlineTimePicker(value: ServiceItemCustom){
        val cal = Calendar.getInstance()
        cal.time = Date().addSeconds(value.defExpiresIn ?: 0)
        val hours = cal.get(Calendar.HOUR_OF_DAY)
        val minutes = cal.get(Calendar.MINUTE)

        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)

        timePickerDeadline.setIs24HourView(true)
        timePickerDeadline.currentHour = cal.get(Calendar.HOUR_OF_DAY)
        timePickerDeadline.currentMinute = cal.get(Calendar.MINUTE)

        datePickerDeadline.updateDate(year, month, day)

    }


    fun initSetPricePopUp(){
        add_price_pop_btn.setOnClickListener {

            val txtPrice= adding_price_holder.text.toString()
            if (txtPrice == ""){
                Toast.makeText(this, "Введите цену", Toast.LENGTH_SHORT).show()
            }else {

                hidePriceCard()
                showDeadlineCard()
                showEditingPostionName()
                currSelectedPostionPrice = txtPrice.toDouble()
                //showList()

                //finish()
            }

        }
    }


    fun initDisplay(){
        var dm = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(dm)

        val widht = dm.widthPixels
        val height = dm.heightPixels

        getWindow().setLayout((widht*1.0).roundToInt(), (height*1.0).roundToInt())
    }


    fun addSelectedPosition(){
        var newPositionsList = ArrayList(App.prefs.selectedPositions)

        val newCheckPos = OrderPostition(
            UUID.randomUUID().toString(),
            currProdInPopUpPrice!!,
            1.0,
            currSelectedPostionPrice,
            currSelectedPostionExpDate)
        newPositionsList.add(newCheckPos)
        App.prefs.selectedPositions = newPositionsList
    }


    fun createProdsList(){
        val listItem = App.prefs.allAllowedProducts
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        select_good_list.layoutManager = layoutManager
        var adapter= PopGoodsListAdapter(listItem, this, this@SelectPositionActivity)
        select_good_list.adapter = adapter
    }

    fun hideList(){
        select_good_list.visibility= View.INVISIBLE
        //txt_search_result.visibility = View.INVISIBLE
    }
    fun showList(){
        select_good_list.visibility = View.VISIBLE
    }



    fun hideEditingPostionName(){
        selected_service_item_card.visibility = View.INVISIBLE
    }

    fun showEditingPostionName(){
        selected_service_item_card.visibility = View.VISIBLE
    }


    fun showPriceCard(){
        price_add_card_view.visibility = View.VISIBLE

    }

    fun hidePriceCard(){
        price_add_card_view.visibility = View.INVISIBLE

    }

    fun hideDeadlineCard(){
        order_deadline_card.visibility = View.INVISIBLE
    }

    fun showDeadlineCard(){
        order_deadline_card.visibility = View.VISIBLE
    }

    fun setCardPricePopProps(value: ServiceItemCustom){
        text_price_popup.text = value.name
        adding_price_holder.text = Editable.Factory.getInstance().newEditable(value.defPrice.toString())
    }


}
