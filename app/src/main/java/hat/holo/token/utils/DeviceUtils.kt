package hat.holo.token.utils

import android.content.Context
import hat.holo.token.models.DeviceInfo


@Suppress("MemberVisibilityCanBePrivate", "unused")
object DeviceUtils {

    private lateinit var id: String
    private lateinit var fp: String
    lateinit var deviceInfo: DeviceInfo

    fun init(ctx: Context) {
        val preDevice = ctx.getSharedPreferences("pre_device.xml", 0)
        id = preDevice.getString("device_id", "").toString()
        val comboDeviceFingerprint = ctx.getSharedPreferences("combo_device_fingerprint", 0)
        fp = comboDeviceFingerprint.getString("fp", "").toString()
        deviceInfo = DeviceInfo(id, fp)
    }
}
