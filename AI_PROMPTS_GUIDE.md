# AI Model Training Guide - Bookkeeping Assistant

This document provides guidelines for training the on-device AI model to understand Hinglish
bookkeeping utterances and generate appropriate JSON responses.

## Model Requirements

### Input

- Natural language utterances in Hinglish (Hindi + English mix, written in Latin script)
- Noisy speech-to-text output (may contain errors)
- Typical length: 5-30 words

### Output

- Strict JSON conforming to either `TransactionSchema` or `QuerySchema`
- Must be valid JSON (parseable)
- All fields must be present (use `null` for optional fields)

## System Prompt for AI Model

```
You are an on-device NLP assistant for a bookkeeping app used by small shopkeepers in India.

Your ONLY job is to:
1. Read natural language input (Hinglish - Hindi + English mix)
2. Determine if it's a TRANSACTION or a QUERY
3. Output ONLY valid JSON, nothing else

User speaks in Hinglish using words like:
- rupaye, paisa (money)
- udhar (loan/credit)
- bikri, becha (sale)
- kharcha, kharch (expense)
- liya (took/received)
- diya (gave)
- baki, balance (remaining balance)
- aaj (today)
- kal (yesterday)
- kitna, kitni (how much)

TRANSACTION SCHEMA - When user describes something that happened:
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "in | out",
  "type": "sale | purchase | loan_given | loan_taken | expense | other",
  "party_name": "string or null",
  "amount": 0,
  "date": "today | yesterday | yyyy-mm-dd | null",
  "notes": "string or null"
}

QUERY SCHEMA - When user asks a question:
{
  "kind": "query",
  "action": "query_total_sales | query_total_expenses | query_overall_summary | query_balance",
  "party_name": "string or null",
  "time_range": "today | yesterday | this_week | this_month | all | null"
}

RULES:
- Output ONLY JSON, no explanations
- "in" = money coming to shopkeeper
- "out" = money going from shopkeeper
- Extract party name from context (words before "se", "ko", "ka")
- Extract amount (first number found)
- If unclear, use sensible defaults
```

## Training Examples

### Transaction Examples

#### 1. Loan Taken (Money Coming In)

**Input**: "Ramesh se 500 rupaye liye udhar mein"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "in",
  "type": "loan_taken",
  "party_name": "Ramesh",
  "amount": 500,
  "date": "today",
  "notes": "Loan taken from Ramesh"
}
```

**Input**: "mukesh ko kal 1000 rupees liye the"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "in",
  "type": "loan_taken",
  "party_name": "Mukesh",
  "amount": 1000,
  "date": "yesterday",
  "notes": "Loan taken from Mukesh"
}
```

#### 2. Loan Given (Money Going Out)

**Input**: "Suresh ko 300 rupaye diya udhar"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "out",
  "type": "loan_given",
  "party_name": "Suresh",
  "amount": 300,
  "date": "today",
  "notes": "Loan given to Suresh"
}
```

**Input**: "ravi ko 2000 diye the kal"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "out",
  "type": "loan_given",
  "party_name": "Ravi",
  "amount": 2000,
  "date": "yesterday",
  "notes": "Loan given to Ravi"
}
```

#### 3. Sales (Money Coming In)

**Input**: "Aaj 2000 ki bikri hui"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "in",
  "type": "sale",
  "party_name": null,
  "amount": 2000,
  "date": "today",
  "notes": "Daily sales"
}
```

**Input**: "3500 ka saman becha"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "in",
  "type": "sale",
  "party_name": null,
  "amount": 3500,
  "date": "today",
  "notes": "Sales"
}
```

**Input**: "Priya ko 1200 ka saman becha"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "in",
  "type": "sale",
  "party_name": "Priya",
  "amount": 1200,
  "date": "today",
  "notes": "Sale to Priya"
}
```

#### 4. Expenses (Money Going Out)

**Input**: "Bijli ka bill 900 rupaye bhar diya"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "out",
  "type": "expense",
  "party_name": null,
  "amount": 900,
  "date": "today",
  "notes": "Electricity bill paid"
}
```

**Input**: "chai pani mein 150 kharcha hua"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "out",
  "type": "expense",
  "party_name": null,
  "amount": 150,
  "date": "today",
  "notes": "Tea and snacks expense"
}
```

**Input**: "rent 5000 pay kar diya"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "out",
  "type": "expense",
  "party_name": null,
  "amount": 5000,
  "date": "today",
  "notes": "Rent paid"
}
```

#### 5. Purchase (Money Going Out)

**Input**: "800 ka stock liya"
**Output**:

```json
{
  "kind": "transaction",
  "action": "add_transaction",
  "direction": "out",
  "type": "purchase",
  "party_name": null,
  "amount": 800,
  "date": "today",
  "notes": "Stock purchase"
}
```

### Query Examples

#### 1. Total Sales Queries

**Input**: "Aaj ki total bikri kitni hai?"
**Output**:

```json
{
  "kind": "query",
  "action": "query_total_sales",
  "party_name": null,
  "time_range": "today"
}
```

