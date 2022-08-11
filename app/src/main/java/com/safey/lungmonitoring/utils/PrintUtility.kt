package com.safey.lungmonitoring.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.colors.WebColors
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.property.BorderRadius
import com.itextpdf.layout.property.HorizontalAlignment
import com.safey.lungmonitoring.BuildConfig
import com.safey.lungmonitoring.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object PrintUtility {

    fun copyLogo(context: Context) {
        var path: String="/Android/data/"
        var fo: FileOutputStream? = null
        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.unnamed)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            //var f = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Natus")
            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "safey.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }


        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.report_cover)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            //var f = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Natus")
            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "reportcover.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.rpt_a)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "a.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        }
        catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.rpt_b)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "b.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.rpt_c)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "c.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.rpt_d)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "d.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.rpt_e)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "e.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.rpt_f)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "f.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.rpt_na)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "na.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.nodata_testdetailreport)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "nodata_testdetailreport.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.rpt_star)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "rpt_star.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }
        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ehealthlogo)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "ehealthlogo.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }

        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_rpt_bg_best)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "ic_rpt_bg_best.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }
        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_rpt_bg_graph)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "ic_rpt_bg_graph.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }
        try {
            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_rpt_bg_measurement)
            val bytes = ByteArrayOutputStream()
            largeIcon.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            var filePath = context.getExternalFilesDir(null).toString()
            if(filePath.contains(path+BuildConfig.APPLICATION_ID)){
                filePath = filePath.split(path+BuildConfig.APPLICATION_ID)[0]
            }
            var f = File(filePath + File.separator + context.getString(R.string.print_file_path))
            if (!f.exists()) {
                f.mkdir()
            }
            var f1 = File(context.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f = File(f1, "ic_rpt_bg_measurement.png")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            f.createNewFile()
            fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())

        } catch (e: java.io.IOException) {
            Log.e("Exception",e.message.toString())
        } finally {
            fo?.close()
        }
    }



     fun keyValuePairParagraphPdf(m1: MutableMap<String, String>): Table {

        val keys: Set<String> = m1.keys
        val iterator: Iterator<String> = keys.iterator()

        val pdfTable = Table(4)
        pdfTable.setWidth(384f)
        //pdfTable.setPadding(16f)
        pdfTable.setBackgroundColor(WebColors.getRGBColor("#F8F9FF"),8f,8f,8f,8f)
        pdfTable.setBorder(Border.NO_BORDER)
        pdfTable.setHorizontalAlignment(HorizontalAlignment.RIGHT)
        while (iterator.hasNext()) {
            var tableRow = Cell()

            tableRow.setBorder(Border.NO_BORDER)
            val k = iterator.next()
            val v = m1[k]

            var p = Paragraph()
            p.add(pdfTextA4(k, true,fontSize = 12.0f))
            tableRow.add(p)
            tableRow.setPaddingLeft(10.0f)
            pdfTable.addCell(tableRow)

            tableRow = Cell()
            tableRow.setBorder(Border.NO_BORDER)
            p = Paragraph()
            p.add(pdfTextA4( "  : "+v!!, false,fontSize = 12.0f))
            tableRow.add(p)
            tableRow.setPaddingRight(10.0f)
            pdfTable.addCell(tableRow)



        }

        pdfTable.setBorderTopLeftRadius(BorderRadius(8f))
        pdfTable.setBorderTopRightRadius(BorderRadius(8f))
        pdfTable.setBorderBottomLeftRadius(BorderRadius(8f))
        pdfTable.setBorderBottomRightRadius(BorderRadius(8f))
        return pdfTable
    }

    private val mColorAccent: Color = DeviceRgb(0, 0, 0)
    private val mColorTableBg: Color = DeviceRgb(248, 249, 255)
    fun pdfTextA4(value: String, isBold: Boolean,color:String="#4D4D4D", fontSize:Float = 14.0f): Text {
        var font = PdfFontFactory.createFont(Constants.getStringResourceById(R.string.mediumfont), Constants.getStringResourceById(R.string.UTF), true)
        if (isBold) {
            font = PdfFontFactory.createFont(Constants.getStringResourceById(R.string.boldfont), Constants.getStringResourceById(R.string.UTF), true)
        }
        return Text(value).setFont(font).setFontSize(fontSize).setFontColor(WebColors.getRGBColor(color))
    }
    fun pdfText(value: String,color:String, fontSize:Float = 14.0f,opacity:Float = 1f): Text {
        var font = PdfFontFactory.createFont(Constants.getStringResourceById(R.string.mediumfont), Constants.getStringResourceById(R.string.UTF), true)
        return Text(value).setOpacity(opacity).setFont(font).setFontSize(fontSize).setFontColor(WebColors.getRGBColor(color))
    }
    fun pdfText(value: String,color:String, fontSize:Float = 14.0f,bgcolor:String): Text {
        var font = PdfFontFactory.createFont(Constants.getStringResourceById(R.string.mediumfont), Constants.getStringResourceById(R.string.UTF), true)
        return Text(value).setFont(font).setFontSize(fontSize).setFontColor(WebColors.getRGBColor(color))
    }

}