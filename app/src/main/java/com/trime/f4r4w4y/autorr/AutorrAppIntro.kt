package com.trime.f4r4w4y.autorr

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType

class AutorrAppIntro : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(
            AppIntroFragment.newInstance(
                title = "Selamat datang di AutoRR !",
                description = "Silahkan baca terlebih dahulu cara untuk menggunakan aplikasinya dihalaman selanjutnya, lalu login kedalam aplikasi menggunakan password yang diberikan di https://autorr.justak.id",
                imageDrawable = R.drawable.animated_ic_logo,
                titleColor = ContextCompat.getColor(applicationContext, R.color.black),
                descriptionColor = ContextCompat.getColor(applicationContext, R.color.black),
                backgroundColor = ContextCompat.getColor(applicationContext, R.color.page1)
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = "Tidur terlentang",
                description = "Carilah dataran yang datar (misal dikasur), lalu tidur terlentang, dan taruh hp anda diatas badan bagian diafragma",
                imageDrawable = R.drawable.page2,
                backgroundColor = ContextCompat.getColor(applicationContext, R.color.page2)
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = "Mulai akusisi data",
                description = "Klik tombol start untuk memulai akusisi data, dan disaat yang bersamaan hitung secara manual jumlah pernapasan yang anda lakukan (terhitung satu saat masuk udara lalu keluar udara)",
                imageDrawable = R.drawable.page3,
                backgroundColor = ContextCompat.getColor(applicationContext, R.color.page3)
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = "Bangun dan tunggu hasil",
                description = "Jika sudah bunyi sebuah suara dari aplikasi, maka anda diperbolehkan untuk bangun (dan ingat hasil perhitungan pernapasan manual), setela itu anda cukup tunggu untuk beberapa saat, lalu isi data hasil perhitungan manual ke tempat yang sudah disediakan pada aplikasi",
                imageDrawable = R.drawable.page4,
                backgroundColor = ContextCompat.getColor(applicationContext, R.color.page4)
            )
        )

        setTransformer(
            AppIntroPageTransformerType.Parallax(
                titleParallaxFactor = 1.0,
                imageParallaxFactor = -1.0,
                descriptionParallaxFactor = 2.0
            )
        )

        isIndicatorEnabled = true
        isWizardMode = true
        setImmersiveMode()

        setIndicatorColor(
            selectedIndicatorColor = ContextCompat.getColor(applicationContext, R.color.teal_700),
            unselectedIndicatorColor = ContextCompat.getColor(applicationContext, R.color.teal_200)
        )
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val editor = getSharedPreferences("autorr_pref", MODE_PRIVATE).edit()
        editor.putBoolean("first_start", false)
        editor.apply()

        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }
}