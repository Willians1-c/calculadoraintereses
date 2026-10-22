package com.upchiapas.kt_template

import android.app.Application
import com.upchiapas.kt_template.quiz.di.QuizModule

class KtApplication : Application() {
    val quizModule by lazy {
        QuizModule()
    }
}
