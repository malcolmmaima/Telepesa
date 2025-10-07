/*
 * Copyright (c) 2025 Telepesa. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.telepesa.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.telepesa.core.ui.theme.telepesaTheme

@Composable
fun telepesaNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = TelepesaScreens.Onboarding.route,
    ) {
        composable(TelepesaScreens.Onboarding.route) {
            onboardingPlaceholder(
                onNavigateToAuth = { navController.navigate(TelepesaScreens.SignIn.route) },
                onSkipOnboarding = { navController.navigate(TelepesaScreens.SignIn.route) },
            )
        }

        composable(TelepesaScreens.SignIn.route) {
            signInPlaceholder(
                onNavigateToSignUp = { navController.navigate(TelepesaScreens.SignUp.route) },
                onNavigateToForgotPassword = { navController.navigate(TelepesaScreens.ForgotPassword.route) },
                onSignInSuccess = { navController.navigate(TelepesaScreens.Home.route) },
            )
        }

        composable(TelepesaScreens.SignUp.route) {
            placeholderScreen(
                title = "Sign Up",
                description = "Create your Telepesa account",
            )
        }

        composable(TelepesaScreens.ForgotPassword.route) {
            placeholderScreen(
                title = "Forgot Password",
                description = "Reset your password",
            )
        }

        composable(TelepesaScreens.Home.route) {
            homePlaceholder(
                onNavigateToProfile = { navController.navigate(TelepesaScreens.Profile.route) },
                onNavigateToSendMoney = { navController.navigate(TelepesaScreens.SendMoney.route) },
                onNavigateToRequestMoney = { navController.navigate(TelepesaScreens.RequestMoney.route) },
                onNavigateToPayments = { navController.navigate(TelepesaScreens.Payments.route) },
                onNavigateToHistory = { navController.navigate(TelepesaScreens.History.route) },
                onNavigateToCards = { navController.navigate(TelepesaScreens.Cards.route) },
                onNavigateToSavings = { navController.navigate(TelepesaScreens.Savings.route) },
            )
        }

        // Feature Screens
        composable(TelepesaScreens.Profile.route) {
            // TODO: Implement ProfileScreen
        }

        composable(TelepesaScreens.SendMoney.route) {
            // TODO: Implement SendMoneyScreen
        }

        composable(TelepesaScreens.RequestMoney.route) {
            // TODO: Implement RequestMoneyScreen
        }

        composable(TelepesaScreens.Payments.route) {
            // TODO: Implement PaymentsScreen
        }

        composable(TelepesaScreens.History.route) {
            // TODO: Implement HistoryScreen
        }

        composable(TelepesaScreens.Cards.route) {
            // TODO: Implement CardsScreen
        }

        composable(TelepesaScreens.Savings.route) {
            // TODO: Implement SavingsScreen
        }
    }
}

/**
 * Sealed class for defining app navigation routes.
 */
sealed class TelepesaScreens(val route: String) {
    object Splash : TelepesaScreens("splash")
    object Onboarding : TelepesaScreens("onboarding")
    object SignIn : TelepesaScreens("signin")
    object SignUp : TelepesaScreens("signup")
    object ForgotPassword : TelepesaScreens("forgot_password")
    object Home : TelepesaScreens("home")
    object Profile : TelepesaScreens("profile")
    object SendMoney : TelepesaScreens("send_money")
    object RequestMoney : TelepesaScreens("request_money")
    object Payments : TelepesaScreens("payments")
    object History : TelepesaScreens("history")
    object Cards : TelepesaScreens("cards")
    object Savings : TelepesaScreens("savings")
}

@Composable
private fun onboardingPlaceholder(onNavigateToAuth: () -> Unit, onSkipOnboarding: () -> Unit) {
    placeholderScreen(
        title = "Welcome to Telepesa",
        description = "Your digital wallet for seamless payments",
        actionText = "Get Started",
        onAction = onNavigateToAuth,
        secondaryActionText = "Skip",
        onSecondaryAction = onSkipOnboarding,
    )
}

@Composable
private fun signInPlaceholder(
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onSignInSuccess: () -> Unit,
) {
    placeholderScreen(
        title = "Sign In",
        description = "Welcome back! Please sign in to continue",
        actionText = "Sign In",
        onAction = onSignInSuccess,
        secondaryActionText = "Sign Up",
        onSecondaryAction = onNavigateToSignUp,
    )
}

@Composable
private fun homePlaceholder(
    onNavigateToProfile: () -> Unit,
    onNavigateToSendMoney: () -> Unit,
    onNavigateToRequestMoney: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToSavings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Telepesa Home",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "Your digital wallet dashboard",
            style = MaterialTheme.typography.bodyLarge,
        )

        Button(
            onClick = onNavigateToSendMoney,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Send Money")
        }

        Button(
            onClick = onNavigateToRequestMoney,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Request Money")
        }

        Button(
            onClick = onNavigateToHistory,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Transaction History")
        }
    }
}

@Composable
private fun placeholderScreen(
    title: String,
    description: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            if (actionText != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionText)
                }
            }

            if (secondaryActionText != null && onSecondaryAction != null) {
                Button(
                    onClick = onSecondaryAction,
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(),
                ) {
                    Text(secondaryActionText)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun telepesaNavigationPreview() {
    telepesaTheme {
        telepesaNavigation()
    }
}
