package com.example.divisaproviderapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.divisaproviderapp.DivisaApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ActualizarDivisasWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository by lazy {
        (applicationContext as DivisaApplication).repository
    }

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                repository.sincronizarDivisas()
            }
            Log.d("ActualizarDivisasWorker", "Sincronización exitosa (cada 15 min).")
            Result.success()
        } catch (e: HttpException) {
            Log.e("ActualizarDivisasWorker", "HttpException: ${e.message}", e)
            Result.retry()
        } catch (e: Exception) {
            Log.e("ActualizarDivisasWorker", "Error: ${e.message}", e)
            Result.failure()
        }
    }
}
