package com.example.be_bored_compose

object TrackedApps {
    val apps = mapOf(
        Pair("Entertainment", listOf("Photos","Maps","YouTube")),
        Pair("Work", listOf("Messaging", "Dialer")),
        Pair("System", listOf("Calendar"))
    )
    val appPackages = mapOf(
        Pair("Photos","com.google.android.apps.photos"),
        Pair("Maps","com.google.android.apps.maps"),
        Pair("Messaging","com.google.android.apps.messaging"),
        Pair("Dialer","com.google.android.dialer"),
        Pair("Calendar","com.google.android.calendar"),
        Pair("YouTube","com.google.android.youtube")
    )
}