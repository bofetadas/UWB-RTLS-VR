package de.MaxBauer.UWBRTLSVR

import android.content.Context

// Entry class for handling recording logic
class RecordingImpl(context: Context): Recording {
    
    private val fileController = FileController(context)

    override fun createRecordingMovementFile(): Boolean {
        return fileController.createRecordingMovementFile()
    }

    override fun writeToFile(line: String) {
        fileController.writeToFile(line)
    }
}