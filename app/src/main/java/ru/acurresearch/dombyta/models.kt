package ru.acurresearch.dombyta

import android.app.Activity
import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import ru.acurresearch.dombyta.App.fromJson
import ru.acurresearch.dombyta.Utils.syncOrder
import ru.acurresearch.dombyta.Utils.syncTask
import ru.evotor.framework.core.IntegrationException
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.action.command.open_receipt_command.OpenSellReceiptCommand
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd
import ru.evotor.framework.inventory.ProductItem
import ru.evotor.framework.navigation.NavigationApi
import ru.evotor.framework.receipt.Position
import ru.evotor.framework.receipt.Receipt
import java.io.IOException
import java.math.BigDecimal

import java.util.*
import java.util.ArrayList
import java.util.Date

//question:[answer:, [id, name], id , name,	], poll_name, result, poll_id

data class ApiAnswer(val id: Int, val name: String){
    fun toJson(): String{
        val gson = GsonBuilder().create()
        return gson.toJson(this)
    }



}

data class ApiQuestion(val id: Int, val name: String, val answer:List<ApiAnswer>){
    fun toJson(): String{
        val gson = GsonBuilder().create()
        return gson.toJson(this)
    }



}


data class ApiPoll(val poll_id: Int, val poll_name: String, val result: Boolean, val question: List<ApiQuestion> ){
    fun toJson(): String{
        val gson = GsonBuilder().create()
        return gson.toJson(this)
    }

   // fun toPollModel(): Poll{
    //
    //
     //   var apiQuestions =
   //         question.map { it -> Question(it.id,it.name, it.answer.map {l -> AnswerOption(l.id,l.name ) }) }

   //     return Poll(poll_id, "Давид",poll_name,apiQuestions)
    //}



}



data class ApiGetPollData (val getRandomPoll: Boolean, val checkUUID: String){
    fun toJson(): String{
        val gson = GsonBuilder().create()
        return gson.toJson(this)
    }
}


data class NeedPollResult(val result: Boolean)





data class Check(       @SerializedName("uuid")         val uuid: String,
                        @SerializedName("check_date")   val date: Date,
                        @SerializedName("check_number") val number: String?,
                        @SerializedName("check_pos")    val position: List<CheckPosition>){


    companion object{
        fun fromEvoReceipt(receipt: Receipt): Check{
            val header = receipt.header

            return Check(
                    receipt.header.uuid,
                    receipt.header.date ?: Date()  ,
                    receipt.header.number,
                    receipt.getPositions().map {CheckPosition.fromEvoPosition(it)  })
        }
    }
}
                        //val type: String,
// api fields fields = ('device_id', 'uuid', 'check_date', 'check_number',    'check_pos')




data class CheckPosition(@SerializedName("pos_uuid")     val uuid: String,
                         @SerializedName("product_uuid") val productUUID: String?,
                         @SerializedName("product_name") val name: String,
                         @SerializedName("quantity")     val quantity: Double,
                         @SerializedName("price")        val price: Double){
    fun toEvotorPositionAdd(): PositionAdd{
        return   PositionAdd(Position.Builder.newInstance(
            uuid,
            productUUID,
            name,
            "шт",
            0,
            BigDecimal(quantity),
            BigDecimal(price)
        ).build())
    }

    companion object {
        fun fromEvoPosition(evoPos : Position ): CheckPosition{
            return CheckPosition(evoPos.getUuid(),
                                  evoPos.getProductUuid(),
                                  evoPos.getName(),
                                  evoPos.getQuantity().toDouble(),
                                  evoPos.getPrice().toDouble())
        }
    }


}




data class Client(@SerializedName("name") val name: String?,
                  @SerializedName("phone") val phone: String?)






data class ServiceItemCustom(@SerializedName("uuid") val uuid: String?,
                             @SerializedName("product_uuid") val productUUID: String?,
                             @SerializedName("product_name") val name: String,
                             @SerializedName("default_price") val defPrice: Double?,
                             @SerializedName("default_expires_delta") val defExpiresIn: Double?){
    companion object{
        fun fromEvoProductItem(evoPos : ProductItem , defPrice: Double? = null,defExpiresIn: Double?  ): ServiceItemCustom{
            return ServiceItemCustom(UUID.randomUUID().toString(),evoPos.uuid, evoPos.name, defPrice,defExpiresIn )
        }

    }
}



data class OrderPostition(@SerializedName("uuid")          val uuid: String,
                          @SerializedName("serv_item_full")  val serviceItem: ServiceItemCustom,
                          @SerializedName("quantity")      val quantity: Double,
                          @SerializedName("price")         val price: Double,
                          @SerializedName("product_name")  val productName: String,
                          @SerializedName("expires_in")    val expDate: Date?){

    @SerializedName("serv_item")
    val servItem_uuid = serviceItem.uuid

    fun toEvotorPositionAdd(): PositionAdd{
        return   PositionAdd(Position.Builder.newInstance(
            uuid,
            null,
            serviceItem.name,
            "шт",
            0,
            BigDecimal(price),
            BigDecimal(quantity)
        ).build())
    }
}




