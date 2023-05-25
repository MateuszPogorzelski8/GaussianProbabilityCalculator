package com.gaussapp.myapplication

import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.fragment.findNavController
import com.myapp.myapplication.R
import com.myapp.myapplication.databinding.FragmentSecondBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
private const val inv_sqrt_2pi = 0.3989422804014327
private const val Epsilon = 0.000001
class SecondFragment : Fragment() {

    // ADS

    private var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null
    private final var TAG = "MainActivity"

    //DEFINING MY XML ITEMS FROM FRAGMENT_SECOND.XML
    private lateinit var a: EditText
    private lateinit var b: EditText
    private lateinit var przycisk_second: Button
    private lateinit var mean_second: EditText
    private lateinit var std_second: EditText
    private lateinit var result_second: TextView
    private lateinit var buttonabove: RadioButton
    private lateinit var buttonbelow: RadioButton
    private lateinit var buttonbetween: RadioButton
    private lateinit var buttonoutside: RadioButton
    private lateinit var deletebutton_second: Button
    private lateinit var imagesecond: ImageView


    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

        // ADS

        mAdManagerInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mAdManagerInterstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                mAdManagerInterstitialAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext().applicationContext
        // GETTING CURRENT MODE
        var mode = AppCompatDelegate.getDefaultNightMode()


        // MY ITEMS FINDING
        a = view.findViewById(R.id.a)
        b = view.findViewById(R.id.b)
        mean_second = view.findViewById(R.id.mean_second)
        std_second = view.findViewById(R.id.std_second)
        przycisk_second = view.findViewById(R.id.calculate_second)
        result_second = view.findViewById(R.id.result_second)
        buttonabove = view.findViewById(R.id.radioButton_above)
        buttonbelow = view.findViewById(R.id.radioButton_below)
        buttonbetween = view.findViewById(R.id.radioButton_between)
        buttonoutside = view.findViewById(R.id.radioButton_outside)
        deletebutton_second = view.findViewById(R.id.deletebutton_second)
        imagesecond = view.findViewById<ImageView>(R.id.imagesecond)


        // THIS FRAGMENT IS FOR WHEN THE NIGHT MODE IS CHANGED
        // THE PROBLEM WAS THEN NIGHT MODE WAS CHANGED AT BUTTONABOVE CHECKED,
        // THE "b" EDIT TEXT WAS STILL VISIBLE
        // THIS FIXES IT
        buttonabove.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                b.visibility = View.INVISIBLE
            }
            else{
                b.visibility = View.VISIBLE
            }
        }
        buttonbelow.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                b.visibility = View.INVISIBLE
            }
            else{
                b.visibility = View.VISIBLE
            }
        }

        // 2 CANVASES; ONE WOULD BE CLEARED, THE OTHER WOULDN'T
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels - 64
        val bitmapheight = 600
        val bitmap = Bitmap.createBitmap(screenWidth, bitmapheight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)


        val mergedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvasmerge = Canvas(mergedBitmap)

        // PLOT LINE COLOR
        val paint = Paint()
        paint.color = ContextCompat.getColor(context, R.color.gauss_plot)
        paint.strokeWidth = 12f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND

        // WHITE PAINT
        val whitepaint = Paint()
        whitepaint.color = Color.WHITE
        whitepaint.strokeWidth = 5f

        // PLOT LINE
        val xPoints = FloatArray(screenWidth)
        val yPoints = FloatArray(screenWidth)
        val amplitude = -400f
        val sigma = screenWidth / 6f // standard deviation
        val m = screenWidth / 2f // medium value


        for (i in xPoints.indices) {
            xPoints[i] = i.toFloat()
            yPoints[i] = (amplitude * Math.exp(
                -(Math.pow(
                    (i - m).toDouble(),
                    2.0
                )) / (2 * Math.pow(sigma.toDouble(), 2.0))
            )).toFloat() + 450f
        }
        canvas.drawPath(Path().apply {
            moveTo(xPoints.first(), yPoints.first())
            xPoints.zip(yPoints).forEach { (x, y) ->
                lineTo(x, y)
            }
        }, paint)

        imagesecond.setImageBitmap(bitmap)


        // OUR VALUE GRAPH ( FILLED GAUSSIAN LINE)
        val maxx = yPoints.max().toInt() + 8
        val bitmap2 = Bitmap.createBitmap(screenWidth, maxx, Bitmap.Config.ARGB_8888)
        val canvas2 = Canvas(bitmap2)

        // OUR VALUE COLOR
        val valuepaint = Paint()
        valuepaint.color = Color.BLUE
        valuepaint.strokeWidth = 12f
        valuepaint.style = Paint.Style.FILL
        valuepaint.strokeCap = Paint.Cap.ROUND


        val xPointsnext = FloatArray(screenWidth)
        val yPointsnext = FloatArray(screenWidth)

        for (i in xPoints.indices) {
            xPointsnext[i] = i.toFloat()
            yPointsnext[i] = (amplitude * Math.exp(
                -(Math.pow(
                    (i - m).toDouble(),
                    2.0
                )) / (2 * Math.pow(sigma.toDouble(), 2.0))
            )).toFloat() + 450f
        }

        val pathnext = Path()

        pathnext.moveTo(xPointsnext.first(), maxx.toFloat())
        xPointsnext.zip(yPointsnext).forEach { (x, y) ->
            pathnext.lineTo(x, y)

        }
        pathnext.lineTo(xPointsnext.last(), maxx.toFloat())
        pathnext.lineTo(xPointsnext.first(), maxx.toFloat())


        pathnext.close()

        canvas2.drawPath(pathnext, valuepaint)


