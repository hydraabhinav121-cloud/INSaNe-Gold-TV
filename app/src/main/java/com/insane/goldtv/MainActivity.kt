package com.insane.goldtv

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gold = Color(0xFFFFD36A)
private val Background = Color(0xFF090A0D)
private val Panel = Color(0xFF17191F)
private val Categories = listOf("All", "Streaming", "Bhojpuri", "Bigg Boss", "IPL", "Free Documentaries", "Comedy", "Radio & Music", "Live TV", "News", "Sports", "Devotional", "Cartoons", "Hindi Kids", "Anime", "Movies", "Free Movies", "Family Movies", "Classics", "Music", "Learning", "My List")

private data class Channel(val title: String, val category: String, val description: String, val url: String) {
    val id: String get() = title
}

private val catalogue = listOf(
    Channel("DD News", "News", "Official news videos and live coverage", "https://www.youtube.com/@DDNewsOfficial"),
    Channel("Sansad TV", "News", "Parliament coverage and public affairs", "https://www.youtube.com/@SansadTV"),
    Channel("Aaj Tak", "News", "Official news videos and live coverage", "https://www.youtube.com/@AajTak"),
    Channel("ABP News", "News", "Official Hindi news channel", "https://www.youtube.com/@abpnews"),
    Channel("WION", "News", "International news and explainers", "https://www.youtube.com/@WION"),
    Channel("ICC", "Sports", "Official cricket highlights and features", "https://www.youtube.com/@ICC"),
    Channel("BCCI", "Sports", "Official Indian cricket videos", "https://www.youtube.com/@BCCI"),
    Channel("Olympics", "Sports", "Official Olympic sports highlights", "https://www.youtube.com/@Olympics"),
    Channel("ChuChu TV", "Cartoons", "Kids songs and animated videos", "https://www.youtube.com/@ChuChuTV"),
    Channel("Wow Kidz", "Cartoons", "Kids animation and cartoon videos", "https://www.youtube.com/@WowKidz"),
    Channel("Green Gold TV", "Cartoons", "Indian animation and kids content", "https://www.youtube.com/@GreenGoldTV"),
    Channel("Peppa Pig", "Cartoons", "Official children's cartoon videos", "https://www.youtube.com/@PeppaPigOfficial"),
    Channel("Muse India", "Anime", "Official anime channel; catalogue varies by region", "https://www.youtube.com/@MuseIndia"),
    Channel("Ani-One Asia", "Anime", "Official anime channel; availability varies by region", "https://www.youtube.com/@AniOneAsia"),
    Channel("Rajshri", "Movies", "Official Indian films and music", "https://www.youtube.com/@Rajshri"),
    Channel("Internet Archive Films", "Movies", "Public-domain and community film collection; check each title's rights", "https://archive.org/details/feature_films"),
    Channel("National Film Board", "Movies", "Official free films and documentaries", "https://www.youtube.com/@NFB"),
    Channel("India TV", "Live TV", "News updates and coverage on its official channel", "https://www.youtube.com/@IndiaTV"),
    Channel("Republic Bharat", "Live TV", "Hindi news and current affairs", "https://www.youtube.com/@RepublicBharat"),
    Channel("News18 India", "Live TV", "Hindi news and current affairs", "https://www.youtube.com/@News18India"),
    Channel("TV9 Bharatvarsh", "Live TV", "Hindi news and coverage", "https://www.youtube.com/@TV9Bharatvarsh"),
    Channel("DD National", "Live TV", "Doordarshan programmes and official videos", "https://www.youtube.com/@DoordarshanNational"),
    Channel("Sanskar TV", "Devotional", "Bhajans, satsang and spiritual programmes", "https://www.youtube.com/@SanskarTV"),
    Channel("Aastha Bhajan", "Devotional", "Bhajans and devotional programmes", "https://www.youtube.com/@AasthaBhajan"),
    Channel("Shemaroo Bhakti", "Devotional", "Bhajans, aartis and devotional music", "https://www.youtube.com/@ShemarooBhakti"),
    Channel("T-Series Bhakti Sagar", "Devotional", "Devotional songs and aartis", "https://www.youtube.com/@TSeriesBhaktiSagar"),
    Channel("Saregama Bhakti", "Devotional", "Devotional music and bhajans", "https://www.youtube.com/@SaregamaBhakti"),
    Channel("Bhakti Songs", "Devotional", "Find official devotional song collections", "https://www.youtube.com/results?search_query=official+bhakti+songs"),
    Channel("Shemaroo Kids", "Hindi Kids", "Children's stories, rhymes and animation", "https://www.youtube.com/@ShemarooKids"),
    Channel("Infobells Hindi", "Hindi Kids", "Hindi rhymes and children's animation", "https://www.youtube.com/@infobellshindi"),
    Channel("Jingle Toons", "Hindi Kids", "Hindi nursery rhymes and kids stories", "https://www.youtube.com/@JingleToons"),
    Channel("Hindi Fairy Tales", "Hindi Kids", "Hindi story and fairy-tale videos", "https://www.youtube.com/results?search_query=official+hindi+fairy+tales+kids"),
    Channel("Sony SAB", "Cartoons", "Family comedy and entertainment clips", "https://www.youtube.com/@SonySAB"),
    Channel("Shemaroo Comedy", "Movies", "Comedy scenes and family entertainment", "https://www.youtube.com/@ShemarooComedy"),
    Channel("T-Series", "Music", "Official Hindi music videos and film songs", "https://www.youtube.com/@tseries"),
    Channel("Zee Music Company", "Music", "Official Hindi songs and music videos", "https://www.youtube.com/@zeemusiccompany"),
    Channel("Saregama Music", "Music", "Hindi classics and official music releases", "https://www.youtube.com/@saregama"),
    Channel("Sony Music India", "Music", "Official Indian music videos", "https://www.youtube.com/@SonyMusicIndia"),
    Channel("Tips Official", "Music", "Official songs and music videos", "https://www.youtube.com/@TipsOfficial"),
    Channel("National Geographic", "Learning", "Nature, science and educational videos", "https://www.youtube.com/@NatGeo"),
    Channel("NASA", "Learning", "Space science, missions and educational videos", "https://www.youtube.com/@NASA"),
    Channel("Khan Academy", "Learning", "Free educational lessons and explainers", "https://www.youtube.com/@khanacademy"),
    Channel("TED-Ed", "Learning", "Animated educational lessons for curious minds", "https://www.youtube.com/@TEDEd"),
    Channel("Muse Asia", "Anime", "Official anime uploads; titles vary by region", "https://www.youtube.com/@MuseAsia"),
    Channel("Pogo", "Cartoons", "Official kids entertainment clips; availability varies", "https://www.youtube.com/results?search_query=Pogo+official+cartoons"),
    Channel("Cartoon Network India", "Cartoons", "Official cartoon clips and kids entertainment", "https://www.youtube.com/results?search_query=Cartoon+Network+India+official"),
    Channel("Prasar Bharati Archives", "Movies", "Classic Indian television and archive programmes", "https://www.youtube.com/@PrasarBharatiArchives"),
    Channel("Internet Archive Documentaries", "Movies", "Documentaries and films; check individual rights and availability", "https://archive.org/details/movies"),
    Channel("Wave Music Bhojpuri", "Bhojpuri", "Bhojpuri songs and videos from the music label's official YouTube results", "https://www.youtube.com/results?search_query=Wave+Music+Bhojpuri+official"),
    Channel("Worldwide Records Bhojpuri", "Bhojpuri", "Bhojpuri music and movie songs from the label's official uploads", "https://www.youtube.com/results?search_query=Worldwide+Records+Bhojpuri+official"),
    Channel("T-Series Hamaar Bhojpuri", "Bhojpuri", "Bhojpuri music and entertainment; browse official uploads", "https://www.youtube.com/results?search_query=T-Series+Hamaar+Bhojpuri+official"),
    Channel("Enterr10 Rangeela", "Bhojpuri", "Bhojpuri entertainment and movie content; availability varies", "https://www.youtube.com/results?search_query=Enterr10+Rangeela+official"),
    Channel("Bhojpuri Full Movies", "Bhojpuri", "Find full films uploaded by official studios and rights holders", "https://www.youtube.com/results?search_query=Bhojpuri+full+movie+official+studio"),
    Channel("Bigg Boss Official", "Bigg Boss", "Official Bigg Boss clips and updates; full episodes may require a subscription", "https://www.youtube.com/results?search_query=Bigg+Boss+official+Colors+TV"),
    Channel("Colors TV", "Bigg Boss", "Official Colors TV videos and Bigg Boss clips; full live episodes may be restricted", "https://www.youtube.com/@colorstv"),
    Channel("Bigg Boss on JioHotstar", "Bigg Boss", "Check official availability; some episodes or seasons may require a subscription", "https://www.jiohotstar.com/"),
    Channel("IPL Official", "IPL", "Official IPL highlights, news and match updates; live coverage may be restricted", "https://www.iplt20.com/"),
    Channel("IPL Official Videos", "IPL", "Official IPL clips and highlights; not a guaranteed free live match stream", "https://www.youtube.com/results?search_query=IPL+official+highlights"),
    Channel("Star Sports India", "IPL", "Official sports clips and match coverage; live broadcast rights apply", "https://www.youtube.com/@StarSportsIndia"),
    Channel("JioHotstar Sports", "IPL", "Check official cricket streaming availability; subscriptions or regional limits may apply", "https://www.jiohotstar.com/"),
    Channel("Free Nature Documentaries", "Free Documentaries", "Official nature and wildlife documentaries and clips", "https://www.youtube.com/@NatGeo"),
    Channel("PBS Nature", "Free Documentaries", "Nature documentary clips from the public broadcaster", "https://www.youtube.com/results?search_query=PBS+Nature+official"),
    Channel("NFB Documentaries", "Free Documentaries", "Free films and documentaries from Canada's National Film Board", "https://www.nfb.ca/"),
    Channel("NASA Space Videos", "Free Documentaries", "Official space missions, Earth views and science documentaries", "https://www.youtube.com/@NASA"),
    Channel("TED Talks", "Free Documentaries", "Talks and explainers across science, culture and ideas", "https://www.youtube.com/@TED"),
    Channel("Shemaroo Comedy", "Comedy", "Official comedy scenes and entertainment clips", "https://www.youtube.com/@ShemarooComedy"),
    Channel("Sony SAB Comedy", "Comedy", "Official comedy and family entertainment clips", "https://www.youtube.com/@SonySAB"),
    Channel("Hindi Comedy Clips", "Comedy", "Search official studio channels for comedy scenes", "https://www.youtube.com/results?search_query=Hindi+comedy+official+channel"),
    Channel("Stand-up Comedy India", "Comedy", "Stand-up clips from official comedians and publishers", "https://www.youtube.com/results?search_query=Indian+standup+comedy+official"),
    Channel("All India Radio", "Radio & Music", "Official radio programmes and music; availability varies", "https://prasarbharati.gov.in/"),
    Channel("Saregama Classics", "Radio & Music", "Hindi classics and curated music from the official label", "https://www.youtube.com/@saregama"),
    Channel("Lofi & Relaxing Music", "Radio & Music", "Find official, licensed relaxation and instrumental music", "https://www.youtube.com/results?search_query=official+lofi+relaxing+music"),
    Channel("Bhajan Radio", "Radio & Music", "Official devotional music collections and bhajans", "https://www.youtube.com/results?search_query=official+bhajan+music"),
    Channel("Free Audiovisual Archive", "Free Documentaries", "Browse archive films and verify rights on each title", "https://archive.org/details/movies"),
    Channel("WAVES by Prasar Bharati", "Free Movies", "Public broadcaster streaming hub; free and premium availability can vary", "https://wavespb.com/"),
    Channel("WAVES Free Streaming", "Streaming", "Public broadcaster movies, shows and live channels; some titles may be premium", "https://wavespb.com/"),
    Channel("Amazon MX Player / Prime Video", "Streaming", "Browse free ad-supported movies and shows where available", "https://www.primevideo.com/"),
    Channel("SonyLIV Free", "Streaming", "Official streaming service; selected episodes and titles may be free", "https://www.sonyliv.com/"),
    Channel("ZEE5 Free", "Streaming", "Open the official catalogue and look for free titles", "https://www.zee5.com/"),
    Channel("TVF Play", "Streaming", "Official TVF web series and entertainment; availability varies", "https://tvfplay.com/"),
    Channel("YouTube Official Movies", "Streaming", "Find full films uploaded by official studios and rights holders", "https://www.youtube.com/results?search_query=official+full+movie+studio"),
    Channel("ZEE5 Movies", "Free Movies", "Browse official movie catalogue and check which titles are marked free", "https://www.zee5.com/movies"),
    Channel("Prime Video India", "Free Movies", "Browse licensed films; only some titles may be free with ads", "https://www.primevideo.com/"),
    Channel("Rajshri Movies", "Free Movies", "Official studio catalogue with selected full films; title availability varies", "https://www.youtube.com/@Rajshri"),
    Channel("Shemaroo Movies", "Free Movies", "Official distributor catalogue with selected full films", "https://www.youtube.com/results?search_query=Shemaroo+Movies+official+full+movie"),
    Channel("Goldmines", "Free Movies", "Official distributor channel for selected Hindi and dubbed films", "https://www.youtube.com/@GoldminesTelefilms"),
    Channel("Ultra Movie Parlour", "Free Movies", "Official Ultra film catalogue; availability varies by title", "https://www.youtube.com/results?search_query=Ultra+Movie+Parlour+official+full+movie"),
    Channel("NH Studioz", "Free Movies", "Official studio source for selected Indian films", "https://www.youtube.com/results?search_query=NH+Studioz+official+full+movie"),
    Channel("FilmRise Movies", "Free Movies", "Official distributor channel; catalogue and regional access vary", "https://www.youtube.com/@FilmRiseMovies"),
    Channel("Classic Hindi Movies", "Classics", "Search for licensed classic Hindi films from official rights holders", "https://www.youtube.com/results?search_query=classic+Hindi+full+movie+official+studio"),
    Channel("Family Hindi Films", "Family Movies", "Find family-friendly Hindi films from official studio channels", "https://www.youtube.com/results?search_query=family+Hindi+full+movie+official+studio"),
    Channel("Rajshri Family Films", "Family Movies", "Search Rajshri's official channel for family films and classics", "https://www.youtube.com/@Rajshri/search?query=family%20movie"),
    Channel("Hindi Comedy Classics", "Classics", "Find classic comedy films; verify the official studio or rights-holder", "https://www.youtube.com/results?search_query=Hindi+comedy+classic+full+movie+official"),
    Channel("Animated Family Films", "Family Movies", "Find licensed family animation and full-length animated films", "https://www.youtube.com/results?search_query=family+animated+movie+official+full+movie"),
    Channel("Indian Film Archives", "Classics", "Browse archival films and check each item's rights and playback terms", "https://archive.org/details/feature_films"),
    Channel("National Film Board Films", "Free Movies", "Official films and documentaries from a public film producer", "https://www.youtube.com/@NFB"),
    Channel("Public Domain Classics", "Classics", "Explore archive films and verify rights on each individual title", "https://archive.org/details/feature_films"),
    Channel("Hindi Inspirational Films", "Family Movies", "Search for official inspirational and family movie releases", "https://www.youtube.com/results?search_query=Hindi+inspirational+full+movie+official"),
    Channel("Kids Animation Movies", "Family Movies", "Find age-appropriate animated films from official publishers", "https://www.youtube.com/results?search_query=kids+animated+movie+official+full+movie"),
    Channel("Free Movie Discovery", "Free Movies", "Search for legally available full movies from studios and rights holders", "https://www.youtube.com/results?search_query=official+full+movie+Hindi+studio")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { GoldTvApp(this) } }
    }
}


