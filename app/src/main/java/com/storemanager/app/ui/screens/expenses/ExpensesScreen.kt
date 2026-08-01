package com.storemanager.app.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storemanager.app.data.entity.Expense
import com.storemanager.app.data.entity.ExpenseCategory
import com.storemanager.app.data.repository.StoreRepository
import com.storemanager.app.ui.repoViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpensesViewModel(private val repo: StoreRepository) : ViewModel() {
    val expenses = repo.getExpenses().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repo.getExpenseCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String) = viewModelScope.launch { repo.addExpenseCategory(ExpenseCategory(name = name)) }

    fun addExpense(categoryId: Long, categoryName: String, amount: Double, note: String) = viewModelScope.launch {
        repo.addExpense(Expense(categoryId = categoryId, categoryName = categoryName, amount = amount, note = note))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen() {
    val vm = repoViewModel { ExpensesViewModel(it) }
    val expenses by vm.expenses.collectAsState()
    val categories by vm.categories.collectAsState()
    var showAddExpense by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    val df = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { showAddExpense = true }) { Icon(Icons.Filled.Add, contentDescription = "Add expense") }
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row {
                Text("Expenses", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { showAddCategory = true }) { Text("+ Category") }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(expenses, key = { it.id }) { expense ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(expense.categoryName, style = MaterialTheme.typography.titleMedium)
                                Text(expense.note, style = MaterialTheme.typography.bodyMedium)
                                Text(df.format(Date(expense.createdAt)), style = MaterialTheme.typography.bodyMedium)
                            }
                            Text("₹${expense.amount}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }

    if (showAddExpense) {
        var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
        var amount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddExpense = false }) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Add Expense", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "", onValueChange = {}, readOnly = true,
                            label = { Text("Category") }, modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategory = cat; expanded = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it }, label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            selectedCategory?.let { vm.addExpense(it.id, it.name, amount.toDoubleOrNull() ?: 0.0, note) }
                            showAddExpense = false
                        },
                        enabled = selectedCategory != null && amount.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save") }
                }
            }
        }
    }

    if (showAddCategory) {
        var name by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAddCategory = false }) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("New Category", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { vm.addCategory(name); showAddCategory = false }, enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Save") }
                }
            }
        }
    }
}
