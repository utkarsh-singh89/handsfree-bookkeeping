# 📱 Logo Setup Instructions

## Adding Your ShreeLekhan Logo

Your logo image has been received and needs to be placed in the correct location in your Android
project.

### Steps to Add the Logo:

#### Option 1: Using Android Studio (Recommended)

1. **Open Android Studio**
2. **Navigate to Project View**: Switch to "Project" view (not "Android" view)
3. **Locate the drawable folder**:
   ```
   app/src/main/res/drawable/
   ```
4. **Right-click on the `drawable` folder** → New → Image Asset → Action Bar and Tab Icons
5. **Or simply drag and drop** your logo image into the `drawable` folder
6. **Rename the file** to `app_logo.png` (or keep it and update code)

#### Option 2: Manual File Placement

1. **Save your logo image** with a suitable name (e.g., `app_logo.png`)
2. **Copy the file** to:
   ```
   C:\Users\vansh\AndroidStudioProjects\handsfree-bookkeeping\app\src\main\res\drawable\
   ```
3. **File formats supported**:
    - PNG (recommended for logos with transparency)
    - JPG/JPEG
    - WebP
    - SVG (will need to be converted to Vector Drawable)

### Logo Specifications:

- **Recommended size**: 120x120 dp (or 360x360 pixels @3x)
- **Format**: PNG with transparent background
- **File name**: `app_logo.png` (or update the resource ID in code)

### Where the Logo Appears:

1. **Welcome Screen**: Center of the screen (120dp size) with pulsing animation
2. **Dashboard Screen**: Top left corner (40dp size) next to "ShreeLekhan" text

### If You Named Your Logo File Differently:

If your logo file is named something other than `app_logo.png`, update the following files:

**In `WelcomeScreen.kt`** (line 60):

```kotlin
painter = painterResource(id = R.drawable.your_logo_name),
```

**In `DashboardScreen.kt`** (line 68):

```kotlin
painter = painterResource(id = R.drawable.your_logo_name),
```

Replace `your_logo_name` with your actual file name (without extension).

### Current Placeholder:

A placeholder XML drawable has been created at:

```
app/src/main/res/drawable/app_logo.xml
```

**You can either**:

- Replace this file with your PNG image named `app_logo.png`
- Or delete `app_logo.xml` and add your `app_logo.png`

The system will automatically use whichever file is present.

### Verify the Logo:

1. **Clean and Rebuild** the project:
    - Build → Clean Project
    - Build → Rebuild Project

2. **Run the app** and check:
    - Welcome screen shows your logo
    - Dashboard top bar shows your logo

### Troubleshooting:

**Issue**: "Cannot resolve symbol 'app_logo'"

- **Solution**: Make sure the file is named exactly `app_logo.png` (or update code)
- Clean and rebuild the project

**Issue**: Logo appears pixelated

- **Solution**: Use a higher resolution image (at least 360x360 pixels)

**Issue**: Logo has white background instead of transparent

- **Solution**: Use PNG format with transparency, not JPG

---

## 🎨 Logo Image Provided

The logo image you provided shows:

- A circular design with peacock feather
- Text in Devanagari script ("श्री")
- Gold/cream color scheme with blue accents
- Suitable for the app's theme

Simply save this image as `app_logo.png` and place it in the drawable folder!