@Composable
private fun GoldTvApp(context: Context) {
    var showIntro by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3400)
        showIntro = false
    }
    Box(Modifier.fillMaxSize().background(Background)) {
        GoldTvHome(context)
        AnimatedVisibility(
            visible = showIntro,
            enter = fadeIn(tween(350)),
            exit = fadeOut(tween(650))
        ) {
            GoldSplash()
        }
    }
}

@Composable
private fun GoldSplash() {
    val transition = rememberInfiniteTransition(label = "gold_splash")
    val glow by transition.animateFloat(
        initialValue = 0.48f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val drift by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "drift"
    )
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(180); revealed = true }

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(
                colors = listOf(Color(0xFF30240E), Color(0xFF111116), Color(0xFF050608)),
                radius = 1150f
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val points = listOf(
                Offset(size.width * .12f, size.height * .18f),
                Offset(size.width * .82f, size.height * .22f),
                Offset(size.width * .18f, size.height * .78f),
                Offset(size.width * .88f, size.height * .76f),
                Offset(size.width * .69f, size.height * .12f),
                Offset(size.width * .32f, size.height * .87f)
            )
            points.forEachIndexed { index, point ->
                drawCircle(
                    color = Gold.copy(alpha = if (index % 2 == 0) glow * .46f else glow * .22f),
                    radius = if (index % 2 == 0) 2.4.dp.toPx() else 1.5.dp.toPx(),
                    center = point
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            AnimatedVisibility(
                visible = revealed,
                enter = scaleIn(initialScale = .62f, animationSpec = tween(850, easing = FastOutSlowInEasing)) +
                    fadeIn(tween(750))
            ) {
                Box(
                    Modifier.size(176.dp).graphicsLayer {
                        translationY = drift
                        scaleX = .98f + glow * .025f
                        scaleY = .98f + glow * .025f
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Gold.copy(alpha = .10f + glow * .08f),
                            radius = size.minDimension * .47f,
                        )
                        drawCircle(
                            color = Gold.copy(alpha = .35f + glow * .45f),
                            radius = size.minDimension * .46f,
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = .10f),
                            radius = size.minDimension * .405f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Shree Reempu Devi TV app icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(112.dp).clip(RoundedCornerShape(24.dp))
                    )
                }
            }
            Spacer(Modifier.height(26.dp))
            AnimatedVisibility(
                visible = revealed,
                enter = fadeIn(tween(900, delayMillis = 260)) +
                    slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(900, delayMillis = 260, easing = FastOutSlowInEasing))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "SHREE REEMPU DEVI",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "T V",
                        color = Gold,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 9.sp
                    )
                    Spacer(Modifier.height(19.dp))
                    Box(
                        Modifier.width(116.dp).height(2.dp)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, Gold.copy(alpha = glow), Color.Transparent)))
                    )
                    Spacer(Modifier.height(15.dp))
                    Text(
                        "A GOLDEN WORLD OF ENTERTAINMENT",
                        color = Color.White.copy(alpha = .68f),
                        fontSize = 9.sp,
                        letterSpacing = 1.6.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TvMark(modifier: Modifier = Modifier, glow: Float) {
    Canvas(modifier) {
        val stroke = 3.dp.toPx()
        val left = size.width * .13f
        val top = size.height * .24f
        val width = size.width * .74f
        val height = size.height * .49f
        drawLine(
            color = Gold.copy(alpha = .88f),
            start = Offset(size.width * .40f, top),
            end = Offset(size.width * .29f, size.height * .10f),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = Gold.copy(alpha = .88f),
            start = Offset(size.width * .60f, top),
            end = Offset(size.width * .71f, size.height * .10f),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawRoundRect(
            color = Gold.copy(alpha = .92f),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(10.dp.toPx()),
            style = Stroke(width = stroke)
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF45330F).copy(alpha = .85f), Color(0xFF17130A).copy(alpha = .9f))
            ),
            topLeft = Offset(left + stroke * 1.5f, top + stroke * 1.5f),
            size = Size(width - stroke * 3f, height - stroke * 3f),
            cornerRadius = CornerRadius(7.dp.toPx())
        )
        drawRoundRect(
            color = Gold.copy(alpha = .45f + glow * .5f),
            topLeft = Offset(left + width * .12f, top + height * .13f),
            size = Size(width * .76f, height * .70f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = 1.4.dp.toPx())
        )
        drawCircle(
            color = Gold.copy(alpha = glow),
            radius = 2.5.dp.toPx(),
            center = Offset(left + width * .87f, top + height * .83f)
        )
        drawLine(
            color = Gold.copy(alpha = .9f),
            start = Offset(size.width * .42f, top + height + 4.dp.toPx()),
            end = Offset(size.width * .36f, size.height * .82f),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = Gold.copy(alpha = .9f),
            start = Offset(size.width * .58f, top + height + 4.dp.toPx()),
            end = Offset(size.width * .64f, size.height * .82f),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = Gold.copy(alpha = .9f),
            start = Offset(size.width * .27f, size.height * .84f),
            end = Offset(size.width * .73f, size.height * .84f),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun GoldTvHome(context: Context) {
    val prefs = remember { context.getSharedPreferences("gold_tv", Context.MODE_PRIVATE) }
    var favourites by remember { mutableStateOf(prefs.getStringSet("favourites", emptySet())?.toSet() ?: emptySet()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("Choose a title to watch inside the app.") }
    var selectedChannel by remember { mutableStateOf<Channel?>(null) }

    val visible = catalogue.filter { channel ->
        (selectedCategory == "All" || (selectedCategory == "My List" && channel.id in favourites) ||
            (selectedCategory != "My List" && (selectedCategory == channel.category || (selectedCategory == "Live TV" && channel.category == "News")))) &&
            (search.isBlank() || channel.title.contains(search, true) || channel.description.contains(search, true) || channel.category.contains(search, true))
    }

    if (selectedChannel != null) {
        val channel = selectedChannel!!
        InAppStreamingScreen(
            title = channel.title,
            description = channel.description,
            url = channel.url,
            onClose = { selectedChannel = null }
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF242016), Background)))
            .padding(start = 30.dp, end = 30.dp, top = 12.dp, bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("SHREE REEMPU DEVI", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(8.dp))
            Text("TV", color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("FREE ENTERTAINMENT HUB", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(Panel).border(1.dp, Color(0xFF6F5726), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⌕", color = Gold, fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (search.isEmpty()) Text("Search channels, cartoons, sports, movies…", color = Color.Gray, fontSize = 15.sp)
                BasicTextField(
                    value = search,
                    onValueChange = { search = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth().focusable()
                )
            }
            if (search.isNotEmpty()) {
                Text("CLEAR", color = Gold, fontSize = 12.sp, modifier = Modifier.clickable { search = "" }.focusable().padding(6.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
            items(Categories) { category ->
                FocusTile(title = category, selected = selectedCategory == category, onClick = { selectedCategory = category })
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF3B2D12), Panel, Color(0xFF111318))))
                .border(1.dp, Color(0xFF8E6B26), RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("A GOLDEN WORLD OF ENTERTAINMENT", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Watch • Explore • Enjoy", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(5.dp))
                Text("Official sources • Family-friendly discovery • No app subscription", color = Color.LightGray, fontSize = 13.sp)
            }
            Text("✦", color = Gold, fontSize = 42.sp)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (selectedCategory == "My List") "MY LIST" else if (search.isNotBlank()) "SEARCH RESULTS" else "EXPLORE " + selectedCategory.uppercase(), color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(visible.size.toString() + " sources", color = Color.Gray, fontSize = 11.sp)
        }
        Spacer(Modifier.height(8.dp))
        if (visible.isEmpty()) {
            Text("No matches yet. Try another search or category.", color = Color.LightGray, modifier = Modifier.padding(12.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
                items(visible, key = { it.id }) { channel ->
                    ChannelRow(
                        channel = channel,
                        favourite = channel.id in favourites,
                        onOpen = {
                            selectedChannel = channel
                            notice = "Opening " + channel.title + " inside SHREE REEMPU DEVI TV."
                        },
                        onFavourite = {
                            val updated = favourites.toMutableSet()
                            if (!updated.add(channel.id)) updated.remove(channel.id)
                            favourites = updated.toSet()
                            prefs.edit().putStringSet("favourites", favourites).apply()
                            notice = if (channel.id in favourites) "Added to My List." else "Removed from My List."
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(notice, color = Gold, fontSize = 11.sp, maxLines = 1)
        Spacer(Modifier.height(3.dp))
        Text("Free availability varies by region. Some providers may show ads, require an account, or change their catalogue.", color = Color(0xFF999999), fontSize = 10.sp)
    }
}

@Composable
private fun ChannelRow(channel: Channel, favourite: Boolean, onOpen: () -> Unit, onFavourite: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().height(62.dp).clip(RoundedCornerShape(13.dp))
            .background(if (focused) Color(0xFF302711) else Panel)
            .border(if (focused) 2.dp else 1.dp, if (focused) Gold else Color(0xFF34363D), RoundedCornerShape(13.dp))
            .onFocusChanged { focused = it.isFocused }.focusable().clickable { onOpen() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(42.dp).height(42.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF29251B)), contentAlignment = Alignment.Center) {
            Text(channel.category.take(1), color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(channel.title, color = if (focused) Gold else Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(channel.description, color = Color.LightGray, fontSize = 10.sp, maxLines = 1)
        }
        Text(if (favourite) "★ SAVED" else "☆ SAVE", color = Gold, fontSize = 11.sp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onFavourite() }.focusable().padding(8.dp))
        Spacer(Modifier.width(8.dp))
        Text("OPEN  ›", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FocusTile(title: String, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
            .background(if (focused || selected) Color(0xFF3A2D12) else Panel)
            .border(if (focused || selected) 2.dp else 1.dp, if (focused || selected) Gold else Color(0xFF393B42), RoundedCornerShape(12.dp))
            .onFocusChanged { focused = it.isFocused }.focusable().clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, color = if (focused || selected) Gold else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
