package com.dscvit.vitty.ui.instructions

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivityInstructionsBinding
import com.dscvit.vitty.ui.schedule.ScheduleActivity

class InstructionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstructionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instructions)
        binding.doneButton.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}