package com.insane.goldtv

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
private val Categories = listOf("All", "Live TV", "News", "Sports", "Movies", "Cartoons", "Anime", "My List")

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
    Channel("National Film Board", "Movies", "Official free films and documentaries", "https://www.youtube.com/@NFB")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { GoldTvHome(this) } }
    }
}

@Composable
private fun GoldTvHome(context: Context) {
    val prefs = remember { context.getSharedPreferences("gold_tv", Context.MODE_PRIVATE) }
    var favourites by remember { mutableStateOf(prefs.getStringSet("favourites", emptySet())?.toSet() ?: emptySet()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("Choose a title to open its official source.") }

    val visible = catalogue.filter { channel ->
        (selectedCategory == "All" || (selectedCategory == "My List" && channel.id in favourites) ||
            (selectedCategory != "My List" && (selectedCategory == channel.category || (selectedCategory == "Live TV" && channel.category == "News")))) &&
            (search.isBlank() || channel.title.contains(search, true) || channel.description.contains(search, true) || channel.category.contains(search, true))
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF242016), Background)))
            .padding(start = 36.dp, end = 36.dp, top = 20.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("INSaNe", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(8.dp))
            Text("GOLD TV", color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("FREE ENTERTAINMENT HUB", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(Panel).border(1.dp, Color(0xFF6F5726), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
            items(Categories) { category ->
                FocusTile(title = category, selected = selectedCategory == category, onClick = { selectedCategory = category })
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF3B2D12), Panel, Color(0xFF111318))))
                .border(1.dp, Color(0xFF8E6B26), RoundedCornerShape(18.dp)).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("YOUR ENTERTAINMENT. YOUR GOLD STANDARD.", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Discover something free.", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(5.dp))
                Text("Official sources • Family-friendly discovery • No app subscription", color = Color.LightGray, fontSize = 13.sp)
            }
            Text("✦", color = Gold, fontSize = 42.sp)
        }
        Spacer(Modifier.height(14.dp))
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
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(channel.url)))
                                notice = "Opening " + channel.title + " in its official source."
                            } catch (_: ActivityNotFoundException) {
                                notice = "No app could open this source. Please check your browser."
                            }
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
