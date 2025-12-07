# 🎨 UI Quick Reference Card

## 📱 Screen Flow

```
┌─────────────────┐
│ WelcomeScreen   │ (First launch)
└────────┬────────┘
         │
         ▼
┌─────────────────┐      ┌─────────────────┐
│  LoginScreen    │◄────►│ RegisterScreen  │
└────────┬────────┘      └─────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│         Main App with Bottom Nav        │
├─────────────────┬───────────────────────┤
│  DashboardScreen│                       │
│  (Home)         │  ◄─ Default Screen    │
├─────────────────┤                       │
│  WalletScreen   │                       │
│  (Stats)        │                       │
├─────────────────┤                       │
│ TransactionsScr │                       │
│  (List)         │                       │
├─────────────────┤                       │
│  ProfileScreen  │                       │
│  (Settings)     │                       │
└─────────────────┴───────────────────────┘
```

---

## 🎨 Color Palette (Hex Codes)

### Primary Colors

```
DarkBackground:  #0A0E27  ████████
DarkSurface:     #131832  ████████
DarkCard:        #1A1F3A  ████████
NeonBlue:        #4169E1  ████████
NeonCyan:        #00D9FF  ████████
```

### Accent Colors

```
AccentPurple:    #8B5CF6  ████████
AccentPink:      #EC4899  ████████
SuccessGreen:    #10B981  ████████
ErrorRed:        #EF4444  ████████
WarningYellow:   #F59E0B  ████████
```

### Text Colors

```
TextPrimary:     #FFFFFF  ████████
TextSecondary:   #B4B8D4  ████████
TextTertiary:    #6B7280  ████████
```

### Gradient Colors

```
GradientStart:   #0F1629  ████████
GradientMiddle:  #1A2847  ████████
GradientEnd:     #1E3A8A  ████████
```

---

## 🧩 Component Usage

### Buttons

```kotlin
// Primary Gradient Button
GradientButton(
    text = "Submit",
    onClick = { /* action */ }
)

// Icon Button with Glow
IconButtonWithGlow(
    icon = Icons.Filled.Settings,
    contentDescription = "Settings",
    onClick = { /* action */ }
)

// Mic Button (Floating)
PulsingMicButton(
    onClick = { /* start recording */ },
    isListening = false
)
```

### Cards

```kotlin
// Neon Card (Standard)
NeonCard {
    Text("Card Content")
}

// Stat Card
StatCard(
    title = "Income",
    value = "₹10,000",
    icon = Icons.Filled.TrendingUp,
    iconColor = SuccessGreen
)
```

### Text Fields

```kotlin
// Standard Text Field
NeonTextField(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    leadingIcon = Icons.Filled.Email
)

// Password Field
NeonTextField(
    value = password,
    onValueChange = { password = it },
    label = "Password",
    leadingIcon = Icons.Filled.Lock,
    isPassword = true
)
```

### Backgrounds

```kotlin
// Gradient Background
GradientBackground {
    // Your content
}
```

### Other

```kotlin
// Section Title
SectionTitle(text = "Recent Activity")

// Empty State
EmptyState(
    icon = Icons.Filled.Receipt,
    title = "No data",
    subtitle = "Add items to see them here"
)
```

---

## 🧭 Navigation Commands

### Navigate to Screen

```kotlin
navController.navigate(Screen.Home.route)
```

### Navigate Back

```kotlin
navController.popBackStack()
```

### Navigate with Pop

```kotlin
navController.navigate(Screen.Home.route) {
    popUpTo(Screen.Welcome.route) { inclusive = true }
}
```

### Bottom Navigation

```kotlin
AppBottomNavigation(
    currentRoute = currentRoute,
    onNavigate = { route -> 
        navController.navigate(route) 
    }
)
```

---

## 📊 Transaction Icons

| Type          | Icon                        | Color        |
|---------------|-----------------------------|--------------|
| Sale          | ShoppingCart                | Green        |
| Purchase      | ShoppingBag                 | Yellow       |
| Expense       | Payment                     | Red          |
| Loan Given    | ArrowUpward                 | Cyan         |
| Loan Taken    | ArrowDownward               | Red          |
| Default       | Receipt                     | Blue         |

---

## 🎭 Animation Specs

### Pulsing Effect

```kotlin
animateFloat(
    initialValue = 1f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
        animation = tween(2000),
        repeatMode = RepeatMode.Reverse
    )
)
```

### Fade In/Out

```kotlin
AnimatedVisibility(
    visible = isVisible,
    enter = fadeIn() + slideInVertically(),
    exit = fadeOut() + slideOutVertically()
)
```

---

## 🔧 Common Modifiers

### Shadow with Glow

```kotlin
Modifier.shadow(
    elevation = 8.dp,
    shape = RoundedCornerShape(16.dp),
    spotColor = NeonBlue.copy(alpha = 0.6f)
)
```

### Gradient Background

```kotlin
Modifier.background(
    Brush.verticalGradient(
        colors = listOf(GradientStart, GradientEnd)
    )
)
```

### Rounded Corners

```kotlin
Modifier.clip(RoundedCornerShape(16.dp))
```

---

## 📱 Screen Responsibilities

