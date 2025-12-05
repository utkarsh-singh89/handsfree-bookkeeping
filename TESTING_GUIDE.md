# Testing Guide - Bookkeeping Assistant

This document outlines the testing strategy for the bookkeeping app.

## 🧪 Test Categories

### 1. Unit Tests

Test individual components in isolation

### 2. Integration Tests

Test interaction between components

### 3. UI Tests

Test user interface and user flows

### 4. AI Model Tests

Test natural language understanding

## 📋 Unit Test Checklist

### Repository Tests

**File**: `BookkeepingRepositoryTest.kt`

```kotlin
class BookkeepingRepositoryTest {
    
    @Test
    fun `addTransaction with valid schema returns success`()
    
    @Test
    fun `executeQuery for total sales returns correct sum`()
    
    @Test
    fun `executeQuery for party balance calculates correctly`()
    
    @Test
    fun `normalizeDateString converts today to current date`()
    
    @Test
    fun `normalizeDateString converts yesterday correctly`()
}
```

### ViewModel Tests

**File**: `BookkeepingViewModelTest.kt`

```kotlin
class BookkeepingViewModelTest {
    
    @Test
    fun `processVoiceInput with sale transaction updates state to Success`()
    
    @Test
    fun `processVoiceInput with query returns correct response`()
    
    @Test
    fun `processVoiceInput with empty string updates state to Error`()
    
    @Test
    fun `buildTransactionResponse formats sale correctly`()
    
    @Test
    fun `buildQueryResponse formats sales query correctly`()
}
```

### AI Service Tests

**File**: `MockAiServiceTest.kt`

```kotlin
class MockAiServiceTest {
    
    @Test
    fun `loan taken pattern returns correct JSON`()
    
    @Test
    fun `loan given pattern returns correct JSON`()
    
    @Test
    fun `sale pattern returns correct JSON`()
    
    @Test
    fun `expense pattern returns correct JSON`()
    
    @Test
    fun `sales query returns correct JSON`()
    
    @Test
    fun `balance query returns correct JSON`()
    
    @Test
    fun `extractAmount finds number in text`()
    
    @Test
    fun `extractPartyName finds name before se or ko`()
    
    @Test
    fun `extractTimeRange identifies today, yesterday, etc`()
}
```

### Schema Parser Tests

**File**: `AiResponseParserTest.kt`

```kotlin
class AiResponseParserTest {
    
    @Test
    fun `parse valid TransactionSchema returns object`()
    
    @Test
    fun `parse valid QuerySchema returns object`()
    
    @Test
    fun `parse invalid JSON returns null`()
    
    @Test
    fun `parse with unknown kind returns null`()
}
```

## 🔗 Integration Tests

### Database Integration

**File**: `TransactionDaoTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {
    
    private lateinit var database: BookkeepingDatabase
    private lateinit var dao: TransactionDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BookkeepingDatabase::class.java).build()
        dao = database.transactionDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveTransaction() = runBlocking {
        val transaction = TransactionEntity(
            direction = "in",
            type = "sale",
            partyName = null,
            amount = 1000.0,
            date = "2024-01-15",
            notes = "Test sale"
        )
        
        val id = dao.insertTransaction(transaction)
        val transactions = dao.getAllTransactions().first()
        
        assertEquals(1, transactions.size)
        assertEquals(1000.0, transactions[0].amount, 0.01)
    }
    
    @Test
    fun getTotalSales() = runBlocking {
        // Insert multiple sales
        dao.insertTransaction(TransactionEntity(
            direction = "in", type = "sale", partyName = null,
            amount = 1000.0, date = "2024-01-15", notes = null
        ))
        dao.insertTransaction(TransactionEntity(
            direction = "in", type = "sale", partyName = null,
            amount = 2000.0, date = "2024-01-15", notes = null
        ))
        
        val total = dao.getTotalSales("2024-01-15")
        assertEquals(3000.0, total, 0.01)
    }
    
    @Test
    fun getBalanceWithParty() = runBlocking {
        // Loan taken (in)
        dao.insertTransaction(TransactionEntity(
            direction = "in", type = "loan_taken", partyName = "Ramesh",
            amount = 1000.0, date = "2024-01-15", notes = null
        ))
        // Partial payment (out)
        dao.insertTransaction(TransactionEntity(
            direction = "out", type = "other", partyName = "Ramesh",
            amount = 300.0, date = "2024-01-16", notes = null
        ))
        
        val balance = dao.getBalanceWithParty("Ramesh")
        assertEquals(700.0, balance, 0.01) // 1000 - 300 = 700 (we owe Ramesh)
    }
}
```

### End-to-End Flow Tests

