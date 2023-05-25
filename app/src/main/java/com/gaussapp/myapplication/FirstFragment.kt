package com.gaussapp.myapplication

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.myapp.myapplication.R
import com.myapp.myapplication.databinding.FragmentFirstBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import kotlin.math.exp
import kotlin.math.roundToInt
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
private const val MSG = "First fragment"
private const val inv_sqrt_2pi = 0.3989422804014327
private const val Epsilon = 0.000000000001
class FirstFragment : Fragment() {

    // ADS

    private var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null
    private final var TAG = "MainActivity"

    //DEFINING MY XML ITEMS FROM FRAGMENT_FIRST.XML
    private lateinit var przycisk: Button
    private lateinit var output: TextView
    private lateinit var point: EditText
    private lateinit var median: EditText
    private lateinit var std: EditText
    private lateinit var deletebutton: Button
    private lateinit var obrazek: ImageView


    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
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
        val context = requireContext().applicationContext

        // GETTING CURRENT MODE
        var mode = AppCompatDelegate.getDefaultNightMode()

        // MY ITEMS FINDING
        przycisk = view.findViewById(R.id.calculate)
        output = view.findViewById(R.id.result_text)
        point = view.findViewById(R.id.point)
        median = view.findViewById(R.id.median_first)
        std = view.findViewById(R.id.std_first)
        deletebutton = view.findViewById(R.id.deletebutton_first)
        obrazek = view.findViewById(R.id.rysunek)

        // 2 CANVASES; ONE WOULD BE CLEARED, THE OTHER WOULDN'T
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels - 64
        val bitmapheight = 600
        val bitmap = Bitmap.createBitmap(screenWidth, bitmapheight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bitmap2 = Bitmap.createBitmap(screenWidth, bitmapheight, Bitmap.Config.ARGB_8888)
        val canvas2 = Canvas(bitmap2)

        val mergedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvasmerge = Canvas(mergedBitmap)


        // PLOT LINE COLOR
        var gauss_plot = Paint()
        gauss_plot.color = ContextCompat.getColor(context, R.color.gauss_plot)
        gauss_plot.strokeWidth = 12f
        gauss_plot.style = Paint.Style.STROKE
        gauss_plot.strokeCap = Paint.Cap.ROUND

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
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            gauss_plot.color = ContextCompat.getColor(context, R.color.mycolor_new_darker)
        }
        canvas.drawPath(Path().apply {
            moveTo(xPoints.first(), yPoints.first())
            xPoints.zip(yPoints).forEach { (x, y) ->
                lineTo(x, y)
            }
        }, gauss_plot)


// TIMELINE LINE
        val path = Path()

        val timelinestartX = 0f
        val timelinestartY = bitmapheight.toFloat() - 128f
        val timelineendX = screenWidth.toFloat()
        val timelinelength = timelineendX - timelinestartX


        path.moveTo(timelinestartX, timelinestartY)
        path.lineTo(timelineendX, timelinestartY)

        val painttimeline = Paint()
        painttimeline.color = Color.BLACK
        painttimeline.strokeWidth = 4f
        painttimeline.style = Paint.Style.STROKE

        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            painttimeline.color = Color.WHITE
        } else {
            painttimeline.color = Color.BLACK
        }
// TIMELINE AND ARROW
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


