package com.example.googlelogindemo.presentation.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.googlelogindemo.R
import com.example.googlelogindemo.presentation.Screens
import com.example.googlelogindemo.presentation.components.ChoiceConfirmDialog
import com.example.googlelogindemo.presentation.components.EmptyStatePlaceholder
import com.example.googlelogindemo.presentation.components.FavoritePostCard
import com.example.googlelogindemo.presentation.components.FavoritePostCardPlaceholder
import com.example.googlelogindemo.presentation.ui.home.PAGE_SIZE
import com.example.googlelogindemo.presentation.ui.post.DetailActivity
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import compose.icons.FeatherIcons
import compose.icons.feathericons.RefreshCw

@Composable
fun FavoritesScreen(navController: NavController, viewModel: FavoritesViewModel) {

    Log.i("messi", viewModel.toString() + " : " + viewModel.s)
    val context = LocalContext.current
    val error = viewModel.error.value

    BackHandler(enabled = true) {
        navController.navigate(Screens.HomeScreen.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
                title = {
                    Icon(
                        painterResource(id = R.mipmap.ic_launcher_foreground), contentDescription = stringResource(R.string.nav_favorites),
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(35.dp)
                            .padding(end = 10.dp)
                    )
                    Text(text = stringResource(R.string.nav_favorites))
                }
            )
        }
    ) {
        Box {
            ChoiceConfirmDialog(
                showDialog = viewModel.deleteDialog.value,
                message = stringResource(R.string.favortie_delele_confirm),
                okMessage = stringResource(R.string.delete),
                cancelMessage = stringResource(R.string.cancel),
                onOk = {
                    viewModel.deleteDialog.value = false
                    viewModel.deleteFavorite()
                },
                onCancel = { viewModel.deleteDialog.value = false },
                onDismissRequest = { viewModel.deleteDialog.value = false }
            )

            SwipeRefresh(
                modifier = Modifier
                    .fillMaxSize(),
                state = rememberSwipeRefreshState(viewModel.isLoading.value),
                onRefresh = { viewModel.newSearch() },
            ) {
                Column {
                    if (viewModel.showErrorHeader.value) {
                        EmptyStatePlaceholder(
                            message = stringResource(R.string.error_connection),
                            actionMessage = stringResource(R.string.try_again),
                            actionIcon = FeatherIcons.RefreshCw
                        ) {
                            viewModel.showErrorHeader.value = false
                            viewModel.newSearch()
                        }
                    }
                    if (viewModel.isLoading.value) {
                        repeat(8) {
                            FavoritePostCardPlaceholder()
                        }
                    } else {
                        if (viewModel.posts.isEmpty()) {
                            EmptyStatePlaceholder(
                                message = stringResource(R.string.no_posts),
                                actionMessage = stringResource(R.string.reload),
                                actionIcon = FeatherIcons.RefreshCw
                            ) {
                                viewModel.newSearch()
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                itemsIndexed(
                                    items = viewModel.posts
                                ) { index, post ->
                                    viewModel.onChangeScrollPosition(index)
                                    if ((index + 1) >= (viewModel.page.value * PAGE_SIZE)) {
                                        viewModel.nextPage()
                                    }
                                    FavoritePostCard(post = post.post, onClick = {
                                        val intent = Intent(context, DetailActivity::class.java)
                                        val bundle = Bundle()
                                        bundle.putParcelable("favorite", post)
                                        intent.putExtra("bundle", bundle)
                                        context.startActivity(intent)
                                    },
                                        onDelete = {
                                            viewModel.deleteSelectedIndex.value = index
                                            viewModel.deleteSelectedPost.value = post
                                            viewModel.deleteDialog.value = true
                                            Log.i("dropstuff", index.toString())
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}