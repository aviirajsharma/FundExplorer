package com.avirajsharma.fundexplorer.di

import android.content.Context
import androidx.room.Room
import com.avirajsharma.fundexplorer.data.api.MFApi
import com.avirajsharma.fundexplorer.data.local.FundDatabase
import com.avirajsharma.fundexplorer.data.local.WatchlistDao
import com.avirajsharma.fundexplorer.data.repository.FundRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideMFApi(client: OkHttpClient): MFApi {
        return Retrofit.Builder()
            .baseUrl(MFApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(MFApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FundDatabase {
        return Room.databaseBuilder(
            context,
            FundDatabase::class.java,
            "fund_explorer_db"
        ).build()
    }

    @Provides
    fun provideWatchlistDao(database: FundDatabase): WatchlistDao {
        return database.watchlistDao()
    }

    @Provides
    @Singleton
    fun provideFundRepository(api: MFApi, dao: WatchlistDao): FundRepository {
        return FundRepository(api, dao)
    }
}