// PATH DRAWING

        obrazek.setImageBitmap(bitmap)






        binding.pierwszy.setOnClickListener {
            // ADS
            showInterstial()
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.calculate.setOnClickListener() {
            loadInterstial()
            calculations()
            // CHECKING NIGHT MODE FOR COLOR OF BACKGROUND ( AND CLEARING COLOR)
            val configuration = resources.configuration
            val isNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            var color_of_background = ContextCompat.getColor(context, R.color.backgroud_day_mode)
            if (isNightMode) {
                color_of_background = ContextCompat.getColor(context, R.color.background_night_mode)
            } else {
                color_of_background = ContextCompat.getColor(context, R.color.backgroud_day_mode)
            }

            // NULL CHECHING
            if (!nullchecker(std.text.toString()) ||!nullchecker(point.text.toString()) || !nullchecker(median.text.toString()) || std.text.toString()
                    .toDouble() <= 0
            ) {
                canvas2.drawColor(color_of_background, PorterDuff.Mode.SRC_OVER)
                canvasmerge.drawBitmap(bitmap2, 0f, 0f, null)
                canvasmerge.drawBitmap(bitmap, 0f, 0f, null)
                obrazek.setImageBitmap(mergedBitmap)
            } else {


                // CREATING CANVAS 2, MERGING IT LATER
                // IT ALSO ALLOWS US TO CLEAR BLANC ONE WITHOUT AFFECTING THE OTHER
                if (Math.abs(
                        point.text.toString().toDouble() - median.text.toString().toDouble()
                    ) < (25 * std.text.toString().toDouble())
                ) {
                    // CLEARING OLD VALUES
                    canvas2.drawColor(color_of_background)


                    // NUMBERS
                    val standarddeviation: Double = std.text.toString().toDouble()
                    val medianvalue: Double = median.text.toString().toDouble()

                    // SETTING DECIMAL SEPARATOR AS "."
                    // ALSO FORMATTING OUR VALUES TO NOT EXCEED 2 DIGITS
                    val df = DecimalFormat("0.0")
                    df.decimalFormatSymbols = DecimalFormatSymbols().apply {
                        decimalSeparator = '.'
                    }

                    var number1: kotlin.String = df.format((medianvalue - (standarddeviation * 2)))
                    var number2: kotlin.String = df.format((medianvalue - standarddeviation))
                    var number3: kotlin.String = df.format(median.text.toString().toDouble())
                    var number4: kotlin.String = df.format(medianvalue + standarddeviation)
                    var number5: kotlin.String = df.format(medianvalue + standarddeviation * 2)

                    // RUNNING REPLACER TO REPLACE ".0"(0.0) WITH JUST 0
                    number1 = replacer(number1)
                    number2 = replacer(number2)
                    number3 = replacer(number3)
                    number4 = replacer(number4)
                    number5 = replacer(number5)

                    // SETTING NUMBERS PAINT
                    val numberspaint = android.graphics.Paint()
                    numberspaint.color = android.graphics.Color.BLACK
                    numberspaint.textSize = 32f
                    if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
                        numberspaint.color = Color.WHITE
                    }

                    // MEASURE NUMBERS WIDTH TO CENTER THEM
                    val number3textwidth = numberspaint.measureText(number3)
                    val number1textwidth = numberspaint.measureText(number1)
                    val number2textwidth = numberspaint.measureText(number2)
                    val number4textwidth = numberspaint.measureText(number4)
                    val number5textwidth = numberspaint.measureText(number5)


                    // DRAWING NUMBERS
                    canvas2.drawText(
                        number1,
                        (((timelinelength) / 2) / 2) - number1textwidth / 2,
                        timelinestartY + numberspaint.descent() + 42f,
                        numberspaint
                    )
                    canvas2.drawText(
                        number2,
                        (((timelinelength) / 2) / 2) + (length2 / 2) - (number2textwidth / 2),
                        timelinestartY + numberspaint.descent() + 42f,
                        numberspaint
                    )
                    canvas2.drawText(
                        number3,
                        (timelinelength - number3textwidth) / 2,
                        timelinestartY + numberspaint.descent() + 42f,
                        numberspaint
                    )
                    canvas2.drawText(
                        number4,
                        (timelinelength - number4textwidth + length2) / 2,
                        timelinestartY + numberspaint.descent() + 42f,
                        numberspaint
                    )
                    canvas2.drawText(
                        number5,
                        (timelinelength - number5textwidth + length2 * 2) / 2,
                        timelinestartY + numberspaint.descent() + 42f,
                        numberspaint
                    )


                    // OUR VALUE LINE
                    val dashedline = android.graphics.Paint()
                    dashedline.color = android.graphics.Color.BLUE
                    dashedline.strokeWidth = 8f
                    dashedline.pathEffect =
                        android.graphics.DashPathEffect(kotlin.floatArrayOf(30f, 15f), 0f)
                    dashedline.strokeCap = android.graphics.Paint.Cap.ROUND


                    // OUR VALUE
                    var drawingpoint: Double = (timelinelength / 2).toDouble()
                    val exponent =
                        point.text.toString().toDouble() - median.text.toString().toDouble()
                    var scale = (timelinelength / 8) * (exponent / std.text.toString().toDouble())
                    var lineX = 0.0


                    // I WANTED THE SCALE TO BE LINEAR FROM -2 STANDARD DEVIATIONS TO 2 STANDARD DEVIATIONS
                    // AND BELOW -2 STD AND ABOVE 2 STD THE SCALE TO BE EXPONENTIAL
                    // SO WE STILL SEE THE LINE OR THE FILLED GAUSSIAN PLOT
                    // BUT THE SCALE IS DIFFERENT
                    if (exponent == 0.0) {
                        lineX = (timelinelength / 2).toDouble()
                    } else if (scale < -(timelinelength / 4)) {
                        lineX = timelinelength - liniowoWykladniczaFunkcja(
                            -scale,
                            (timelinelength / 4 + timelinelength / 2).toDouble(),
                            0.0018,
                            timelinelength.toDouble(),
                        )

                    } else if (scale > timelinelength / 4) {
                        lineX = liniowoWykladniczaFunkcja(
                            scale,
                            (timelinelength / 4 + timelinelength / 2).toDouble(),
                            0.0018,
                            timelinelength.toDouble(),
                        )

                    } else {
                        lineX = scale + drawingpoint
                    }




                    canvas2.drawLine(
                        lineX.toFloat(),
                        32f,
                        lineX.toFloat(),
                        -amplitude + 32f,
                        dashedline
                    )


                    // CANVAS CONNECTION

                    canvasmerge.drawBitmap(bitmap2, 0f, 0f, null)
                    canvasmerge.drawBitmap(bitmap, 0f, 0f, null)
                    obrazek.setImageBitmap(mergedBitmap)

                } else {
                    // ELSE CLEAR SECOND CANVAS
                    canvas2.drawColor(color_of_background, PorterDuff.Mode.SRC_OVER)
                    canvasmerge.drawBitmap(bitmap2, 0f, 0f, null)
                    canvasmerge.drawBitmap(bitmap, 0f, 0f, null)
                    obrazek.setImageBitmap(mergedBitmap)
                }
            }
        }

        binding.deletebuttonFirst.setOnClickListener() {


            //DELETE BUTTON LOGIC
            output.text = "0.0"
            std.text.clear()
            median.text.clear()
            point.text.clear()
            var color_bckgrnd = ContextCompat.getColor(context, R.color.backgroud_day_mode)
            if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
                color_bckgrnd = ContextCompat.getColor(context, R.color.background_night_mode)
            }
            canvas2.drawColor(color_bckgrnd)
            canvasmerge.drawBitmap(bitmap2, 0f, 0f, null)
            canvasmerge.drawBitmap(bitmap, 0f, 0f, null)
            obrazek.setImageBitmap(mergedBitmap)


        }
    }

    private fun showInterstial() {
        val context = requireContext().applicationContext
        if (mAdManagerInterstitialAd != null) {
            mAdManagerInterstitialAd?.show(requireActivity())
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

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
               // Toast.makeText(context,"Ad loaded", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun calculations() {
        var checkingnulls = true
        if(!nullchecker(point.text.toString()) || !nullchecker(median.text.toString()) || !nullchecker(std.text.toString()) ){
            checkingnulls = false
        }
        if (!checkingnulls || std.text.toString().toDouble() <= 0) {
            output.text = "0.0"


        } else {
            val p = point.text.toString().toDouble()
            val s = std.text.toString().toDouble()
            val m = median.text.toString().toDouble()
            var gauss_result: Double = gauss(m, s, p)
            val round_off = (gauss_result * 100000).roundToInt() / 100000.0
            output.text = roundofftester(round_off)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun gauss(m: Double, s: Double, p: Double): Double {

        var a: Double = (p - m) / s

        return inv_sqrt_2pi / s * exp(-0.5f * a * a)


    }

    private fun replacer(x: String): String {
        if (x.endsWith(".0")) {
            return x.substring(0, x.length - 2)
        } else {
            return x
        }
    }

    fun liniowoWykladniczaFunkcja(x: Double, a: Double, b: Double, c: Double): Double {
        // LINEAR-EXPONENTIAL FUNCTION
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

}
