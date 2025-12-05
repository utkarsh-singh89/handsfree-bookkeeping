# Usage Examples - Bookkeeping Assistant

This document provides comprehensive examples of how users interact with the app using natural
Hinglish language.

## 🗣️ How to Use the App

1. Open the app to see your transaction history
2. Tap the **floating action button** (big + button) at the bottom right
3. Type or speak your transaction in natural Hinglish
4. The app will automatically understand and record it

## 📝 Transaction Examples

### 1. Recording Sales (Bikri)

**What you say** → **What happens**

| Input | Result |
|-------|--------|
| "Aaj 2000 ki bikri hui" | Records ₹2000 in sales for today |
| "3500 ka saman becha" | Records ₹3500 in sales |
| "500 rupaye ki bikri kal hui thi" | Records ₹500 sales for yesterday |
| "Ramesh ko 1200 ka saman becha" | Records ₹1200 sale to Ramesh |
| "daily sales 4500" | Records ₹4500 in sales |

### 2. Recording Expenses (Kharcha)

| Input | Result |
|-------|--------|
| "Bijli ka bill 900 rupaye bhar diya" | Records ₹900 electricity expense |
| "chai pani mein 150 kharcha hua" | Records ₹150 tea/snacks expense |
| "rent 5000 pay kar diya" | Records ₹5000 rent expense |
| "petrol mein 800 lagaye" | Records ₹800 petrol expense |
| "staff salary 3000 diye" | Records ₹3000 salary expense |
| "kirana 1500 ka liya" | Records ₹1500 grocery expense |

### 3. Recording Loans Taken (Udhar Liya - Money Coming In)

When someone gives you money (loan):

| Input | Result |
|-------|--------|
| "Ramesh se 500 rupaye liye udhar mein" | Records ₹500 loan from Ramesh |
| "Mukesh ko 1000 rupees liye the" | Records ₹1000 loan from Mukesh |
| "Sunil se kal 2000 udhar liya tha" | Records ₹2000 loan from Sunil (yesterday) |
| "amit se 5000 loan liya" | Records ₹5000 loan from Amit |

### 4. Recording Loans Given (Udhar Diya - Money Going Out)

When you give someone money (loan):

| Input | Result |
|-------|--------|
| "Suresh ko 300 rupaye diya udhar" | Records ₹300 loan given to Suresh |
| "Ravi ko 2000 diye the kal" | Records ₹2000 loan given to Ravi (yesterday) |
| "priya ko 1500 udhar diye" | Records ₹1500 loan given to Priya |
| "vijay ko 800 loan diya" | Records ₹800 loan given to Vijay |

### 5. Recording Purchases (Kharid)

| Input | Result |
|-------|--------|
| "800 ka stock liya" | Records ₹800 stock purchase |
| "naya saman 3000 ka liya" | Records ₹3000 inventory purchase |
| "wholesale se 5000 ka maal liya" | Records ₹5000 wholesale purchase |

## ❓ Query Examples

### 1. Check Today's Sales

| Input | What You Get |
|-------|--------------|
| "Aaj ki total bikri kitni hai?" | Today's sales: ₹5000 |
| "aaj kitna becha?" | Today's sales: ₹5000 |
| "today sales kitni hui?" | Today's sales: ₹5000 |

### 2. Check Today's Expenses

| Input | What You Get |
|-------|--------------|
| "Aaj ka total kharcha kitna hai?" | Today's expenses: ₹1200 |
| "aaj kitna kharch hua?" | Today's expenses: ₹1200 |
| "today expenses kitne hue?" | Today's expenses: ₹1200 |

### 3. Check Overall Profit/Loss

| Input | What You Get |
|-------|--------------|
| "Ab tak ka overall summary kya hai?" | Overall profit: ₹15,000 (or loss) |
| "total profit kitna hai?" | Overall profit: ₹15,000 |
| "kitna faida hua ab tak?" | Overall profit: ₹15,000 |
| "overall balance kya hai?" | Overall profit: ₹15,000 |

### 4. Check Balance with Someone

| Input | What You Get |
|-------|--------------|
| "Ramesh ka kitna baki hai?" | Ramesh owes you: ₹500 (or you owe Ramesh) |
| "Sunil ka balance kya hai?" | You owe Sunil: ₹2000 (or Sunil owes you) |
| "mukesh ke saath kitna baki?" | Balance with Mukesh |

### 5. Weekly/Monthly Queries

| Input | What You Get |
|-------|--------------|
| "Is hafte ki sales kitni hai?" | This week's sales: ₹25,000 |
| "is mahine mein kitni bikri hui?" | This month's sales: ₹80,000 |
| "this week ka kharcha kitna?" | This week's expenses: ₹8,000 |

## 💡 Smart Features

### Quick Action Buttons

The app provides three quick action buttons on the home screen:

1. **Today's Sales** - Tap to instantly see today's total sales
2. **Expenses** - Tap to see today's expenses
3. **Summary** - Tap for overall profit/loss summary

### Transaction Cards

