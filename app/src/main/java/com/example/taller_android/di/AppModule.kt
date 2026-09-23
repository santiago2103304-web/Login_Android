package com.example.taller_android.di

import android.content.Context
import androidx.room.Room
import com.example.taller_android.data.TaskDao
import com.example.taller_android.data.TaskDatabase
import com.example.taller_android.data.TaskRepositoryImpl
import com.example.taller_android.domain.TaskRepository
import com.example.taller_android.domain.TaskUseCases
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias con Hilt para proveer las instancias principales de la app.
 *
 * @author Santiago
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideTaskDatabase(@ApplicationContext context: Context): TaskDatabase =
        Room.databaseBuilder(context, TaskDatabase::class.java, "task_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTaskDao(db: TaskDatabase): TaskDao = db.taskDao

    @Provides
    @Singleton
    fun provideTaskRepository(dao: TaskDao, firestore: FirebaseFirestore): TaskRepository =
        TaskRepositoryImpl(dao, firestore)

    @Provides
    @Singleton
    fun provideTaskUseCases(repository: TaskRepository): TaskUseCases = TaskUseCases(repository)
}
