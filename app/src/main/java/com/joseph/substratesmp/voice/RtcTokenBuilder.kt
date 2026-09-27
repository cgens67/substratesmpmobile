package com.joseph.substratesmp.voice

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.TreeMap
import java.util.zip.CRC32
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

interface PackableEx {
  fun marshal(out: ByteBuf): ByteBuf
  fun unmarshal(`in`: ByteBuf)
}

class ByteBuf {
  private var buffer: ByteBuffer = ByteBuffer.allocate(2048).order(ByteOrder.LITTLE_ENDIAN)

  constructor()

  constructor(bytes: ByteArray) {
    this.buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
  }

  fun asBytes(): ByteArray {
    val out = ByteArray(buffer.position())
    buffer.rewind()
    buffer.get(out, 0, out.size)
    return out
  }

  fun put(v: Short): ByteBuf {
    buffer.putShort(v)
    return this
  }

  fun put(v: ByteArray): ByteBuf {
    put(v.size.toShort())
    buffer.put(v)
    return this
  }

  fun put(v: Int): ByteBuf {
    buffer.putInt(v)
    return this
  }

  fun put(v: Long): ByteBuf {
    buffer.putLong(v)
    return this
  }

  fun put(v: String): ByteBuf {
    return put(v.toByteArray(StandardCharsets.UTF_8))
  }

  fun putIntMap(extra: TreeMap<Short, Int>): ByteBuf {
    put(extra.size.toShort())
    for ((k, v) in extra) {
      put(k)
      put(v)
    }
    return this
  }

  fun readShort(): Short = buffer.short
  fun readInt(): Int = buffer.int

  fun readBytes(): ByteArray {
    val length = readShort().toInt()
    val bytes = ByteArray(length)
    buffer.get(bytes)
    return bytes
  }

  fun readString(): String {
    val bytes = readBytes()
    return String(bytes, StandardCharsets.UTF_8)
  }

  fun readIntMap(): TreeMap<Short, Int> {
    val map = TreeMap<Short, Int>()
    val length = readShort().toInt()
    for (i in 0 until length) {
      val k = readShort()
      val v = readInt()
      map[k] = v
    }
    return map
  }
}

object TokenUtils {
  private val secureRandom = SecureRandom()

  fun randomInt(): Int = secureRandom.nextInt()

  fun getTimestamp(): Int = (System.currentTimeMillis() / 1000).toInt()

  fun isUUID(uuid: String?): Boolean {
    if (uuid == null || uuid.length != 32) return false
    return uuid.matches(Regex("\\p{XDigit}+"))
  }

  fun crc32(data: String): Int {
    val bytes = data.toByteArray(StandardCharsets.UTF_8)
    return crc32(bytes)
  }

  fun crc32(bytes: ByteArray): Int {
    val checksum = CRC32()
    checksum.update(bytes)
    return checksum.value.toInt()
  }

  fun hmacSign(keyString: String, msg: ByteArray): ByteArray {
    val keySpec = SecretKeySpec(keyString.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(keySpec)
    return mac.doFinal(msg)
  }

  fun pack(packableEx: PackableEx): ByteArray {
    val buffer = ByteBuf()
    packableEx.marshal(buffer)
    return buffer.asBytes()
  }

  fun base64Encode(data: ByteArray): String {
    return try {
      Base64.encodeToString(data, Base64.NO_WRAP)
    } catch (_: Throwable) {
      java.util.Base64.getEncoder().encodeToString(data)
    }
  }
}

class PrivilegeMessage : PackableEx {
  var salt: Int = TokenUtils.randomInt()
  var ts: Int = TokenUtils.getTimestamp() + 24 * 3600
  var messages: TreeMap<Short, Int> = TreeMap()

  override fun marshal(out: ByteBuf): ByteBuf {
    return out.put(salt).put(ts).putIntMap(messages)
  }