// X AXIS LINE
        val path = Path()

        val timelinestartX = 0f
        val timelinestartY = bitmapheight.toFloat() - 120f
        val timelineendX = screenWidth.toFloat()
        val timelinelength = timelineendX - timelinestartX


        path.moveTo(timelinestartX, timelinestartY)
        path.lineTo(timelineendX, timelinestartY)

        val painttimeline = Paint()
        painttimeline.color = Color.BLACK
        painttimeline.strokeWidth = 4f
        painttimeline.style = Paint.Style.STROKE
// TIMELINE ARROW
        val arrowSize = 20f
        val degrees = Math.toDegrees(
            Math.atan2(
                (timelinestartY - timelinestartY).toDouble(),
                (timelineendX - timelinestartX).toDouble()
            )
        ).toFloat()
        val x1 =
            timelineendX - arrowSize * Math.cos(Math.toRadians(degrees.toDouble() + 30)).toFloat()
        val y1 =
            timelinestartY - arrowSize * Math.sin(Math.toRadians(degrees.toDouble() + 30)).toFloat()
        val x2 =
            timelineendX - arrowSize * Math.cos(Math.toRadians(degrees.toDouble() - 30)).toFloat()
        val y2 =
            timelinestartY - arrowSize * Math.sin(Math.toRadians(degrees.toDouble() - 30)).toFloat()
        path.moveTo(timelineendX, timelinestartY)
        path.lineTo(x1, y1)
        path.moveTo(timelineendX, timelinestartY)
        path.lineTo(x2, y2)

        canvas.drawPath(path, painttimeline)

