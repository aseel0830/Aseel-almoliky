package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.data.local.ElectricalDao
import com.example.data.local.ItemEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.StageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ProjectTransferHelper {

    /**
     * Encodes any image (file path, file:// URI, content:// URI) into a compact Base64 JPEG string.
     */
    fun encodeImageToBase64(context: Context, pathOrUri: String?): String? {
        if (pathOrUri.isNullOrBlank()) return null
        return try {
            val bitmap: Bitmap? = when {
                pathOrUri.startsWith("/") -> {
                    val file = File(pathOrUri)
                    if (file.exists() && file.isFile) BitmapFactory.decodeFile(file.absolutePath) else null
                }
                pathOrUri.startsWith("file://") -> {
                    val cleanPath = Uri.parse(pathOrUri).path
                    if (cleanPath != null) {
                        val file = File(cleanPath)
                        if (file.exists() && file.isFile) BitmapFactory.decodeFile(file.absolutePath) else null
                    } else null
                }
                pathOrUri.startsWith("content://") -> {
                    val uri = Uri.parse(pathOrUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                else -> {
                    val file = File(pathOrUri)
                    if (file.exists() && file.isFile) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else null
                }
            }

            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                // Scale if very large to prevent memory overhead while keeping high visual clarity
                val maxDim = 800
                val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                    val ratio = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
                    Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
                } else {
                    bitmap
                }
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a Base64 encoded string into a persistent file in the internal storage directory,
     * returning the absolute path to the file.
     */
    fun saveBase64ToInternalFile(dir: File, prefix: String, base64: String): String? {
        if (base64.isBlank()) return null
        return try {
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}_${(100..999).random()}.jpg")
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Exports a complete project to a portable JSON file, encoding attached images of
     * windows (stages) and items to Base64 so it can be shared with and opened on any other device.
     */
    suspend fun exportProjectFile(
        context: Context,
        project: ProjectEntity,
        stages: List<StageEntity>,
        items: List<ItemEntity>
    ): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "أصيل المليكي")
        root.put("type", "PROJECT_EXPORT")
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())

        // Project Info
        val projectObj = JSONObject()
        projectObj.put("name", project.name)
        projectObj.put("clientName", project.clientName)
        projectObj.put("notes", project.notes)
        root.put("project", projectObj)

        // Stages (Windows)
        val stagesArray = JSONArray()
        stages.forEach { stage ->
            val sObj = JSONObject()
            sObj.put("name", stage.name)
            sObj.put("orderIndex", stage.orderIndex)
            sObj.put("isDefault", stage.isDefault)
            if (!stage.imageUri.isNullOrBlank()) {
                val b64 = encodeImageToBase64(context, stage.imageUri)
                if (b64 != null) {
                    sObj.put("imageBase64", b64)
                }
            }
            stagesArray.put(sObj)
        }
        root.put("stages", stagesArray)

        // Items
        val itemsArray = JSONArray()
        items.forEach { item ->
            val iObj = JSONObject()
            iObj.put("stageName", item.stageName)
            iObj.put("name", item.name)
            iObj.put("quantityMode", item.quantityMode)
            iObj.put("quantity", item.quantity)
            iObj.put("q1", item.q1)
            iObj.put("q2", item.q2)
            iObj.put("q3", item.q3)
            iObj.put("wireColor", item.wireColor ?: "")
            iObj.put("c1", item.c1 ?: "")
            iObj.put("c2", item.c2 ?: "")
            iObj.put("c3", item.c3 ?: "")
            iObj.put("unit", item.unit)
            if (!item.imageUri.isNullOrBlank()) {
                val b64 = encodeImageToBase64(context, item.imageUri)
                if (b64 != null) {
                    iObj.put("imageBase64", b64)
                }
            }
            itemsArray.put(iObj)
        }
        root.put("items", itemsArray)

        val cleanName = project.name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val exportDir = File(context.cacheDir, "project_exports").apply { mkdirs() }
        val file = File(exportDir, "مشروع_${cleanName}.json")
        file.writeText(root.toString(2))
        file
    }

    /**
     * Imports a project from an exported JSON file (or full backup file) and writes it into the local database,
     * decoding and saving any attached images to persistent internal storage.
     */
    suspend fun importProjectFile(
        context: Context,
        fileUri: Uri,
        dao: ElectricalDao
    ): Long = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val jsonString = inputStream?.bufferedReader().use { it?.readText() }
            ?: throw IllegalArgumentException("فشل قراءة الملف المرفق")

        val root = JSONObject(jsonString)
        val importedImagesDir = File(context.filesDir, "imported_images").apply { mkdirs() }

        // Check if this is a single project export or a multi-project backup file
        if (root.has("project")) {
            val projObj = root.getJSONObject("project")
            val rawName = projObj.optString("name", "مشروع مستورد")
            val client = projObj.optString("clientName", "")
            val notes = projObj.optString("notes", "")

            val newProjectName = if (rawName.endsWith("(مستورد)")) rawName else "$rawName (مستورد)"

            // Clear current flag and insert project
            dao.clearCurrentProjectFlag()
            val newProjectId = dao.insertProject(
                ProjectEntity(
                    name = newProjectName,
                    clientName = client,
                    notes = notes,
                    isCurrent = true,
                    isBase = false
                )
            )

            // Import Stages
            val stagesArray = root.optJSONArray("stages")
            val stageEntities = mutableListOf<StageEntity>()
            if (stagesArray != null) {
                for (i in 0 until stagesArray.length()) {
                    val sObj = stagesArray.getJSONObject(i)
                    val stageName = sObj.optString("name", "نافذة $i")
                    val orderIndex = sObj.optInt("orderIndex", i)
                    val isDefault = sObj.optBoolean("isDefault", false)
                    val imageBase64 = sObj.optString("imageBase64", "")
                    val savedImageUri = if (imageBase64.isNotBlank()) {
                        saveBase64ToInternalFile(importedImagesDir, "stage_${newProjectId}_$i", imageBase64)
                    } else null

                    stageEntities.add(
                        StageEntity(
                            projectId = newProjectId,
                            name = stageName,
                            orderIndex = orderIndex,
                            isDefault = isDefault,
                            imageUri = savedImageUri
                        )
                    )
                }
            }
            if (stageEntities.isNotEmpty()) {
                dao.insertStages(stageEntities)
            }

            // Import Items
            val itemsArray = root.optJSONArray("items")
            val itemEntities = mutableListOf<ItemEntity>()
            if (itemsArray != null) {
                for (i in 0 until itemsArray.length()) {
                    val iObj = itemsArray.getJSONObject(i)
                    val stageName = iObj.optString("stageName", "")
                    val name = iObj.optString("name", "")
                    val quantityMode = iObj.optString("quantityMode", "SINGLE")
                    val quantity = iObj.optInt("quantity", 0)
                    val q1 = iObj.optInt("q1", 0)
                    val q2 = iObj.optInt("q2", 0)
                    val q3 = iObj.optInt("q3", 0)
                    val wireColor = iObj.optString("wireColor", "").ifBlank { null }
                    val c1 = iObj.optString("c1", "").ifBlank { null }
                    val c2 = iObj.optString("c2", "").ifBlank { null }
                    val c3 = iObj.optString("c3", "").ifBlank { null }
                    val unit = iObj.optString("unit", "حبة")
                    val imageBase64 = iObj.optString("imageBase64", "")
                    val savedImageUri = if (imageBase64.isNotBlank()) {
                        saveBase64ToInternalFile(importedImagesDir, "item_${newProjectId}_$i", imageBase64)
                    } else null

                    itemEntities.add(
                        ItemEntity(
                            projectId = newProjectId,
                            stageName = stageName,
                            name = name,
                            imageUri = savedImageUri,
                            quantityMode = quantityMode,
                            quantity = quantity,
                            q1 = q1,
                            q2 = q2,
                            q3 = q3,
                            wireColor = wireColor,
                            c1 = c1,
                            c2 = c2,
                            c3 = c3,
                            unit = unit
                        )
                    )
                }
            }
            if (itemEntities.isNotEmpty()) {
                dao.insertItems(itemEntities)
            }

            return@withContext newProjectId
        } else if (root.has("projects")) {
            // Full backup format import
            val projectsArray = root.getJSONArray("projects")
            val stagesArray = root.optJSONArray("stages")
            val itemsArray = root.optJSONArray("items")

            val oldToNewProjectId = mutableMapOf<Long, Long>()
            var lastNewProjectId = 1L

            dao.clearCurrentProjectFlag()

            for (i in 0 until projectsArray.length()) {
                val pObj = projectsArray.getJSONObject(i)
                val oldId = pObj.optLong("id", (i + 1).toLong())
                val rawName = pObj.optString("name", "مشروع مستورد $i")
                val client = pObj.optString("clientName", "")
                val notes = pObj.optString("notes", "")
                val isBase = pObj.optBoolean("isBase", false)

                val newProjId = dao.insertProject(
                    ProjectEntity(
                        name = if (isBase) rawName else "$rawName (مستورد)",
                        clientName = client,
                        notes = notes,
                        isCurrent = (i == 0),
                        isBase = isBase
                    )
                )
                oldToNewProjectId[oldId] = newProjId
                lastNewProjectId = newProjId
            }

            if (stagesArray != null) {
                for (i in 0 until stagesArray.length()) {
                    val sObj = stagesArray.getJSONObject(i)
                    val oldPId = sObj.optLong("projectId", 1L)
                    val newPId = oldToNewProjectId[oldPId] ?: lastNewProjectId
                    val stageName = sObj.optString("name", "نافذة $i")
                    val imageBase64 = sObj.optString("imageBase64", "")
                    val savedImageUri = if (imageBase64.isNotBlank()) {
                        saveBase64ToInternalFile(importedImagesDir, "stage_${newPId}_$i", imageBase64)
                    } else null

                    dao.insertStage(
                        StageEntity(
                            projectId = newPId,
                            name = stageName,
                            orderIndex = sObj.optInt("orderIndex", i),
                            isDefault = sObj.optBoolean("isDefault", false),
                            imageUri = savedImageUri
                        )
                    )
                }
            }

            if (itemsArray != null) {
                val itemsToInsert = mutableListOf<ItemEntity>()
                for (i in 0 until itemsArray.length()) {
                    val iObj = itemsArray.getJSONObject(i)
                    val oldPId = iObj.optLong("projectId", 1L)
                    val newPId = oldToNewProjectId[oldPId] ?: lastNewProjectId
                    val imageBase64 = iObj.optString("imageBase64", "")
                    val savedImageUri = if (imageBase64.isNotBlank()) {
                        saveBase64ToInternalFile(importedImagesDir, "item_${newPId}_$i", imageBase64)
                    } else null

                    itemsToInsert.add(
                        ItemEntity(
                            projectId = newPId,
                            stageName = iObj.optString("stageName", ""),
                            name = iObj.optString("name", ""),
                            imageUri = savedImageUri,
                            quantityMode = iObj.optString("quantityMode", "SINGLE"),
                            quantity = iObj.optInt("quantity", 0),
                            q1 = iObj.optInt("q1", 0),
                            q2 = iObj.optInt("q2", 0),
                            q3 = iObj.optInt("q3", 0),
                            wireColor = iObj.optString("wireColor", "").ifBlank { null },
                            c1 = iObj.optString("c1", "").ifBlank { null },
                            c2 = iObj.optString("c2", "").ifBlank { null },
                            c3 = iObj.optString("c3", "").ifBlank { null },
                            unit = iObj.optString("unit", "حبة")
                        )
                    )
                }
                if (itemsToInsert.isNotEmpty()) {
                    dao.insertItems(itemsToInsert)
                }
            }

            return@withContext lastNewProjectId
        } else {
            throw IllegalArgumentException("الملف لا يحتوي على تنسيق مشروع صالح")
        }
    }
}
