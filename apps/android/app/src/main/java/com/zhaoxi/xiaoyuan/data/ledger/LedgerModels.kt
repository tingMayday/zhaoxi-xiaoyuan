package com.zhaoxi.xiaoyuan.data.ledger

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val kind: String,
)

@Serializable
data class FinanceSummary(
    val incomeCents: Int,
    val expenseCents: Int,
    val balanceCents: Int,
    val totalBudgetCents: Int? = null,
    val remainingCents: Int? = null,
    val usedRatio: Double? = null,
    val categories: List<CategorySpend> = emptyList(),
)

@Serializable
data class CategorySpend(
    val id: String,
    val name: String,
    val amountCents: Int,
    val budgetCents: Int? = null,
)

@Serializable
data class Transaction(
    val id: String,
    val occurredOn: String,
    val direction: String,
    val amountCents: Int,
    val note: String = "",
    val category: CategoryName = CategoryName(),
)

@Serializable
data class CategoryName(val name: String = "")

@Serializable
data class NewTransaction(
    val occurredOn: String,
    val direction: String,
    val amountCents: Int,
    val categoryId: String,
    val note: String = "",
)

@Serializable
data class UpsertBudget(
    val yearMonth: String,
    val categoryKey: String = "total",
    val amountCents: Int,
)
