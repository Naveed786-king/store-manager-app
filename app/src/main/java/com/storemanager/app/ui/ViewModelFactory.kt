package com.storemanager.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.storemanager.app.StoreManagerApp
import com.storemanager.app.data.repository.StoreRepository

class GenericViewModelFactory(private val creator: (StoreRepository) -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator(repositoryFromApp) as T
    }

    companion object {
        lateinit var repositoryFromApp: StoreRepository
    }
}

@Composable
inline fun <reified T : ViewModel> repoViewModel(noinline creator: (StoreRepository) -> T): T {
    val context = LocalContext.current
    val app = context.applicationContext as StoreManagerApp
    return viewModel(factory = GenericViewModelFactory { repo -> creator(repo) }.also {
        GenericViewModelFactory.repositoryFromApp = app.repository
    })
}
