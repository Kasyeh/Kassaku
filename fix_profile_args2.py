import re

file_path = "app/src/main/java/com/example/kassaku/ui/ProfileScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

# Add the parameter back
content = content.replace(
"""fun ProfileScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
)""",
"""fun ProfileScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    onNavigateToReminderSettings: () -> Unit,
    modifier: Modifier = Modifier
)"""
)

# Replace the onClick
content = content.replace(
    "onClick = { Toast.makeText(context, \"Fitur pengingat akan segera hadir!\", Toast.LENGTH_SHORT).show() },",
    "onClick = onNavigateToReminderSettings,"
)

with open(file_path, "w") as f:
    f.write(content)
