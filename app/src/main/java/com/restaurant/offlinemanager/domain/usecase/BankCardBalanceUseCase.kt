package com.restaurant.offlinemanager.domain.usecase

import com.restaurant.offlinemanager.data.local.entity.PurchasePaymentType
import com.restaurant.offlinemanager.domain.model.BankCardBalance
import com.restaurant.offlinemanager.domain.model.RestaurantSnapshot
import com.restaurant.offlinemanager.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BankCardBalanceUseCase(private val repository: RestaurantRepository) {
    fun observeBalances(): Flow<List<BankCardBalance>> =
        repository.observeSnapshot().map(::calculateBalances)

    fun calculateBalances(snapshot: RestaurantSnapshot): List<BankCardBalance> =
        snapshot.bankCards.map { card ->
            val incoming = snapshot.projectPayments.filter { it.bankCardId == card.id }.sumOf { it.amount }
            val cardPurchases = snapshot.purchases
                .filter { it.bankCardId == card.id && it.paymentType == PurchasePaymentType.CARD }
                .sumOf { it.paidAmount }
            val supplierPaid = snapshot.supplierPayments.filter { it.bankCardId == card.id }.sumOf { it.amount }
            val expenses = snapshot.expenses.filter { it.bankCardId == card.id }.sumOf { it.amount }
            BankCardBalance(card, card.initialBalance + incoming - cardPurchases - supplierPaid - expenses)
        }.sortedByDescending { it.balance }

    fun totalBalance(snapshot: RestaurantSnapshot): Long =
        calculateBalances(snapshot).sumOf { it.balance }
}
