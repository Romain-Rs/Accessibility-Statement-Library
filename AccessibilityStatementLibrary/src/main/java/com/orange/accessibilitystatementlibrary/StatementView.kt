package com.orange.accessibilitystatementlibrary

import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.view_statement.view.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class StatementView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    val XML_DATE = "audit_date"
    val XML_REFERENTIAL = "referential"
    val XML_REFERENTIAL_NAME = "name"
    val XML_REFERENTIAL_VERSION = "version"
    val XML_REFERENTIAL_LEVEL = "level"
    val XML_RESULTS = "pages_results"
    val XML_SCREEN_AUDITED = "page"
    val XML_ATTRIBUTE_CONFORMITY = "conformity"
    val XML_ATTRIBUTE_SCREEN_NAME = "name"
    val XML_TECHNO = "technology"
    val XML_TITLE = "title"
    val XML_DETAILS = "details"

    var screensAudited: MutableList<String>? = null
    var referential: String = ""
    var technologies: String = ""
    var date: String? = null
    var resultScore: String? = null
    var title: String = ""

    var referentialStarted = false
    var urlAccessibilityDeclaration: String? = null
    var detailsStarted = false

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_statement, this)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.DeclarationView)
        manageResultsFromXML(attributes)
    }

    fun manageResultsFromXML(attributes: TypedArray) {
        parseXML()

        try {
            val text = attributes.getString(R.styleable.DeclarationView_declarant)
            urlAccessibilityDeclaration =
                attributes.getString(R.styleable.DeclarationView_details_url)
            declarantTextView.text = text

            buttonSeeMore.setOnClickListener {
                if (urlAccessibilityDeclaration != null) {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(urlAccessibilityDeclaration))
                    context.startActivity(browserIntent)
                }
            }
        } finally {
            attributes.recycle()
        }
    }

    private fun parseXML() {
        var parserFactory: XmlPullParserFactory
        parserFactory = XmlPullParserFactory.newInstance()

        var pullPaser = parserFactory.newPullParser()
        val inputStream: InputStream = context.assets.open("accessibility_result.xml")
        pullPaser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        pullPaser.setInput(inputStream, null)
        processParsing(pullPaser)
        displayResults()
    }

    private fun processParsing(parser: XmlPullParser) {
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            var eltName = ""

            if (eventType == XmlPullParser.START_TAG) {
                eltName = parser.name

                if (XML_DATE.equals(eltName)) {
                    date = parser.nextText()
                }
                if (XML_TECHNO.equals(eltName)) {
                    val technology = parser.nextText()
                    if (technologies == null || technologies.length == 0) {
                        technologies = "$technology"
                    } else {
                        technologies = "$technologies, $technology"
                    }
                }
                if (XML_REFERENTIAL.equals(eltName)) {
                    referentialStarted = true
                }
                if (XML_REFERENTIAL_NAME.equals(eltName) && referentialStarted) {
                    val name = parser.nextText()
                    referential = "$name"
                }
                if (XML_REFERENTIAL_VERSION.equals(eltName) && referentialStarted) {
                    val version = parser.nextText()
                    referential = "$referential $version"
                }
                if (XML_REFERENTIAL_LEVEL.equals(eltName) && referentialStarted) {
                    val level = parser.nextText()
                    referential = "$referential $level"
                    referentialStarted = false
                }
                if (XML_RESULTS.equals(eltName)) {
                    resultScore = parser.getAttributeValue(null, XML_ATTRIBUTE_CONFORMITY)
                    screensAudited = mutableListOf()
                }
                if (XML_SCREEN_AUDITED.equals(eltName) && screensAudited != null) {
                    val screenName = parser.getAttributeValue(null, XML_ATTRIBUTE_SCREEN_NAME)
                    screensAudited?.add(screenName)
                }
                if (XML_DETAILS.equals(eltName)) {
                    detailsStarted = true
                }
                if (XML_TITLE.equals(eltName) && !detailsStarted) {
                    title = parser.nextText()
                    title.toString()
                }
            }
            eventType = parser.next()
        }
    }

    private fun displayResults() {
        val resultPercentValue = resultScore?.toInt() ?: 0
        resultTextView.text = context.getString(R.string.result, resultPercentValue)
        resultTextView.contentDescription =
            context.getString(R.string.result_content_desc, resultPercentValue)
        resultProgresBar.max = 100
        resultProgresBar.progress = resultPercentValue
        dateTextView.text = parseDateToddMMyyyy(date)
        referentialTextView.text = referential
        technologieTextView.text = technologies
        displayConformityState(resultPercentValue)
    }

    private fun displayConformityState(percentValue: Int) {
        var nameOfApplication = title
        if (nameOfApplication == null || nameOfApplication.length == 0) {
            nameOfApplication = context.getString(R.string.nameOfApp)
        }
        if (percentValue < 50) {
            resultDetailTextView.text =
                context.getString(R.string.no_conformity_state, nameOfApplication)
        } else if (percentValue < 100) {
            resultDetailTextView.text =
                context.getString(R.string.partial_conformity_state, nameOfApplication)
        } else {
            resultDetailTextView.text =
                context.getString(R.string.total_conformity_state, nameOfApplication)
        }
    }

    fun parseDateToddMMyyyy(time: String?): String? {
        val inputPattern = "yyyy-MM-dd"
        val outputPatternFr = "dd-MMM-yyyy"
        val outputPatternEn = "dd MMMM yyyy"
        val inputFormat = SimpleDateFormat(inputPattern)
        val outputFormat = SimpleDateFormat(outputPatternEn)
        var date: Date? = null
        var str: String? = null
        try {
            date = inputFormat.parse(time)
            str = outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return str
    }
}