package com.app.cyclejournal.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import com.app.cyclejournal.billing.AdMobManager
import com.app.cyclejournal.ui.theme.Dimens
import com.app.cyclejournal.ui.theme.Ink3
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Privacy-safe non-personalized AdMob Adaptive/Standard Banner inside a native SoftCard.
 * Disappears completely when the user is Pro or ads are disabled.
 */
@Composable
fun AdMobBannerCard(
    modifier: Modifier = Modifier,
    isProUser: Boolean = false
) {
    if (!AdMobManager.ENABLE_ADS || isProUser) return

    SoftCard(
        modifier = modifier.fillMaxWidth(),
        radius = Dimens.CardRadius
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SPONSORED",
                color = Ink3,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, bottom = 4.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.wrapContentSize(),
                    factory = { context ->
                        AdView(context).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = AdMobManager.BANNER_AD_UNIT_ID
                            loadAd(
                                AdRequest.Builder()
                                    .addNetworkExtrasBundle(
                                        AdMobAdapter::class.java,
                                        bundleOf("npa" to "1")
                                    )
                                    .build()
                            )
                        }
                    }
                )
            }
        }
    }
}
