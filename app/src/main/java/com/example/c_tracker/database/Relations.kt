package com.example.c_tracker.database

import androidx.room.Embedded
import androidx.room.Relation

// RelationClasses

data class ReachedPrefectureWithCities(
    @Embedded val prefecture: ReachedPrefecture,
    @Relation(
        parentColumn = "code",
        entityColumn = "prefecture_code"
    )
    val cities: List<ReachedCity>
)