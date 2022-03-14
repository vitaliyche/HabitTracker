package com.codeliner.habittracker.billing

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*

class BillingManager(val activity: AppCompatActivity) { //58 инициализируем класс встроенных покупок в приложении
    private var bClient: BillingClient? = null //58 переменная для биллинг клиента

    init { //59 инициализируем биллинг клиента
        setUpBillingClient()
    }

    private fun setUpBillingClient() { //58 настройка биллинг клиента
        bClient = BillingClient.newBuilder(activity) //58 с помощью bClient сможем настроить подключение к плей маркету, чтобы  показывало диалог в приложении, где можно произвести покупку
            .setListener(getPurchaseListener()) //58 слушатель ждет когда реализуется покупка
            .enablePendingPurchases()
            .build()
    }

    private fun savePref(isPurchase: Boolean) { //61 сохранение в память, что произведена покупка
        val pref = activity.getSharedPreferences(MAIN_PREF, Context.MODE_PRIVATE) //61 MODE_PRIVATE - только наше приложение будет иметь доступ к таблице main pref, где хранится была произведена покупка или нет
        val editor = pref.edit()
        editor.putBoolean(REMOVE_ADS_KEY, isPurchase) //61 под ключом REMOVE_ADS_KEY запишется значение true/false
        editor.apply()
    } //61 запускаем из nonConsumableItem

    fun startConnection() { //59 подготовка покупки, вызываем диалог с ценой, названием товара и кнопкой покупки
        bClient?.startConnection(object : BillingClientStateListener {

            override fun onBillingServiceDisconnected() { //59 если мы сделали запрос и он выключился по какой-то причине

            }

            override fun onBillingSetupFinished(p0: BillingResult) { //59 все подключилось, настройка прошла, мы смогли связаться с гугл плей
                getItem() //59 чтобы связаться с плей маркетом, чтоб он выдал наш продукт, цену и мы могли его купить
            }
        })
    }

    private fun getItem() { //59 чтобы связаться с плей маркетом, чтоб он выдал наш продукт, цену и мы могли его купить
        val skuList = ArrayList<String>()
        skuList.add(REMOVE_AD_ITEM)
        val skuDetails = SkuDetailsParams.newBuilder() //59 детали покупки
        skuDetails.setSkusList(skuList).setType(BillingClient.SkuType.INAPP) //59 нужно передать сюда список вариантов покупки (у нас один - встроенная покупка удаление рекламы)
        bClient?.querySkuDetailsAsync(skuDetails.build()) { //59 будем делать асинхронно, на второстепенном потоке, чтобы не тормозить основной поток
            bResult, list ->
            run {
                if (bResult.responseCode == BillingClient.BillingResponseCode.OK) { //59 если все нормально
                    if (list != null) { //list не должен быть нулевым
                        if (list.isNotEmpty()) { //список не пустой
                            val bFlowParams = BillingFlowParams //59 создаем billling flow params для launchBillingFlow
                                .newBuilder()
                                .setSkuDetails(list[0]).build()
                            bClient?.launchBillingFlow(activity, bFlowParams) //59 launchBillingFlow - запускаем диалог для покупки
                        }
                    }
                }
            }
        }
    }

    private fun getPurchaseListener(): PurchasesUpdatedListener { //58 слушатель для bClient
        return PurchasesUpdatedListener { //58 не запустится, пока не реализована покупка
                bResult, list ->
            run { //58 включаем run блок, следуя подсказке
                if (bResult.responseCode == BillingClient.BillingResponseCode.OK) { //58 если результат правильный, слушатель успешно инициализировался и может получать данные
                    list?.get(0)?.let { nonConsumableItem(it) } //59 если список не пустой (не null), только тогда запустится функция
                }// одобряем покупку
            }
        }
    }

    private fun nonConsumableItem(purchase: Purchase) { //58 подтверждение покупки, nonConsumable - единоразовая покупка, насовсем (отключение рекламы)
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) { //58 если покупка подтверждена
            if (!purchase.isAcknowledged) { //58 если покупка еще не одобрена
                val acParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build() //58 передаем параметры покупки в переменную
                bClient?.acknowledgePurchase(acParams) { //58 значит можем подтверждать удаление рекламы
                    if (it.responseCode == BillingClient.BillingResponseCode.OK) { //58 если результат правильный, слушатель успешно инициализировался и может получать данные
                        savePref(true) //61 записываем в память true
                        Toast.makeText(activity, "Thanks for the purchase!", Toast.LENGTH_LONG).show()//61 для проверки что все сработало запустим сообщение toast
                    } else { //если прошло неуспешно
                        savePref(false) //61 записываем в память false
                        Toast.makeText(activity, "Purchase error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    } //после покупки рекламу отключаем из MainActivity

    fun closeConnection() { //60 при выходе закрываем соединение
        bClient?.endConnection()
    }


    companion object {
        const val REMOVE_AD_ITEM = "remove_ad_item_id" //59 название нужно загружать в плей консоль
        const val MAIN_PREF = "main_pref"
        const val REMOVE_ADS_KEY = "remove_ads_key"
    }
}