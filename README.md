# 📚 ShriLekhan:Personal Book-keeping App

An offline-first Android bookkeeping app designed for small shopkeepers in India with low literacy.
Features an on-device AI assistant that understands natural language (Hinglish) to record and query
business transactions.

## 🎯 Features

- **🗣️ Natural Language Input**: Understands Hinglish (Hindi + English mix) utterances
    - Examples: "Ramesh se 500 rupaye liye udhar mein", "Aaj ki total bikri kitni hai?"

- **💾 Offline-First**: Complete functionality without internet connection

- **🤖 On-Device AI**: All NLP processing happens locally for privacy

- **📊 Transaction Management**:
    - Sales tracking
    - Expense recording
    - Loan management (given/taken)
    - Party-wise balance tracking

- **📈 Smart Queries**:
    - Daily/weekly/monthly sales and expenses
    - Overall profit/loss summary
    - Individual party balance

- **🎨 Beautiful UI**: Modern Material Design 3 interface with Jetpack Compose

## 🏗️ Architecture

### Tech Stack

- **Platform**: Android (Kotlin)
- **UI**: Jetpack Compose with Material Design 3
- **Database**: Room (SQLite) for local storage
- **Architecture**: MVVM (Model-View-ViewModel)
- **Async**: Kotlin Coroutines + Flow
- **AI**: RunAnywhere SDK + Firebender plugin (ready for integration)

### Project Structure

```
com.root2rise.bookkeeping/
├── ai/
│   └── AiService.kt              # AI service interface + mock implementation
├── data/
│   ├── TransactionEntity.kt      # Room entity for transactions
│   ├── TransactionDao.kt         # Database queries
│   └── BookkeepingDatabase.kt    # Room database
├── model/
│   └── AiSchemas.kt              # JSON schemas for AI responses
├── repository/
│   └── BookkeepingRepository.kt  # Data layer abstraction
├── viewmodel/
│   └── BookkeepingViewModel.kt   # Business logic + state management
├── ui/
│   ├── screen/
│   │   └── HomeScreen.kt         # Main UI
│   └── theme/                    # Material Design theme
└── MainActivity.kt               # Entry point
```

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest version)
- Android SDK 24+ (Android 7.0+)
- Kotlin 1.9+
- Figma (For UI Drsigns)

### Dependencies

All dependencies are managed via Gradle. Key libraries:

- Jetpack Compose (UI)
- Room Database (local storage)
- Lifecycle ViewModel (state management)
- Kotlin Coroutines (async operations)
- Gson (JSON parsing)

### Build & Run

1. Clone the repository:

```bash
git clone <repository-url>
cd bookkeeping
```

2. Open in Android Studio

3. Sync Gradle dependencies

4. Run on emulator or device

### Integrating Real AI Model

The app currently uses `MockAiService` for testing. To integrate the actual AI model:

1. Add RunAnywhere SDK and Firebender dependencies to `app/build.gradle.kts`

2. Create `FirebenderAiService.kt`:

```kotlin
class FirebenderAiService(private val context: Context) : AiService {
    private val model = RunAnywhereModel.load(context, "bookkeeping_model.tflite")
    
    override suspend fun processUtterance(utterance: String): String {
        return model.generate(utterance)
    }
}
```

3. Update `MainActivity.kt`:

```kotlin
val aiService = FirebenderAiService(this) // Replace MockAiService
```

## 💬 Usage Examples

### Adding Transactions

**Hinglish Input** → **AI Output** → **App Action**

1. "Ramesh se 500 rupaye liye udhar mein"
   → Loan taken schema
   → Records ₹500 loan from Ramesh

2. "Aaj 2000 ki bikri hui"
   → Sale transaction schema
   → Records ₹2000 in sales

3. "Bijli ka bill 900 rupaye bhar diya"
   → Expense transaction schema
   → Records ₹900 electricity expense

### Querying Data

1. "Aaj ki total bikri kitni hai?"
   → Query schema (total_sales, today)
   → Shows today's total sales

2. "Ramesh ka kitna baki hai?"
   → Query schema (balance, party_name: Ramesh)
   → Shows balance with Ramesh

3. "Ab tak ka overall summary kya hai?"
   → Query schema (overall_summary, all)
   → Shows profit/loss summary

## 🎨 UI Screenshots

The app features a modern, intuitive interface:

- **Home Screen**: Displays recent transactions with quick action buttons
- **Voice Input Dialog**: Natural language input for transactions and queries
- **Transaction Cards**: Visual representation of each transaction with icons
- **Response Cards**: Immediate feedback for user actions

## 🔐 Privacy

- **100% Offline**: No data leaves the device
- **Local AI**: All NLP processing on-device
- **No Cloud**: No server dependencies
- **User Data**: Stored locally in encrypted SQLite database

## 🛠️ Development

### Running Tests

```bash
./gradlew test           # Unit tests
./gradlew connectedTest  # Instrumentation tests
```

### Code Style

The project follows Kotlin coding conventions and uses:

- ktlint for linting
- detekt for static analysis

## 📝 License

[Add your license here]

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.


## 📊 Current Status

- ✅ **Development**: In progress 
- ✅ **Build**:In progress 
- ✅ **Architecture**: MVVM with Jetpack Compose
- ✅ **Database**: Room implementation ready
- ✅ **UI**: Material Design 3 complete
- ✅ **Mock AI**: Fully functional 


## 📧 Support

For issues and questions:

- **Issues**: Open a GitHub issue
- **Discussions**: GitHub Discussions
- **Documentation**: See docs/ folder

---

Built with ❤️ for small shopkeepers in India 🇮🇳
