package ru.acurresearch.dombyta_new.data.network.interactor

import android.content.Context
import com.google.gson.Gson
import com.hadisatrio.optional.Optional
import com.pixplicity.easyprefs.library.Prefs
import io.reactivex.Single
import ru.acurresearch.dombyta.App.fromJson
import ru.acurresearch.dombyta_new.data.network.Api
import ru.acurresearch.dombyta_new.data.network.model.CashBoxServerData
import ru.acurresearch.dombyta_new.data.network.model.SiteToken

class TokenInteractor(
    private val context: Context,
    private val gson: Gson,
    private val api: Api
) {
    companion object {
        const val KEY_TOKEN = "TOKEN"
    }

    fun updateToken(
        siteToken: SiteToken
    ): Single<CashBoxServerData> =
        api.getToken(siteToken)
            .doOnSuccess { Prefs.putString(KEY_TOKEN, gson.toJson(it)) }

    fun getToken(
    ): Single<Optional<CashBoxServerData>> =
        Single.fromCallable { Prefs.getString(KEY_TOKEN, "") }
            .map {
                if(it != "") Optional.of(gson.fromJson<CashBoxServerData>(it))
                else Optional.absent()
            }
}