**File**: `TransactionFlowTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class TransactionFlowTest {
    
    private lateinit var repository: BookkeepingRepository
    private lateinit var aiService: AiService
    private lateinit var viewModel: BookkeepingViewModel
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(
            context, 
            BookkeepingDatabase::class.java
        ).build()
        
        repository = BookkeepingRepository(database)
        aiService = MockAiService()
        viewModel = BookkeepingViewModel(repository, aiService)
    }
    
    @Test
    fun completeSaleTransactionFlow() = runBlocking {
        // User speaks
        viewModel.processVoiceInput("Aaj 2000 ki bikri hui")
        
        // Wait for processing
        delay(100)
        
        // Check state
        val state = viewModel.uiState.first()
        assertTrue(state is UiState.Success)
        
        // Check transaction was saved
        val transactions = viewModel.transactions.first()
        assertEquals(1, transactions.size)
        assertEquals(2000.0, transactions[0].amount, 0.01)
        assertEquals("sale", transactions[0].type)
    }
    
    @Test
    fun completeLoanTakenFlow() = runBlocking {
        viewModel.processVoiceInput("Ramesh se 500 rupaye liye udhar")
        delay(100)
        
        val transactions = viewModel.transactions.first()
        assertEquals(1, transactions.size)
        assertEquals("in", transactions[0].direction)
        assertEquals("loan_taken", transactions[0].type)
        assertEquals("Ramesh", transactions[0].partyName)
        assertEquals(500.0, transactions[0].amount, 0.01)
    }
    
    @Test
    fun completeSalesQueryFlow() = runBlocking {
        // Add some sales
        viewModel.processVoiceInput("1000 ki bikri")
        delay(100)
        viewModel.processVoiceInput("2000 ka saman becha")
        delay(100)
        
        // Query total
        viewModel.processVoiceInput("Aaj ki total bikri kitni hai?")
        delay(100)
        
        val response = viewModel.lastResponse.first()
        assertTrue(response.contains("3000"))
    }
}
```

## 🎨 UI Tests

### Compose UI Tests

**File**: `HomeScreenTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun appDisplaysTitle() {
        composeTestRule.onNodeWithText("दुकान का हिसाब").assertIsDisplayed()
    }
    
    @Test
    fun floatingActionButtonIsDisplayed() {
        composeTestRule.onNodeWithContentDescription("Add Transaction")
            .assertIsDisplayed()
    }
    
    @Test
    fun clickingFABOpensDialog() {
        composeTestRule.onNodeWithContentDescription("Add Transaction").performClick()
        composeTestRule.onNodeWithText("Add Transaction").assertIsDisplayed()
    }
    
    @Test
    fun quickActionButtonsAreDisplayed() {
        composeTestRule.onNodeWithText("Today's Sales").assertIsDisplayed()
        composeTestRule.onNodeWithText("Expenses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Summary").assertIsDisplayed()
    }
    
    @Test
    fun canEnterAndSubmitTransaction() {
        // Open dialog
        composeTestRule.onNodeWithContentDescription("Add Transaction").performClick()
        
        // Enter text
        composeTestRule.onNode(hasSetTextAction())
            .performTextInput("Aaj 2000 ki bikri hui")
        
        // Submit
        composeTestRule.onNodeWithText("Submit").performClick()
        
        // Verify success message
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sale recorded", substring = true)
            .assertIsDisplayed()
    }
    
    @Test
    fun transactionsAreDisplayedInList() {
        // Mock some data first
        // ... (inject test data)
        
        composeTestRule.onNodeWithText("Recent Transactions").assertIsDisplayed()
        composeTestRule.onNode(hasScrollAction()).performScrollToIndex(0)
    }
}
```

## 🤖 AI Model Tests

### NLP Accuracy Tests

**File**: `AiModelAccuracyTest.kt`

Test the AI model's ability to understand various inputs:

