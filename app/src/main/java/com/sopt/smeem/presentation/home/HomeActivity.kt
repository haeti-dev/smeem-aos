package com.sopt.smeem.presentation.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sopt.smeem.R
import com.sopt.smeem.data.SmeemDataStore.RECENT_DIARY_DATE
import com.sopt.smeem.data.SmeemDataStore.dataStore
import com.sopt.smeem.databinding.ActivityHomeBinding
import com.sopt.smeem.domain.dto.RetrievedBadgeDto
import com.sopt.smeem.event.AmplitudeEventType
import com.sopt.smeem.presentation.EventVM
import com.sopt.smeem.presentation.IntentConstants.DIARY_ID
import com.sopt.smeem.presentation.IntentConstants.RETRIEVED_BADGE_DTO
import com.sopt.smeem.presentation.IntentConstants.SNACKBAR_TEXT
import com.sopt.smeem.presentation.base.BindingActivity
import com.sopt.smeem.presentation.base.DefaultSnackBar
import com.sopt.smeem.presentation.compose.components.Banner
import com.sopt.smeem.presentation.compose.theme.SmeemTheme
import com.sopt.smeem.presentation.detail.DiaryDetailActivity
import com.sopt.smeem.presentation.home.WritingBottomSheet.Companion.TAG
import com.sopt.smeem.presentation.home.calendar.SmeemCalendar
import com.sopt.smeem.presentation.home.calendar.core.CalendarState
import com.sopt.smeem.presentation.home.calendar.core.Period
import com.sopt.smeem.presentation.mypage.MyPageActivity
import com.sopt.smeem.presentation.write.foreign.ForeignWriteActivity
import com.sopt.smeem.presentation.write.natiive.NativeWriteStep1Activity
import com.sopt.smeem.util.DateUtil
import com.sopt.smeem.util.getParcelableArrayListExtraCompat
import com.sopt.smeem.util.getWeekStartDate
import com.sopt.smeem.util.setComposeContent
import com.sopt.smeem.util.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class HomeActivity : BindingActivity<ActivityHomeBinding>(R.layout.activity_home) {
    private lateinit var bs: WritingBottomSheet
    private var backPressedTime: Long = 0

    private val homeViewModel by viewModels<HomeViewModel>()
    private val eventVm: EventVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val calendar = binding.composeCalendar
        val banner = binding.composeBanner
        bs = WritingBottomSheet()
        initView(LocalDate.now())
        setInitListener()

        setComposeContent(calendar) {
            SmeemTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SmeemCalendar(homeViewModel)
                }
            }
        }

        observeBannerState(banner)
        moveToMyPage()
        observeData()
        onTouchWrite()
        eventVm.sendEvent(AmplitudeEventType.HOME_VIEW)
        initBackPressedCallback()
        homeViewModel.activeVisit { Timber.e("visit count 반영 실패. ", it) }
    }

    override fun onResume() {
        super.onResume()
        initView(LocalDate.now())
    }

    override fun onRestart() {
        super.onRestart()
        initView(LocalDate.now())
    }

    private fun onTouchWrite() {
        binding.btnWriteDiary.setOnSingleClickListener {
            bs.show(supportFragmentManager, TAG)
        }
    }

    private fun moveToMyPage() {
        binding.ivMyPage.setOnClickListener {
            startActivity(Intent(this, MyPageActivity::class.java))
        }
    }

    private fun initView(day: LocalDate) {
        lifecycleScope.launch {
            with(homeViewModel) {
                onStateChange(
                    CalendarState.LoadNextDates(
                        startDate = day.minusWeeks(1).getWeekStartDate(),
                        period = Period.WEEK,
                    ),
                )
                onStateChange(CalendarState.SelectDate(date = day))
                updateWriteDiaryButtonVisibility()
            }
        }
        showDiaryCompleted()
        showBadgeDialog()
    }

    private fun showDiaryCompleted() {
        val msg = intent.getStringExtra(SNACKBAR_TEXT)
        if (msg != null) {
            DefaultSnackBar.make(binding.root, msg).show()
            intent.removeExtra(SNACKBAR_TEXT)
        }
    }

    private fun showBadgeDialog() {
        val retrievedBadge =
            intent.getParcelableArrayListExtraCompat<RetrievedBadgeDto>(RETRIEVED_BADGE_DTO)
                ?: emptyList()
        if (retrievedBadge.isNotEmpty()) {
            val badgeList = retrievedBadge.asReversed()
            badgeList.map { badge ->
                supportFragmentManager
                    .beginTransaction()
                    .add(
                        BadgeDialogFragment.newInstance(
                            badge.name,
                            badge.imageUrl,
                            homeViewModel.isFirstBadge,
                        ),
                        "badgeDialog",
                    ).commitAllowingStateLoss()
                with(homeViewModel) {
                    if (isFirstBadge) isFirstBadge = false
                }
            }
            intent.removeExtra(RETRIEVED_BADGE_DTO)
        }
    }

    private suspend fun updateWriteDiaryButtonVisibility() {
        val recentDiaryDateFlow: Flow<String> =
            dataStore.data
                .map { storage ->
                    storage[RECENT_DIARY_DATE] ?: "2023-01-14"
                }

        recentDiaryDateFlow.collect { date ->
            val recent = DateUtil.asLocalDate(date)
            val isTodaySelected = homeViewModel.selectedDate.value == LocalDate.now()
            val isTodayDiaryWritten = recent == LocalDate.now()

            binding.btnWriteDiary.visibility =
                if (isTodaySelected && !isTodayDiaryWritten) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun setInitListener() {
        binding.clDiaryList.setOnSingleClickListener {
            Intent(this, DiaryDetailActivity::class.java)
                .apply {
                    putExtra(DIARY_ID, homeViewModel.diaryList.value?.id)
                }.run(::startActivity)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            homeViewModel.selectedDate.collectLatest {
                updateWriteDiaryButtonVisibility()
            }
        }
        // 홈에 일기 띄우는 로직
        homeViewModel.diaryList.observe(this) {
            val timeFormatter = DateTimeFormatter.ofPattern("h : mm a", Locale.ENGLISH)

            with(homeViewModel.diaryList.value) {
                if (this == null) {
                    binding.clDiaryList.visibility = View.GONE
                    binding.clNoDiary.visibility = View.VISIBLE
                } else {
                    binding.clDiaryList.visibility = View.VISIBLE
                    binding.clNoDiary.visibility = View.GONE
                    binding.tvDiaryWritenTime.text = this.createdAt.format(timeFormatter)
                    binding.tvDiary.text = this.content
                }
            }
        }
    }

    private fun initBackPressedCallback() {
        val onBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (System.currentTimeMillis() - backPressedTime >= BACK_PRESSED_INTERVAL) {
                        backPressedTime = System.currentTimeMillis()
                        Snackbar
                            .make(
                                binding.root,
                                getString(R.string.notice_back_process),
                                Snackbar.LENGTH_SHORT,
                            ).show()
                    } else {
                        finish()
                    }
                }
            }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun observeBannerState(bannerView: ComposeView) {
        lifecycleScope.launch {
            homeViewModel.configInfo
                .combine(homeViewModel.isBannerVisible) { configInfo, isVisible ->
                    Pair(configInfo, isVisible)
                }.collect { (configInfo, isVisible) ->
                    setComposeContent(bannerView) {
                        SmeemTheme {
                            if (isVisible) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.background,
                                ) {
                                    Banner(
                                        title = configInfo.bannerTitle,
                                        content = configInfo.bannerContent,
                                        onBannerClick = {
                                            handleBannerClickEvent(configInfo)

                                            eventVm.sendEvent(
                                                AmplitudeEventType.BANNER_CLICK,
                                                mapOf(AmplitudeEventType.BANNER_CLICK.propertyKey.toString() to true)
                                            )
                                        },
                                        onBannerClose = {
                                            homeViewModel.closeBanner()

                                            eventVm.sendEvent(
                                                AmplitudeEventType.BANNER_X,
                                                mapOf(AmplitudeEventType.BANNER_CLICK.propertyKey.toString() to true)
                                            )
                                        },
                                        modifier =
                                        Modifier.padding(
                                            horizontal = 18.dp,
                                            vertical = 12.dp,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    /**
     * 배너 클릭 이벤트 처리
     * 외부 이벤트일 시 크롬 탭으로 URL 열기
     * 내부 이벤트일 시 내부 화면으로 이동
     */
    private fun handleBannerClickEvent(configInfo: ConfigInfo) {
        if (configInfo.isExternalEvent) {
            CustomTabsIntent.Builder().build().run {
                launchUrl(this@HomeActivity, Uri.parse(configInfo.bannerEventPath))
            }
        } else {
            when (configInfo.bannerEventPath) {
                // 한국어 일기 작성
                "native_write_diary" -> {
                    startActivity(Intent(this, NativeWriteStep1Activity::class.java))
                }
                // 영어 일기 작성
                "foreign_write_diary" -> {
                    startActivity(Intent(this, ForeignWriteActivity::class.java))
                }
                // 마이페이지 - 성과 요약
                "my_page" -> {
                    startActivity(Intent(this, MyPageActivity::class.java))
                }
            }
        }
    }

    companion object {
        const val BACK_PRESSED_INTERVAL = 2000
    }
}
