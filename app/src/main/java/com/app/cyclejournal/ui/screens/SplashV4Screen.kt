package com.app.cyclejournal.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.ui.theme.AppType
import com.app.cyclejournal.ui.theme.BrandEnd
import com.app.cyclejournal.ui.theme.BrandGradient
import com.app.cyclejournal.ui.theme.Ink
import com.app.cyclejournal.ui.theme.Ink2
import com.app.cyclejournal.ui.theme.Line
import com.app.cyclejournal.ui.theme.OnBrand
import com.app.cyclejournal.ui.theme.Paper
import com.app.cyclejournal.ui.theme.ShadowBrandSoft
import kotlinx.coroutines.delay

private val SplashTitleStyle = TextStyle(fontFamily = AppType.Display, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
private val SplashBodyStyle = TextStyle(fontFamily = AppType.Ui, fontSize = 15.sp, lineHeight = 23.sp)
private val SplashButtonStyle = TextStyle(fontFamily = AppType.Ui, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)

private data class SplashSlide(val imageRes: Int, val titleRes: Int, val bodyRes: Int)

/** How long each onboarding slide stays on screen before the carousel advances itself. */
private const val SPLASH_AUTO_ADVANCE_MS = 4_000L

/**
 * First-run onboarding — CycleJournal v4 design (`Splash Slide 1..3`, ID + EN).
 *
 * Three illustrated slides with the pagination dots from the design; the primary
 * action starts a fresh profile and the secondary one hands off to the backup
 * restore picker.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SplashV4Screen(
    onNewUser: () -> Unit,
    onRestore: () -> Unit
) {
    val slides = listOf(
        SplashSlide(R.drawable.v4_splash_1, R.string.v4_splash1_title, R.string.v4_splash1_body),
        SplashSlide(R.drawable.v4_splash_2, R.string.v4_splash2_title, R.string.v4_splash2_body),
        SplashSlide(R.drawable.v4_splash_3, R.string.v4_splash3_title, R.string.v4_splash3_body)
    )
    val pagerState = rememberPagerState(pageCount = { slides.size })

    // The carousel plays on its own, and every manual swipe simply resets the timer for the page
    // the user landed on - an onboarding nobody has to drag to read.
    LaunchedEffect(pagerState.currentPage) {
        delay(SPLASH_AUTO_ADVANCE_MS)
        pagerState.animateScrollToPage((pagerState.currentPage + 1) % slides.size)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .testTag("screen_onboarding"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("onboarding_pager")
        ) { page ->
            val slide = slides[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(slide.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(410.dp)
                )
                Spacer(Modifier.height(40.dp))
                Text(
                    text = stringResource(slide.titleRes),
                    style = SplashTitleStyle,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .padding(horizontal = 12.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(slide.bodyRes),
                    style = SplashBodyStyle,
                    color = Ink2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 310.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .widthIn(max = 310.dp)
                .fillMaxWidth()
                .height(56.dp)
                .shadow(24.dp, CircleShape, ambientColor = ShadowBrandSoft, spotColor = ShadowBrandSoft)
                .clip(CircleShape)
                .background(BrandGradient)
                .clickable(onClick = onNewUser)
                .testTag("onboarding_new_user"),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.v4_splash_new_user), style = SplashButtonStyle, color = OnBrand)
        }

        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .widthIn(max = 310.dp)
                .fillMaxWidth()
                .height(48.dp)
                .clip(CircleShape)
                .clickable(onClick = onRestore)
                .testTag("onboarding_restore"),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.v4_splash_restore), style = SplashButtonStyle, color = BrandEnd)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                slides.indices.forEach { index ->
                    val active = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (active) 9.dp else 7.dp)
                            .clip(CircleShape)
                            .background(if (active) BrandEnd else Line)
                    )
                }
            }
        }
    }
}
