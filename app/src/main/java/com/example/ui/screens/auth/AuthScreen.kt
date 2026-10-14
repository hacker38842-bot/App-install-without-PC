package com.example.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EsportsGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppLanguage
import com.example.util.Localization

@Composable
fun AuthScreen(
    currentLanguage: AppLanguage,
    initialReferralCode: String? = null,
    onLogin: (email: String, pass: String, rememberMe: Boolean, onResult: (Boolean, String) -> Unit) -> Unit,
    onRegister: (name: String, email: String, pass: String, referralCode: String?, onResult: (Boolean, String) -> Unit) -> Unit,
    onForgotPassword: (email: String, onResult: (Boolean, String) -> Unit) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (!initialReferralCode.isNullOrBlank()) 1 else 0) } // 0 = Login, 1 = Register
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Login Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var rememberSession by remember { mutableStateOf(true) }
    var isLoginLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Register Form State
    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirmPassword by remember { mutableStateOf("") }
    var registerReferralCode by remember { mutableStateOf(initialReferralCode ?: "") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var registerConfirmVisible by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var isRegisterLoading by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }
    var registerSuccess by remember { mutableStateOf<String?>(null) }

    // Forgot Password Dialog
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var isForgotLoading by remember { mutableStateOf(false) }
    var forgotMessage by remember { mutableStateOf<String?>(null) }
    var forgotSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Branding Hero
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_arena_x_logo),
                contentDescription = "ARENA X Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(68.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = Localization.getString("app_title", currentLanguage),
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            color = Color.White
        )

        Text(
            text = "Competitive Esports Tournament Network",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Auth Container Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_container_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Tab Navigation (Login / Register)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurfaceElevated,
                    contentColor = CyberCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberCyan,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            loginError = null
                        },
                        text = {
                            Text(
                                text = Localization.getString("login", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selectedTab == 0) Color.White else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_login")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            registerError = null
                        },
                        text = {
                            Text(
                                text = Localization.getString("register_tab", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selectedTab == 1) Color.White else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_register")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedTab == 0) {
                    // ==========================================
                    // LOGIN FORM
                    // ==========================================
                    Text(
                        text = "Sign In with Firebase",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Enter your verified credentials to enter your tournament dashboard.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (loginError != null) {
                        Surface(
                            color = CrimsonRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_error_banner")
                        ) {
                            Text(
                                text = loginError ?: "",
                                color = CrimsonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email Field
                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = {
                            loginEmail = it
                            loginError = null
                        },
                        label = { Text(Localization.getString("email", currentLanguage)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            loginError = null
                        },
                        label = { Text(Localization.getString("password", currentLanguage)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                Icon(
                                    imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (loginPasswordVisible) "Hide password" else "Show password",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricViolet,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = ElectricViolet,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Remember Me & Forgot Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberSession,
                                onCheckedChange = { rememberSession = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CyberCyan,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = Color.Black
                                ),
                                modifier = Modifier.testTag("remember_session_checkbox")
                            )
                            Text(
                                text = Localization.getString("remember_session", currentLanguage),
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }

                        TextButton(
                            onClick = {
                                forgotPasswordEmail = loginEmail.trim()
                                forgotMessage = null
                                forgotSuccess = false
                                showForgotPasswordDialog = true
                            },
                            modifier = Modifier.testTag("forgot_password_button")
                        ) {
                            Text(
                                text = Localization.getString("forgot_password", currentLanguage),
                                fontSize = 11.sp,
                                color = CyberCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Login Submit Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val emailTrim = loginEmail.trim()
                            val passTrim = loginPassword
                            if (emailTrim.isEmpty()) {
                                loginError = "Please enter your email address."
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches()) {
                                loginError = "Please enter a valid email address (e.g. user@domain.com)."
                                return@Button
                            }
                            if (passTrim.isEmpty()) {
                                loginError = "Please enter your password."
                                return@Button
                            }

                            isLoginLoading = true
                            loginError = null
                            onLogin(emailTrim, passTrim, rememberSession) { success, msg ->
                                isLoginLoading = false
                                if (!success) {
                                    loginError = msg
                                }
                            }
                        },
                        enabled = !isLoginLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (isLoginLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Localization.getString("logging_in", currentLanguage), color = Color.White)
                        } else {
                            Text(
                                text = Localization.getString("login", currentLanguage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // ==========================================
                    // REGISTER FORM
                    // ==========================================
                    Text(
                        text = "Create Competitive Account",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Sign up with Firebase to start competing in verified tournaments.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (registerError != null) {
                        Surface(
                            color = CrimsonRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_error_banner")
                        ) {
                            Text(
                                text = registerError ?: "",
                                color = CrimsonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (registerSuccess != null) {
                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_success_banner")
                        ) {
                            Text(
                                text = registerSuccess ?: "",
                                color = EmeraldGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Full Name
                    OutlinedTextField(
                        value = registerName,
                        onValueChange = {
                            registerName = it
                            registerError = null
                        },
                        label = { Text(Localization.getString("full_name", currentLanguage)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Address
                    OutlinedTextField(
                        value = registerEmail,
                        onValueChange = {
                            registerEmail = it
                            registerError = null
                        },
                        label = { Text(Localization.getString("email", currentLanguage)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = registerPassword,
                        onValueChange = {
                            registerPassword = it
                            registerError = null
                        },
                        label = { Text(Localization.getString("password", currentLanguage)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { registerPasswordVisible = !registerPasswordVisible }) {
                                Icon(
                                    imageVector = if (registerPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (registerPasswordVisible) "Hide password" else "Show password",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (registerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricViolet,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = ElectricViolet,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password
                    OutlinedTextField(
                        value = registerConfirmPassword,
                        onValueChange = {
                            registerConfirmPassword = it
                            registerError = null
                        },
                        label = { Text(Localization.getString("confirm_password", currentLanguage)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { registerConfirmVisible = !registerConfirmVisible }) {
                                Icon(
                                    imageVector = if (registerConfirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (registerConfirmVisible) "Hide password" else "Show password",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (registerConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricViolet,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = ElectricViolet,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_confirm_password_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Referral / Invite Code (Optional)
                    OutlinedTextField(
                        value = registerReferralCode,
                        onValueChange = {
                            registerReferralCode = it.uppercase()
                        },
                        label = { Text("Referral Code (Optional)") },
                        placeholder = { Text("e.g. ARENA9X", color = TextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = EsportsGold, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EsportsGold,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = EsportsGold,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_referral_code_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Terms Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { acceptedTerms = !acceptedTerms },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = CyberCyan,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = Color.Black
                            ),
                            modifier = Modifier.testTag("accept_terms_checkbox")
                        )
                        Text(
                            text = Localization.getString("accept_terms", currentLanguage),
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Register Submit Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val nameTrim = registerName.trim()
                            val emailTrim = registerEmail.trim()
                            val pass = registerPassword
                            val confirm = registerConfirmPassword

                            if (nameTrim.isEmpty()) {
                                registerError = "Please enter your full name."
                                return@Button
                            }
                            if (emailTrim.isEmpty()) {
                                registerError = "Please enter your email address."
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches()) {
                                registerError = "Please enter a valid email address."
                                return@Button
                            }
                            if (pass.length < 6) {
                                registerError = "Password must be at least 6 characters."
                                return@Button
                            }
                            if (pass != confirm) {
                                registerError = "Passwords do not match."
                                return@Button
                            }
                            if (!acceptedTerms) {
                                registerError = "You must accept the terms and conditions to register."
                                return@Button
                            }

                            isRegisterLoading = true
                            registerError = null
                            registerSuccess = null
                            onRegister(nameTrim, emailTrim, pass, registerReferralCode.trim().ifBlank { null }) { success, msg ->
                                isRegisterLoading = false
                                if (success) {
                                    registerSuccess = msg
                                } else {
                                    registerError = msg
                                }
                            }
                        },
                        enabled = !isRegisterLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("register_submit_button")
                    ) {
                        if (isRegisterLoading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Localization.getString("registering", currentLanguage), color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = Localization.getString("register_tab", currentLanguage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secure Real Firebase indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Powered by Real Firebase Auth • muskan-panel-store-website",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isForgotLoading) showForgotPasswordDialog = false
            },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = Localization.getString("reset_password", currentLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your registered email address and Firebase will send a secure password reset link.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (forgotMessage != null) {
                        Surface(
                            color = if (forgotSuccess) EmeraldGreen.copy(alpha = 0.2f) else CrimsonRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = forgotMessage ?: "",
                                color = if (forgotSuccess) EmeraldGreen else CrimsonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = {
                            forgotPasswordEmail = it
                            forgotMessage = null
                        },
                        label = { Text(Localization.getString("email", currentLanguage)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forgot_password_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val email = forgotPasswordEmail.trim()
                        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            forgotMessage = "Please enter a valid email address."
                            forgotSuccess = false
                            return@Button
                        }
                        isForgotLoading = true
                        onForgotPassword(email) { ok, msg ->
                            isForgotLoading = false
                            forgotSuccess = ok
                            forgotMessage = msg
                        }
                    },
                    enabled = !isForgotLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.testTag("send_reset_link_button")
                ) {
                    if (isForgotLoading) {
                        CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Send Reset Link", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showForgotPasswordDialog = false },
                    enabled = !isForgotLoading
                ) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }
}
