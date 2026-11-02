package com.mindInk.app.ui.articles.stars

import androidx.compose.runtime.compositionLocalOf

val LocalStarredArticleIds = compositionLocalOf<Set<String>> { emptySet() }