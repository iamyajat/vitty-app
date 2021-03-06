package com.dscvit.vitty.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dscvit.vitty.ui.auth.IntroFragment

class IntroAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val numPages = 3

    override fun getItemCount(): Int = numPages

    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putString("frag_id", position.toString())
        val fragment = IntroFragment()
        fragment.arguments = bundle
        return fragment
    }
}
