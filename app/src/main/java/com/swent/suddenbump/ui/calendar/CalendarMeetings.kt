package com.swent.suddenbump.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarMeetingsScreen(navigationActions: NavigationActions, meetingViewModel: MeetingViewModel, userViewModel: UserViewModel) {
    LaunchedEffect(Unit) { meetingViewModel.getMeetings() }
    val meetings by meetingViewModel.meetings.collectAsState()
    val userFriends by userViewModel.getUserFriends().collectAsState(initial = emptyList())

    Scaffold(
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { route -> navigationActions.navigateTo(route) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = navigationActions.currentRoute()
            )
        },
        content = { padding ->
            Column(modifier = Modifier
                .padding(padding)
                .background(Color.Black)
                .fillMaxSize()) {
                ScrollableInfiniteTimeline(meetings = meetings, userFriends = userFriends)
            }
        }
    )
}

@Composable
fun ScrollableInfiniteTimeline(meetings: List<Meeting>, userFriends: List<User>) {
    val currentDate = Calendar.getInstance()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val initialStartDate by remember { mutableStateOf(currentDate.clone() as Calendar) }
    var currentEndDate by remember {
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) })
    }

    val meetingsByDay = meetings.groupBy { meeting ->
        val date = meeting.date.toDate()
        formatter.format(date)
    }

    val dayList = remember {
        mutableStateListOf<Calendar>().apply {
            addAll(generateDayList(initialStartDate, currentEndDate))
        }
    }

    val listState = rememberLazyListState()
    var visibleMonthYear by remember { mutableStateOf(getMonthYearString(currentDate.time)) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisibleItemIndex ->
                val visibleDay = dayList.getOrNull(firstVisibleItemIndex)
                visibleDay?.let { visibleMonthYear = getMonthYearString(it.time) }
            }
    }

    MonthYearHeader(monthYear = visibleMonthYear)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(dayList) { index, day ->
            if (index == dayList.size - 1) {
                val newEndDate = (dayList.last().clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 30) }
                dayList.addAll(generateDayList(dayList.last(), newEndDate))
                currentEndDate = newEndDate
            }

            val dayKey = formatter.format(day.time)
            val meetingsForDay = meetingsByDay[dayKey] ?: emptyList()

            DayRow(day = day.time, meetings = meetingsForDay, userFriends = userFriends)
        }
    }
}

@Composable
fun DayRow(day: Date, meetings: List<Meeting>, userFriends: List<User>) {
    val dayFormat = SimpleDateFormat("EEE, d", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(com.swent.suddenbump.ui.theme.Purple40, MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        Text(
            text = dayFormat.format(day),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (meetings.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                meetings.forEach { meeting ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(com.swent.suddenbump.ui.theme.Purple40, MaterialTheme.shapes.medium),
                        colors = CardDefaults.cardColors(containerColor = com.swent.suddenbump.ui.theme.Pink40)
                    ) {
                        val friend = userFriends.find { it.uid == meeting.friendId }
                        val friendName = friend?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Friend"
                        val formattedDate = formatDate(meeting.date.toDate())
                        Text(
                            text = "Meet $friendName at ${meeting.location} on $formattedDate",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().padding(12.dp)
                        )
                    }
                }
            }
        } else {
            Text(
                text = "No meetings for this day",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp),
                color = Color.White
            )
        }
    }
}

@Composable
fun MonthYearHeader(monthYear: String) {
    Text(
        text = monthYear,
        style = MaterialTheme.typography.headlineSmall,
        color = Color.White,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    )
}

fun generateDayList(startDate: Calendar, endDate: Calendar): List<Calendar> {
    val dayList = mutableListOf<Calendar>()
    val currentDate = startDate.clone() as Calendar
    while (currentDate.before(endDate)) {
        dayList.add(currentDate.clone() as Calendar)
        currentDate.add(Calendar.DAY_OF_YEAR, 1)
    }
    return dayList
}

fun getMonthYearString(date: Date): String {
    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return formatter.format(date)
}

fun formatDate(date: Date?): String {
    return if (date != null) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.format(date)
    } else {
        "Invalid Date"
    }
}

