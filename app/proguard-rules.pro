# Block Crush Puzzle - R8 / ProGuard rules for release builds.
# Google Mobile Ads, UMP and Play Billing ship their own consumer rules,
# so nothing special is required for them. The rules below keep crash
# reports readable and protect classes that are referenced from XML.

# Keep line numbers for readable stack traces in Play Console.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Views that are inflated from XML layouts by name.
-keep public class com.blockcrush.puzzle.ui.GameView { public <init>(...); }

# Broadcast receivers referenced from the manifest.
-keep public class com.blockcrush.puzzle.notifications.ReminderReceiver
-keep public class com.blockcrush.puzzle.notifications.BootReceiver
