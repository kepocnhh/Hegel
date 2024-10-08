package org.kepocnhh.hegel.module.receiver

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.showToast

@Composable
private fun CheckPermissions(
    permissions: List<String>,
    onGranted: @Composable () -> Unit,
    onNotGranted: (List<String>) -> Unit,
) {
    val context = LocalContext.current
    val requested = remember { mutableStateOf(false) }
    val granted = remember {
        val value = permissions.all { permission ->
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
        mutableStateOf(value)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        requested.value = true
    }
    LaunchedEffect(requested.value) {
        if (!granted.value) {
            val notGranted = permissions.filter { permission ->
                context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
            }
            if (notGranted.isEmpty()) {
                granted.value = true
            } else if (!requested.value) {
                launcher.launch(permissions.toTypedArray())
            } else {
                onNotGranted(notGranted)
            }
        }
    }
    if (granted.value) {
        onGranted()
    }
}

private fun getPermissions(): List<String> {
    val permissions = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    return permissions
}

@Composable
internal fun ReceiverScreen(onBack: () -> Unit) {
    val logger = remember { App.injection.loggers.create("[Receiver|Screen]") }
    val state = ReceiverService.states.collectAsState().value
    LaunchedEffect(state) {
        logger.debug("on state: $state")
    }
    val context = LocalContext.current
    CheckPermissions(
        permissions = getPermissions(),
        onGranted = {
            ReceiverScreen(
                onBack = onBack,
                state = state,
            )
        },
        onNotGranted = {
            context.showToast("Not granted: $it")
            onBack()
        },
    )
}
