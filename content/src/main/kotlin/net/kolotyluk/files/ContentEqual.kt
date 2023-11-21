package net.kolotyluk.files

import java.io.IOException
//import java.lang.reflect.InvocationTargetException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileChannel.MapMode
import java.nio.file.Files
import java.nio.file.Path
//import java.security.AccessController
//import java.security.PrivilegedAction
import kotlin.math.min

object ContentEqual {


    /**
     * # Identical File Contents
     *
     * Uses
     * [Memory Mapped Files](http://en.wikipedia.org/wiki/Memory-mapped_file)
     * for fast comparison of large files. This is about 4 times faster than calling<pre>
     * [org.apache.commons.io.FileUtils.contentEquals(File file1,File file2)](http://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/FileUtils.html#contentEquals%28java.io.File,%20java.io.File%29)</pre>
     *
     * with identical 4
     * [GiB](http://en.wikipedia.org/wiki/Gibibyte)
     * files. Performance may suffer slightly for smaller files.
     * A future version may be optimized for smaller files too.
     *
     * Useful for checking if media files, such as music and video,
     * have identical or duplicate content. Video files can easily
     * be over 4 GiB in size. Technically this method will compare
     * files as large as the file system supports.
     *
     * Performance is about 16 seconds for two 4 GiB files with
     * identical content on an Intel Xeon 5580 at 3.2 GHz using a
     * file system on a LSI MegaRAID SAS 9266-8i.
     *
     * @param file1 1st file to be compared
     * @param file2 2nd file to be compared
     * @return true when both files have identical contents
     * @throws IllegalArgumentException if the arguments are not files.
     * @throws IOException if there is a problem with File I/O
     * @throws RuntimeException if there was a problem unmapping the
     * buffers or closing the channels, which may leave one or more
     * files locked for read.
     */
    @Throws(IOException::class)
    fun mappedFiles(file1: Path, file2: Path): Boolean {

        require(Files.isRegularFile(file1)) { file1.toString() + "is not a regular file" }
        require(Files.isRegularFile(file2)) { file2.toString() + "is not a regular file" }

        // While we can map files larger than Integer.MAX_VALUE, we should get more experience first.
        val mapSize = Integer.MAX_VALUE.toLong()

        try {
            val fileSize = Files.size(file1)
            if (fileSize != Files.size(file2)) return false
            var mapSize: Long = min(mapSize, fileSize)
            var mapPosition: Long = 0

            FileChannel.open(file1).use {channel1 ->
                FileChannel.open(file2).use { channel2 ->
                    while (mapSize > 0) {
                        val buffer1 = channel1.map(MapMode.READ_ONLY, mapPosition, mapSize)
                        val buffer2 = channel2.map(MapMode.READ_ONLY, mapPosition, mapSize)
                        try {
                            // if (!buffer1.equals(buffer2)) return false;
                            // The line above is much slower than the code below.
                            // It should not be, but it is, possibly because it is
                            // loading the entire buffer into memory before comparing
                            // the contents. See the corresponding unit test. EK
                            var longs: Long = mapSize / 8
                            var bytes: Long = mapSize % 8
                            while (longs-- > 0) if (buffer1.long != buffer2.long) return false
                            while (bytes-- > 0) if (buffer1.get() != buffer2.get()) return false
                            mapPosition += mapSize
                            mapSize = min(mapSize, fileSize - mapPosition)
                        } finally {
                            // This may be obsolete, but at the time, it did fix a problem.
                            // It is important to clean up, so we do not hold any file locks,
                            // in case the caller wants to do something else with the files.

                            // In terms of functional programming, holding a lock after
                            // returning to the caller would be an unwelcome side effect.
                            cleanDirectByteBuffer(buffer1)
                            cleanDirectByteBuffer(buffer2)
                        }
                    }
                }
            }

            // This might be faster calling out to a foreign function interface. For example
            // https://stackoverflow.com/questions/27339763/faster-memory-compare-for-equality-16-bytes-block-than-memcmp
            // The only thing faster would be using assembly language, and specialized instructions such as ...

        } catch (e: Exception) {
            // TODO something useful
        }
        return true
    }

    /**
     * # Clean or unmap a direct ByteBuffer
     *
     * DirectByteBuffers are garbage collected by using a phantom reference and a
     * reference queue. Every once a while, the JVM checks the reference queue and
     * cleans the DirectByteBuffers. However, as this doesn't happen immediately
     * after discarding all references to a DirectByteBuffer, it's easy get
     * [OutOfMemoryError] problems using direct ByteBuffers.
     *
     *
     * Also, if a file is still mapped, via [MappedByteBuffer], then it is
     * locked and cannot be destroyed or possibly written to if it was previously
     * mapped read. Trying to destroy or write to a locked file will result in an
     * [IOException]
     *
     *
     * This function explicitly calls the cleaner method of a [ByteBuffer] using
     * reflection because it is not publicly accessible.
     *
     * @param byteBuffer The DirectByteBuffer that will be "cleaned". Returns immediately if
     * the argument is null.
     * @throws IllegalArgumentException if byteBuffer isn't direct
     * @throws RuntimeException if cleaning may have failed
     */
    fun cleanDirectByteBuffer(byteBuffer: ByteBuffer?) {
        if (byteBuffer == null) return
        require(byteBuffer.isDirect) { "byteBuffer isn't direct!" }
//        AccessController.doPrivileged(PrivilegedAction<Void?> {
//            try {
//                val cleanerMethod = byteBuffer.javaClass.getMethod("cleaner")
//                cleanerMethod.setAccessible(true)
//                val cleaner = cleanerMethod.invoke(byteBuffer)
//                val cleanMethod = cleaner.javaClass.getMethod("clean")
//                cleanMethod.setAccessible(true)
//                cleanMethod.invoke(cleaner)
//            } catch (e: NoSuchMethodException) {
//                throw RuntimeException("Could not clean MappedByteBuffer -- File may still be locked!")
//            } catch (e: SecurityException) {
//                throw RuntimeException("Could not clean MappedByteBuffer -- File may still be locked!")
//            } catch (e: IllegalAccessException) {
//                throw RuntimeException("Could not clean MappedByteBuffer -- File may still be locked!")
//            } catch (e: IllegalArgumentException) {
//                throw RuntimeException("Could not clean MappedByteBuffer -- File may still be locked!")
//            } catch (e: InvocationTargetException) {
//                throw RuntimeException("Could not clean MappedByteBuffer -- File may still be locked!")
//            }
//            null // nothing to return
//        })
    }

}