# Quick Start Guide - Bookkeeping Assistant

Get the app running in 5 minutes! ⚡

## 🚀 Prerequisites

- Android Studio (latest version)
- Android device or emulator (Android 7.0+)
- Basic knowledge of Kotlin and Android

## 📥 Setup

### Step 1: Clone or Open Project

```bash
cd C:/Users/singh/AndroidStudioProjects/bookkeeping
```

### Step 2: Sync Gradle

Open the project in Android Studio and wait for Gradle sync to complete.

### Step 3: Build the Project

```bash
./gradlew build
```

Or use Android Studio: **Build > Make Project**

### Step 4: Run on Device/Emulator

Click the **Run** button (▶️) in Android Studio or:

```bash
./gradlew installDebug
```

## 🎯 First Run

### 1. Open the App

You'll see the home screen with:

- Title: "दुकान का हिसाब / Bookkeeping Assistant"
- Three quick action buttons
- A large floating action button (+)

### 2. Add Your First Transaction

1. Tap the **floating action button** (big + at bottom right)
2. Type: `Aaj 2000 ki bikri hui`
3. Tap **Submit**
4. You'll see: "Sale recorded: ₹2000" ✅
5. The transaction appears in the list below

### 3. Try More Examples

**Record an expense:**

```
Bijli ka bill 900 rupaye bhar diya
```

Result: "Expense recorded: ₹900"

**Record a loan taken:**

```
Ramesh se 500 rupaye liye udhar
```

Result: "Loan taken from Ramesh: ₹500"

**Query today's sales:**

```
Aaj ki total bikri kitni hai?
```

Result: "Today's sales: ₹2000"

### 4. Use Quick Actions

Tap the quick action buttons:

- **Today's Sales**: See total sales for today
- **Expenses**: See total expenses for today
- **Summary**: See overall profit/loss

## 🧪 Testing the AI

The app currently uses `MockAiService` which understands these patterns:

### Transaction Patterns

| Input | What It Does |
|-------|--------------|
| `[name] se [amount] liye udhar` | Records loan taken from [name] |
| `[name] ko [amount] diya udhar` | Records loan given to [name] |
| `[amount] ki bikri` | Records sale |
| `[amount] ka kharcha` | Records expense |
| `[item] ka bill [amount]` | Records expense for [item] |

### Query Patterns

| Input | What It Does |
|-------|--------------|
| `aaj ki bikri kitni?` | Shows today's sales |
| `aaj ka kharcha?` | Shows today's expenses |
| `[name] ka balance?` | Shows balance with [name] |
| `overall summary?` | Shows profit/loss |

## 📱 Try These Demo Scenarios

### Scenario 1: Daily Shop Operations

```
1. "aaj 3000 ki bikri hui"        → Sale: ₹3000
2. "chai pani 100 rupaye"          → Expense: ₹100
3. "petrol mein 500"               → Expense: ₹500
4. "aaj ki total bikri kitni?"     → Sales: ₹3000
5. "aaj ka kharcha kitna?"         → Expenses: ₹600
```

### Scenario 2: Managing Loans

```
1. "Ramesh se 1000 liye udhar"     → Loan taken: ₹1000
2. "Sunil ko 500 diya udhar"       → Loan given: ₹500
3. "Ramesh ka balance?"            → You owe Ramesh: ₹1000
4. "Sunil ka balance?"             → Sunil owes you: ₹500
```

### Scenario 3: End of Day

```
1. "aaj ki total bikri?"           → Today's sales
2. "aaj ka kharcha?"               → Today's expenses
3. "overall summary?"              → Total profit/loss
```

## 🎨 Understanding the UI

### Home Screen Components

