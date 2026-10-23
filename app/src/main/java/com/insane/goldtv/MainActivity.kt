package com.insane.goldtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gold = Color(0xFFFFD36A)
private val Background = Color(0xFF090A0D)
private val Panel = Color(0xFF17191F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { GoldTvHome() } }
    }
}

@Composable
private fun GoldTvHome() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF242016), Background)))
            .padding(start = 48.dp, end = 48.dp, top = 28.dp, bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("INSaNe", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(8.dp))
            Text("GOLD TV", color = Gold, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("HOME  •  SEARCH  •  SETTINGS", color = Color.LightGray, fontSize = 13.sp)
        }
        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF3B2D12), Panel, Color(0xFF111318))))
                .border(1.dp, Color(0xFF8E6B26), RoundedCornerShape(24.dp))
                .padding(30.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text("YOUR ENTERTAINMENT. YOUR GOLD STANDARD.", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("Welcome to INSaNe Gold TV", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Text("Choose a category below to explore available content.", color = Color.LightGray, fontSize = 17.sp)
                Spacer(Modifier.height(20.dp))
                Text("Select a tile with your TV remote", color = Gold, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("EXPLORE", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        val categories = listOf(
            "Live TV" to "News • TV channels",
            "Sports" to "Cricket • Highlights",
            "Movies" to "Family-friendly picks",
            "Cartoons" to "Kids • Animation",
            "Anime" to "Dubbed where available",
            "My List" to "Saved favourites"
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(categories) { (title, subtitle) ->
                CategoryTile(title, subtitle)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Content availability depends on official apps, licences and subscriptions.",
            color = Color(0xFF999999),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun CategoryTile(title: String, subtitle: String) {
    androidx.compose.runtime var focused by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(
        modifier = Modifier
            .width(185.dp)
            .height(108.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (focused) Color(0xFF3A2D12) else Panel)
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) Gold else Color(0xFF393B42),
                shape = RoundedCornerShape(16.dp)
            )
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .padding(14.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, color = if (focused) Gold else Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = Color.LightGray, fontSize = 11.sp)
    }
}
