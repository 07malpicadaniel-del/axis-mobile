# TaskApp - Personal Agenda & Integrated Routine Assistant

**TaskApp** is a native Android application built with **Jetpack Compose**, **Material Design 3**, and **Room Database**. It is designed as an all-in-one personal assistant, seamlessly integrating task management, daily habits, calendar reflections, weekly schedules, workout routines, personal finance, and unified historical analytics in a clean 100% monochrome design.

---

## 📱 Core Modules & Features

### 1. 📋 Tasks & Daily Streak Tracker
- **Full Task CRUD**: Title, description, due date, priority (*High*, *Medium*, *Low*), and category (*General*, *Work*, *Personal*, *Study*, *Home*).
- **Real-Time Search & Filters**: Filter by status (*All*, *Pending*, *Completed*), category, and live search query.
- **Overdue Indicators**: Highlighted badges for overdue pending tasks.
- **Daily App Streak (Dynamic Black/White Flame 🔥)**: Overall streak counter with a dynamic flame icon that automatically displays in **Black in Light Mode** and **White in Dark Mode**.
- **Automated Task Reminders**: System notifications triggered automatically to remind users of pending tasks due today.

### 2. 📅 Dedicated Calendar & Daily Reflection
- **Focused Calendar Grid**: Custom Month Calendar with priority indicators inside day cells.
- **Daily Journal & Mood**: Record daily mood (*Great*, *Good*, *Okay*, *Tired*) and personal reflections for any selected date with a clean responsive `FlowRow` selector.
- **Streamlined Design**: Simplified view dedicated exclusively to calendar grid and daily mood reflection.

### 3. ⏰ Schedule & Workout / Gym Routine
- **Weekly Schedule Blocks**: Hourly time blocks organized by day of the week (*Monday* to *Sunday*).
- **Schedule Adherence Checkboxes**: Mark activities as accomplished directly in the schedule view.
- **Preset Routines (1-Click)**: Instant loading of standard routine templates (*Office & Work*, *Student*, *Weekend*).
- **Workout / Gym Routine Subsection**: Dedicated workout tracking tab per day:
  - Add exercises with target sets/reps (e.g. *Flat Bench Press 4x10*) and muscle group categories (*Chest*, *Back*, *Legs*, *Shoulders*, *Biceps*, *Triceps*, *Cardio*, *Abs*).
  - Checkboxes to complete exercises as you train during the day with workout progress bar.
  - 1-Click Gym Routine Presets (*Chest & Triceps*, *Back & Biceps*, *Legs & Shoulders*, *Cardio & Abs*).

### 4. 💰 Personal Finances & Savings Goals
- **Monthly Budget**: Configurable budget/income baseline (e.g., *$1,000.00*).
- **Income & Expense Entries**: Categorized transactions (*Rent*, *Groceries*, *Utilities*, *Transport*, *Leisure*, *Savings*, *Other*).
- **Balance & Progress**: Tracks total expenses, remaining balance, and budget usage percentage.
- **Savings Goals**: Target tracking (*Emergency Fund, Laptop, Trip*) with direct deposit actions.

### 5. 📊 Analytics & 7-Day Performance History Log
Control dashboard organized into 5 tabs:
- **Overview**: Executive summary of task completion %, schedule adherence %, and remaining budget.
- **Tasks**: Circular Canvas completion ring and priority distribution.
- **Finances**: Expense breakdown by category and budget consumption bar.
- **Schedule**: Routine adherence rate and time distribution across categories.
- **History (7-Day Performance Log)**: Historical performance log for each of the past 7 days displaying completed tasks, habits done, expenses, and recorded mood.

### 6. ⚙️ Settings, Monochrome Theme & Notifications
- **100% Monochrome Black & White Theme**: Pure minimal design without color distractions, with explicitly defined color roles for containers, FABs, chips, and top bars.
- **Light / Dark Mode Switch**: Instant theme toggle.
- **Intuitive Navigation**: Native Back button in Settings top bar.
- **Local Notifications (Android 13+ Ready)**: Native system reminders with runtime permission handling (`POST_NOTIFICATIONS`).

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material Design 3
- **Navigation**: Jetpack Navigation Compose (5 main tabs + Settings)
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Repository Pattern
- **State Management**: `StateFlow`, `MutableStateFlow`, Coroutines, `AndroidViewModel`
- **Local Database**: **Room Database (Version 7)**
  - Entities: `Task`, `ScheduleSlot`, `FinanceEntry`, `Habit`, `HabitLog`, `DailyNote`, `SavingGoal`, `GymExercise`
  - DAOs: `TaskDao`, `ScheduleDao`, `FinanceDao`, `HabitDao`, `DailyNoteDao`, `SavingGoalDao`, `GymExerciseDao`

---

## 🚀 Project Structure

```
com.example.taskapp
├── data
│   ├── Task.kt / TaskDao.kt
│   ├── ScheduleSlot.kt / ScheduleDao.kt
│   ├── FinanceEntry.kt / FinanceDao.kt
│   ├── Habit.kt / HabitLog.kt / HabitDao.kt
│   ├── DailyNote.kt / DailyNoteDao.kt
│   ├── SavingGoal.kt / SavingGoalDao.kt
│   ├── GymExercise.kt / GymExerciseDao.kt
│   ├── Converters.kt
│   ├── TaskDatabase.kt (Version 7)
│   └── TaskRepository.kt
├── ui
│   ├── navigation
│   │   └── navigation.kt
│   ├── screens
│   │   ├── tasks.kt
│   │   ├── calendar.kt (Dedicated Calendar & Mood Journal)
│   │   ├── schedule.kt (Schedule & Workout / Gym)
│   │   ├── finances.kt
│   │   ├── stats.kt (Overview, Tasks, Finances, Schedule, History)
│   │   └── settings.kt
│   ├── theme
│   │   ├── Color.kt
│   │   ├── Theme.kt (100% Pure Monochrome Theme)
│   │   ├── ThemePreset.kt (MonochromePalette)
│   │   └── Type.kt
│   └── viewmodel
│       └── TaskViewModel.kt
├── util
│   └── NotificationHelper.kt
└── MainActivity.kt
```

---

## 📝 Build & Execution

1. Open project in **Android Studio**.
2. Sync Gradle (`Sync Project with Gradle Files`).
3. Run build task `:app:assembleDebug`.
4. Deploy to an emulator or Android device running **API 24+**.
