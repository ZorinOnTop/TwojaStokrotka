package io.github.zorinontop.twojastokrotka

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.zorinontop.twojastokrotka.network.LoginApiService
import io.github.zorinontop.twojastokrotka.network.SmsRequest
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import io.github.zorinontop.twojastokrotka.utils.formatPhoneNumber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@Composable
fun OtpScreen(phoneNumber: String, onBack: () -> Unit, onVerifySuccess: (String) -> Unit) {
    var otpCode by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(60) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { LoginApiService.create() }
    val sessionManager = remember { SessionManager(context) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        }
    }

    LaunchedEffect(otpCode) {
        if (otpCode.length == 6 && !isLoading) {
            isLoading = true
            errorMessage = null
            coroutineScope.launch {
                try {
                    val response = apiService.verifyOtp(
                        otpCode = otpCode,
                        phoneNumber = "+48$phoneNumber"
                    )
                    if (response.isSuccessful) {
                        val token = response.body()?.access_token ?: ""
                        val refreshToken = response.body()?.refresh_token
                        sessionManager.saveSession(token, refreshToken)
                        onVerifySuccess(token)
                    } else {
                        errorMessage = if (response.code() == 400) {
                            "Niepoprawny kod SMS"
                        } else {
                            "Błąd weryfikacji: ${response.code()}"
                        }
                        otpCode = ""
                    }
                } catch (e: Exception) {
                    errorMessage = "Błąd połączenia: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_transparent),
            contentDescription = "Logo",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Wpisz kod SMS,",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "który za chwilę otrzymasz",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "+48 ${formatPhoneNumber(phoneNumber)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(contentAlignment = Alignment.Center) {
            TextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it.filter { c -> c.isDigit() } },
                modifier = Modifier
                    .size(1.dp)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                enabled = !isLoading,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) { focusRequester.requestFocus() },
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(6) { index ->
                    val char = otpCode.getOrNull(index)?.toString() ?: ""
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(40.dp)
                    ) {
                        Text(
                            text = if (isLoading && char.isEmpty()) "" else char,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    when {
                                        isLoading -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        char.isEmpty() -> Color.Gray
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                        )
                    }
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (timeLeft > 0) "Wyślij ponownie ($timeLeft)" else "Wyślij ponownie",
            color = if (timeLeft > 0 || isLoading) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable(enabled = timeLeft == 0 && !isLoading) {
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val response = apiService.sendSms(SmsRequest(phoneNumber = "+48$phoneNumber"))
                            if (response.isSuccessful) {
                                timeLeft = 60
                            } else {
                                errorMessage = "Błąd ponownego wysyłania: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Błąd połączenia: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
        )

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(onClick = onBack, enabled = !isLoading) {
            Text("Zmień numer telefonu", color = if (isLoading) Color.Gray else MaterialTheme.colorScheme.primary)
        }
    }
}