  override fun unmarshal(`in`: ByteBuf) {
    salt = `in`.readInt()
    ts = `in`.readInt()
    messages = `in`.readIntMap()
  }
}

class PackContent(
  var signature: ByteArray = ByteArray(0),
  var crcChannelName: Int = 0,
  var crcUid: Int = 0,
  var rawMessage: ByteArray = ByteArray(0)
) : PackableEx {

  override fun marshal(out: ByteBuf): ByteBuf {
    return out.put(signature).put(crcChannelName).put(crcUid).put(rawMessage)
  }

  override fun unmarshal(`in`: ByteBuf) {
    signature = `in`.readBytes()
    crcChannelName = `in`.readInt()
    crcUid = `in`.readInt()
    rawMessage = `in`.readBytes()
  }
}

class AccessToken(
  val appId: String,
  val appCertificate: String,
  val channelName: String,
  val uid: String
) {
  enum class Privileges(val intValue: Short) {
    kJoinChannel(1),
    kPublishAudioStream(2),
    kPublishVideoStream(3),
    kPublishDataStream(4),
    kRtmLogin(1000)
  }

  companion object {
    const val VER = "006"
  }

  private val message = PrivilegeMessage()

  fun addPrivilege(privilege: Privileges, expireTimestamp: Int) {
    message.messages[privilege.intValue] = expireTimestamp
  }

  fun build(): String {
    if (!TokenUtils.isUUID(appId) || !TokenUtils.isUUID(appCertificate)) {
      return ""
    }
    val messageRawContent = TokenUtils.pack(message)
    val signature = generateSignature(appCertificate, appId, channelName, uid, messageRawContent)
    val crcChannelName = TokenUtils.crc32(channelName)
    val crcUid = if (uid.isEmpty()) 0 else TokenUtils.crc32(uid)
    val packContent = PackContent(signature, crcChannelName, crcUid, messageRawContent)
    val content = TokenUtils.pack(packContent)
    return VER + appId + TokenUtils.base64Encode(content)
  }

  private fun generateSignature(
    appCertificate: String,
    appId: String,
    channelName: String,
    uid: String,
    message: ByteArray
  ): ByteArray {
    val baos = ByteArrayOutputStream()
    baos.write(appId.toByteArray(StandardCharsets.UTF_8))
    baos.write(channelName.toByteArray(StandardCharsets.UTF_8))
    baos.write(uid.toByteArray(StandardCharsets.UTF_8))
    baos.write(message)
    return TokenUtils.hmacSign(appCertificate, baos.toByteArray())
  }
}

class RtcTokenBuilder {
  enum class Role(val initValue: Int) {
    Role_Attendee(0),
    Role_Publisher(1),
    Role_Subscriber(2),
    Role_Admin(101)
  }

  fun buildTokenWithUid(
    appId: String,
    appCertificate: String,
    channelName: String,
    uid: Int,
    role: Role,
    privilegeTs: Int
  ): String {
    val account = if (uid == 0) "" else uid.toString()
    return buildTokenWithUserAccount(appId, appCertificate, channelName, account, role, privilegeTs)
  }

  fun buildTokenWithUserAccount(
    appId: String,
    appCertificate: String,
    channelName: String,
    account: String,
    role: Role,
    privilegeTs: Int
  ): String {
    val builder = AccessToken(appId, appCertificate, channelName, account)
    builder.addPrivilege(AccessToken.Privileges.kJoinChannel, privilegeTs)
    if (role == Role.Role_Publisher || role == Role.Role_Subscriber || role == Role.Role_Admin) {
      builder.addPrivilege(AccessToken.Privileges.kPublishAudioStream, privilegeTs)
      builder.addPrivilege(AccessToken.Privileges.kPublishVideoStream, privilegeTs)
      builder.addPrivilege(AccessToken.Privileges.kPublishDataStream, privilegeTs)
    }
    return try {
      builder.build()
    } catch (e: Exception) {
      e.printStackTrace()
      ""
    }
  }
}
