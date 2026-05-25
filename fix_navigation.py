import re

file_path = "app/src/main/java/com/example/kassaku/ui/HomeScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

def replace_nav(match):
    route = match.group(1)
    return f"""navController.navigate({route}) {{
                                            popUpTo(navController.graph.findStartDestination().id) {{
                                                saveState = true
                                            }}
                                            launchSingleTop = true
                                            restoreState = true
                                        }}"""

content = re.sub(r'navController\.navigate\((BottomNavItem\.[a-zA-Z]+\.route)\)', replace_nav, content)

with open(file_path, "w") as f:
    f.write(content)
