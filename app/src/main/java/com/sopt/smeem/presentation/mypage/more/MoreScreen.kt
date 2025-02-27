package com.sopt.smeem.presentation.mypage.more

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sopt.smeem.R
import com.sopt.smeem.presentation.compose.components.SmeemDialog
import com.sopt.smeem.presentation.compose.theme.Typography
import com.sopt.smeem.presentation.compose.theme.black
import com.sopt.smeem.presentation.compose.theme.gray100
import com.sopt.smeem.presentation.compose.theme.gray600
import com.sopt.smeem.presentation.mypage.navigation.MoreNavGraph
import com.sopt.smeem.presentation.splash.SplashLoginActivity
import com.sopt.smeem.util.VerticalSpacer

@Composable
fun MoreScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: MoreViewModel = hiltViewModel()
    val context = LocalContext.current

    val (showLogoutDialog, setShowLogoutDialog) = rememberSaveable { mutableStateOf(false) }

    if (showLogoutDialog) {
        SmeemDialog(
            setShowDialog = setShowLogoutDialog,
            title = stringResource(R.string.smeem_dialog_logout_title),
            content = stringResource(R.string.smeem_dialog_logout_content),
            onConfirmButtonClick = {
                viewModel.clearLocal()
                context.startActivity(Intent(context, SplashLoginActivity::class.java))
                (context as? Activity)?.finishAffinity()
            })
    }

    /**** UI ****/
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {

        VerticalSpacer(height = 30.dp)

        Text(
            text = stringResource(R.string.my_page_more_information),
            style = Typography.titleMedium,
            color = black
        )

        VerticalSpacer(height = 9.dp)


        Box(modifier = Modifier
            .clickable {
                CustomTabsIntent
                    .Builder()
                    .build()
                    .run {
                        launchUrl(
                            context, Uri.parse(
                                context.getString(R.string.manual_url)
                            )
                        )
                    }
            }
            .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.my_page_more_manual),
                style = Typography.bodyMedium,
                color = gray600,
            )
        }

        VerticalSpacer(height = 30.dp)

        HorizontalDivider(
            color = gray100,
            thickness = 1.dp,
        )

        VerticalSpacer(height = 30.dp)

        Text(
            text = stringResource(R.string.my_page_more_account_management),
            style = Typography.titleMedium,
            color = black,
        )

        VerticalSpacer(height = 10.dp)

        Box(
            modifier = Modifier
                .clickable { setShowLogoutDialog(true) }
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.my_page_more_logout),
                style = Typography.bodyMedium,
                color = gray600,
            )
        }

        VerticalSpacer(height = 6.dp)

        Box(
            modifier = Modifier
                .clickable {
                    navController.navigate(MoreNavGraph.DeleteAccount.route)
                }
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.my_page_more_delete_account),
                style = Typography.bodyMedium,
                color = gray600,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MoreScreenPreview() {
    MoreScreen(navController = rememberNavController(), modifier = Modifier)
}