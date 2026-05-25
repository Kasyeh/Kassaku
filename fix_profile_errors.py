import re

file_path = "app/src/main/java/com/example/kassaku/ui/ProfileScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

# Fix LogoutConfirmationDialog
content = content.replace(
"""        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    showLogoutDialog = false
                    onLogout()
                },
                onDismiss = { showLogoutDialog = false }
            )
        }""",
"""        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    showLogoutDialog = false
                    onLogout()
                },
                onDismissRequest = { showLogoutDialog = false },
                isDark = isDark
            )
        }"""
)

# Replace the whole Password, Avatar, and Currency sheets with inline implementations or remove if not needed.
# Since Avatar is handled by imagePickerLauncher, we don't need AvatarSelectionSheet.
# But we have `showAvatarSheet = true` clicking on the avatar. We can just change it to launch imagePickerLauncher directly.
content = content.replace(
    ".clickable { showAvatarSheet = true }",
    ".clickable { imagePickerLauncher.launch(\"image/*\") }"
)

# Remove the showAvatarSheet dialog entirely
avatar_sheet_regex = r"        if \(showAvatarSheet\) \{\s*com\.example\.kassaku\.ui\.components\.AvatarSelectionSheet\([\s\S]*?\}\s*\)\s*\}"
content = re.sub(avatar_sheet_regex, "", content)

# Remove the showPasswordDialog from bottom and replace with inline AlertDialog
password_dialog_old = r"        if \(showPasswordDialog\) \{\s*com\.example\.kassaku\.ui\.components\.ChangePasswordDialog\([\s\S]*?\}\s*\)"
password_dialog_new = """        if (showPasswordDialog) {
            var currentPassword by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text("Ganti Password") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Password Lama") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Password Baru") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Konfirmasi Password Baru") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            homeViewModel.updatePassword(currentPassword, newPassword, confirmPassword)
                        },
                        enabled = currentPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false }) { Text("Batal") }
                }
            )
        }"""
content = re.sub(password_dialog_old, password_dialog_new, content)

# Remove the showCurrencySheet from bottom and replace with inline AlertDialog
currency_sheet_old = r"        if \(showCurrencySheet\) \{\s*com\.example\.kassaku\.ui\.components\.CurrencySelectionSheet\([\s\S]*?\}\s*\)\s*\}"
currency_sheet_new = """        if (showCurrencySheet) {
            var selectedCurrency by remember { mutableStateOf(balanceData?.currency ?: "IDR") }
            var selectedFormat by remember { mutableStateOf(balanceData?.currencyFormat ?: "standard") }
            
            AlertDialog(
                onDismissRequest = { showCurrencySheet = false },
                title = { Text("Format Mata Uang") },
                text = {
                    Column {
                        Text("Pilih Mata Uang", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            FilterChip(selected = selectedCurrency == "IDR", onClick = { selectedCurrency = "IDR" }, label = { Text("IDR") })
                            FilterChip(selected = selectedCurrency == "USD", onClick = { selectedCurrency = "USD" }, label = { Text("USD") })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Format Tampilan", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            FilterChip(selected = selectedFormat == "standard", onClick = { selectedFormat = "standard" }, label = { Text("Standar") })
                            FilterChip(selected = selectedFormat == "compact", onClick = { selectedFormat = "compact" }, label = { Text("Ringkas") })
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        homeViewModel.updateCurrency(userId, selectedCurrency, selectedFormat)
                    }) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCurrencySheet = false }) { Text("Batal") }
                }
            )
        }"""
content = re.sub(currency_sheet_old, currency_sheet_new, content)

# Remove unused imports and fix SettingItemRowDestructive call missing isDark in some places? No, it has it.
# Wait, FilterChip might need OptIn ExperimentalMaterial3Api, but ProfileScreen already has it at the top of the composable.

with open(file_path, "w") as f:
    f.write(content)
