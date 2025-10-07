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
package com.telepesa.feature.home.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.telepesa.core.ui.theme.TelepesaTextStyles
import com.telepesa.core.ui.theme.telepesaTheme

/**
 * Home screen showing user dashboard with account overview and quick actions.
 * Follows the Single Responsibility Principle by focusing only on home UI.
 * Implements the Dependency Inversion Principle through ViewModel injection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun homeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSendMoney: () -> Unit,
    onNavigateToRequestMoney: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToSavings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    telepesaTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Good ${getGreeting()},",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                            Text(
                                text = uiState.user?.firstName ?: "User",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Account Balance Card
                item {
                    accountBalanceCard(
                        account = uiState.primaryAccount,
                        isLoading = uiState.isLoading,
                    )
                }

                // Quick Actions
                item {
                    quickActionsSection(
                        onSendMoney = onNavigateToSendMoney,
                        onRequestMoney = onNavigateToRequestMoney,
                        onPayments = onNavigateToPayments,
                        onHistory = onNavigateToHistory,
                    )
                }

                // Statistics Cards
                item {
                    statisticsSection(
                        statistics = uiState.statistics,
                        isLoading = uiState.isLoading,
                    )
                }

                // Recent Transactions
                item {
                    recentTransactionsSection(
                        transactions = uiState.recentTransactions,
                        isLoading = uiState.isLoading,
                        onViewAll = onNavigateToHistory,
                    )
                }

                // More Services
                item {
                    moreServicesSection(
                        onCards = onNavigateToCards,
                        onSavings = onNavigateToSavings,
                    )
                }

                // Add bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun accountBalanceCard(account: com.telepesa.core.domain.model.Account?, isLoading: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp),
                        ),
                )
            } else {
                Text(
                    text = "KSh ${account?.balance ?: "0.00"}",
                    style = TelepesaTextStyles.amountLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Account: ${account?.accountNumber ?: "****"}" ?: "****",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )

                Text(
                    text = "Available: KSh ${account?.availableBalance ?: "0.00"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun quickActionsSection(
    onSendMoney: () -> Unit,
    onRequestMoney: () -> Unit,
    onPayments: () -> Unit,
    onHistory: () -> Unit,
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                quickActionButton(
                    icon = Icons.Default.Send,
                    label = "Send",
                    onClick = onSendMoney,
                )
            }
            item {
                quickActionButton(
                    icon = Icons.Default.Send,
                    label = "Request",
                    onClick = onRequestMoney,
                )
            }
            item {
                quickActionButton(
                    icon = Icons.Default.Send,
                    label = "Pay",
                    onClick = onPayments,
                )
            }
            item {
                quickActionButton(
                    icon = Icons.Default.Send,
                    label = "History",
                    onClick = onHistory,
                )
            }
        }
    }
}

@Composable
private fun quickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun statisticsSection(
    statistics: List<com.telepesa.feature.home.presentation.StatisticItem>,
    isLoading: Boolean,
) {
    Column {
        Text(
            text = "This Month",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(statistics) { statistic ->
                statisticCard(
                    statistic = statistic,
                    isLoading = isLoading,
                )
            }
        }
    }
}

@Composable
private fun statisticCard(statistic: com.telepesa.feature.home.presentation.StatisticItem, isLoading: Boolean) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = statistic.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp),
                        ),
                )
            } else {
                Text(
                    text = "KSh ${statistic.amount}",
                    style = TelepesaTextStyles.amountMedium,
                    color = statistic.color,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun recentTransactionsSection(
    transactions: List<com.telepesa.core.domain.model.Transaction>,
    isLoading: Boolean,
    onViewAll: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            TextButton(onClick = onViewAll) {
                Text("View All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            repeat(3) {
                transactionItemPlaceholder()
                if (it < 2) Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            transactions.take(3).forEachIndexed { index, transaction ->
                transactionItem(transaction = transaction)
                if (index < 2) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun transactionItem(transaction: com.telepesa.core.domain.model.Transaction) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = when (transaction.type) {
                    com.telepesa.core.domain.model.TransactionType.CREDIT -> Icons.Default.Send
                    com.telepesa.core.domain.model.TransactionType.DEBIT -> Icons.Default.Send
                },
                contentDescription = null,
                tint = when (transaction.type) {
                    com.telepesa.core.domain.model.TransactionType.CREDIT -> {
                        MaterialTheme.colorScheme.primary
                    }
                    com.telepesa.core.domain.model.TransactionType.DEBIT -> {
                        MaterialTheme.colorScheme.error
                    }
                },
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )

                Text(
                    text = transaction.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }

            Text(
                text = buildString {
                    if (transaction.type == com.telepesa.core.domain.model.TransactionType.CREDIT) {
                        append("+")
                    } else {
                        append("-")
                    }
                    append("KSh ${transaction.amount}")
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when (transaction.type) {
                    com.telepesa.core.domain.model.TransactionType.CREDIT -> {
                        MaterialTheme.colorScheme.primary
                    }
                    com.telepesa.core.domain.model.TransactionType.DEBIT -> {
                        MaterialTheme.colorScheme.error
                    }
                },
            )
        }
    }
}

@Composable
private fun transactionItemPlaceholder() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp),
                    ),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp),
                        ),
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp),
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp),
                    ),
            )
        }
    }
}

@Composable
private fun moreServicesSection(onCards: () -> Unit, onSavings: () -> Unit) {
    Column {
        Text(
            text = "More Services",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            moreServiceCard(
                icon = Icons.Default.AccountCircle,
                title = "Cards",
                subtitle = "Manage your cards",
                onClick = onCards,
                modifier = Modifier.weight(1f),
            )

            moreServiceCard(
                icon = Icons.Default.AccountCircle,
                title = "Savings",
                subtitle = "Grow your money",
                onClick = onSavings,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun moreServiceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Morning"
        in 12..17 -> "Afternoon"
        else -> "Evening"
    }
}

@Preview(showBackground = true)
@Composable
private fun homeScreenPreview() {
    telepesaTheme {
        homeScreen(
            onNavigateToProfile = {},
            onNavigateToSendMoney = {},
            onNavigateToRequestMoney = {},
            onNavigateToPayments = {},
            onNavigateToHistory = {},
            onNavigateToCards = {},
            onNavigateToSavings = {},
        )
    }
}
