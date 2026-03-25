📱 ScanMyPills – Android Frontend
🧠 Overview
ScanMyPills is an AI-powered mobile application designed to simplify medicine management. The Android frontend provides an intuitive user interface that allows users to scan medicine strips, identify loose tablets, manage medications, and receive timely reminders for dosage and expiry tracking.

🚀 Features
📸 Medicine Scanning
Capture images of medicine strips and extract relevant details using OCR-based processing.

💊 Tablet Identification
Identify loose tablets by analyzing visual patterns and matching them with stored data.

⏰ Smart Reminders
Schedule and receive notifications for medicine intake at the right time.

📋 Medicine Management
Add, update, and track medicines with dosage and expiry details.

🔐 User Authentication
Secure login and registration system for personalized experience.

🛠️ Tech Stack
Language: Kotlin

IDE: Android Studio

Architecture: MVVM (Model-View-ViewModel)

Networking: Retrofit

UI Components: XML Layouts, Material Design

Backend Integration: REST APIs (Flask Backend)

📂 Project Structure
scanmypills-android/
│
├── app/                     # Main application source code
├── gradle/                  # Gradle wrapper files
├── gradlew                  # Gradle script (Linux/Mac)
├── gradlew.bat              # Gradle script (Windows)
├── build.gradle.kts         # Project build configuration
├── settings.gradle.kts      # Project settings
├── gradle.properties        # Gradle properties
├── .gitignore               # Ignored files
⚙️ Setup Instructions
1️⃣ Clone the Repository
git clone https://github.com/your-username/scanmypills-android.git
cd scanmypills-android
2️⃣ Open in Android Studio
Open Android Studio

Click Open Project

Select the project folder

3️⃣ Sync Gradle
Wait for Gradle to sync automatically

If not, click "Sync Now"

4️⃣ Run the App
Connect an emulator or physical device

Click ▶️ Run

🔗 Backend Integration
Make sure the backend server is running.

Update the base URL in your Retrofit client:

baseUrl = "http://YOUR_BACKEND_IP:5000/"
🔒 Security Notes
Sensitive data (API keys, local configs) are excluded using .gitignore

Do NOT commit:

local.properties

API keys

build files

🧪 Testing
Unit testing for core logic

UI testing for screens and navigation

Integration testing with backend APIs

🎯 Future Enhancements
AI-based improved tablet recognition

Cloud-based medicine database

Multi-language support

Dark mode UI

👩‍💻 Author
Mannuru Sruthi
Android Developer | AI-Based Mobile Systems

📄 License
This project is developed for academic and educational purposes.
