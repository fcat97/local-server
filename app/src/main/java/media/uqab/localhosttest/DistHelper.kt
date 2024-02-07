package media.uqab.localhosttest

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.zip.ZipFile

class DistHelper(private val context: Context) {
    companion object {
        private const val TAG = "DistHelper"
        const val DIST_ROOT_DIR = "web_apps"
    }
    private val cacheDir: File? get() = context.externalCacheDir
    private val appRootDir: String? get() = context.applicationContext.getExternalFilesDir(null)?.path

    private fun getDistRootPath(): String? {
        val distRoot = File(appRootDir, DIST_ROOT_DIR)
        if (!distRoot.exists()) distRoot.mkdirs()
        return distRoot.path
    }

    fun getAvailableDistList(): List<File> {
        val distDir = getDistRootPath()?.let { File(it) } ?: return emptyList()

        return distDir.listFiles()
            ?.filter { !it.isFile }
            ?.mapNotNull { it }
            ?: emptyList()
    }

    fun getDistPath(distName: String): String {
        val root = getDistRootPath() ?: ""
        return File(root, distName).path
    }

    fun isDistAvailable(distName: String): Boolean {
        val path = getDistRootPath() ?: return false

        val dist = File(path, distName)
        val index = File(dist, "index.html")
        return dist.exists() && dist.isDirectory && index.exists()
    }

    fun getLoadingFile(): String? {
        val cacheDir = cacheDir ?: return null
        val index = File(cacheDir, "loading.html")

        val os = FileOutputStream(index)
        val text = """
            <!DOCTYPE html>
            <html>
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
            .container {
              font-family: arial;
              font-size: 24px;
              margin: 25px;
              width: 350px;
              height: 200px;
            }
            
            .text-center {
              margin: 0 auto;
              text-align: center;
            }
            
            .loader {
              border: 16px solid #f3f3f3;
              border-radius: 50%;
              border-top: 16px solid #3498db;
              width: 120px;
              height: 120px;
              margin: 0 auto;
              -webkit-animation: spin 2s linear infinite; /* Safari */
              animation: spin 2s linear infinite;
              margin-top: 35%;
            }
            
            /* Safari */
            @-webkit-keyframes spin {
              0% { -webkit-transform: rotate(0deg); }
              100% { -webkit-transform: rotate(360deg); }
            }
            
            @keyframes spin {
              0% { transform: rotate(0deg); }
              100% { transform: rotate(360deg); }
            }
            </style>
            </head>
            <body>
            
            <div class="container text-center">
            <h2>Downloading in progress</h2>
            <h4>please wait...</h4>
            </div>
            
            
            <div class="loader"></div>
            
            </body>
            </html>
        """.trimIndent().toByteArray()

        os.write(text)
        os.close()

        return index.path
    }

    fun getErrorFile(): String? {
        val cacheDir = cacheDir ?: return null
        val page404 = File(cacheDir, "404.html")

        if (page404.exists()) page404.delete()

        val os = FileOutputStream(page404)
        val text = """
            <!DOCTYPE html>
            <html lang="en">
            
            <head>
                <title>404 Error Page</title>
            
                <meta charset="utf-8">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
            
                <!-- Bootstrap -->
                <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
            
                <!-- Custom stlylesheet -->
                <link type="text/css" rel="stylesheet" href="css/style.css" />
            </head>
            
            <body>
            
            <div class="vertical-center">
                <div class="container">
                    <div id="notfound" class="text-center ">
                        <h1>😮</h1>
                        <h2>Oops! Page Not Be Found</h2>
                        <p>Sorry but the page you are looking for does not exist.</p>
                        <a href="#">Back to homepage</a>
                    </div>
                </div>
            </div>
            
            </body>
            
            </html>
        """.trimIndent().toByteArray()

        os.write(text)
        os.close()

        return page404.path
    }

    @WorkerThread
    suspend fun downloadAndImportDist(url: String, onDownloadStart: () -> Unit): Boolean {
        return try {
            val cacheDir = context.externalCacheDir ?: throw FileNotFoundException("cache dir not found")
            val zipFile = File(cacheDir, url.substringAfterLast("/"))

            if (!zipFile.exists()) {
                onDownloadStart()
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "downloadAndImportDist: download started")
                    URL(url).openStream().use { input ->
                        input.copyTo(FileOutputStream(zipFile))
                    }
                    Log.d(TAG, "downloadAndImportDist: download done")
                }
            }

            val extractDir = getDistRootPath()?.let { File(it) } ?: throw FileNotFoundException("cache dir not found")

            extractZipFile(zipFile, extractDir, true)
            zipFile.delete()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Import a web_app distribution
     */
    @WorkerThread
    fun importDist(uri: Uri): Boolean {
        return try {
            if (!DocumentFile.isDocumentUri(context, uri)) return false

            val document = DocumentFile.fromSingleUri(context, uri) ?: return false

            extractZip(document) != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Extract a dist archive file into [getDistRootPath] directory
     */
    private fun extractZip(zipFile: DocumentFile): File? {
        return try {
            val cacheDir = context.externalCacheDir ?: throw FileNotFoundException("cache dir not found")
            val extractDir = getDistRootPath()?.let { File(it) } ?: throw FileNotFoundException("app dir not found")

            val zf = File(cacheDir, zipFile.name!!)
            copyFile(zipFile.uri, zf)

            val res = extractZipFile(zf, extractDir, true)
            zf.delete()

            res
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Extract a zip file into any directory
     *
     * @param zipFile src zip file
     * @param extractTo directory to extract into.
     * There will be new folder with the zip's name inside [extractTo] directory.
     * @param extractHere no extra folder will be created and will be extracted
     * directly inside [extractTo] folder.
     *
     * @return the extracted directory i.e, [extractTo] folder if [extractHere] is `true`
     * and [extractTo]\zipFile\ folder otherwise.
     */
    private fun extractZipFile(
        zipFile: File,
        extractTo: File,
        extractHere: Boolean = false,
    ): File? {
        return try {
            val outputDir = if (extractHere) {
                extractTo
            } else {
                File(extractTo, zipFile.nameWithoutExtension)
            }

            ZipFile(zipFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        if (entry.isDirectory) {
                            val d = File(outputDir, entry.name)
                            if (!d.exists()) d.mkdirs()
                        } else {
                            val f = File(outputDir, entry.name)
                            if (f.parentFile?.exists() != true)  f.parentFile?.mkdirs()

                            f.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }

            extractTo
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * copy all the contents of a folder to another.
     *
     * @param srcDir [Uri] of source directory
     * @param destRootDir parent directory of destination.
     */
    private fun copyDir(srcDir: Uri, destRootDir: File) {
        if (!DocumentFile.isDocumentUri(context, srcDir)) return

        if (!destRootDir.exists()) destRootDir.mkdirs()

        val srcDoc = DocumentFile.fromTreeUri(context, srcDir)
            ?: DocumentFile.fromSingleUri(context, srcDir)
            ?: return

        if (srcDoc.isDirectory) {
            val destDir = File(destRootDir, srcDoc.name ?: return)
            srcDoc.listFiles().forEach {
                copyDir(it.uri, destDir)
            }
        }

        if (srcDoc.isFile) {
            copyFile(srcDoc.uri, File(destRootDir, srcDoc.name ?: return))
        }
    }

    @Throws(IOException::class)
    private fun copyFile(pathFrom: Uri, pathTo: File) {
        context.contentResolver.openInputStream(pathFrom).use { `in` ->
            `in`?.copyTo(pathTo.outputStream())
        }
    }
}