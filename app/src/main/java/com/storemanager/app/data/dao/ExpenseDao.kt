package com.storemanager.app.data.dao

import androidx.room.*
import com.storemanager.app.data.entity.Expense
import com.storemanager.app.data.entity.ExpenseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    fun getBetween(start: Long, end: Long): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount),0) FROM expenses WHERE createdAt BETWEEN :start AND :end")
    fun getTotalBetween(start: Long, end: Long): Flow<Double>

    @Insert
    suspend fun insert(expense: Expense): Long

    @Query("SELECT * FROM expense_categories ORDER BY name ASC")
    fun getCategories(): Flow<List<ExpenseCategory>>

    @Insert
    suspend fun insertCategory(category: ExpenseCategory): Long

    @Delete
    suspend fun deleteCategory(category: ExpenseCategory)
}
