package com.sopt.smeem.presentation.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sopt.smeem.R
import com.sopt.smeem.presentation.compose.theme.Typography
import com.sopt.smeem.presentation.compose.theme.black
import com.sopt.smeem.presentation.compose.theme.gray500
import com.sopt.smeem.presentation.compose.theme.point
import com.sopt.smeem.presentation.compose.theme.white
import com.sopt.smeem.util.HorizontalSpacer
import com.sopt.smeem.util.VerticalSpacer
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CoachRoute(
    viewModel: CoachViewModel = hiltViewModel(),
    onClosesClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val state = viewModel.collectAsState().value

    viewModel.collectSideEffect {
        when (it) {
            is CoachSideEffect.NavigateToCoachDetail -> {
                // TODO
            }
        }
    }

    CoachScreen(
        state = state,
        onClosesClick = onClosesClick,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(
    state: CoachState,
    modifier: Modifier = Modifier,
    onClosesClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors().copy(
                    containerColor = white
                ),
                title = {},
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 6.dp),
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    Text(
                        text = "닫기",
                        style = Typography.bodyMedium,
                        color = black,
                        modifier = Modifier
                            .padding(end = 18.dp)
                            .clickable { onClosesClick() }
                    )
                }
            )
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(white)
                .padding(it)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(color = point, shape = RoundedCornerShape(5.dp))
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_crown),
                    contentDescription = "crown",
                    tint = white
                )

                HorizontalSpacer(3.dp)

                Text(
                    text = "하루 한 번 무료 AI 코칭",
                    style = Typography.bodyLarge,
                    color = white
                )
            }

            Text(
                modifier = Modifier.padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 16.dp,
                    bottom = 24.dp
                ),
                text = state.diaryContent,
                color = black,
                style = Typography.bodyMedium.copy(lineHeight = 22.sp)
            )


            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = state.createdAt,
                        style = Typography.labelMedium,
                        color = gray500
                    )
                }

                VerticalSpacer(4.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = state.writerUsername,
                        style = Typography.labelMedium,
                        color = gray500
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CoachScreenPreview() {
    CoachScreen(
        state = CoachState(
            diaryId = 1,
            diaryContent = "I watched Avatar with my boyfriend at Hongdae CGV. I should have skimmed the previous season - Avatar1.. I really couldn’t get what they were saying and the universe(??). What I was annoyed then was 두팔 didn’t know that as me. I think 두팔 who is my boyfriend should study before wathcing…. but Avatar2 is amazing movie I think. In my personal opinion, the jjin main character of Avatar2 is not Sully, but his son.",
            createdAt = "2023년 3월 27일 4:18 PM",
            writerUsername = "유진이"
        ),
        onClosesClick = {}
    )
}
