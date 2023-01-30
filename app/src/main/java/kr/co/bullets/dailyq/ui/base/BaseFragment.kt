package kr.co.bullets.dailyq.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.api.ApiService

abstract class BaseFragment : Fragment() {

    val api: ApiService by lazy {
        ApiService.getInstance()
    }
}