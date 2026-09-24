package com.example.taller_android.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val startDestination = if (authViewModel.user.value != null) "task_list" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("task_list") { popUpTo("login") { inclusive = true } } }
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigate("task_list") { popUpTo("login") { inclusive = true } } }
            )
        }
        composable("task_list") {
            val taskViewModel: TaskViewModel = hiltViewModel()
            TaskListScreen(
                taskViewModel = taskViewModel,
                authViewModel = authViewModel,
                onLogout = { navController.navigate("login") { popUpTo("task_list") { inclusive = true } } }
            )
        }
    }
}