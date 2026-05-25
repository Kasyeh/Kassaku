import re

file_path = "app/src/main/java/com/example/kassaku/ui/HomeScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

# Remove the import we added
content = content.replace("import androidx.navigation.NavGraph.Companion.findStartDestination\n", "")

def replace_nav(match):
    # This will replace the whole block that the previous script created
    return ""

# The previous script created blocks like:
# navController.navigate(com.example.kassaku.ui.components.BottomNavItem.Impian.route) {
#                                             popUpTo(navController.graph.findStartDestination().id) {
#                                                 saveState = true
#                                             }
#                                             launchSingleTop = true
#                                             restoreState = true
#                                         }
content = re.sub(
    r'navController\.navigate\(([^)]+)\)\s*\{\s*popUpTo\(navController\.graph[^)]*\)[^\}]+\}\s*launchSingleTop = true\s*restoreState = true\s*\}',
    r'navController.navigate(\1) {\n                                            popUpTo(com.example.kassaku.AppDestinations.HOME_ROUTE) {\n                                                saveState = true\n                                            }\n                                            launchSingleTop = true\n                                            restoreState = true\n                                        }',
    content
)

with open(file_path, "w") as f:
    f.write(content)
