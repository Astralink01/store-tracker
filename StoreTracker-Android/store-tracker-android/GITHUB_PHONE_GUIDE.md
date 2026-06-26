# How to Build the APK Using Only Your Phone

No laptop needed. Follow these steps on your phone's browser.

---

## Step 1 — Create a free GitHub account
Go to **github.com** → Sign up (free)

---

## Step 2 — Create a new repository
1. Tap the **+** icon (top right) → **New repository**
2. Name it: `store-tracker`
3. Set it to **Public** (required for free Actions minutes)
4. Tap **Create repository**

---

## Step 3 — Upload the project files
After creating the repo, tap **"uploading an existing file"** link on the page.

Upload ALL files from the `store-tracker-android/` folder, keeping the folder structure:
```
.github/workflows/build-apk.yml
app/build.gradle
app/proguard-rules.pro
app/src/main/AndroidManifest.xml
app/src/main/java/com/storetracker/...  (all .java files)
app/src/main/res/...                    (all XML files)
build.gradle
settings.gradle
gradle.properties
gradle/wrapper/gradle-wrapper.properties
.gitignore
```

> TIP: On some phones you can select multiple files at once in the file picker.
> If you can't upload folders, use a file manager app to zip the folder first,
> then use github.dev (see Step 3b below).

### Step 3b — Easier upload via GitHub web editor (recommended)
1. After creating the repo, go to:
   `https://github.dev/YOUR_USERNAME/store-tracker`
   (replace YOUR_USERNAME with your GitHub username)
2. This opens a VS Code editor in your browser
3. Drag and drop the entire `store-tracker-android` folder into the file panel on the left
4. Click the **Source Control** icon (branch icon) → type a message → click **Commit & Push**

---

## Step 4 — Watch the build
1. Go back to your repo on **github.com**
2. Tap the **Actions** tab at the top
3. You will see **"Build Store Tracker APK"** running (yellow dot = in progress)
4. Wait about **3–6 minutes**
5. When it turns green ✅ — your APK is ready!

---

## Step 5 — Download the APK to your phone
1. Tap the completed workflow run (the green row)
2. Scroll down to **Artifacts**
3. Tap **StoreTracker-debug-apk** → it downloads a ZIP
4. Open the ZIP → inside is **app-debug.apk**

---

## Step 6 — Install the APK on your phone
1. Open your **File Manager** app and find the APK
2. Tap it → your phone will ask to allow "Install from unknown sources"
3. Go to Settings → Security → Enable **"Install unknown apps"** for your file manager
4. Come back and tap the APK → **Install**
5. Open **Store Tracker** 🎉

---

## Trigger a new build anytime
Whenever you want a fresh build (e.g. after changes):
1. Go to your repo → **Actions** tab
2. Click **"Build Store Tracker APK"** on the left
3. Click **"Run workflow"** → **Run workflow** (green button)

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Build fails with "JDK" error | It auto-resolves — the workflow installs JDK 17 automatically |
| "Install blocked" on phone | Enable "Install unknown apps" in phone Settings → Security |
| Can't find the APK after download | Check your Downloads folder or the notification bar |
| Actions tab not visible | Make sure the `.github/workflows/build-apk.yml` file was uploaded |
