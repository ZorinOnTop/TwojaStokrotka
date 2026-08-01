package io.github.zorinontop.twojastokrotka

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.zorinontop.twojastokrotka.network.LoginApiService
import io.github.zorinontop.twojastokrotka.network.SmsRequest
import io.github.zorinontop.twojastokrotka.utils.PhoneVisualTransformation
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { LoginApiService.create() }

    val isValid = phoneNumber.length == 9

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
            text = "Witaj!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Podaj swój numer telefonu",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 9) phoneNumber = it.filter { char -> char.isDigit() } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Numer telefonu") },
            prefix = {
                Text(
                    text = "+48",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            isError = errorMessage != null,
            visualTransformation = PhoneVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(56.dp))

        Button(
            onClick = {
                if (isValid) {
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val response = apiService.sendSms(SmsRequest(phoneNumber = "+48$phoneNumber"))
                            if (response.isSuccessful) {
                                onLoginSuccess(phoneNumber)
                            } else {
                                errorMessage = "Błąd serwera: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Błąd połączenia: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            enabled = isValid && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "DALEJ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