```kotlin
class AiModelAccuracyTest {
    
    private lateinit var aiService: AiService
    
    @Before
    fun setup() {
        aiService = MockAiService() // or FirebenderAiService for production
    }
    
    @Test
    fun `recognizes sale transactions correctly`() = runBlocking {
        val testCases = listOf(
            "Aaj 2000 ki bikri hui",
            "3500 ka saman becha",
            "500 rupaye ki bikri",
            "daily sales 4500"
        )
        
        testCases.forEach { input ->
            val json = aiService.processUtterance(input)
            val schema = AiResponseParser.parse(json) as? TransactionSchema
            
            assertNotNull(schema)
            assertEquals("transaction", schema?.kind)
            assertEquals("sale", schema?.type)
            assertEquals("in", schema?.direction)
            assertTrue((schema?.amount ?: 0.0) > 0)
        }
    }
    
    @Test
    fun `recognizes loan taken correctly`() = runBlocking {
        val testCases = mapOf(
            "Ramesh se 500 rupaye liye udhar" to Pair("Ramesh", 500.0),
            "mukesh se 1000 liye" to Pair("Mukesh", 1000.0)
        )
        
        testCases.forEach { (input, expected) ->
            val json = aiService.processUtterance(input)
            val schema = AiResponseParser.parse(json) as? TransactionSchema
            
            assertNotNull(schema)
            assertEquals("loan_taken", schema?.type)
            assertEquals("in", schema?.direction)
            assertTrue(schema?.partyName?.contains(expected.first, ignoreCase = true) == true)
            assertEquals(expected.second, schema?.amount, 0.01)
        }
    }
    
    @Test
    fun `recognizes queries correctly`() = runBlocking {
        val testCases = mapOf(
            "Aaj ki total bikri kitni hai?" to "query_total_sales",
            "Ramesh ka kitna baki hai?" to "query_balance",
            "ab tak ka summary?" to "query_overall_summary"
        )
        
        testCases.forEach { (input, expectedAction) ->
            val json = aiService.processUtterance(input)
            val schema = AiResponseParser.parse(json) as? QuerySchema
            
            assertNotNull(schema)
            assertEquals("query", schema?.kind)
            assertEquals(expectedAction, schema?.action)
        }
    }
    
    @Test
    fun `handles noisy input`() = runBlocking {
        val noisyInputs = listOf(
            "ram se 50 0 rupy lie udar",  // Should extract 500
            "bijli ka bil 90 0 rupaee",   // Should extract 900
            "aaj 200 0 ki bikree"          // Should extract 2000
        )
        
        noisyInputs.forEach { input ->
            val json = aiService.processUtterance(input)
            val schema = AiResponseParser.parse(json)
            
            assertNotNull(schema) // Should still parse successfully
        }
    }
}
```

## 📊 Test Coverage Goals

### Minimum Coverage Targets

- **Unit Tests**: 80%
- **Integration Tests**: 70%
- **UI Tests**: 60%
- **AI Model Accuracy**: 90%

### Critical Paths (100% Coverage Required)

1. Transaction recording flow
2. Query execution flow
3. Database operations
4. JSON parsing
5. Amount extraction

## 🚀 Running Tests

### Run All Tests

```bash
./gradlew test
```

### Run Unit Tests Only

```bash
./gradlew testDebugUnitTest
```

### Run Instrumentation Tests

```bash
./gradlew connectedDebugAndroidTest
```

### Run with Coverage

```bash
./gradlew jacocoTestReport
```

### Run Specific Test Class

```bash
./gradlew test --tests BookkeepingRepositoryTest
```

## 📈 Continuous Integration

### CI Pipeline

```yaml
name: Android CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Run instrumentation tests
      run: ./gradlew connectedAndroidTest
    
    - name: Generate coverage report
      run: ./gradlew jacocoTestReport
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v2
```

## 🐛 Test Data

### Sample Transaction Data

```kotlin
object TestData {
    val sampleTransactions = listOf(
        TransactionEntity(
            id = 1,
            direction = "in",
            type = "sale",
            partyName = null,
            amount = 2000.0,
            date = "2024-01-15",
            notes = "Daily sales",
            timestamp = System.currentTimeMillis()
        ),
        TransactionEntity(
            id = 2,
            direction = "out",
            type = "expense",
            partyName = null,
            amount = 900.0,
            date = "2024-01-15",
            notes = "Electricity bill",
            timestamp = System.currentTimeMillis()
        ),
        TransactionEntity(
            id = 3,
            direction = "in",
            type = "loan_taken",
            partyName = "Ramesh",
            amount = 1000.0,
            date = "2024-01-14",
            notes = "Loan from Ramesh",
            timestamp = System.currentTimeMillis()
        )
    )
    
    val sampleSaleInputs = listOf(
        "Aaj 2000 ki bikri hui",
        "3500 ka saman becha",
        "Priya ko 1200 ka saman becha"
    )
    
    val sampleExpenseInputs = listOf(
        "Bijli ka bill 900 rupaye bhar diya",
        "chai pani mein 150 kharcha",
        "rent 5000 pay kiya"
    )
}
```

## ✅ Test Checklist

Before each release, verify:

- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] UI tests pass on multiple devices
- [ ] AI model accuracy > 90%
- [ ] Database migrations work correctly
- [ ] App works offline
- [ ] No memory leaks
- [ ] Proper error handling
- [ ] Accessibility features work
- [ ] Performance benchmarks met

## 🔍 Debugging Tests

### View Test Reports

After running tests, find reports at:

- Unit tests: `app/build/reports/tests/testDebugUnitTest/index.html`
- Coverage: `app/build/reports/jacoco/index.html`

### Common Issues

**Issue**: Tests fail with database errors
**Solution**: Ensure using `inMemoryDatabaseBuilder` for tests

**Issue**: UI tests are flaky
**Solution**: Add proper wait conditions and use `composeTestRule.waitForIdle()`

**Issue**: Mock AI service not returning valid JSON
**Solution**: Verify regex patterns and JSON formatting

---

**Remember**: Good tests are the foundation of a reliable app. Test early, test often! 🧪
