package com.safey.lungmonitoring.ui.dashboard.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.safey.lungmonitoring.R
import kotlinx.android.synthetic.main.fragment_about.*
import java.lang.Exception

class LegalFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_legal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webviewPrivacy.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(
                view: WebView,
                progress: Int
            ) { //Make the bar disappear after URL is loaded, and changes string to Loading...
                try {
                    if (progress == 100) progressBar5.visibility = View.GONE
                } catch (e: Exception) {

                }
            }
        }

        webviewPrivacy.settings.javaScriptEnabled = true


        webviewPrivacy.loadUrl("https://www.safeymedicaldevices.com/termsandconditions")

        this.webviewPrivacy.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
    }


}