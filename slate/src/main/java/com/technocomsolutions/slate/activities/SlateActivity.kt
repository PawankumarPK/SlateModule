package com.technocomsolutions.slate.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.technocomsolutions.slate.R
import com.technocomsolutions.slate.utils.PaintView.BRUSH_SIZE
import kotlinx.android.synthetic.main.activity_slate.*
import kotlinx.android.synthetic.main.dialog_eraser.*
import kotlinx.android.synthetic.main.dialog_pencil.*
import kotlinx.android.synthetic.main.savefile_dialog.*
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File

class SlateActivity : AppCompatActivity() {

    private val displayRectangle = Rect()
    private var width = 0
    private lateinit var dialog: Dialog
    private lateinit var path: File
    private var color = -0x100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slate)
        dialog = Dialog(this)

        if (resources.displayMetrics.widthPixels > resources.displayMetrics.heightPixels)
            Toast.makeText(this, "Screen switched to Landscape mode", Toast.LENGTH_SHORT).show()

        path = File(Environment.getExternalStorageDirectory().path, "/" + resources.getString(R.string.app_name))
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        width = (displayRectangle.width() * 0.9f).toInt()
        paintView.init(metrics, pencil)
        pencil.setOnClickListener { pencilDialog() }
        eraser.setOnClickListener { eraserDialog() }
        save.setOnClickListener {
            if (paintView.isDirty)
                toastLong("Cant Save")
            else
                saveBitmap()
        }
        mRefresh.setOnClickListener { paintView.clear() }
        mColor.setOnClickListener { openDialog(false) }
        share.setOnClickListener {  sendPaint(path.path)}
        Log.e("Rotation", paintView.rotation.toString())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e("ImageData", paintView.width.toString())
    }

    private fun saveBitmap() {
        if (!path.exists())
            path.mkdir()
        if (paintView.saveBitmap(path.path, "${System.currentTimeMillis()}") && paintView.file.exists())
            openDialog(paintView.file)
    }

    //click on save dila
    private fun openDialog(paintFile: File) {
        dialog.setContentView(R.layout.savefile_dialog)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.mCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.mView.setOnClickListener {
            openPaint(paintFile)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun openPaint(file: File) {
        val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(Uri.fromFile(file), "image/*")
        val intent = Intent.createChooser(target, "Open File")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toastLong("No Application Found to open paint")
        }
    }

    @SuppressLint("InflateParams")
    private fun pencilDialog() {
        val layout = LayoutInflater.from(this).inflate(R.layout.dialog_pencil, null)
        layout.minimumWidth = width
        val dialog = getDialog()
        dialog.setContentView(layout)
        dialog.thinPencil.setOnClickListener { paintView.changeColorAndText(Color.WHITE, 10);dialog.dismiss() }
        dialog.smallPencil.setOnClickListener { paintView.changeColorAndText(Color.WHITE, 15);dialog.dismiss() }
        dialog.mediumPencil.setOnClickListener { paintView.changeColorAndText(Color.WHITE, 25);dialog.dismiss() }
        dialog.largePencil.setOnClickListener { paintView.changeColorAndText(Color.WHITE, 45);dialog.dismiss() }
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.dismiss()
        dialog.show()

    }

    @SuppressLint("InflateParams")
    private fun eraserDialog() {
        val layout = LayoutInflater.from(this).inflate(R.layout.dialog_eraser, null)
        layout.minimumWidth = width
        val dialog = getDialog()
        dialog.setContentView(layout)
        dialog.smallEraser.setOnClickListener { paintView.eraserColorAndText(Color.BLACK, 35);dialog.dismiss() }
        dialog.mediumEraser.setOnClickListener { paintView.eraserColorAndText(Color.BLACK, 45);dialog.dismiss() }
        dialog.largeEraser.setOnClickListener { paintView.eraserColorAndText(Color.BLACK, 65);dialog.dismiss() }
        dialog.clearAll.setOnClickListener { paintView.clear();dialog.dismiss() }
        dialog.show()
    }


    private fun getDialog(): Dialog {
        if (!::dialog.isInitialized) {
            val dialog = Dialog(this, R.style.CustomDialog)
            dialog.setCanceledOnTouchOutside(false)
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            this.dialog = dialog
        }
        return this.dialog
    }

    override fun onBackPressed() {
        if (getDialog().isShowing)
            getDialog().dismiss()
        super.onBackPressed()
    }

    private fun openDialog(supportsAlpha: Boolean) {
        val dialog = AmbilWarnaDialog(this@SlateActivity, color, supportsAlpha, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                Toast.makeText(applicationContext, "Selected", Toast.LENGTH_SHORT).show()
                paintView.changeColorAndText(color, BRUSH_SIZE)
                this@SlateActivity.color = color
                // displayColor()
            }

            override fun onCancel(dialog: AmbilWarnaDialog) {
                //  Toast.makeText(applicationContext, "cancel", Toast.LENGTH_SHORT).show()
            }
        })
        dialog.show()
    }

    private fun sendPaint(filePath: String) {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        val file = File(filePath)
        if (file.exists()) {
            intentShareFile.type = "image/*"
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$filePath"))
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...")
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
            this.startActivity( Intent.createChooser(intentShareFile, "Share File"), null)
        } else
            Toast.makeText(this, "File not Found !", Toast.LENGTH_SHORT).show()
    }


    private fun toastLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /*  private fun displayColor() {

       // text1.text = String.format("Current color: 0x%08x", color)
    }*/


}