**Input**: "is mahine mein kitni sales hui?"
**Output**:

```json
{
  "kind": "query",
  "action": "query_total_sales",
  "party_name": null,
  "time_range": "this_month"
}
```

**Input**: "ab tak kitna becha?"
**Output**:

```json
{
  "kind": "query",
  "action": "query_total_sales",
  "party_name": null,
  "time_range": "all"
}
```

#### 2. Expense Queries

**Input**: "Aaj ka total kharcha kitna hai?"
**Output**:

```json
{
  "kind": "query",
  "action": "query_total_expenses",
  "party_name": null,
  "time_range": "today"
}
```

**Input**: "is hafte mein kitna kharch hua?"
**Output**:

```json
{
  "kind": "query",
  "action": "query_total_expenses",
  "party_name": null,
  "time_range": "this_week"
}
```

#### 3. Overall Summary

**Input**: "Ab tak ka overall summary kya hai?"
**Output**:

```json
{
  "kind": "query",
  "action": "query_overall_summary",
  "party_name": null,
  "time_range": "all"
}
```

**Input**: "profit kitna hua?"
**Output**:

```json
{
  "kind": "query",
  "action": "query_overall_summary",
  "party_name": null,
  "time_range": "all"
}
```

#### 4. Party Balance Queries

**Input**: "Ramesh ka kitna baki hai?"
**Output**:

```json
{
  "kind": "query",
  "action": "query_balance",
  "party_name": "Ramesh",
  "time_range": null
}
```

**Input**: "Sunil ka balance kya hai?"
**Output**:

```json
{
  "kind": "query",
  "action": "query_balance",
  "party_name": "Sunil",
  "time_range": null
}
```

## Edge Cases & Handling

### Ambiguous Direction

- Default to "in" (income) if unclear
- Look for keywords: "liya", "mila", "aya" → "in"
- Look for keywords: "diya", "kharch", "pay", "bhar" → "out"

### Missing Amount

- Use 0 as default
- Try to extract any number from utterance

### Missing Party Name

- Use null for party_name
- Only extract if explicitly mentioned with "se", "ko", "ka"

### Noisy Input

```
Input: "ram se 50 0 rupy lie udar"
→ Clean: "ram se 500 rupaye liye udhar"
→ Amount: 500 (combine digits)
→ Party: "ram" → "Ram" (capitalize)
```

### Multiple Numbers

```
Input: "2 kg chini 50 rupaye mein kharidi"
→ Amount: 50 (last number is usually amount)
```

### Date References

- "aaj", "today" → "today"
- "kal", "yesterday" → "yesterday"
- "parso" → "yesterday" (approximation)
- No date mentioned → "today"
- Specific date: "15 jan", "15-01-2024" → "2024-01-15"

## Common Hinglish Patterns

### Money Terms

- rupaye, rupees, rs, ₹, paisa, taka

### Loan Patterns

- "X se Y liye udhar" → loan_taken from X, amount Y
- "X ko Y diya udhar" → loan_given to X, amount Y
- "Y udhar liya X se" → loan_taken from X, amount Y

### Sale Patterns

- "Y ki bikri" → sale, amount Y
- "Y ka saman becha" → sale, amount Y
- "X ko Y becha" → sale to X, amount Y

### Expense Patterns

- "Y kharcha" → expense, amount Y
- "Y ka bill bhara" → expense, amount Y
- "Y pay kiya" → expense, amount Y

### Query Patterns

- "kitni/kitna X" → query for X
- "total X kya hai" → query for total X
- "X ka balance" → query balance with X

## Testing the Model

### Test Coverage Requirements

1. **Transaction Types** (6 types × 10 examples = 60 samples)
    - sale, purchase, loan_given, loan_taken, expense, other

2. **Query Types** (4 types × 10 examples = 40 samples)
    - total_sales, total_expenses, overall_summary, balance

3. **Edge Cases** (20+ samples)
    - Noisy input, missing fields, ambiguous direction

4. **Regional Variations** (30+ samples)
    - Different Hindi dialects, mixed English ratios

**Total minimum training samples: 150+**

### Validation Metrics

- **JSON Validity**: 100% (all outputs must be valid JSON)
- **Schema Conformance**: 100% (all fields present)
- **Direction Accuracy**: >95% (correct in/out classification)
- **Amount Extraction**: >90% (correct number extraction)
- **Party Name Extraction**: >85% (correct name identification)

## Model Deployment

### Format

- TensorFlow Lite (.tflite) for on-device inference
- Model size: <50MB for optimal performance
- Inference time: <500ms per utterance

### Integration

```kotlin
class FirebenderAiService(context: Context) : AiService {
    private val model = RunAnywhereModel.load(context, "bookkeeping_model.tflite")
    
    override suspend fun processUtterance(utterance: String): String {
        // Model returns JSON string directly
        return model.generate(utterance)
    }
}
```

---

**Note**: This guide should be used to train a small, efficient LLM (like Phi-2, Gemma-2B, or custom
BERT) fine-tuned specifically for this bookkeeping task with Hinglish data.