```
┌───────────────────────────────────┐
│ 🏪 दुकान का हिसाब                │ ← Title
│    Bookkeeping Assistant          │
├───────────────────────────────────┤
│ ✓ Sale recorded: ₹2000           │ ← Success card
├───────────────────────────────────┤
│ [Sales] [Expenses] [Summary]     │ ← Quick actions
├───────────────────────────────────┤
│ Recent Transactions               │
│                                   │
│ 🛒 Sale              +₹2000      │ ← Transaction card
│    Today                          │    (Green = money in)
│                                   │
│ 🗑️ Expense           -₹900       │ ← Transaction card
│    Electricity bill               │    (Red = money out)
│    Today                          │
└───────────────────────────────────┘
                  ⊕                   ← Floating button
```

### Transaction Cards

- **Green (+)**: Money coming in (sales, loans received)
- **Red (-)**: Money going out (expenses, loans given)
- **Icons**: Indicate transaction type
- **Name**: Party name (if applicable)
- **Notes**: Additional details
- **Date**: When it happened

## 🛠️ Development Tips

### Enable Debug Logging

Add this to see AI service logs:

```kotlin
// In MockAiService.kt
override suspend fun processUtterance(utterance: String): String {
    Log.d("AI_SERVICE", "Input: $utterance")
    val json = // ... process utterance
    Log.d("AI_SERVICE", "Output: $json")
    return json
}
```

### Test Different Inputs

Modify `MockAiService.kt` to add custom patterns:

```kotlin
when {
    lowerUtterance.contains("your_custom_pattern") -> {
        // Your custom JSON response
    }
}
```

### Inspect Database

Use Android Studio Database Inspector:

1. **View > Tool Windows > App Inspection**
2. Select your device/emulator
3. Click **Database Inspector**
4. View `transactions` table

## 🔧 Common Issues

### Issue: Build fails with missing dependencies

**Solution:**

```bash
./gradlew clean build
```

### Issue: App crashes on launch

**Solution:**

- Check Android version (needs 7.0+)
- Clear app data
- Rebuild: **Build > Clean Project** then **Build > Rebuild Project**

### Issue: Transactions not appearing

**Solution:**

- Check if success message appears
- Scroll down in transaction list
- Check database using Database Inspector

### Issue: AI doesn't understand input

**Solution:**

- Use simpler Hinglish
- Check spelling
- Try example patterns from above
- Remember: It's a mock service with limited patterns

## 📚 Next Steps

### For Users

1. Read **USAGE_EXAMPLES.md** for more patterns
2. Try all transaction types
3. Experiment with queries
4. Check transaction history

### For Developers

1. Read **PROJECT_SUMMARY.md** for architecture overview
2. Read **INTEGRATION_GUIDE.md** to add real AI model
3. Read **TESTING_GUIDE.md** to add tests
4. Read **AI_PROMPTS_GUIDE.md** to understand AI training

### Customization

**Change app name:**

- Edit `app/src/main/res/values/strings.xml`

**Change colors:**

- Edit `app/src/main/java/com/root2rise/bookkeeping/ui/theme/Color.kt`

**Add new transaction types:**

- Update `TransactionSchema.type` in `AiSchemas.kt`
- Add handling in `MockAiService.kt`

**Add new query types:**

- Update `QuerySchema.action` in `AiSchemas.kt`
- Add query in `TransactionDao.kt`
- Handle in `BookkeepingRepository.kt`

## ✅ Verification Checklist

Make sure everything works:

- [ ] App builds without errors
- [ ] App runs on device/emulator
- [ ] Can open input dialog
- [ ] Can submit a transaction
- [ ] Transaction appears in list
- [ ] Quick action buttons work
- [ ] Can query data
- [ ] Success messages appear
- [ ] Colors are correct (green/red)
- [ ] No crashes

## 🎉 You're Ready!

You now have a working bookkeeping app!

**Next: Add real transactions and test with actual usage patterns.**

For detailed usage, see **USAGE_EXAMPLES.md**.
For development, see **PROJECT_SUMMARY.md**.

---

**Need Help?** Check the documentation files or raise an issue.

**Happy Bookkeeping! 📚💰**
