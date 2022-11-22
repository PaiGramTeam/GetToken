package hat.holo.token.utils

import android.graphics.drawable.Drawable
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.*

@Suppress("SpellCheckingInspection")
object Res {

    val iconToken: Drawable? get() {
        val s = "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"
        return Base64.getDecoder().decode(s).inputStream().use { stream ->
            Drawable.createFromStream(stream, null)
        }
    }

    val iconCopy: ImageVector
        get() {
            if (_contentCopy != null) {
                return _contentCopy!!
            }
            _contentCopy = materialIcon(name = "Outlined.ContentCopy") {
                materialPath {
                    moveTo(16.0f, 1.0f)
                    lineTo(4.0f, 1.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(14.0f)
                    horizontalLineToRelative(2.0f)
                    lineTo(4.0f, 3.0f)
                    horizontalLineToRelative(12.0f)
                    lineTo(16.0f, 1.0f)
                    close()
                    moveTo(19.0f, 5.0f)
                    lineTo(8.0f, 5.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(14.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(11.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    lineTo(21.0f, 7.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(19.0f, 21.0f)
                    lineTo(8.0f, 21.0f)
                    lineTo(8.0f, 7.0f)
                    horizontalLineToRelative(11.0f)
                    verticalLineToRelative(14.0f)
                    close()
                }
            }
            return _contentCopy!!
        }

    @Suppress("ObjectPropertyName")
    private var _contentCopy: ImageVector? = null

}
