@file:JvmName("AwtSystem")

package org.runestar.client.api.util

import org.kxtra.slf4j.debug
import org.kxtra.slf4j.getLogger
import java.awt.*
import java.nio.file.Files
import java.nio.file.Path

private val logger = getLogger()

private inline fun <T> safe(f: () -> T): T? {
    return try {
        f()
    } catch (e: Exception) {
        logger.debug(e.message)
        null
    }
}

fun safeTrayIcon(image: Image, tooltip: String) = safe { TrayIcon(image, tooltip) }

val taskbar: Taskbar? get() = safe { Taskbar.getTaskbar() }

fun Taskbar.safeSetWindowProgressValue(w: Window, value: Int) {
    safe { setWindowProgressValue(w, value) }
}

fun Taskbar.safeSetWindowProgressState(w: Window, state: Taskbar.State) {
    safe { setWindowProgressState(w, state) }
}

fun Taskbar.safeRequestWindowUserAttention(w: Window) {
    safe { requestWindowUserAttention(w) }
}

var Taskbar.safeMenu: PopupMenu?
    get() = safe { menu }
    set(value) { safe { menu = value } }

var Taskbar.safeIconImage: Image?
    get() = safe { iconImage }
    set(value) { safe { iconImage = value } }

val desktop: Desktop? get() = safe { Desktop.getDesktop() }

fun Desktop.safeOpen(path: Path) {
    if (isSupported(Desktop.Action.OPEN)) {
        try {
            open(path.toFile())
        } catch (e: Exception) {
            logger.debug { "Desktop failed to open $path" }
            if (Files.isRegularFile(path)) {
                val dir = path.parent
                logger.debug { "Desktop opening parent directory $dir" }
                safeOpen(dir)
            }
        }
    } else {
        logger.debug { "Desktop open is not supported" }
    }
}

val systemTray: SystemTray? get() = safe { SystemTray.getSystemTray() }