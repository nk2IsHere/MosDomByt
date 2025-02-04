package ru.acurresearch.dombyta.Utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.acurresearch.dombyta.App.App
import ru.acurresearch.dombyta.Order
import ru.acurresearch.dombyta.Task
import java.util.*

//code review: можно использовать расширение функционала (extension functions) Order.syncOrder(context)
fun syncOrder(context: Context, order: Order){
    fun onSuccess(resp_data: Order){
        Toast.makeText(context, "Статус заказа изменен", Toast.LENGTH_SHORT).show()
    }

    order.dateCreated = order.dateCreated ?: Date()
    var call : Call<Order>? = null
    if (order.id != null){
        call = App.api.syncOrderStatus(order, order.id, App.prefs.cashBoxServerData.authHeader)
    }
    else{
        call = App.api.sendOrder(order, App.prefs.cashBoxServerData.authHeader)
    }
    call.enqueue(object : Callback<Order> {
        override fun onResponse(call: Call<Order>, response: Response<Order>) {
            Log.e("processServerRquests",response.errorBody().toString() )
            if (response.isSuccessful)
                onSuccess(response.body()!!)
            else
                //code review: наличие взаимодействия с ui здесь может привести к спагетти-коду
                Toast.makeText(context,"Ошибка на сервере. Мы устраняем проблему. Повторите позже.", Toast.LENGTH_LONG).show()
        }
        override fun onFailure(call: Call<Order>, t: Throwable) {
            Toast.makeText(context,"Проверьте подключение к интернету!", Toast.LENGTH_LONG).show()
        }
    })
}


//code review: можно использовать расширение функционала (extension functions) Order.syncOrder(context)
fun syncTask(contex: Context, task: Task){
    fun onSuccess(resp_data: Task){
        Toast.makeText(contex, "Статус заказа изменен", Toast.LENGTH_SHORT).show()
    }

    val call = App.api.syncServerTask(task, task.id, App.prefs.cashBoxServerData.authHeader)
    call.enqueue(object : Callback<Task> {
        override fun onResponse(call: Call<Task>, response: Response<Task>) {
            Log.e("processServerRquests",response.errorBody().toString() )
            if (response.isSuccessful)
                onSuccess(response.body()!!)
            else
                //code review: наличие взаимодействия с ui здесь может привести к спагетти-коду
                Toast.makeText(contex,"Ошибка на сервере. Мы устраняем проблему. Повторите позже.", Toast.LENGTH_LONG).show()
        }
        override fun onFailure(call: Call<Task>, t: Throwable) {
            Toast.makeText(contex,"Ошибка! Проверьте подключение к интернету!", Toast.LENGTH_LONG).show()
        }
    })
}