| Screen          | Purpose                           | Key Features                    |
|-----------------|-----------------------------------|---------------------------------|
| Welcome         | App intro                         | Animated logo, features list    |
| Login           | Authentication                    | Email/password, validation      |
| Register        | New user signup                   | Full form, validation           |
| Dashboard       | Main overview                     | Stats, transactions, mic button |
| Wallet          | Financial stats                   | Charts, breakdowns, insights    |
| Transactions    | Complete history                  | Filters, grouping, actions      |
| Profile         | Settings & account                | User info, settings, logout     |

---

## 🎯 Key Interactions

### Voice Input

```
User taps mic button → VoiceService.startListening()
→ Transcription received → ViewModel.processTranscription()
→ UI updates with response
```

### Transaction Actions

```
User taps transaction → Dialog appears
→ Options: Read Aloud / Modify / Delete
→ Action executed via ViewModel
```

### Bottom Navigation

```
User taps tab → Navigate to screen
→ Previous state restored if available
→ Smooth transition with animations
```

---

## 🚦 Validation Rules

### Login

- Email must contain `@`
- Password minimum 6 characters

### Register

- All fields required
- Email must contain `@`
- Phone must be exactly 10 digits
- Password minimum 6 characters
- Passwords must match

---

## 📐 Spacing Guidelines

```
Extra Small:  4.dp
Small:        8.dp
Medium:      16.dp
Large:       24.dp
Extra Large: 32.dp
```

### Common Padding

```kotlin
.padding(horizontal = 16.dp, vertical = 8.dp)  // Card content
.padding(16.dp)                                // Screen edges
.padding(24.dp)                                // Large spacing
```

---

## 🎨 Gradient Recipes

### Vertical Screen Gradient

```kotlin
Brush.verticalGradient(
    colors = listOf(GradientStart, GradientMiddle, DarkBackground)
)
```

### Horizontal Card Gradient

```kotlin
Brush.horizontalGradient(
    colors = listOf(NeonBlueDark, NeonBlue, NeonBlueLight)
)
```

### Radial Button Gradient

```kotlin
Brush.radialGradient(
    colors = listOf(NeonBlue, NeonBlueDark)
)
```

### Radial Glow Effect

```kotlin
Brush.radialGradient(
    colors = listOf(
        NeonBlue.copy(alpha = 0.3f),
        Color.Transparent
    )
)
```

---

## 🔢 Typography Sizes

```
Title Large:    20-22sp  (Heavy)
Title Medium:   18sp     (SemiBold)
Body Large:     16sp     (Medium)
Body Medium:    14sp     (Normal)
Body Small:     12sp     (Normal)
Label Small:    11sp     (Medium)
```

---

## 🎪 State Management

### ViewModel State

```kotlin
val uiState by viewModel.uiState.collectAsState()
val transactions by viewModel.transactions.collectAsState()
val lastResponse by viewModel.lastResponse.collectAsState()
```

### Local State

```kotlin
var isLoading by remember { mutableStateOf(false) }
var errorMessage by remember { mutableStateOf<String?>(null) }
var selectedItem by remember { mutableStateOf<Item?>(null) }
```

---

## 💡 Pro Tips

1. **Always use `GradientBackground`** for screen backgrounds
2. **Use `NeonCard`** for grouped content
3. **Apply shadow with spotColor** for glow effects
4. **Use StateFlow.collectAsState()** for ViewModel data
5. **Keep composables small** - extract to private functions
6. **Use Material Icons** - no need for custom assets
7. **Test on dark theme only** - app forces dark mode
8. **Use animations sparingly** - only for key interactions

---

## 🚀 Getting Started Checklist

- [x] Dependencies added (Navigation Compose)
- [x] Theme system implemented
- [x] All 7 screens created
- [x] Navigation graph configured
- [x] Bottom navigation implemented
- [x] Components library complete
- [x] MainActivity updated
- [ ] Test authentication flow
- [ ] Test voice input
- [ ] Test transaction operations
- [ ] Test all navigation paths

---

## 📞 Common Issues & Fixes

### Issue: "Unresolved reference: navigation"

**Fix**: Sync Gradle - dependency already added

### Issue: Colors not showing

**Fix**: Wrap content in `BookkeepingTheme { }`

### Issue: Bottom nav not visible

**Fix**: Ensure route is in `bottomNavRoutes` list

### Issue: Navigation not working

**Fix**: Use correct route from `Screen.Route.route`

---

## 🎬 Demo Flow

1. **Launch** → Welcome Screen
2. **Tap "Get Started"** → Login Screen
3. **Enter credentials** → Dashboard
4. **Tap mic button** → Voice input
5. **Tap bottom nav** → Switch screens
6. **Tap transaction** → Actions dialog
7. **Go to Profile** → Settings options
8. **Tap Logout** → Back to login

---

## 📦 Files to Know

**Must Edit**:

- `MainActivity.kt` - Entry point ✅ Already updated
- `AppNavigation.kt` - Add new screens
- `Screen.kt` - Add new routes

**Can Customize**:

- `Color.kt` - Change colors
- `Type.kt` - Change fonts
- `Shape.kt` - Change corner radius
- `CommonComponents.kt` - Add components

**Don't Touch**:

- `BookkeepingViewModel.kt` - Business logic
- `BookkeepingRepository.kt` - Data layer
- `TransactionEntity.kt` - Data model

---

**That's it! Your UI is complete and ready to use! 🎉**
