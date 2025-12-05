package com.root2rise.bookkeeping.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.root2rise.bookkeeping.ai.AiService
import com.root2rise.bookkeeping.ai.TransactionAiService
import com.root2rise.bookkeeping.data.TransactionEntity
import com.root2rise.bookkeeping.model.AiResponseParser
import com.root2rise.bookkeeping.model.QuerySchema
import com.root2rise.bookkeeping.model.TransactionAiResponseParser
import com.root2rise.bookkeeping.model.TransactionSchema
import com.root2rise.bookkeeping.repository.BookkeepingRepository
import com.root2rise.bookkeeping.repository.QueryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookkeepingViewModel(
    private val repository: BookkeepingRepository,
    private val aiService: AiService,
    private val transactionAiService: TransactionAiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    private val _lastResponse = MutableStateFlow<String>("")
    val lastResponse: StateFlow<String> = _lastResponse.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions().collect { txns ->
                _transactions.value = txns
            }
        }
    }

    fun processVoiceInput(utterance: String) {
        processTranscription(utterance) { /* The result is spoken by the voice service */ }
    }

    fun processTranscription(transcription: String, onResult: (String) -> Unit) {
        if (transcription.isBlank()) {
            _uiState.value = UiState.Error("Please say something")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Processing

            try {
                val jsonResponse = aiService.processUtterance(transcription)
                val aiResponse = AiResponseParser.parse(jsonResponse)

                if (aiResponse == null) {
                    _uiState.value = UiState.Error("Could not understand the input")
                    onResult("Could not understand the input")
                    return@launch
                }

                when (aiResponse) {
                    is TransactionSchema -> {
                        val result = repository.addTransaction(aiResponse)
                        if (result.isSuccess) {
                            val responseText = buildTransactionResponse(aiResponse)
                            _lastResponse.value = responseText
                            _uiState.value = UiState.Success(responseText)
                            onResult(responseText)
                        } else {
                            _uiState.value = UiState.Error("Failed to save transaction")
                            onResult("Failed to save transaction")
                        }
                    }
                    is QuerySchema -> {
                        val result = repository.executeQuery(aiResponse)
                        if (result.isSuccess) {
                            val queryResult = result.getOrNull()
                            val responseText = buildQueryResponse(queryResult)
                            _lastResponse.value = responseText
                            _uiState.value = UiState.Success(responseText)
                            onResult(responseText)
                        } else {
                            _uiState.value = UiState.Error("Failed to execute query")
                            onResult("Failed to execute query")
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error: ${e.message}")
                onResult("Error: ${e.message}")
            }
        }
    }

    fun processTransactionUpdate(transaction: TransactionEntity, command: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val jsonResponse = transactionAiService.processTransactionUpdate(command)
            val response = TransactionAiResponseParser.parse(jsonResponse)

            if (response == null) {
                onResult("Could not understand the command.")
                return@launch
            }

            when (response.intent) {
                "delete_transaction" -> {
                    repository.deleteTransaction(transaction)
                    onResult("Transaction deleted.")
                }
                "read_aloud_transaction" -> {
                    val text = "Transaction: ${transaction.amount} rupees, notes: ${transaction.notes ?: "not available"}"
                    onResult(text)
                }
                "modify_transaction" -> {
                    val fields = response.fields
                    if (fields != null) {
                        val newAmount = fields.amount ?: transaction.amount
                        val newNotes = fields.notes ?: transaction.notes
                        val updatedTransaction = transaction.copy(amount = newAmount, notes = newNotes)
                        repository.updateTransaction(updatedTransaction)
                        onResult("Transaction updated.")
                    } else {
                        onResult("Could not understand the changes.")
                    }
                }
                else -> {
                    onResult("I can only modify, delete, or read transactions.")
                }
            }
        }
    }

    private fun buildTransactionResponse(schema: TransactionSchema): String {
        val amountText = "₹${schema.amount.toInt()}"
        return when (schema.type) {
            "sale" -> "Sale recorded: $amountText"
            "purchase" -> "Purchase recorded: $amountText"
            "loan_given" -> {
                val party = schema.partyName ?: "Unknown"
                "Loan given to $party: $amountText"
            }

            "loan_taken" -> {
                val party = schema.partyName ?: "Unknown"
                "Loan taken from $party: $amountText"
            }

            "expense" -> "Expense recorded: $amountText"
            else -> "Transaction recorded: $amountText"
        }
    }

    private fun buildQueryResponse(queryResult: QueryResult?): String {
        return when (queryResult) {
            is QueryResult.Sales -> {
                val amount = "₹${queryResult.total.toInt()}"
                val period = when (queryResult.timeRange) {
                    "today" -> "Today's"
                    "yesterday" -> "Yesterday's"
                    "this_week" -> "This week's"
                    "this_month" -> "This month's"
                    else -> "Total"
                }
                "$period sales: $amount"
            }

            is QueryResult.Expenses -> {
                val amount = "₹${queryResult.total.toInt()}"
                val period = when (queryResult.timeRange) {
                    "today" -> "Today's"
                    "yesterday" -> "Yesterday's"
                    "this_week" -> "This week's"
                    "this_month" -> "This month's"
                    else -> "Total"
                }
                "$period expenses: $amount"
            }

            is QueryResult.Summary -> {
                val amount = "₹${kotlin.math.abs(queryResult.balance).toInt()}"
                if (queryResult.balance >= 0) {
                    "Overall profit: $amount"
                } else {
                    "Overall loss: $amount"
                }
            }

            is QueryResult.PartyBalance -> {
                val amount = "₹${kotlin.math.abs(queryResult.balance).toInt()}"
                when {
                    queryResult.balance > 0 -> "${queryResult.partyName} owes you: $amount"
                    queryResult.balance < 0 -> "You owe ${queryResult.partyName}: $amount"
                    else -> "Balance with ${queryResult.partyName} is clear"
                }
            }

            is QueryResult.Error -> queryResult.message
            null -> "No result found"
        }
    }
}

sealed class UiState {
    object Idle : UiState()
    object Processing : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
}

class BookkeepingViewModelFactory(
    private val repository: BookkeepingRepository,
    private val aiService: AiService,
    private val transactionAiService: TransactionAiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookkeepingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookkeepingViewModel(repository, aiService, transactionAiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
