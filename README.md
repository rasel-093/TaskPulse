# TaskPulse

A thoughtful, production-grade Android task and reminder app built with **Jetpack Compose**, **Room Database**, and **WorkManager**. Designed with a tactile, editorial **Paper & Ink** aesthetic, TaskPulse connects your day through a live vertical timeline, battery-aware background scheduling, and interactive notifications.

---

## 📱 Screenshots

| Timeline & Filters | Add Reminder Sheet | Interactive Notifications |
|:---:|:---:|:---:|
| ![Timeline Screen](screenshots/task_list.png) | ![Add Reminder Sheet](screenshots/add_task.png) | ![Interactive Notifications](screenshots/notification_actions.png) |

---

## ✨ Features

- **Live Vertical Timeline**:
  - Continuous vertical connecting track behind task cards.
  - State-tied status dots: **Moss** (completed), **Ochre** (recurring), and **Indigo** (active).
  - Subtle breathing halo animation on urgent tasks (due within 5 minutes).
  - Quick filter pills: **All**, **Today**, **Recurring**, and **Done**.

- **Thoughtful Task Creation**:
  - Modal bottom sheet with quick time presets (**10 min**, **1 hour**, **Tomorrow**, **Custom minutes**).
  - **Repeat daily** toggle for recurring reminders.
  - Respect silent hours toggle for evening scheduling.

- **Interactive Notification Actions**:
  - **Done**: Directly marks task completed in Room database via background `MarkTaskDoneWorker` and cancels remaining alarms.
  - **Snooze (10m)**: Reschedules the reminder for 10 minutes later via `WorkManager` with `ExistingWorkPolicy.REPLACE`.
  - Expandable `BigTextStyle` notification layout with brand `Indigo` accents.

- **Production UX & Polish**:
  - **Swipe-to-delete**: Smooth `SwipeToDismissBox` gesture that removes tasks and cancels lingering WorkManager requests.
  - **Confirmation Snackbars**: Formatted feedback upon setting reminders (e.g. *"Reminder set for Water the fig — 6:00 PM"*).
  - **Resilient Scheduling**: `setRequiresBatteryNotLow(true)` constraints prevent notification alarms from draining low battery, and `enqueueUniqueWork` prevents duplicate worker jobs.
  - **Loading & Empty States**: Dedicated empty states with direct call-to-action shortcuts.

---

## 🎨 Design System: Fog, Paper & Ink

TaskPulse uses a custom design token architecture inspired by print editorial design:

### Backgrounds & Structure
| Token | Hex | Role |
| :--- | :--- | :--- |
| **Fog** | `#E9E7E1` | Soft grey-stone base background |
| **Paper** | `#F6F4EF` | Elevated card surfaces |
| **Paper Shade** | `#DEDBD2` | Inputs, chips, and nested pressed states |
| **Rule** | `#C8C4B8` | Dividers and timeline track like a faint pencil rule |

### Typography & Accents
| Token | Hex | Role |
| :--- | :--- | :--- |
| **Ink** | `#2B2924` | High-emphasis near-black typography |
| **Graphite** | `#6E6A5F` | Secondary labels and timestamps |
| **Graphite Faint** | `#A6A196` | Placeholders, disabled states, done strikethrough |
| **Indigo** | `#3C4A6B` | Primary action / active pulse accent |
| **Ochre** | `#A87B2E` | Recurring reminders indicator |
| **Moss** | `#5B7355` | Done / complete organic green |

---

## 🛠️ Architecture & Tech Stack

- **UI**: Jetpack Compose, Material 3, custom Compose Design System (`Theme.kt`, `Color.kt`, `Type.kt`, `Shape.kt`)
- **Architecture**: MVVM + Repository Pattern with Kotlin Coroutines and `StateFlow`
- **Database**: Room Database with reactive queries (`Flow<List<Task>>`)
- **Background Jobs**: AndroidX WorkManager (`OneTimeWorkRequest`, `PeriodicWorkRequest`, `Constraints`, `ExistingWorkPolicy`)
- **Notifications**: NotificationCompat with BroadcastReceiver (`NotificationActionReceiver`)
- **Testing**: JUnit4, Mockito, WorkManagerTestInitHelper (`ReminderWorkerTest`)

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17+**
- **Android SDK 29+** (Target SDK 36, Min SDK 29)
- Android device or emulator running Android 10+

---

### Option 1: Run via Android Studio (Recommended)

1. Launch **Android Studio**.
2. Select **Open** and choose the `TaskPulse` project folder.
3. Allow Gradle to download dependencies and finish project sync.
4. Select your connected device or emulator in the run configuration bar.
5. Click the green **Run** button (▶️) or press `Shift + F10`.

---

### Option 2: Run via Command Line / Terminal

```bash
# Clone the repository
git clone https://github.com/your-username/TaskPulse.git
cd TaskPulse

# Build the debug APK
./gradlew assembleDebug

# Install and run on your connected device/emulator
./gradlew installDebug

# Run unit tests (including WorkManager tests)
./gradlew testDebugUnitTest
```