// TIMELINE LITTLE DIVIDERS ( ---------|---------------|---------------|---------->)
        val length2 = (timelinelength / 2) / 2
        canvas.drawLine(
            length2 * 2,
            timelinestartY - 12,
            length2 * 2,
            timelinestartY + 12,
            painttimeline
        )
        canvas.drawLine(length2, timelinestartY - 12, length2, timelinestartY + 12, painttimeline)
        canvas.drawLine(
            length2 + length2 / 2,
            timelinestartY - 12,
            length2 + length2 / 2,
            timelinestartY + 12,
            painttimeline
        )

        canvas.drawLine(
            length2 * 2 + length2,
            timelinestartY - 12,
            length2 * 2 + length2,
            timelinestartY + 12,
            painttimeline
        )
        canvas.drawLine(
            length2 * 2 + length2 / 2,
            timelinestartY - 12,
            length2 * 2 + length2 / 2,
            timelinestartY + 12,
            painttimeline
        )



        binding.drugi.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            showInterstial()
        }

        // MAIN CALCULATIONS
        binding.calculateSecond.setOnClickListener() {
            loadInterstial()
            initialCalculations()

            // THAT'S FOR CLEARING NUMBERS UNDER AXIS LINE
            val left = 0f
            val top = timelinestartY + 12
            val right = timelinelength
            val bottom = bitmapheight

            val rect = RectF(left, top, right, bottom.toFloat())
            if(mode == AppCompatDelegate.MODE_NIGHT_YES){
                whitepaint.color = ContextCompat.getColor(context, R.color.background_night_mode)
            }
            else{
                whitepaint.color = ContextCompat.getColor(context, R.color.backgroud_day_mode)
            }
            canvas.drawRect(rect, whitepaint)
            whitepaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
            canvas.drawRect(rect, whitepaint)

            val checkfornulls = nullcheckerpro()
            if(mode == AppCompatDelegate.MODE_NIGHT_YES){
                // valuepaint.color == ContextCompat.getColor(context, R.color.value_color_darker)
                valuepaint.color = ContextCompat.getColor(context, R.color.value_color_darker)
            }

            if (!checkfornulls || std_second.text.toString().toDouble() <= 0) {
                canvas2.drawRect(
                    0f,
                    bitmap2.height.toFloat(),
                    bitmap2.width.toFloat(),
                    0f,
                    whitepaint
                )
                whitepaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                canvas2.drawRect(
                    0f,
                    bitmap2.height.toFloat(),
                    bitmap2.width.toFloat(),
                    0f,
                    whitepaint
                )

            } else {

                // OUR VALUE DRAWING (FILLED GAUSSIAN )
                val xPointsnext = FloatArray(screenWidth)
                val yPointsnext = FloatArray(screenWidth)

                for (i in xPoints.indices) {
                    xPointsnext[i] = i.toFloat()
                    yPointsnext[i] = (amplitude * Math.exp(
                        -(Math.pow((i - m).toDouble(), 2.0)) / (2 * Math.pow(
                            sigma.toDouble(),
                            2.0
                        ))
                    )).toFloat() + 450f
                }

                val pathnext = Path()

                pathnext.moveTo(xPointsnext.first(), maxx.toFloat())
                xPointsnext.zip(yPointsnext).forEach { (x, y) ->
                    pathnext.lineTo(x, y)

                }
                pathnext.lineTo(xPointsnext.last(), maxx.toFloat())
                pathnext.lineTo(xPointsnext.first(), maxx.toFloat())


                pathnext.close()

                canvas2.drawPath(pathnext, valuepaint)

                // NUMBERS
                val standarddeviation: Double = std_second.text.toString().toDouble()
                val medianvalue: Double = mean_second.text.toString().toDouble()
                // SETTING DECIMAL SEPARATOR AS "."
                // ALSO FORMATTING OUR VALUES TO NOT EXCEED 2 DIGITS
                val df = DecimalFormat("0.0")
                df.decimalFormatSymbols = DecimalFormatSymbols().apply {
                    decimalSeparator = '.'
                }

                var number1second: kotlin.String =
                    df.format((medianvalue - (standarddeviation * 2)))
                var number2second: kotlin.String = df.format((medianvalue - standarddeviation))
                var number3second: kotlin.String = df.format(mean_second.text.toString().toDouble())
                var number4second: kotlin.String = df.format(medianvalue + standarddeviation)
                var number5second: kotlin.String = df.format(medianvalue + standarddeviation * 2)

                // RUNNING REPLACER TO REPLACE ".0"(0.0) WITH JUST 0
                number1second = replacer(number1second)
                number2second = replacer(number2second)
                number3second = replacer(number3second)
                number4second = replacer(number4second)
                number5second = replacer(number5second)

                // SETTING NUMBERS PAINT
                val numberspaint = android.graphics.Paint()
                numberspaint.color = android.graphics.Color.BLACK
                numberspaint.textSize = 32f

                // MEASURE NUMBERS WIDTH TO CENTER THEM
                val number3textwidth = numberspaint.measureText(number3second)
                val number1textwidth = numberspaint.measureText(number1second)
                val number2textwidth = numberspaint.measureText(number2second)
                val number4textwidth = numberspaint.measureText(number4second)
                val number5textwidth = numberspaint.measureText(number5second)

                val newheight =


                    // DRAWING NUMBERS
                    canvas.drawText(
                        number1second,
                        (((timelinelength) / 2) / 2) - number1textwidth / 2,
                        timelinestartY + numberspaint.descent() + 42f,
                        numberspaint
                    )
                canvas.drawText(
                    number2second,
                    (((timelinelength) / 2) / 2) + (length2 / 2) - (number2textwidth / 2),
                    timelinestartY + numberspaint.descent() + 42f,
                    numberspaint
                )
                canvas.drawText(
                    number3second,
                    (timelinelength - number3textwidth) / 2,
                    timelinestartY + numberspaint.descent() + 42f,
                    numberspaint
                )
                canvas.drawText(
                    number4second,
                    (timelinelength - number4textwidth + length2) / 2,
                    timelinestartY + numberspaint.descent() + 42f,
                    numberspaint
                )
                canvas.drawText(
                    number5second,
                    (timelinelength - number5textwidth + length2 * 2) / 2,
                    timelinestartY + numberspaint.descent() + 42f,
                    numberspaint
                )


                var rectangle_plot_clear = RectF(0f, 0f, 0f, 0f)


                var drawingpoint: Double = (bitmap2.width / 2).toDouble()
                var lineX = 0.0
                var lineY = 0.0
                if (buttonbelow.isChecked || buttonabove.isChecked) {
                    var drawingpoint: Double = (bitmap2.width / 2).toDouble()
                    //SCALE CALCULATIONS
                    val exponent =
                        a.text.toString().toDouble() - mean_second.text.toString().toDouble()
                    var scale =
                        (timelinelength / 8) * (exponent / std_second.text.toString().toDouble())


                    // I WANTED THE SCALE TO BE LINEAR FROM -2 STANDARD DEVIATIONS TO 2 STANDARD DEVIATIONS
                    // AND BELOW -2 STD AND ABOVE 2 STD THE SCALE TO BE EXPONENTIAL
                    // SO WE STILL SEE THE LINE OR THE FILLED GAUSSIAN PLOT
                    // BUT THE SCALE IS DIFFERENT
                    if (scale > timelinelength / 3) {
                        lineX = liniowoWykladniczaFunkcja(
                            scale,
                            (timelinelength / 4 + timelinelength / 2).toDouble(),
                            0.0015,
                            timelinelength.toDouble() + 64f
                        )
                    } else {

                        lineX = scale + drawingpoint
                    }

                    if (buttonabove.isChecked) {
                        rectangle_plot_clear =
                            RectF(0f, bitmap2.height.toFloat(), lineX.toFloat(), 0f)

                    }
                    if (buttonbelow.isChecked) {
                        rectangle_plot_clear = RectF(
                            lineX.toFloat(),
                            bitmap2.height.toFloat(),
                            bitmap2.width.toFloat(),
                            0f
                        )
                    }
                } else {

                    var exponent_a =
                        a.text.toString().toDouble() - mean_second.text.toString().toDouble()
                    var exponent_b =
                        b.text.toString().toDouble() - mean_second.text.toString().toDouble()
                    if (exponent_a > exponent_b) {
                        val temp = exponent_b
                        exponent_b = exponent_a
                        exponent_a = temp

                    }
                    var scale_a =
                        (timelinelength / 8) * (exponent_a / std_second.text.toString().toDouble())
                    var scale_b =
                        (timelinelength / 8) * (exponent_b / std_second.text.toString().toDouble())
                    lineX = scale_a + drawingpoint
                    lineY = scale_b + drawingpoint
                    if (buttonoutside.isChecked) {
                        rectangle_plot_clear =
                            RectF(lineX.toFloat(), bitmap2.height.toFloat(), lineY.toFloat(), 0f)
                    } else if (buttonbetween.isChecked) {

                        rectangle_plot_clear =
                            RectF(0f, bitmap2.height.toFloat(), lineX.toFloat(), 0f)
                        val rectangle_plot_clear2 = RectF(
                            lineY.toFloat(),
                            bitmap2.height.toFloat(),
                            bitmap2.width.toFloat(),
                            0f
                        )
                        canvas2.drawRect(rectangle_plot_clear2, whitepaint)
                    }
                }


                canvas2.drawRect(rectangle_plot_clear, whitepaint)
                whitepaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                canvas2.drawRect(rectangle_plot_clear, whitepaint)

            }


            // DRAWING BLANC RECTANGLE

            /*
            val rectangle = RectF((bitmap2.width/2).toFloat(), bitmap2.height.toFloat(), bitmap2.width.toFloat(), 0f)
            canvas2.drawRect(rectangle,paintnextt)
            paintnextt.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
            canvas2.drawRect(rectangle,paintnextt)
            */


// CANVAS CONNECTION

            canvasmerge.drawBitmap(bitmap2, 0f, 0f, null)
            canvasmerge.drawBitmap(bitmap, 0f, 0f, null)
            imagesecond.setImageBitmap(mergedBitmap)


        }
        binding.radioButtonAbove.setOnClickListener() {
            b.visibility = View.INVISIBLE
        }
        binding.radioButtonBelow.setOnClickListener() {
            b.visibility = View.INVISIBLE
        }
        binding.radioButtonBetween.setOnClickListener() {
            b.visibility = View.VISIBLE
        }
        binding.radioButtonOutside.setOnClickListener() {
            b.visibility = View.VISIBLE
        }
        binding.deletebuttonSecond.setOnClickListener() {
            result_second.text = "0.0"
            mean_second.text.clear()
            std_second.text.clear()
            a.text.clear()

            b.text.clear()
            if(mode == AppCompatDelegate.MODE_NIGHT_YES){
                // valuepaint.color == ContextCompat.getColor(context, R.color.value_color_darker)
                valuepaint.color = ContextCompat.getColor(context, R.color.value_color_darker)
            }

            val left = 0f
            val top = timelinestartY + 12
            val right = timelinelength
            val bottom = bitmapheight
            whitepaint.color = ContextCompat.getColor(context, R.color.backgroud_day_mode)

            if(mode == AppCompatDelegate.MODE_NIGHT_YES){
                whitepaint.color = ContextCompat.getColor(context, R.color.background_night_mode)
            }
            val rect = RectF(left, top, right, bottom.toFloat())

            canvas.drawRect(rect, whitepaint)
            whitepaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
            canvas.drawRect(rect, whitepaint)
            canvasmerge.drawBitmap(bitmap, 0f, 0f, null)
            canvasmerge.drawBitmap(bitmap2, 0f, 0f, null)
            imagesecond.setImageBitmap(bitmap)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Gauss(m: Double, s: Double, p: Double): Double {

        var a: Double = (p - m) / s
        return inv_sqrt_2pi / s * exp(-0.5f * a * a)

    }

    // Gaussian surface area (Cumulative distribution) is calculated with Trapeziod-rule
    // Bell curve surface is divided into multiple trapezoids, then algorythm calculates their surface area
    // Gaussian plot is divided on 2^n parts.
    // Algorythm stops if results cease (<0.000001) to change
    private fun Trapezoid(a: Double, b: Double, h: Double, nn: Int, m: Double, s: Double): Double {
        var suma: Double = Gauss(m, s, a)
        var x: Double = a
        for (i in 1..nn) {
            x = a + h * i;
            suma += Gauss(m, s, x)
        }
        suma -= ((Gauss(m, s, a) + Gauss(m, s, b)) / 2);
        return h * suma;
    }

    private fun Four_pow_2(x: IntArray) {
        x[0] = 1
        for (i in 1..x.size - 1) {
            x[i] = x[i - 1] * 4
        }
    }

    private fun Calculations(a: Double, b: Double, m: Double, s: Double): Double {
        var sformatowanaLiczba: String = ""

        // IT WORKS LIKE   |_|
        //                 |_| |_|
        //                 |_| |_| |_|
        // WITH VALUES IN "_" BEING MORE AND MORE PRECISE AND LESS DIFFERENT FROM PREVIOUS ROWS AND COLUMNS
        // IF THE DIFFERENCE BETWEEN LAST AND SECOND TO LAST CELL OF LESS THAN EPSILON (0.000000001)
        // THEN EXIT THE LOOP AND RETURN VALUE IN LAST CELL
        if (s > 0) {
            var nn: Int = 1
            var h: Double = b - a
            var delta: Double = 1.000000
            var wynik_koncowy: Double = 0.000000
            val MyArray = Array(20) { DoubleArray(20) }
            val four = IntArray(20)
            Four_pow_2(four)
            var iteration: Int = 1

            MyArray[0][0] = Trapezoid(a, b, h, nn, m, s)
            while (iteration < 20 && delta > Epsilon) {
                h /= 2.0
                nn *= 2
                MyArray[iteration][0] = Trapezoid(a, b, h, nn, m, s)
                for (g in 1..iteration) {

                    MyArray[iteration][g] =
                        MyArray[iteration][g - 1] + (MyArray[iteration][g - 1] - MyArray[iteration - 1][g - 1]) / (four[g] - 1)

                    delta =
                        abs(MyArray[iteration][iteration] - MyArray[iteration - 1][iteration - 1])

                    wynik_koncowy = MyArray[iteration - 1][iteration - 1]
                }
                iteration++
            }
            var round_off = (wynik_koncowy * 100000).roundToInt() / 100000.toDouble()
            return round_off.toDouble()
           //
        } else {
            return 0.0
        }
    }

    private fun initialCalculations() {
        var finalresult: Double = 0.0
        var nullckeckervalues = true
        nullckeckervalues = nullcheckerpro()
        if (!nullckeckervalues || std_second.text.toString().toDouble()<=0 ) {
                result_second.text = "0.0"
            }
        else {

            var aa = 0.0
            var bb = 0.0
            val m = mean_second.text.toString().toDouble()
            val s = std_second.text.toString().toDouble()
            if (buttonabove.isChecked) {
                bb = 100.00
                aa = a.text.toString().toDouble()
                finalresult = Calculations(aa, bb, m, s)
            }
            if (buttonbelow.isChecked) {
                aa = -100.00
                bb = a.text.toString().toDouble()
                finalresult = Calculations(aa, bb, m, s)
            }
            if (buttonbetween.isChecked) {
                aa = a.text.toString().toDouble()
                bb = b.text.toString().toDouble()
                if (aa > bb) {
                    var temp = bb
                    bb = aa
                    aa = temp
                }
                finalresult = Calculations(aa, bb, m, s)
            }

            if (buttonoutside.isChecked) {
                aa = a.text.toString().toDouble()
                bb = b.text.toString().toDouble()
                if (aa > bb) {
                    var temp = bb
                    bb = aa
                    aa = temp
                }
                finalresult = Calculations(aa, bb, m, s)
                finalresult = 1.0 - finalresult


            }
            result_second.text = roundofftester(finalresult)

        }
    }

    private fun replacer(x: String): String {
        if (x.endsWith(".0")) {
            return x.substring(0, x.length - 2)
        } else {
            return x
        }
    }

    fun liniowoWykladniczaFunkcja(x: Double, a: Double, b: Double, c: Double): Double {
        return (c - a) * (1 - Math.exp(-b * x)) + a
    }

    fun nullchecker(x: String): Boolean {
        if (x.equals("-")) {
            return false
        } else if (x.isEmpty()) {
            return false
        } else if (x.equals(".") || x.equals(",")) {
            return false
        }
        return true
    }

    fun nullcheckerpro(): Boolean {
        if (buttonbelow.isChecked || buttonabove.isChecked) {
            if (a.text.isEmpty() || mean_second.text.isEmpty() || std_second.text.isEmpty()) {
                return false
            }
            if (a.text.toString() == (",") || mean_second.text.toString() == (",") || std_second.text.toString() == (",")) {
                return false
            }
            if (a.text.toString() == (".") || mean_second.text.toString() == (".") || std_second.text.toString() == (".")) {
                return false
            }
            return !(a.text.toString() == "-" || mean_second.text.toString() == ("-") || std_second.text.toString() == ("-"))

        } else {
            if (a.text.isEmpty() || mean_second.text.isEmpty() || std_second.text.isEmpty() || b.text.isEmpty()) {
                return false
            }
            if (a.text.toString() == (",") || mean_second.text.toString() == (",") || std_second.text.toString() == (",") || b.text.toString() == (",")
            ) {
                return false
            }
            if (a.text.toString() == (".") || mean_second.text.toString() == (".") || std_second.text.toString() == (".") || b.text.toString()== (".")
            ) {
                return false
            }
            return !(a.text.toString() == ("-") || mean_second.text.toString() == ("-") || std_second.text.toString() == ("-") || b.text.toString() == ("-"))


        }


    }
    fun roundofftester(x: Double): String{
        var testing1 = x.toString()
        var y: Double = 0.0
        if(testing1.contains("E")){
                testing1 = String.format("%.5f", x)
                val final = testing1.replace(",", ".")
            return final
            }
            return  x.toString()
        }

    // AD
    private fun showInterstial() {
        if (mAdManagerInterstitialAd != null) {
            mAdManagerInterstitialAd?.show(requireActivity())
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    //AD
    private fun loadInterstial() {
        // ADS
        var adRequest = AdManagerAdRequest.Builder().build()
        val context = requireContext().applicationContext

        AdManagerInterstitialAd.load(context,"ca-app-pub-3940256099942544/1033173712", adRequest, object : AdManagerInterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mAdManagerInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mAdManagerInterstitialAd = interstitialAd

                // Uncomment if you want prompt when ad is loaded
                //Toast.makeText(context,"Ad loaded", Toast.LENGTH_SHORT).show()
            }
        })
    }

    }



