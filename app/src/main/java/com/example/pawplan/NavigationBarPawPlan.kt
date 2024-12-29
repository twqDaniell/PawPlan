package com.example.pawplan

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.outlined.FoodBank
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun NavigationBarPawPlan() {
    var selectedItem by remember { mutableStateOf(0) }
    val barItems = listOf(
        BarItem(
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            route = "profile"
        ),
        BarItem(
            title = "Health",
            selectedIcon = Icons.Filled.Vaccines,
            unselectedIcon = Icons.Outlined.Vaccines,
            route = "health"
        ),
        BarItem(
            title = "Missing",
            selectedIcon = Icons.Filled.Policy,
            unselectedIcon = Icons.Outlined.Policy,
            route = "missing"
        ),
        BarItem(
            title = "Food",
            selectedIcon = Icons.Filled.FoodBank,
            unselectedIcon = Icons.Outlined.FoodBank,
            route = "food"
        )
    )

    NavigationBar(
        containerColor = Color(0x4DFFDBC8),
        contentColor = Color(0xFFB3839A)
    ) {
        barItems.forEachIndexed { index, barItem ->
            val selected = selectedItem == index

            NavigationBarItem(
                selected = selected,
                onClick = { selectedItem = index },
                icon = {
                    Icon(
                        imageVector = if (selected) barItem.selectedIcon else barItem.unselectedIcon,
                        contentDescription = barItem.title
                    )
                },
                label = { androidx.compose.material3.Text(text = barItem.title) },
                colors = NavigationBarItemColors(
                    selectedIconColor = Color(0xFFB3839A),
                    unselectedIconColor = Color(0xFFB3839A),
                    selectedTextColor = Color(0xFFB3839A),
                    unselectedTextColor = Color(0xFFB3839A),
                    selectedIndicatorColor = Color(0x7EEABCD1),
                    disabledIconColor = Color(0xFFB3839A),
                    disabledTextColor = Color(0xFFB3839A)
                )
            )
        }
    }
}

data class BarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)