data class Order(@SerializedName("id")             val id: Int?,
                 @SerializedName("positions_list") var positionsList: ArrayList<OrderPostition>,
                 @SerializedName("custom_price")   var custom_price: Double?,
                 @SerializedName("client")         var client: Client,
                 @SerializedName("billing_type")   var billType: String,
                 @SerializedName("order_status")   var status: String,
                 @SerializedName("created_at")     var dateCreated: Date?=null,
                 @SerializedName("id_in_store")    var internalId: Int? = null,
                 @SerializedName("in_house_kvitok") var printLabel: String? = null,
                 @SerializedName("clients_kvitok")  var printKvitok: String? = null,
                 @SerializedName("evotor_receipt_uuid") var evoResUuid: String? = null
) {

    @SerializedName("is_paid")
    var isPaid:Boolean = false


    val price: Double
        get() = custom_price ?: positionsList.map { it.price*it.quantity }.sum()


    fun realize(activity: Activity){
        val changes = ArrayList<PositionAdd>()
        var listItem = ArrayList(positionsList.map { it.toEvotorPositionAdd() })
        listItem.forEach { changes.add(it) }

        OpenSellReceiptCommand(changes, null).process(
            activity
        ) { integrationManagerFuture ->
            try {
                val result = integrationManagerFuture.result
                val tmp = result.data
                if (result.type == IntegrationManagerFuture.Result.Type.OK) {
                    //Чтобы открыть другие документы используйте методы NavigationApi.
                    activity.startActivity(NavigationApi.createIntentForSellReceiptEdit())
                        //code review: наличие взаимодействия с ui в логике может 
                        //привести к неожиданным и случайным последствиям
                }
            } catch (e: IOException) {
                e.printStackTrace()
                //code review: желательно обрабатывать исключения с помощью сервисов по типу crashlytics
            } catch (e: IntegrationException) {
                e.printStackTrace()
            }
        }
    }

    fun toJson(): String{
        return GsonBuilder().create().toJson(this)
    }

    //code review: наличие логики (кроме парсинга данных) в моделях нежелательно
    fun suggestAction(): String{
        if (billType == Constants.BillingType.PREPAY){
            if (!isPaid)
                return Constants.OrderSuggestedAction.PAY

            if (status == Constants.OrderStatus.PRE_CREATED)
                return Constants.OrderSuggestedAction.CREATE

            if (status == Constants.OrderStatus.READY)
                return Constants.OrderSuggestedAction.CLOSE

        }

        if (billType == Constants.BillingType.POSTPAY){
            if (status == Constants.OrderStatus.PRE_CREATED)
                return Constants.OrderSuggestedAction.CREATE

            if ((status == Constants.OrderStatus.READY) && (!isPaid))
                return Constants.OrderSuggestedAction.PAY

            if ((status == Constants.OrderStatus.READY) && (isPaid))
                return Constants.OrderSuggestedAction.CLOSE
        }

        if (!isPaid)
            return Constants.OrderSuggestedAction.PAY

        return Constants.OrderSuggestedAction.NOTHING
    }

    fun toTaskList():List<Task>{
        return positionsList.map {Task(0, it.serviceItem.name,it.expDate,internalId,Constants.TaskStatus.NEW) }
    }

    fun close(context: Context){
        status = Constants.OrderStatus.CLOSED
        if (id == null)
            return


        syncOrder(context, this@Order)
    }

    fun setPaid(context: Context, evor_receipt_uuid: String, need_sync: Boolean = true){
        evoResUuid = evor_receipt_uuid
        isPaid = true

        //Создание заказа у нас бывает только по соотвествующей кнопке.
        // Если заказ не был создан (id==null), при оплате мы его не синхронизируем - он будет синхронизирован позже
        if (id == null)
            return
        
        //code review: наличие логики (кроме парсинга данных) в моделях нежелательно
        if (need_sync)
            syncOrder(context, this@Order)

    }



    companion object {
        fun fromJson(inpJson : String ): Order{
            return Gson().fromJson<Order>(inpJson)
        }

        fun empty(): Order{
            return Order(null,
                            ArrayList(listOf<OrderPostition>()),
                null,
                            Client("", ""),
                            Constants.BillingType.PREPAY ,
                            Constants.OrderStatus.PRE_CREATED)
        }
        fun newPrePaid(): Order{
            return Order(null,
                    ArrayList(listOf<OrderPostition>()),
                    null,
                    Client("", ""),
                    Constants.BillingType.PREPAY ,
                    Constants.OrderStatus.PRE_CREATED)
        }

        fun newPostPaid(): Order{
            return Order(null,
                ArrayList(listOf<OrderPostition>()),
                null,
                Client("", ""),
                Constants.BillingType.POSTPAY ,
                Constants.OrderStatus.PRE_CREATED)
        }
    }

}

data class Master(  @SerializedName("id")  val id: Int?,
                    @SerializedName("name") val name: String,
                    @SerializedName("specialization") val specialization: String)


data class Task(@SerializedName("id")  val id: Int,
                @SerializedName("name")  val name: String?,
                @SerializedName("exp_date")  val expDate: Date?,
                @SerializedName("order_internal_id")  val orderInternalId: Int?,
                @SerializedName("status") var status: String,
                @SerializedName("master") var master: Master? = null){

    fun takeInWork(assignedMaster: Master?, context: Context){
        master = assignedMaster
        status = Constants.TaskStatus.IN_WORK
        syncTask(context, this@Task)
    }
    fun finish(context: Context){
        status = Constants.TaskStatus.COMPLETE
        syncTask(context, this@Task)

    }


}





data class SiteToken(val token: String)
data class CashBoxServerData(val token: String,
                             val cashbox_uuid: String){
    val authHeader: String
        get() = "Bearer "+token
}




//val productCode: String,
                               //)
// api fields fields = ('pos_uuid', 'product_uuid', 'product_name', 'quantity', 'price')

data class PhoneNumber(@SerializedName("tel_str") val telStr: String)



object CheckExample{

    val check = Check( "uuid_0097", Date(),"product_name" ,listOf(CheckPosition("pos_uuid_1",
                                                                                                        "product_uuid_2",
                                                                                                        "Milk2.0",
                                                                                                        12.0,
                                                                                                        120.0 )))
}

