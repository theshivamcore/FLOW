package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: FlowViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    var isForgotPassword by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val usernameAvailability by viewModel.usernameAvailability.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FLOW Logo Brand
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(FlowTeal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AllInclusive,
                    contentDescription = "FLOW Logo",
                    tint = FlowTeal,
                    modifier = Modifier.size(42.dp)
                )
            }

            Text(
                text = "FLOW",
                style = MaterialTheme.typography.displayMedium,
                color = TextHigh,
                letterSpacing = 2.sp
            )

            Text(
                text = if (isForgotPassword) "Reset your password"
                else if (isSignUp) "Design your daily mastery"
                else "Welcome back to your flow state",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Card Form Container
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (authError != null) {
                        Surface(
                            color = FlowRose.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = FlowRose, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = authError ?: "", color = FlowRose, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    if (isSignUp) {
                        // Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = FlowTeal) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("fullname_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlowTeal,
                                focusedLabelColor = FlowTeal
                            )
                        )

                        // Unique Username
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = {
                                    username = it.lowercase().trim()
                                    viewModel.checkUsername(it)
                                },
                                label = { Text("Unique Username") },
                                leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = FlowTeal) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("username_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (usernameAvailability == false) FlowRose else FlowTeal,
                                    focusedLabelColor = if (usernameAvailability == false) FlowRose else FlowTeal
                                ),
                                isError = usernameAvailability == false
                            )

                            // Immediate username availability feedback
                            if (username.length >= 3) {
                                if (usernameAvailability == false) {
                                    Text(
                                        text = "Username unavailable",
                                        color = FlowRose,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                } else if (usernameAvailability == true) {
                                    Text(
                                        text = "✓ Username available",
                                        color = FlowEmerald,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(if (isSignUp || isForgotPassword) "Email Address" else "Email or Username") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = FlowTeal) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlowTeal,
                            focusedLabelColor = FlowTeal
                        )
                    )

                    // Password Field (if not forgot password)
                    if (!isForgotPassword) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FlowTeal) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextLow
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlowTeal,
                                focusedLabelColor = FlowTeal
                            )
                        )
                    }

                    if (isForgotPassword) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("New Password") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = FlowTeal) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlowTeal,
                                focusedLabelColor = FlowTeal
                            )
                        )
                    }

                    // Submit Action Button
                    Button(
                        onClick = {
                            if (isForgotPassword) {
                                viewModel.resetPassword(email, password)
                                isForgotPassword = false
                            } else if (isSignUp) {
                                if (usernameAvailability != false) {
                                    viewModel.signup(username, email, password, fullName)
                                }
                            } else {
                                viewModel.login(email, password)
                            }
                        },
                        enabled = if (isSignUp) (username.isNotBlank() && email.isNotBlank() && password.length >= 4 && usernameAvailability != false)
                        else (email.isNotBlank() && password.isNotBlank()),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button")
                    ) {
                        Text(
                            text = if (isForgotPassword) "Reset Password" else if (isSignUp) "Create Account" else "Sign In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Quick Demo Access
                    OutlinedButton(
                        onClick = {
                            viewModel.signup("flowuser", "demo@flow.app", "flow123", "Alex Rivers")
                            viewModel.login("demo@flow.app", "flow123")
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FlowTeal),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlowTeal.copy(alpha = 0.5f))),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("quick_demo_button")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quick Start / Demo Flow", fontWeight = FontWeight.SemiBold)
                    }

                    // Secondary Links (Forgot password, Switch between Sign in and Sign up)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isForgotPassword && !isSignUp) {
                            TextButton(onClick = { isForgotPassword = true }) {
                                Text("Forgot Password?", color = TextMedium, style = MaterialTheme.typography.bodyMedium)
                            }
                        } else if (isForgotPassword) {
                            TextButton(onClick = { isForgotPassword = false }) {
                                Text("Back to Sign In", color = FlowTeal, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        TextButton(
                            onClick = {
                                isSignUp = !isSignUp
                                isForgotPassword = false
                            }
                        ) {
                            Text(
                                text = if (isSignUp) "Already have an account? Sign In" else "New to FLOW? Sign Up",
                                color = FlowTeal,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
