package com.sopt.smeem.presentation.onboarding

import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.sopt.smeem.R
import com.sopt.smeem.databinding.FragmentSettingTrainingPlanBinding
import com.sopt.smeem.presentation.BindingFragment
import com.sopt.smeem.presentation.onboarding.TrainingPlanType.EVERY
import com.sopt.smeem.presentation.onboarding.TrainingPlanType.FIVE
import com.sopt.smeem.presentation.onboarding.TrainingPlanType.NOT_SELECTED
import com.sopt.smeem.presentation.onboarding.TrainingPlanType.ONE
import com.sopt.smeem.presentation.onboarding.TrainingPlanType.THREE
import com.sopt.smeem.util.ButtonUtil.switchOff
import com.sopt.smeem.util.ButtonUtil.switchOn
import com.sopt.smeem.util.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

enum class TrainingPlanType {
    ONE,
    THREE,
    FIVE,
    EVERY,
    NOT_SELECTED,
    ;

    var id: Int = 0
    var selected: Boolean = false

    companion object {
        fun findById(id: Int) =
            entries.find { it.id == id } ?: throw IllegalStateException()
    }
}

@AndroidEntryPoint
class TrainingPlanSettingFragment :
    BindingFragment<FragmentSettingTrainingPlanBinding>(R.layout.fragment_setting_training_plan) {
    private var _buttons: Map<TrainingPlanType, MaterialButton>? = null
    private val buttons: Map<TrainingPlanType, MaterialButton>
        get() = _buttons ?: emptyMap()

    private val vm: OnBoardingVM by activityViewModels()

    override fun constructLayout() {
        ONE.id = binding.icOnBoardingTrainingPlanButton1.id
        THREE.id = binding.icOnBoardingTrainingPlanButton2.id
        FIVE.id = binding.icOnBoardingTrainingPlanButton3.id
        EVERY.id = binding.icOnBoardingTrainingPlanButton4.id

        _buttons = mapOf(
            ONE to binding.icOnBoardingTrainingPlanButton1,
            THREE to binding.icOnBoardingTrainingPlanButton2,
            FIVE to binding.icOnBoardingTrainingPlanButton3,
            EVERY to binding.icOnBoardingTrainingPlanButton4,
        )

        buttonSelected()
    }

    override fun addListeners() {
        onTouchButtons()
    }

    private fun onTouchButtons() {
        buttons.values.forEach { button ->
            button.setOnSingleClickListener {
                if (NOT_SELECTED != vm.selectedPlan.value) {
                    buttons[vm.selectedPlan.value]?.switchOff() // 기존 off
                    vm.upsertPlanType(TrainingPlanType.findById(button.id))

                    if (vm.selectedPlan.value!!.selected) {
                        button.switchOn() // 새로 On
                    } else {
                        vm.nonePlanType()
                    }

                } else { // 최초 선택
                    button.switchOn()
                    vm.upsertPlanType(TrainingPlanType.findById(button.id))
                }
            }
        }
    }

    private fun buttonSelected() {
        if (vm.selectedPlan.value != ONE) {
            buttons[vm.selectedPlan.value]?.switchOn()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _buttons = null
    }
}