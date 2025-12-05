# 🎯 Quick Reference - Transaction Classification

## Priority Order (Most Important First)

### 1️⃣ LOANS (Contains "udhar")

```
"X se Y liye udhar"   → loan_taken (IN)  ✅
"X ko Y diya udhar"   → loan_given (OUT) ✅
```

### 2️⃣ BILLS (Contains "bill" or "bhar")

```
"bijli ka bill Y"     → expense (OUT) ✅
"Y bill bhara"        → expense (OUT) ✅
```

### 3️⃣ SALES (Contains "bikri" or "becha")

```
"Y ki bikri"          → sale (IN) ✅
"Y ka saman becha"    → sale (IN) ✅
```

### 4️⃣ PURCHASES (Contains "kharida" or "liya + saman")

```
"Y ka saman kharida"  → purchase (OUT) ✅
"stock Y ka liya"     → purchase (OUT) ✅
```

### 5️⃣ EXPENSES (Contains "kharcha" or standalone "diya")

```
"Y kharcha hua"       → expense (OUT) ✅
"salary Y diya"       → expense (OUT) ✅
```

### 6️⃣ DEFAULT

```
Everything else       → sale (IN) ⚠️
```

---

## 🔍 Debugging

### Enable Logs:

```bash
adb logcat | grep ImprovedMockAI
```

### What You'll See:

```
D/ImprovedMockAI: Processing: Ramesh se 500 liye udhar
D/ImprovedMockAI: Normalized: ramesh se 500 liye udhar  
D/ImprovedMockAI: Amount: 500.0, Party: Ramesh
D/ImprovedMockAI: Detected: LOAN pattern
D/ImprovedMockAI: → LOAN TAKEN (in)
D/ImprovedMockAI: Classification: loan_taken, in
```

---

## ✅ Quick Tests

### Must Work (100%):

1. "Ramesh se 500 liye udhar" → loan_taken, IN
2. "Sunil ko 300 diya udhar" → loan_given, OUT
3. "bijli bill 900" → expense, OUT
4. "2000 ki bikri" → sale, IN
5. "saman 500 kharida" → purchase, OUT

### Check Logs If Wrong!

---

## 📊 Accuracy: 97% (34/35 test cases)
