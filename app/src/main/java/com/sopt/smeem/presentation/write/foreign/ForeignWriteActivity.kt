package com.sopt.smeem.presentation.write.foreign

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import com.sopt.smeem.R
import com.sopt.smeem.databinding.ActivityForeignWriteBinding
import com.sopt.smeem.event.AmplitudeEventType
import com.sopt.smeem.presentation.EventVM
import com.sopt.smeem.presentation.IntentConstants.DIARY_CONTENT
import com.sopt.smeem.presentation.IntentConstants.DIARY_ID
import com.sopt.smeem.presentation.IntentConstants.RETRIEVED_BADGE_DTO
import com.sopt.smeem.presentation.IntentConstants.SNACKBAR_TEXT
import com.sopt.smeem.presentation.base.BindingActivity
import com.sopt.smeem.presentation.base.DefaultSnackBar
import com.sopt.smeem.presentation.coach.CoachActivity
import com.sopt.smeem.presentation.write.Constant.tooltipHasNeverChecked
import com.sopt.smeem.util.TooltipUtil.createTopicTooltip
import com.sopt.smeem.util.hideKeyboard
import com.sopt.smeem.util.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.Serializable

@AndroidEntryPoint
class ForeignWriteActivity :
    BindingActivity<ActivityForeignWriteBinding>(R.layout.activity_foreign_write) {

    private val viewModel by viewModels<ForeignWriteViewModel>()
    private val eventVm: EventVM by viewModels()

    override fun constructLayout() {
        getTooltipChecked()
        with(binding) {
            // databinding
            vm = viewModel
            lifecycleOwner = this@ForeignWriteActivity
            // ui
            etForeignWrite.requestFocus()
            layoutForeignWriteRandomTopic.layoutSection.visibility = View.GONE
            showTooltip(lifecycleOwner)
        }
    }

    override fun addListeners() {
        goBackToHome()
        setTopicVisibility()
        refreshTopic()
        completeDiary()
    }

    override fun addObservers() {
        checkDiary()
    }

    private fun showTooltip(owner: LifecycleOwner?) {
        if (tooltipHasNeverChecked) {
            binding.layoutForeignWriteBottomToolbar.cbRandomTopic.createTopicTooltip(
                this@ForeignWriteActivity, owner
            )
        }
    }

    private fun goBackToHome() {
        binding.layoutForeignWriteToolbar.tvCancel.setOnSingleClickListener {
            saveToolTipStatus()
            finish()
        }
    }

    private fun setTopicVisibility() {
        with(binding) {
            layoutForeignWriteBottomToolbar.cbRandomTopic.setOnCheckedChangeListener { _, isChecked ->
                when (isChecked) {
                    true -> {
                        setRandomTopic()
                        layoutForeignWriteRandomTopic.layoutSection.visibility = View.VISIBLE
                    }

                    false -> {
                        viewModel.topicId = -1
                        layoutForeignWriteRandomTopic.layoutSection.visibility = View.GONE
                        viewModel.topic.value = ""
                    }
                }
            }
        }
    }

    private fun refreshTopic() {
        binding.layoutForeignWriteRandomTopic.btnRefresh.setOnSingleClickListener {
            setRandomTopic()
        }
    }

    private fun setRandomTopic() {
        viewModel.getRandomTopic { t ->
            Toast.makeText(this@ForeignWriteActivity, t.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun completeDiary() {
        binding.layoutForeignWriteToolbar.tvDone.setOnSingleClickListener {
            saveToolTipStatus()
            when (viewModel.isValidDiary.value) {
                true -> {
                    hideKeyboard(currentFocus ?: View(this))
                    viewModel.uploadDiary(
                        onSuccess = { diaryId, badgeList ->
                            Intent(this, CoachActivity::class.java).apply {
                                putExtra(DIARY_ID, diaryId)
                                putExtra(RETRIEVED_BADGE_DTO, badgeList as Serializable)
                                putExtra(
                                    SNACKBAR_TEXT,
                                    resources.getString(R.string.diary_write_done_message)
                                )
                                putExtra(DIARY_CONTENT, viewModel.diary.value)
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }.run(::startActivity)
                        },
                        onError = { t ->
                            Timber.e(t)
                            Toast.makeText(this, t.message, Toast.LENGTH_SHORT).show()
                        }
                    )
                    eventVm.sendEvent(AmplitudeEventType.DIARY_COMPLETE)
                }

                else -> {
                    DefaultSnackBar.makeOnTopOf(
                        binding.root,
                        R.id.layout_foreign_write_bottom_toolbar,
                        "외국어를 포함해 일기를 작성해 주세요 :("
                    ).show()
                }
            }
        }
    }

    private fun checkDiary() {
        viewModel.isValidDiary.observe(this) {
            when (it) {
                true -> {
                    binding.layoutForeignWriteToolbar.tvDone.setTextColor(
                        resources.getColor(R.color.point, null)
                    )
                }

                false -> {
                    binding.layoutForeignWriteToolbar.tvDone.setTextColor(
                        resources.getColor(R.color.gray_300, null)
                    )
                }
            }
        }
    }

    private fun getTooltipChecked() {
        tooltipHasNeverChecked = viewModel.neverClickedRandomToolTip()
    }

    private fun saveToolTipStatus() {
        if (!tooltipHasNeverChecked) {
            viewModel.randomTopicTooltipOff()
        }
    }
}
