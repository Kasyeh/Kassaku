import re

file_path = "app/src/main/java/com/example/kassaku/ui/ProfileScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

# Remove the parameter
content = content.replace(
    "    onNavigateToReminderSettings: () -> Unit,\n    modifier: Modifier = Modifier",
    "    modifier: Modifier = Modifier"
)

# Replace the onClick
content = content.replace(
    "onClick = onNavigateToReminderSettings,",
    "onClick = { Toast.makeText(context, \"Fitur pengingat akan segera hadir!\", Toast.LENGTH_SHORT).show() },"
)

with open(file_path, "w") as f:
    f.write(content)
