package net.kolotyluk.files

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.lang.Long.min
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

/**
 * # Identical File Contents
 */
class ContentEqualTests : DescribeSpec({

    val tempDirectory = createTempDirectory("content-equal-tests-")
    println ("tempDirectory: $tempDirectory")
    val file1 = kotlin.io.path.createTempFile(tempDirectory, "file1-", ".bin")
    val file2 = kotlin.io.path.createTempFile(tempDirectory, "file2-", ".bin")
    val file3 = kotlin.io.path.createTempFile(tempDirectory, "file3-", ".bin")
    val file4 = kotlin.io.path.createTempFile(tempDirectory, "file4-", ".bin")

    val largeSize = Integer.MAX_VALUE * 10L
    val smallSize = Short.MAX_VALUE * 10L

    fill(file1, smallSize, 0)
    fill(file2, smallSize, 0)
    fill(file3, smallSize, 1)
    fill(file4, smallSize - 1, 0)

    describe("test ContentEqual functionality") {

        it("mappedEqual same file") {
            ContentEqual.mappedFiles(file1, file1) shouldBe true
        }

        it("mappedEqual files with same content") {
            ContentEqual.mappedFiles(file1, file2) shouldBe true
        }

        it("mappedEqual files with different content") {
            ContentEqual.mappedFiles(file1, file3) shouldBe false
        }

        it("mappedEqual files with different content size") {
            ContentEqual.mappedFiles(file1, file4) shouldBe false
        }
    }

})

fun fill(file: Path, size: Long, suffix: Byte) {
    val buffer = ByteArray(1024 * 1024)
    var remaining = size
    Files.newOutputStream(file).use { out ->
        while (remaining > 0) {
            val length = min(buffer.size.toLong(), remaining).toInt()
            out.write(buffer, 0, length)
            remaining -= length
        }
        out.write(suffix.toInt())
    }

}