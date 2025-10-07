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
package com.telepesa.feature.onboarding.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.telepesa.core.ui.theme.telepesaTheme

/**
 * Onboarding screen with 3 pages introducing the Telepesa app.
 * Implements the Single Responsibility Principle by focusing only on onboarding UI.
 * Uses the Dependency Inversion Principle through ViewModel injection.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun onboardingScreen(
    onNavigateToAuth: () -> Unit,
    onSkipOnboarding: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(uiState.isOnboardingCompleted) {
        if (uiState.isOnboardingCompleted) {
            onNavigateToAuth()
        }
    }

    telepesaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Skip button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onSkipOnboarding,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        ),
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Pager content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                ) { page ->
                    onboardingPage(
                        page = page,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Page indicators
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    },
                                ),
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Back button (only show if not on first page)
                    if (pagerState.currentPage > 0) {
                        TextButton(
                            onClick = {
                                // Navigate to previous page
                                if (pagerState.currentPage > 0) {
                                    // This would need to be handled with LaunchedEffect
                                }
                            },
                        ) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(64.dp))
                    }

                    // Next/Get Started button
                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                // Navigate to next page - would need LaunchedEffect
                                viewModel.nextPage()
                            } else {
                                viewModel.completeOnboarding()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            text = if (pagerState.currentPage == 2) "Get Started" else "Next",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun onboardingPage(page: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Placeholder for illustration/image
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                // Placeholder emoji
                text = "📱",
                style = MaterialTheme.typography.displayLarge,
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = when (page) {
                0 -> "Welcome to Telepesa"
                1 -> "Send Money Easily"
                2 -> "Secure & Fast"
                else -> ""
            },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = when (page) {
                0 ->
                    "Your trusted digital wallet for all your financial needs. " +
                        "Manage your money with ease and security."
                1 ->
                    "Transfer money to friends and family instantly. " +
                        "No more waiting in long queues at banks."
                2 ->
                    "Bank-level security with biometric authentication. " +
                        "Your money is safe with us."
                else -> ""
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun onboardingScreenPreview() {
    telepesaTheme {
        onboardingScreen(
            onNavigateToAuth = {},
            onSkipOnboarding = {},
        )
    }
}