Each transaction is displayed as a colorful card showing:

- **Icon** based on transaction type (sale, expense, loan)
- **Amount** in green (money in) or red (money out)
- **Party name** if applicable
- **Date and notes**

### Voice to Text Tips

For best results when using voice input:

✅ **Do's:**

- Speak clearly and at normal pace
- Use common Hinglish words
- Mention amount clearly
- Say person's name before "se" or "ko"

❌ **Don'ts:**

- Don't speak too fast
- Avoid complex sentences
- Don't use only English or only Hindi

## 📱 UI Walkthrough

### Home Screen

```
┌─────────────────────────────────┐
│ 🏪 दुकान का हिसाब              │
│    Bookkeeping Assistant         │
├─────────────────────────────────┤
│                                  │
│ [Success Card]                   │
│ ✓ Sale recorded: ₹2000          │
│                                  │
├─────────────────────────────────┤
│ Quick Actions:                   │
│ ┌──────┐ ┌──────┐ ┌──────┐     │
│ │Today'│ │Expen-│ │Summ- │     │
│ │Sales │ │ ses  │ │ ary  │     │
│ └──────┘ └──────┘ └──────┘     │
├─────────────────────────────────┤
│ Recent Transactions              │
│                                  │
│ 🛒 Sale                  +₹2000 │
│    Today                         │
│                                  │
│ 🔻 Loan Given to Suresh  -₹500  │
│    Today                         │
│                                  │
│ 🔺 Loan Taken from Ram  +₹1000  │
│    Yesterday                     │
│                                  │
└─────────────────────────────────┘
                ⊕  (Floating button)
```

### Input Dialog

```
┌─────────────────────────────────┐
│ ➕ Add Transaction               │
├─────────────────────────────────┤
│ Type or speak your transaction: │
│                                  │
│ ┌─────────────────────────────┐ │
│ │ Ramesh se 500 rupaye liye   │ │
│ │ udhar mein                  │ │
│ └─────────────────────────────┘ │
│                                  │
│ Examples:                        │
│ 💡 Aaj 2000 ki bikri hui        │
│ 💡 Bijli ka bill 900 rupaye...  │
│                                  │
│      [Cancel]      [Submit]      │
└─────────────────────────────────┘
```

## 🌟 Advanced Examples

### Complex Scenarios

**Scenario 1: Multiple transactions in a day**

```
Morning: "aaj subah 1500 ki bikri hui"
Noon:    "chai nashta 100 rupaye"
Evening: "shaam ko 2500 ka saman becha"
Night:   "aaj ki total bikri kitni hai?"
Result:  Today's sales: ₹4000
```

**Scenario 2: Managing multiple party balances**

```
"Ramesh se 1000 liye udhar"
"Sunil ko 500 diye udhar"
"Ramesh ka balance kya hai?"
Result: You owe Ramesh: ₹1000

"Sunil ka balance?"
Result: Sunil owes you: ₹500
```

**Scenario 3: End of day summary**

```
"aaj ki total bikri kitni?"     → ₹8000
"aaj ka kharcha kitna?"          → ₹2500
"ab tak ka overall summary?"     → Profit: ₹35,000
```

## 🔄 Common Patterns

### Pattern 1: Person + se + Amount + liye

- "Ramesh se 500 liye" → Loan taken from Ramesh

### Pattern 2: Person + ko + Amount + diya

- "Sunil ko 300 diya" → Loan given to Sunil

### Pattern 3: Amount + ki + Transaction

- "2000 ki bikri" → Sale of ₹2000
- "500 ka kharcha" → Expense of ₹500

### Pattern 4: Transaction + Time

- "kal 1000 ki bikri hui" → Yesterday's sale
- "aaj 500 kharcha" → Today's expense

## 📊 Understanding Your Balance

### Money Coming In (Green +)

- Sales to customers
- Loans received from others
- Payments received

### Money Going Out (Red -)

- Expenses (bills, rent, etc.)
- Loans given to others
- Purchases/stock

### Party Balance

- **Positive (+)**: They owe you money
- **Negative (-)**: You owe them money
- **Zero (0)**: Account settled

## 🎯 Pro Tips

1. **Be Consistent**: Use similar phrases for similar transactions
2. **Check Daily**: Review "Today's Sales" every evening
3. **Monitor Expenses**: Keep track of daily expenses
4. **Follow Up**: Check party balances regularly
5. **Use Notes**: Add context in transaction notes
6. **Review Summary**: Check overall summary weekly/monthly

## 🆘 Troubleshooting

### App doesn't understand my input?

- Try simpler sentences
- Use common Hinglish words
- Mention amount clearly
- Check spelling of names

### Wrong amount recorded?

- Speak numbers clearly
- Use "rupaye" or "rupees" after amount
- Avoid multiple numbers in one sentence

### Can't find a transaction?

- Scroll through the list
- Use date-based queries
- Check party-specific queries

---

**Remember**: The app learns from common patterns. The more you use it, the better it understands
your style! 🎉
